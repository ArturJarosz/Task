package com.arturjarosz.task.notification.application;

import com.arturjarosz.task.dto.NotificationReadDto;
import com.arturjarosz.task.dto.NotificationSettingDto;
import com.arturjarosz.task.notification.domain.NotificationExceptionCodes;
import com.arturjarosz.task.notification.model.Notification;
import com.arturjarosz.task.notification.model.NotificationType;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertEntityPresent;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotNull;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

public final class NotificationValidator {

    // Another user's notification is reported as missing, so its id tells nothing about it
    public static void validateNotificationExistence(Optional<Notification> maybeNotification, Long notificationId) {
        assertEntityPresent(maybeNotification.isPresent(),
                createMessageCode(ExceptionCodes.NOT_EXIST, NotificationExceptionCodes.NOTIFICATION), notificationId);
    }

    public static void validateNotificationReadDto(NotificationReadDto notificationReadDto) {
        assertNotNull(notificationReadDto != null ? notificationReadDto.getRead() : null,
                createMessageCode(ExceptionCodes.NULL, NotificationExceptionCodes.NOTIFICATION,
                        NotificationExceptionCodes.READ));
    }

    public static void validateNotificationSettingDtos(List<NotificationSettingDto> settingDtos) {
        assertNotNull(settingDtos, createMessageCode(ExceptionCodes.NULL,
                NotificationExceptionCodes.NOTIFICATION_SETTING));
        var types = new HashSet<NotificationType>();
        for (var settingDto : settingDtos) {
            assertNotNull(settingDto.getType(), createMessageCode(ExceptionCodes.NULL,
                    NotificationExceptionCodes.NOTIFICATION_SETTING, NotificationExceptionCodes.TYPE));
            var type = NotificationType.valueOf(settingDto.getType().name());
            assertIsTrue(type.isSetByUser() && types.add(type), createMessageCode(ExceptionCodes.NOT_VALID,
                    NotificationExceptionCodes.NOTIFICATION_SETTING, NotificationExceptionCodes.TYPE));
            assertNotNull(settingDto.getEnabled(), createMessageCode(ExceptionCodes.NULL,
                    NotificationExceptionCodes.NOTIFICATION_SETTING, NotificationExceptionCodes.ENABLED));
            var daysBefore = settingDto.getDaysBefore();
            assertNotNull(daysBefore, createMessageCode(ExceptionCodes.NULL,
                    NotificationExceptionCodes.NOTIFICATION_SETTING, NotificationExceptionCodes.DAYS_BEFORE));
            assertIsTrue(daysBefore.stream().allMatch(days -> days != null && days >= 0
                            && days <= NotificationType.MAX_DAYS_BEFORE),
                    createMessageCode(ExceptionCodes.NOT_VALID, NotificationExceptionCodes.NOTIFICATION_SETTING,
                            NotificationExceptionCodes.DAYS_BEFORE), NotificationType.MAX_DAYS_BEFORE);
            // A type that is off may have none
            var reminders = daysBefore.stream().distinct().count();
            assertIsTrue(reminders <= NotificationType.MAX_REMINDERS && (reminders > 0 || !settingDto.getEnabled()),
                    createMessageCode(ExceptionCodes.NOT_VALID, NotificationExceptionCodes.NOTIFICATION_SETTING,
                            NotificationExceptionCodes.REMINDERS), NotificationType.MAX_REMINDERS);
        }
    }

    private NotificationValidator() {
        throw new IllegalStateException(ExceptionCodes.NOT_FOR_INSTANTIATING);
    }
}
