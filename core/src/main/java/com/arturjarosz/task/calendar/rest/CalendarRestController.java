package com.arturjarosz.task.calendar.rest;

import com.arturjarosz.task.calendar.application.CalendarEventApplicationService;
import com.arturjarosz.task.calendar.application.GoogleCalendarConnectionApplicationService;
import com.arturjarosz.task.calendar.application.GoogleCalendarShareApplicationService;
import com.arturjarosz.task.calendar.domain.CalendarExceptionCodes;
import com.arturjarosz.task.calendar.domain.GoogleCalendarException;
import com.arturjarosz.task.dto.CalendarEventDto;
import com.arturjarosz.task.dto.CalendarShareDto;
import com.arturjarosz.task.dto.GoogleCalendarConnectionDto;
import com.arturjarosz.task.rest.CalendarApi;
import com.arturjarosz.task.sharedkernel.exceptions.ErrorMessage;
import com.arturjarosz.task.sharedkernel.testhelpers.HttpHeadersBuilder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

@Slf4j
@RequiredArgsConstructor
@RestController
public class CalendarRestController implements CalendarApi {
    private static final String CALENDAR_EVENTS_API = "/calendars/events";
    private static final String CALENDAR_SHARES_API = "/calendars/shares";
    private static final String GOOGLE_CALENDAR_REQUEST_FAILED = createMessageCode(CalendarExceptionCodes.REQUEST_FAILED,
            CalendarExceptionCodes.GOOGLE_CALENDAR);

    @NonNull
    private final CalendarEventApplicationService calendarEventApplicationService;
    @NonNull
    private final GoogleCalendarConnectionApplicationService googleCalendarConnectionApplicationService;
    @NonNull
    private final GoogleCalendarShareApplicationService googleCalendarShareApplicationService;
    @NonNull
    private final MessageSource messageSource;

    @Override
    public ResponseEntity<CalendarEventDto> createCalendarEvent(CalendarEventDto calendarEventDto) {
        var createdEventDto = this.calendarEventApplicationService.createCalendarEvent(calendarEventDto);
        var headers = new HttpHeadersBuilder()
                .withLocation("%s/{id}".formatted(CALENDAR_EVENTS_API), createdEventDto.getId())
                .build();
        return new ResponseEntity<>(createdEventDto, headers, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<CalendarEventDto> updateCalendarEvent(CalendarEventDto calendarEventDto, Long eventId) {
        return new ResponseEntity<>(
                this.calendarEventApplicationService.updateCalendarEvent(eventId, calendarEventDto), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> deleteCalendarEvent(Long eventId) {
        this.calendarEventApplicationService.deleteCalendarEvent(eventId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CalendarEventDto> getCalendarEvent(Long eventId) {
        return new ResponseEntity<>(this.calendarEventApplicationService.getCalendarEvent(eventId), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<CalendarEventDto>> getCalendarEvents(LocalDate startDate, LocalDate endDate) {
        return new ResponseEntity<>(this.calendarEventApplicationService.getCalendarEvents(startDate, endDate),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<GoogleCalendarConnectionDto> getGoogleCalendarConnectionStatus() {
        return new ResponseEntity<>(this.googleCalendarConnectionApplicationService.getConnectionStatus(),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<CalendarShareDto>> getGoogleCalendarShares() {
        return new ResponseEntity<>(this.googleCalendarShareApplicationService.getShares(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CalendarShareDto> addGoogleCalendarShare(CalendarShareDto calendarShareDto) {
        var share = this.googleCalendarShareApplicationService.addShare(calendarShareDto);
        var headers = new HttpHeadersBuilder().withLocation(CALENDAR_SHARES_API).build();
        return new ResponseEntity<>(share, headers, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> removeGoogleCalendarShare(String email) {
        this.googleCalendarShareApplicationService.removeShare(email);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    // Sharing is done live in Google, so its refusals are reported to the user instead of an internal error; Google
    // explains them only in English, so its explanation is added to the translated message
    @ExceptionHandler(GoogleCalendarException.class)
    public ResponseEntity<ErrorMessage> handleGoogleCalendarException(GoogleCalendarException exception) {
        LOG.error(exception.getMessage(), exception);
        var message = this.messageSource.getMessage(GOOGLE_CALENDAR_REQUEST_FAILED,
                new Object[]{exception.getMessage()}, exception.getMessage(), Locale.getDefault());
        return new ResponseEntity<>(new ErrorMessage(message), HttpStatus.BAD_GATEWAY);
    }
}
