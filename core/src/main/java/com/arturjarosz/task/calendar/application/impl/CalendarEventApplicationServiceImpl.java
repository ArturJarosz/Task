package com.arturjarosz.task.calendar.application.impl;

import com.arturjarosz.task.calendar.application.CalendarEventApplicationService;
import com.arturjarosz.task.calendar.application.CalendarSyncService;
import com.arturjarosz.task.calendar.application.mapper.CalendarEventMapper;
import com.arturjarosz.task.calendar.infrastructure.repository.CalendarEventRepository;
import com.arturjarosz.task.calendar.model.CalendarEvent;
import com.arturjarosz.task.calendar.model.CalendarEventSyncStatus;
import com.arturjarosz.task.dto.CalendarEventDto;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.arturjarosz.task.calendar.application.CalendarEventValidator.validateCalendarEventDto;
import static com.arturjarosz.task.calendar.application.CalendarEventValidator.validateCalendarEventExistence;

@Slf4j
@RequiredArgsConstructor
@ApplicationService
public class CalendarEventApplicationServiceImpl implements CalendarEventApplicationService {

    private final CalendarEventRepository calendarEventRepository;
    private final CalendarEventMapper calendarEventMapper;
    private final CalendarSyncService calendarSyncService;

    @Transactional
    @Override
    public CalendarEventDto createCalendarEvent(CalendarEventDto calendarEventDto) {
        LOG.debug("creating calendar event");

        validateCalendarEventDto(calendarEventDto);
        var calendarEvent = new CalendarEvent(calendarEventDto.getName(), calendarEventDto.getDescription(),
                LocalDateTime.parse(calendarEventDto.getStartDateTime()),
                LocalDateTime.parse(calendarEventDto.getEndDateTime()), calendarEventDto.getAllDay());
        calendarEvent = this.calendarEventRepository.save(calendarEvent);
        calendarEvent = this.calendarSyncService.pushEvent(calendarEvent);

        LOG.debug("calendar event created");
        return this.calendarEventMapper.mapToDto(calendarEvent);
    }

    @Transactional
    @Override
    public CalendarEventDto updateCalendarEvent(Long eventId, CalendarEventDto calendarEventDto) {
        LOG.debug("updating calendar event with id {}", eventId);

        var maybeCalendarEvent = this.calendarEventRepository.findById(eventId);
        validateCalendarEventExistence(maybeCalendarEvent, eventId);
        validateCalendarEventDto(calendarEventDto);
        var calendarEvent = maybeCalendarEvent.orElseThrow(ResourceNotFoundException::new);
        calendarEvent.update(calendarEventDto.getName(), calendarEventDto.getDescription(),
                LocalDateTime.parse(calendarEventDto.getStartDateTime()),
                LocalDateTime.parse(calendarEventDto.getEndDateTime()), calendarEventDto.getAllDay());
        calendarEvent = this.calendarEventRepository.save(calendarEvent);
        calendarEvent = this.calendarSyncService.pushEvent(calendarEvent);

        LOG.debug("calendar event with id {} updated", eventId);
        return this.calendarEventMapper.mapToDto(calendarEvent);
    }

    @Transactional
    @Override
    public void deleteCalendarEvent(Long eventId) {
        LOG.debug("removing calendar event with id {}", eventId);

        var maybeCalendarEvent = this.calendarEventRepository.findById(eventId);
        validateCalendarEventExistence(maybeCalendarEvent, eventId);
        var calendarEvent = maybeCalendarEvent.orElseThrow(ResourceNotFoundException::new);
        if (calendarEvent.getGoogleEventId() == null) {
            this.calendarEventRepository.delete(calendarEvent);
        } else {
            calendarEvent.markDeletePending();
            calendarEvent = this.calendarEventRepository.save(calendarEvent);
            this.calendarSyncService.pushDelete(calendarEvent);
        }

        LOG.debug("calendar event with id {} removed", eventId);
    }

    @Override
    public CalendarEventDto getCalendarEvent(Long eventId) {
        var maybeCalendarEvent = this.calendarEventRepository.findById(eventId);
        validateCalendarEventExistence(maybeCalendarEvent, eventId);

        LOG.debug("calendar event with id {} loaded", eventId);
        return this.calendarEventMapper.mapToDto(maybeCalendarEvent.orElseThrow(ResourceNotFoundException::new));
    }

    @Override
    public List<CalendarEventDto> getCalendarEvents(LocalDate startDate, LocalDate endDate) {
        List<CalendarEvent> calendarEvents;
        if (startDate == null && endDate == null) {
            calendarEvents = this.calendarEventRepository.findBySyncStatusNot(CalendarEventSyncStatus.DELETE_PENDING);
        } else {
            var rangeStart = startDate != null ? startDate.atStartOfDay() : LocalDate.EPOCH.atStartOfDay();
            var rangeEnd = endDate != null
                    ? endDate.plusDays(1).atStartOfDay()
                    : LocalDate.of(2999, 12, 31).atStartOfDay();
            calendarEvents = this.calendarEventRepository
                    .findBySyncStatusNotAndEndDateTimeGreaterThanEqualAndStartDateTimeLessThan(
                            CalendarEventSyncStatus.DELETE_PENDING, rangeStart, rangeEnd);
        }
        return calendarEvents.stream().map(this.calendarEventMapper::mapToDto).toList();
    }
}
