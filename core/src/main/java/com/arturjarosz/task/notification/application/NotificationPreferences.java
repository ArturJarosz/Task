package com.arturjarosz.task.notification.application;

import com.arturjarosz.task.notification.domain.NotificationPreference;
import com.arturjarosz.task.notification.infrastructure.repository.NotificationSettingRepository;
import com.arturjarosz.task.notification.model.NotificationSetting;
import com.arturjarosz.task.notification.model.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Notification settings of users, with the defaults of each type for the ones a user hasn't changed.
 */
@RequiredArgsConstructor
@Component
public class NotificationPreferences {

    private final NotificationSettingRepository notificationSettingRepository;

    public Map<Long, NotificationPreference> get(Collection<Long> userIds, NotificationType type) {
        var settingType = type.getSettingType();
        var saved = userIds.isEmpty() ? Map.<Long, NotificationSetting>of()
                : this.notificationSettingRepository.findByUserIdInAndType(userIds, settingType).stream()
                        .collect(Collectors.toMap(NotificationSetting::getUserId, Function.identity()));
        return userIds.stream().collect(Collectors.toMap(Function.identity(), userId -> {
            var setting = saved.get(userId);
            return setting != null ? setting.toPreference() : defaultOf(settingType);
        }));
    }

    public static NotificationPreference defaultOf(NotificationType type) {
        return new NotificationPreference(type.isEnabledByDefault(), type.getDefaultDaysBefore());
    }
}
