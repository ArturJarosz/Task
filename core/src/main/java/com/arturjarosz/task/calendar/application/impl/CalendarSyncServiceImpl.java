package com.arturjarosz.task.calendar.application.impl;

import com.arturjarosz.task.calendar.application.CalendarSyncService;
import com.arturjarosz.task.calendar.domain.GoogleCalendarService;
import com.arturjarosz.task.calendar.domain.SyncTokenExpiredException;
import com.arturjarosz.task.calendar.infrastructure.repository.CalendarEventRepository;
import com.arturjarosz.task.calendar.infrastructure.repository.CalendarSyncStateRepository;
import com.arturjarosz.task.calendar.model.CalendarEvent;
import com.arturjarosz.task.calendar.model.CalendarEventSyncStatus;
import com.arturjarosz.task.calendar.model.CalendarSyncState;
import com.arturjarosz.task.calendar.model.dto.GoogleEventChangeDto;
import com.arturjarosz.task.calendar.model.dto.GoogleEventsSyncDto;
import com.arturjarosz.task.properties.GoogleCalendarProperties;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@ApplicationService
public class CalendarSyncServiceImpl implements CalendarSyncService {

    private final CalendarEventRepository calendarEventRepository;
    private final CalendarSyncStateRepository calendarSyncStateRepository;
    private final ObjectProvider<GoogleCalendarService> googleCalendarService;
    private final GoogleCalendarProperties googleCalendarProperties;

    @Override
    public CalendarEvent pushEvent(CalendarEvent calendarEvent) {
        var google = this.googleCalendarService.getIfAvailable();
        if (google == null) {
            return calendarEvent;
        }
        try {
            var syncState = this.getSyncStateWithCalendar(google);
            var eventData = calendarEvent.getGoogleEventId() == null
                    ? google.insertEvent(syncState.getGoogleCalendarId(), calendarEvent)
                    : google.updateEvent(syncState.getGoogleCalendarId(), calendarEvent);
            calendarEvent.markSynced(eventData.googleEventId(), eventData.googleEtag());
            calendarEvent = this.calendarEventRepository.save(calendarEvent);
        } catch (Exception e) {
            LOG.warn("Pushing calendar event with id {} to Google Calendar failed, event stays in {} status.",
                    calendarEvent.getId(), calendarEvent.getSyncStatus(), e);
        }
        return calendarEvent;
    }

    @Override
    public void pushDelete(CalendarEvent calendarEvent) {
        var google = this.googleCalendarService.getIfAvailable();
        if (google == null) {
            return;
        }
        try {
            var syncState = this.getSyncStateWithCalendar(google);
            google.deleteEvent(syncState.getGoogleCalendarId(), calendarEvent.getGoogleEventId());
            this.calendarEventRepository.delete(calendarEvent);
        } catch (Exception e) {
            LOG.warn("Removing calendar event with id {} from Google Calendar failed, event stays in {} status.",
                    calendarEvent.getId(), calendarEvent.getSyncStatus(), e);
        }
    }

    @Transactional
    @Override
    public void pushPendingEvents() {
        if (this.googleCalendarService.getIfAvailable() == null) {
            return;
        }
        this.calendarEventRepository.findBySyncStatus(CalendarEventSyncStatus.PENDING).forEach(this::pushEvent);
        this.calendarEventRepository.findBySyncStatus(CalendarEventSyncStatus.DELETE_PENDING).forEach(this::pushDelete);
    }

    @Transactional
    @Override
    public void runIncrementalSync() {
        var google = this.googleCalendarService.getIfAvailable();
        if (google == null) {
            return;
        }
        var syncState = this.getSyncStateWithCalendar(google);
        var fullSync = syncState.getSyncToken() == null;
        GoogleEventsSyncDto sync;
        try {
            sync = google.listEvents(syncState.getGoogleCalendarId(), syncState.getSyncToken());
        } catch (SyncTokenExpiredException e) {
            LOG.info("Sync token expired, running full synchronization with Google Calendar.");
            fullSync = true;
            sync = google.listEvents(syncState.getGoogleCalendarId(), null);
        }
        this.applyChanges(sync.events());
        if (fullSync) {
            this.removeLocalEventsMissingInGoogle(sync.events());
        }
        syncState.setSyncToken(sync.nextSyncToken());
        this.calendarSyncStateRepository.save(syncState);
    }

    private void applyChanges(List<GoogleEventChangeDto> changes) {
        for (var change : changes) {
            var maybeEvent = this.calendarEventRepository.findByGoogleEventId(change.googleEventId());
            if (change.cancelled()) {
                maybeEvent.ifPresent(this.calendarEventRepository::delete);
                continue;
            }
            if (maybeEvent.isEmpty()) {
                var event = new CalendarEvent(change.name(), change.description(), change.startDateTime(),
                        change.endDateTime(), change.allDay());
                event.markSynced(change.googleEventId(), change.googleEtag());
                this.calendarEventRepository.save(event);
                continue;
            }
            var event = maybeEvent.get();
            // local changes waiting for push win over remote ones and overwrite them on the next push
            if (event.getSyncStatus() != CalendarEventSyncStatus.SYNCED) {
                continue;
            }
            if (!Objects.equals(event.getGoogleEtag(), change.googleEtag())) {
                event.updateFromGoogle(change.name(), change.description(), change.startDateTime(),
                        change.endDateTime(), change.allDay(), change.googleEtag());
                this.calendarEventRepository.save(event);
            }
        }
    }

    private void removeLocalEventsMissingInGoogle(List<GoogleEventChangeDto> changes) {
        Set<String> googleEventIds = changes.stream()
                .filter(change -> !change.cancelled())
                .map(GoogleEventChangeDto::googleEventId)
                .collect(Collectors.toSet());
        this.calendarEventRepository.findBySyncStatus(CalendarEventSyncStatus.SYNCED)
                .stream()
                .filter(event -> event.getGoogleEventId() != null && !googleEventIds.contains(
                        event.getGoogleEventId()))
                .forEach(this.calendarEventRepository::delete);
    }

    private CalendarSyncState getSyncStateWithCalendar(GoogleCalendarService google) {
        var syncState = this.calendarSyncStateRepository.findFirstByOrderByIdAsc()
                .orElseGet(() -> this.calendarSyncStateRepository.save(new CalendarSyncState()));
        if (syncState.getGoogleCalendarId() == null) {
            var googleCalendarId = google.createCalendar(this.googleCalendarProperties.calendarName());
            syncState.setGoogleCalendarId(googleCalendarId);
            syncState = this.calendarSyncStateRepository.save(syncState);
            LOG.info("Created dedicated Google Calendar with id {}.", googleCalendarId);
        }
        return syncState;
    }
}
