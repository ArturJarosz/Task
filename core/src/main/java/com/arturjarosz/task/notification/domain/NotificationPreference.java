package com.arturjarosz.task.notification.domain;

import java.util.Comparator;
import java.util.List;

/**
 * Whether a user gets a notification type, and its reminders as days before the due date. The reminders are kept
 * furthest first and each day once, so equal settings are equal and are listed in the same order.
 */
public record NotificationPreference(boolean enabled, List<Integer> daysBefore) {

    public NotificationPreference {
        daysBefore = daysBefore.stream().distinct().sorted(Comparator.reverseOrder()).toList();
    }
}
