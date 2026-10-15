package com.arturjarosz.task.calendar.domain;

import com.arturjarosz.task.calendar.model.CalendarEvent;
import com.arturjarosz.task.calendar.model.dto.GoogleEventDataDto;
import com.arturjarosz.task.calendar.model.dto.GoogleEventsSyncDto;

/**
 * Port for communication with the Google Calendar API. Implementation is only available when the Google Calendar
 * integration is enabled, all callers have to handle its absence.
 */
public interface GoogleCalendarService {

    /**
     * Creates a dedicated calendar with given name and returns its Google calendar id.
     */
    String createCalendar(String name);

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
}
