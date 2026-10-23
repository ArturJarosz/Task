package com.arturjarosz.task.calendar.infrastructure.schedule;

import com.arturjarosz.task.calendar.application.CalendarSyncService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled jobs keeping local calendar events and the Google Calendar in sync. They do nothing while the Google
 * Calendar integration is disabled in the application settings.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class CalendarSyncJobs {

    @NonNull
    private final CalendarSyncService calendarSyncService;

    @Scheduled(fixedDelayString = "${task.google-calendar.push-retry-delay:60000}")
    public void pushPendingEvents() {
        try {
            this.calendarSyncService.pushPendingEvents();
        } catch (Exception e) {
            LOG.warn("Pushing pending calendar events to Google Calendar failed.", e);
        }
    }

    @Scheduled(fixedDelayString = "${task.google-calendar.poll-interval:900000}", initialDelayString = "${task.google-calendar.poll-initial-delay:30000}")
    public void pollGoogleCalendar() {
        try {
            this.calendarSyncService.runIncrementalSync();
        } catch (Exception e) {
            LOG.warn("Synchronization with Google Calendar failed.", e);
        }
    }
}
