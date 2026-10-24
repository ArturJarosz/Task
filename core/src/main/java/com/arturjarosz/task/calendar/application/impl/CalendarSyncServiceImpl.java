package com.arturjarosz.task.calendar.application.impl;

import com.arturjarosz.task.calendar.application.CalendarSyncService;
import com.arturjarosz.task.calendar.domain.GoogleCalendarException;
import com.arturjarosz.task.calendar.domain.GoogleCalendarService;
import com.arturjarosz.task.calendar.domain.SyncTokenExpiredException;
import com.arturjarosz.task.calendar.infrastructure.repository.CalendarEventRepository;
import com.arturjarosz.task.calendar.infrastructure.repository.CalendarSyncStateRepository;
import com.arturjarosz.task.calendar.model.CalendarEvent;
import com.arturjarosz.task.calendar.model.CalendarEventSyncStatus;
import com.arturjarosz.task.calendar.model.CalendarSyncState;
import com.arturjarosz.task.calendar.model.dto.GoogleEventChangeDto;
import com.arturjarosz.task.calendar.model.dto.GoogleEventsSyncDto;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.systemparameter.application.GoogleCalendarSettingsProvider;
import com.arturjarosz.task.systemparameter.application.dto.GoogleCalendarSettings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@ApplicationService
public class CalendarSyncServiceImpl implements CalendarSyncService {

    private final CalendarEventRepository calendarEventRepository;
    private final CalendarSyncStateRepository calendarSyncStateRepository;
    private final GoogleCalendarService googleCalendarService;
    private final GoogleCalendarSettingsProvider googleCalendarSettingsProvider;

    @Override
    public CalendarEvent pushEvent(CalendarEvent calendarEvent) {
        var maybeSettings = this.googleCalendarSettingsProvider.getActiveSettings();
        if (maybeSettings.isEmpty()) {
            return calendarEvent;
        }
        try {
            var syncState = this.getSyncStateWithCalendar(maybeSettings.get());
            var eventData = calendarEvent.getGoogleEventId() == null
                    ? this.googleCalendarService.insertEvent(syncState.getGoogleCalendarId(), calendarEvent)
                    : this.googleCalendarService.updateEvent(syncState.getGoogleCalendarId(), calendarEvent);
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
        var maybeSettings = this.googleCalendarSettingsProvider.getActiveSettings();
        if (maybeSettings.isEmpty()) {
            return;
        }
        try {
            var syncState = this.getSyncStateWithCalendar(maybeSettings.get());
            this.googleCalendarService.deleteEvent(syncState.getGoogleCalendarId(), calendarEvent.getGoogleEventId());
            this.calendarEventRepository.delete(calendarEvent);
        } catch (Exception e) {
            LOG.warn("Removing calendar event with id {} from Google Calendar failed, event stays in {} status.",
                    calendarEvent.getId(), calendarEvent.getSyncStatus(), e);
        }
    }

    @Transactional
    @Override
    public void pushPendingEvents() {
        if (this.googleCalendarSettingsProvider.getActiveSettings().isEmpty()) {
            return;
        }
        this.calendarEventRepository.findBySyncStatus(CalendarEventSyncStatus.PENDING).forEach(this::pushEvent);
        this.calendarEventRepository.findBySyncStatus(CalendarEventSyncStatus.DELETE_PENDING).forEach(this::pushDelete);
    }

    @Transactional
    @Override
    public void runIncrementalSync() {
        var maybeSettings = this.googleCalendarSettingsProvider.getActiveSettings();
        if (maybeSettings.isEmpty()) {
            return;
        }
        var syncState = this.getSyncStateWithCalendar(maybeSettings.get());
        var fullSync = syncState.getSyncToken() == null;
        GoogleEventsSyncDto sync;
        try {
            sync = this.googleCalendarService.listEvents(syncState.getGoogleCalendarId(), syncState.getSyncToken());
        } catch (SyncTokenExpiredException e) {
            LOG.info("Sync token expired, running full synchronization with Google Calendar.");
            fullSync = true;
            sync = this.googleCalendarService.listEvents(syncState.getGoogleCalendarId(), null);
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

    @Transactional
    @Override
    public void resetSynchronizationIfAccountChanged(String previousServiceAccount, String serviceAccount) {
        var maybeSyncState = this.calendarSyncStateRepository.findFirstByOrderByIdAsc()
                .filter(syncState -> syncState.getGoogleCalendarId() != null);
        if (maybeSyncState.isEmpty()) {
            return;
        }
        // calendars created before the account was stored belong to the account of the previous key
        var calendarAccount = Optional.ofNullable(maybeSyncState.get().getGoogleAccount())
                .orElse(previousServiceAccount);
        if (!Objects.equals(calendarAccount, serviceAccount)) {
            LOG.info("Google Calendar credentials belong to another service account than the synchronized calendar.");
            this.resetSynchronization(maybeSyncState.get());
        }
    }

    @Override
    public Optional<String> getGoogleCalendarId() {
        return this.calendarSyncStateRepository.findFirstByOrderByIdAsc().map(CalendarSyncState::getGoogleCalendarId);
    }

    private void resetSynchronization(CalendarSyncState syncState) {
        syncState.setGoogleCalendarId(null);
        syncState.setSyncToken(null);
        syncState.setGoogleAccount(null);
        syncState.setCalendarName(null);
        this.calendarSyncStateRepository.save(syncState);
        // events waiting for removal cannot be removed from a calendar of another account anymore
        this.calendarEventRepository.findBySyncStatus(CalendarEventSyncStatus.DELETE_PENDING)
                .forEach(this.calendarEventRepository::delete);
        this.calendarEventRepository.findBySyncStatusNot(CalendarEventSyncStatus.DELETE_PENDING).forEach(event -> {
            event.detachFromGoogle();
            this.calendarEventRepository.save(event);
        });
        LOG.info("Synchronization with Google Calendar reset, all events will be pushed to a new calendar.");
    }

    private CalendarSyncState getSyncStateWithCalendar(GoogleCalendarSettings settings) {
        var syncState = this.calendarSyncStateRepository.findFirstByOrderByIdAsc()
                .orElseGet(() -> this.calendarSyncStateRepository.save(new CalendarSyncState()));
        if (syncState.getGoogleCalendarId() == null) {
            // the calendar outlives the local database, so reuse it and let the full sync import its events again
            var existingCalendarId = this.googleCalendarService.findCalendarByName(settings.calendarName());
            var googleCalendarId = existingCalendarId
                    .orElseGet(() -> this.googleCalendarService.createCalendar(settings.calendarName()));
            syncState.setGoogleCalendarId(googleCalendarId);
            syncState.setSyncToken(null);
            syncState.setCalendarName(settings.calendarName());
            syncState.setGoogleAccount(
                    this.googleCalendarService.readServiceAccountEmail(settings.credentials()).orElse(null));
            syncState = this.calendarSyncStateRepository.save(syncState);
            if (existingCalendarId.isPresent()) {
                LOG.info("Reusing existing Google Calendar with id {}.", googleCalendarId);
            } else {
                LOG.info("Created dedicated Google Calendar with id {}.", googleCalendarId);
            }
        } else if (!Objects.equals(syncState.getCalendarName(), settings.calendarName())) {
            syncState = this.renameCalendar(syncState, settings.calendarName());
        }
        return syncState;
    }

    /**
     * Applies the configured calendar name in Google. A failed rename does not stop the synchronization, it is
     * retried with the next call.
     */
    private CalendarSyncState renameCalendar(CalendarSyncState syncState, String calendarName) {
        try {
            this.googleCalendarService.renameCalendar(syncState.getGoogleCalendarId(), calendarName);
        } catch (GoogleCalendarException e) {
            LOG.warn("Renaming Google Calendar to {} failed.", calendarName, e);
            return syncState;
        }
        syncState.setCalendarName(calendarName);
        LOG.info("Renamed Google Calendar to {}.", calendarName);
        return this.calendarSyncStateRepository.save(syncState);
    }
}
