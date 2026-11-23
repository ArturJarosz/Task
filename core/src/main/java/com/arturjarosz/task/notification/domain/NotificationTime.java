package com.arturjarosz.task.notification.domain;

import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;

import java.time.ZoneId;

public final class NotificationTime {
    // Dates fall due on calendar days in Poland, where the app is used; the server's own time zone can be behind it
    public static final ZoneId ZONE = ZoneId.of("Europe/Warsaw");

    private NotificationTime() {
        throw new IllegalStateException(ExceptionCodes.NOT_FOR_INSTANTIATING);
    }
}
