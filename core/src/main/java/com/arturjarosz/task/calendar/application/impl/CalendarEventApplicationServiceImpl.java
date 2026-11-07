package com.arturjarosz.task.calendar.application.impl;

import com.arturjarosz.task.calendar.application.CalendarEventApplicationService;
import com.arturjarosz.task.calendar.application.CalendarSyncService;
import com.arturjarosz.task.calendar.application.mapper.CalendarEventMapper;
import com.arturjarosz.task.calendar.infrastructure.repository.CalendarEventRepository;
import com.arturjarosz.task.calendar.model.CalendarEvent;
import com.arturjarosz.task.calendar.model.CalendarEventSyncStatus;
import com.arturjarosz.task.client.application.ClientValidator;
import com.arturjarosz.task.dto.CalendarEventDto;
import com.arturjarosz.task.project.application.ProjectValidator;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.query.ProjectQueryService;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.arturjarosz.task.calendar.application.CalendarEventValidator.validateCalendarEventDto;
import static com.arturjarosz.task.calendar.application.CalendarEventValidator.validateCalendarEventExistence;
import static com.arturjarosz.task.calendar.application.CalendarEventValidator.validateProjectBelongsToClient;

@Slf4j
@RequiredArgsConstructor
@ApplicationService
public class CalendarEventApplicationServiceImpl implements CalendarEventApplicationService {

    private final CalendarEventRepository calendarEventRepository;
    private final CalendarEventMapper calendarEventMapper;
    private final CalendarSyncService calendarSyncService;
    private final ClientValidator clientValidator;
    private final ProjectValidator projectValidator;
    private final ProjectQueryService projectQueryService;

    @Transactional
    @Override
    public CalendarEventDto createCalendarEvent(CalendarEventDto calendarEventDto) {
        LOG.info("creating calendar event");

        validateCalendarEventDto(calendarEventDto);
        this.validateConnections(calendarEventDto);
        var calendarEvent = new CalendarEvent(calendarEventDto.getName(), calendarEventDto.getDescription(),
                LocalDateTime.parse(calendarEventDto.getStartDateTime()),
                LocalDateTime.parse(calendarEventDto.getEndDateTime()), calendarEventDto.getAllDay());
        calendarEvent.connect(calendarEventDto.getClientId(), calendarEventDto.getProjectId());
        calendarEvent = this.calendarEventRepository.save(calendarEvent);
        calendarEvent = this.calendarSyncService.pushEvent(calendarEvent);

        LOG.info("calendar event created");
        return this.calendarEventMapper.mapToDto(calendarEvent);
    }

    @Transactional
    @Override
    public CalendarEventDto updateCalendarEvent(Long eventId, CalendarEventDto calendarEventDto) {
        LOG.info("updating calendar event with id {}", eventId);

        var maybeCalendarEvent = this.calendarEventRepository.findById(eventId);
        validateCalendarEventExistence(maybeCalendarEvent, eventId);
        validateCalendarEventDto(calendarEventDto);
        this.validateConnections(calendarEventDto);
        var calendarEvent = maybeCalendarEvent.orElseThrow(ResourceNotFoundException::new);
        calendarEvent.update(calendarEventDto.getName(), calendarEventDto.getDescription(),
                LocalDateTime.parse(calendarEventDto.getStartDateTime()),
                LocalDateTime.parse(calendarEventDto.getEndDateTime()), calendarEventDto.getAllDay());
        calendarEvent.connect(calendarEventDto.getClientId(), calendarEventDto.getProjectId());
        calendarEvent = this.calendarEventRepository.save(calendarEvent);
        calendarEvent = this.calendarSyncService.pushEvent(calendarEvent);

        LOG.info("calendar event with id {} updated", eventId);
        return this.calendarEventMapper.mapToDto(calendarEvent);
    }

    @Transactional
    @Override
    public void deleteCalendarEvent(Long eventId) {
        LOG.info("removing calendar event with id {}", eventId);

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

        LOG.info("calendar event with id {} removed", eventId);
    }

    @Override
    public CalendarEventDto getCalendarEvent(Long eventId) {
        var maybeCalendarEvent = this.calendarEventRepository.findById(eventId);
        validateCalendarEventExistence(maybeCalendarEvent, eventId);

        LOG.info("calendar event with id {} loaded", eventId);
        return this.calendarEventMapper.mapToDto(maybeCalendarEvent.orElseThrow(ResourceNotFoundException::new));
    }

    @Override
    public List<CalendarEventDto> getCalendarEvents(LocalDate startDate, LocalDate endDate, Long clientId,
            Long projectId) {
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
        var clientProjectIds = clientId == null ? Set.<Long>of()
                : this.projectQueryService.getProjectsForClientId(clientId).stream().map(Project::getId)
                        .collect(Collectors.toSet());
        return calendarEvents.stream()
                .filter(event -> clientId == null || clientId.equals(event.getClientId())
                        || clientProjectIds.contains(event.getProjectId()))
                .filter(event -> projectId == null || projectId.equals(event.getProjectId()))
                .map(this.calendarEventMapper::mapToDto).toList();
    }

    private void validateConnections(CalendarEventDto calendarEventDto) {
        var clientId = calendarEventDto.getClientId();
        var projectId = calendarEventDto.getProjectId();
        if (clientId != null) {
            this.clientValidator.validateClientExistence(clientId);
        }
        if (projectId != null) {
            this.projectValidator.validateProjectExistence(projectId);
        }
        if (clientId != null && projectId != null) {
            validateProjectBelongsToClient(this.projectQueryService.getProjectsForClientId(clientId).stream()
                    .anyMatch(project -> projectId.equals(project.getId())));
        }
    }
}
