package com.arturjarosz.task.notification.application.impl;

import com.arturjarosz.task.dto.NotificationSettingDto;
import com.arturjarosz.task.notification.application.NotificationPreferences;
import com.arturjarosz.task.notification.application.NotificationSettingsApplicationService;
import com.arturjarosz.task.notification.application.NotificationSynchronizer;
import com.arturjarosz.task.notification.application.mapper.NotificationMapper;
import com.arturjarosz.task.notification.domain.NotificationPreference;
import com.arturjarosz.task.notification.infrastructure.repository.NotificationSettingRepository;
import com.arturjarosz.task.notification.model.NotificationItemType;
import com.arturjarosz.task.notification.model.NotificationSetting;
import com.arturjarosz.task.notification.model.NotificationType;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.user.application.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.arturjarosz.task.notification.application.NotificationValidator.validateNotificationSettingDtos;

@Slf4j
@RequiredArgsConstructor
@ApplicationService
public class NotificationSettingsApplicationServiceImpl implements NotificationSettingsApplicationService {

    private final CurrentUserProvider currentUserProvider;
    private final NotificationMapper notificationMapper;
    private final NotificationSettingRepository notificationSettingRepository;
    private final NotificationSynchronizer notificationSynchronizer;

    @Transactional(readOnly = true)
    @Override
    public List<NotificationSettingDto> getNotificationSettings() {
        LOG.info("Getting notification settings of current user.");

        var saved = this.currentUserProvider.findCurrentUser()
                .map(user -> this.loadSettings(user.getId()))
                .orElseGet(Map::of);
        return this.toDtos(saved);
    }

    @Transactional
    @Override
    public List<NotificationSettingDto> updateNotificationSettings(List<NotificationSettingDto> settingDtos) {
        LOG.info("Updating notification settings of current user.");

        validateNotificationSettingDtos(settingDtos);
        var user = this.currentUserProvider.getOrCreateCurrentUser();
        var saved = this.loadSettings(user.getId());
        var changedItemTypes = EnumSet.noneOf(NotificationItemType.class);
        for (var settingDto : settingDtos) {
            var type = NotificationType.valueOf(settingDto.getType().name());
            var preference = new NotificationPreference(settingDto.getEnabled(), settingDto.getDaysBefore());
            var setting = saved.get(type);
            var current = setting != null ? setting.toPreference() : NotificationPreferences.defaultOf(type);
            if (current.equals(preference)) {
                continue;
            }
            if (setting != null) {
                setting.update(preference);
            } else {
                saved.put(type, this.notificationSettingRepository.save(new NotificationSetting(user.getId(), type,
                        preference)));
            }
            changedItemTypes.add(type.getItemType());
        }
        // Turning a type off removes its notifications and turning it on adds the ones now due, so the bell matches
        // the settings at once
        if (!changedItemTypes.isEmpty()) {
            this.notificationSynchronizer.synchronizeUserItemTypes(user.getId(), changedItemTypes);
        }

        LOG.info("Notification settings of current user updated.");
        return this.toDtos(saved);
    }

    private Map<NotificationType, NotificationSetting> loadSettings(Long userId) {
        return this.notificationSettingRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(NotificationSetting::getType, Function.identity(), (a, b) -> a,
                        () -> new EnumMap<>(NotificationType.class)));
    }

    private List<NotificationSettingDto> toDtos(Map<NotificationType, NotificationSetting> saved) {
        return Arrays.stream(NotificationType.values()).filter(NotificationType::isSetByUser)
                .map(type -> {
                    var setting = saved.get(type);
                    var preference = setting != null ? setting.toPreference()
                            : NotificationPreferences.defaultOf(type);
                    return this.notificationMapper.mapToSettingDto(type, preference);
                })
                .toList();
    }
}
