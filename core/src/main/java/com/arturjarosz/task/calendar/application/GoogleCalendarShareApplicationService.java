package com.arturjarosz.task.calendar.application;

import com.arturjarosz.task.dto.CalendarShareDto;

import java.util.List;

public interface GoogleCalendarShareApplicationService {

    /**
     * Returns people the synchronized Google Calendar is shared with. The service account owning the calendar and the
     * calendar's own address are not listed.
     */
    List<CalendarShareDto> getShares();

    /**
     * Shares the synchronized Google Calendar with the email of given {@link CalendarShareDto}. Google notifies the
     * person by email.
     */
    CalendarShareDto addShare(CalendarShareDto calendarShareDto);

    /**
     * Revokes the access to the synchronized Google Calendar for given email.
     */
    void removeShare(String email);
}
