package com.arturjarosz.task.calendar.application;

import com.arturjarosz.task.dto.CalendarEventDto;

import java.time.LocalDate;
import java.util.List;

public interface CalendarEventApplicationService {

    /**
     * Creates a new {@link com.arturjarosz.task.calendar.model.CalendarEvent} from given {@link CalendarEventDto}
     * and pushes it to the Google Calendar.
     */
    CalendarEventDto createCalendarEvent(CalendarEventDto calendarEventDto);

    /**
     * Updates the {@link com.arturjarosz.task.calendar.model.CalendarEvent} with given eventId and pushes the change
     * to the Google Calendar.
     */
    CalendarEventDto updateCalendarEvent(Long eventId, CalendarEventDto calendarEventDto);

    /**
     * Removes the {@link com.arturjarosz.task.calendar.model.CalendarEvent} with given eventId and removes the
     * connected event from the Google Calendar.
     */
    void deleteCalendarEvent(Long eventId);

    /**
     * Returns the {@link com.arturjarosz.task.calendar.model.CalendarEvent} with given eventId.
     */
    CalendarEventDto getCalendarEvent(Long eventId);

    /**
     * Returns events overlapping given date range. When no range is provided, all events are returned. Events
     * waiting for removal from the Google Calendar are not returned.
     */
    List<CalendarEventDto> getCalendarEvents(LocalDate startDate, LocalDate endDate);
}
