package com.arturjarosz.task.calendar.domain;

import com.arturjarosz.task.calendar.model.CalendarEvent;
import com.arturjarosz.task.calendar.model.dto.GoogleEventDataDto;
import com.arturjarosz.task.calendar.model.dto.GoogleEventsSyncDto;
import com.arturjarosz.task.dto.GoogleCalendarConnectionStatusDto;
import com.arturjarosz.task.systemparameter.application.dto.GoogleCalendarSettings;

import java.util.Optional;

/**
 * Port for communication with the Google Calendar API. Calls connect with the stored configuration and throw
 * {@link GoogleCalendarException} when the integration is disabled or has no credentials, so callers check
 * the active settings first.
 */
public interface GoogleCalendarService {

    /**
     * Creates a dedicated calendar with given name and returns its Google calendar id.
     */
    String createCalendar(String name);

    /**
     * Finds a calendar with given name owned by the connected account and returns its Google calendar id. When more
     * calendars have that name, the first one that contains events is returned.
     */
    Optional<String> findCalendarByName(String name);

    /**
     * Changes the name of the calendar with given id.
     */
    void renameCalendar(String googleCalendarId, String name);

    /**
     * Inserts given {@link CalendarEvent} into the calendar with given id and returns identifying data of the created
     * Google event.
     */
    GoogleEventDataDto insertEvent(String googleCalendarId, CalendarEvent calendarEvent);

    /**
     * Updates the Google event connected with given {@link CalendarEvent} and returns its identifying data.
     */
    GoogleEventDataDto updateEvent(String googleCalendarId, CalendarEvent calendarEvent);

    /**
     * Removes the event with given googleEventId from the calendar. Events already removed on the Google side are
     * treated as removed successfully.
     */
    void deleteEvent(String googleCalendarId, String googleEventId);

    /**
     * Lists events of the calendar with given id. When syncToken is provided, only changes since the last
     * synchronization are returned, otherwise all events are listed. Throws
     * {@link SyncTokenExpiredException} when the provided syncToken is no longer valid.
     */
    GoogleEventsSyncDto listEvents(String googleCalendarId, String syncToken);

    /**
     * Checks whether the Google Calendar can be reached with given settings. When googleCalendarId is provided, the
     * calendar used for synchronization has to be reachable as well.
     */
    ConnectionCheckResult checkConnection(GoogleCalendarSettings settings, String googleCalendarId);

    /**
     * Reads the service account email from given JSON key. Returns empty when the text is not a valid service
     * account key.
     */
    Optional<String> readServiceAccountEmail(String credentials);

    record ConnectionCheckResult(GoogleCalendarConnectionStatusDto status, String message) {
    }
}
