package com.arturjarosz.task.calendar.application;

import com.arturjarosz.task.calendar.model.CalendarEvent;

import java.util.Optional;

public interface CalendarSyncService {

    /**
     * Pushes given {@link CalendarEvent} to the Google Calendar. The push is best effort - when the integration is
     * disabled or the Google Calendar cannot be reached, the event stays in PENDING status and is retried later by
     * the scheduled job.
     */
    CalendarEvent pushEvent(CalendarEvent calendarEvent);

    /**
     * Removes the Google event connected with given {@link CalendarEvent} and on success removes the local event as
     * well. When the Google Calendar cannot be reached, the event stays in DELETE_PENDING status and is retried
     * later by the scheduled job.
     */
    void pushDelete(CalendarEvent calendarEvent);

    /**
     * Retries pushing all events waiting in PENDING and DELETE_PENDING statuses.
     */
    void pushPendingEvents();

    /**
     * Pulls changes from the Google Calendar using the stored sync token and applies them to local events. When the
     * sync token is expired, a full synchronization is performed instead.
     */
    void runIncrementalSync();

    /**
     * Forgets the connected Google Calendar and its events when it belongs to another service account than given
     * one, so all local events are pushed to a new calendar. When the calendar's account is unknown, the account of
     * the previous key is used; when that is unknown as well, the synchronization is reset.
     */
    void resetSynchronizationIfAccountChanged(String previousServiceAccount, String serviceAccount);

    /**
     * Returns id of the Google Calendar used for synchronization, empty until it is created.
     */
    Optional<String> getGoogleCalendarId();
}
