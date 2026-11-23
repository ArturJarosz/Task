package com.arturjarosz.task.notification.domain;

import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;

public final class NotificationExceptionCodes {
    public static final String DAYS_BEFORE = "daysBefore";
    public static final String ENABLED = "enabled";
    public static final String NOTIFICATION = "notification";
    public static final String NOTIFICATION_SETTING = "notificationSetting";
    public static final String READ = "read";
    public static final String REMINDERS = "reminders";
    public static final String TYPE = "type";

    private NotificationExceptionCodes() {
        throw new IllegalStateException(ExceptionCodes.NOT_FOR_INSTANTIATING);
    }
}
