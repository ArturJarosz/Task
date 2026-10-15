package com.arturjarosz.task.calendar.model;

/**
 * Synchronization status of a {@link CalendarEvent} against the Google Calendar.
 */
public enum CalendarEventSyncStatus {
    PENDING,
    SYNCED,
    DELETE_PENDING
}
