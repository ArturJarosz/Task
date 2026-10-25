package com.arturjarosz.task.calendar.domain;

import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;

public final class CalendarExceptionCodes {

    public static final String CALENDAR_EVENT = "calendarEvent";
    public static final String NAME = "name";
    public static final String START_DATETIME = "startDateTime";
    public static final String END_DATETIME = "endDateTime";
    public static final String DATES = "dates";
    public static final String CALENDAR_SHARE = "calendarShare";
    public static final String EMAIL = "email";
    public static final String ROLE = "role";
    public static final String SYNC = "sync";
    public static final String GOOGLE_CALENDAR = "googleCalendar";
    public static final String REQUEST_FAILED = "requestFailed";

    private CalendarExceptionCodes() {
        throw new IllegalStateException(ExceptionCodes.NOT_FOR_INSTANTIATING);
    }
}
