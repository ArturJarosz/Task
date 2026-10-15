package com.arturjarosz.task.calendar.application;

import com.arturjarosz.task.calendar.domain.CalendarExceptionCodes;
import com.arturjarosz.task.calendar.model.CalendarEvent;
import com.arturjarosz.task.dto.CalendarEventDto;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertEntityPresent;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotEmpty;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

/**
 * Validates calendar event related dtos and CalendarEvent domain model.
 */
public final class CalendarEventValidator {

    public static void validateCalendarEventDto(CalendarEventDto calendarEventDto) {
        assertIsTrue(calendarEventDto != null,
                createMessageCode(ExceptionCodes.NULL, CalendarExceptionCodes.CALENDAR_EVENT));
        assertIsTrue(calendarEventDto.getName() != null,
                createMessageCode(ExceptionCodes.NULL, CalendarExceptionCodes.CALENDAR_EVENT,
                        CalendarExceptionCodes.NAME));
        assertNotEmpty(calendarEventDto.getName(),
                createMessageCode(ExceptionCodes.EMPTY, CalendarExceptionCodes.CALENDAR_EVENT,
                        CalendarExceptionCodes.NAME));
        var startDateTime = parseDateTime(calendarEventDto.getStartDateTime(), CalendarExceptionCodes.START_DATETIME);
        var endDateTime = parseDateTime(calendarEventDto.getEndDateTime(), CalendarExceptionCodes.END_DATETIME);
        assertIsTrue(!endDateTime.isBefore(startDateTime),
                createMessageCode(ExceptionCodes.NOT_VALID, CalendarExceptionCodes.CALENDAR_EVENT,
                        CalendarExceptionCodes.DATES));
    }

    public static void validateCalendarEventExistence(Optional<CalendarEvent> maybeCalendarEvent, Long eventId) {
        assertEntityPresent(maybeCalendarEvent.isPresent(),
                createMessageCode(ExceptionCodes.NOT_EXIST, CalendarExceptionCodes.CALENDAR_EVENT), eventId);
    }

    private static LocalDateTime parseDateTime(String dateTime, String fieldCode) {
        assertIsTrue(dateTime != null,
                createMessageCode(ExceptionCodes.NULL, CalendarExceptionCodes.CALENDAR_EVENT, fieldCode));
        assertNotEmpty(dateTime,
                createMessageCode(ExceptionCodes.EMPTY, CalendarExceptionCodes.CALENDAR_EVENT, fieldCode));
        try {
            return LocalDateTime.parse(dateTime);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    createMessageCode(ExceptionCodes.NOT_VALID, CalendarExceptionCodes.CALENDAR_EVENT, fieldCode));
        }
    }

    private CalendarEventValidator() {
        // static validator methods only
    }
}
