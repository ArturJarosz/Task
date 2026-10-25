package com.arturjarosz.task.calendar.application;

import com.arturjarosz.task.calendar.domain.CalendarExceptionCodes;
import com.arturjarosz.task.dto.CalendarShareDto;
import com.arturjarosz.task.dto.CalendarShareRoleDto;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import com.arturjarosz.task.sharedkernel.model.Email;
import com.arturjarosz.task.systemparameter.application.dto.GoogleCalendarSettings;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertEntityPresent;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsFalse;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotEmpty;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

/**
 * Validates sharing of the Google Calendar with people.
 */
public final class GoogleCalendarShareValidator {
    // Other roles can be given only in Google; owners could remove the calendar or the service account access
    private static final Set<CalendarShareRoleDto> ALLOWED_ROLES = Set.of(CalendarShareRoleDto.READER,
            CalendarShareRoleDto.WRITER);

    public static void validateCalendarShareDto(CalendarShareDto calendarShareDto) {
        assertIsTrue(calendarShareDto != null,
                createMessageCode(ExceptionCodes.NULL, CalendarExceptionCodes.CALENDAR_SHARE));
        validateEmailNotEmpty(calendarShareDto.getEmail());
        // throws when the email is not valid
        new Email(calendarShareDto.getEmail().trim());
        assertIsTrue(calendarShareDto.getRole() != null,
                createMessageCode(ExceptionCodes.NULL, CalendarExceptionCodes.CALENDAR_SHARE,
                        CalendarExceptionCodes.ROLE));
        assertIsTrue(ALLOWED_ROLES.contains(calendarShareDto.getRole()),
                createMessageCode(ExceptionCodes.NOT_VALID, CalendarExceptionCodes.CALENDAR_SHARE,
                        CalendarExceptionCodes.ROLE));
    }

    // Access can be given in Google to emails the application does not accept, so revoking only needs the email
    public static void validateEmailNotEmpty(String email) {
        assertNotEmpty(email, createMessageCode(ExceptionCodes.EMPTY, CalendarExceptionCodes.CALENDAR_SHARE,
                CalendarExceptionCodes.EMAIL));
    }

    public static void validateSyncActive(Optional<GoogleCalendarSettings> maybeSettings) {
        assertIsTrue(maybeSettings.isPresent(),
                createMessageCode(ExceptionCodes.FORBIDDEN, CalendarExceptionCodes.CALENDAR_SHARE,
                        CalendarExceptionCodes.SYNC));
    }

    public static void validateCalendarExistence(Optional<String> maybeGoogleCalendarId) {
        assertIsTrue(maybeGoogleCalendarId.isPresent(),
                createMessageCode(ExceptionCodes.NOT_EXIST, CalendarExceptionCodes.CALENDAR_SHARE,
                        CalendarExceptionCodes.GOOGLE_CALENDAR));
    }

    public static void validateNotShared(List<CalendarShareDto> shares, String email) {
        assertIsFalse(isShared(shares, email),
                createMessageCode(ExceptionCodes.ALREADY_SET, CalendarExceptionCodes.CALENDAR_SHARE,
                        CalendarExceptionCodes.EMAIL), email);
    }

    public static void validateShared(List<CalendarShareDto> shares, String email) {
        assertEntityPresent(isShared(shares, email),
                createMessageCode(ExceptionCodes.NOT_EXIST, CalendarExceptionCodes.CALENDAR_SHARE,
                        CalendarExceptionCodes.EMAIL), email);
    }

    private static boolean isShared(List<CalendarShareDto> shares, String email) {
        return shares.stream().anyMatch(share -> share.getEmail().equalsIgnoreCase(email));
    }

    private GoogleCalendarShareValidator() {
        // static validator methods only
    }
}
