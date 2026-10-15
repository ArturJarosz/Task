package com.arturjarosz.task.calendar.rest;

import com.arturjarosz.task.calendar.application.CalendarEventApplicationService;
import com.arturjarosz.task.dto.CalendarEventDto;
import com.arturjarosz.task.rest.CalendarApi;
import com.arturjarosz.task.sharedkernel.testhelpers.HttpHeadersBuilder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class CalendarRestController implements CalendarApi {
    private static final String CALENDAR_EVENTS_API = "/calendars/events";

    @NonNull
    private final CalendarEventApplicationService calendarEventApplicationService;

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
}
