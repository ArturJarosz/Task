package com.arturjarosz.task.calendar.infrastructure.schedule;

import com.arturjarosz.task.calendar.application.CalendarSyncService;
import com.arturjarosz.task.properties.GoogleCalendarProperties;
import com.arturjarosz.task.systemparameter.application.GoogleCalendarSettingsProvider;
import com.arturjarosz.task.systemparameter.application.SystemParameterChangeListener;
import com.arturjarosz.task.systemparameter.domain.SystemParameters;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Supplier;

/**
 * Scheduled jobs keeping local calendar events and the Google Calendar in sync. They do nothing while the Google
 * Calendar integration is disabled in the application settings. Their intervals are system parameters, read again
 * before every run, and a changed interval applies right away instead of after the wait already scheduled. A changed
 * connection synchronizes events both ways right away.
 */
@Slf4j
@Component
public class CalendarSyncJobs implements SystemParameterChangeListener {
    private static final Set<String> RESCHEDULING_PARAMETERS = Set.of(SystemParameters.GOOGLE_CALENDAR_POLL_INTERVAL,
            SystemParameters.GOOGLE_CALENDAR_PUSH_RETRY_INTERVAL, SystemParameters.GOOGLE_CALENDAR_SYNC,
            SystemParameters.GOOGLE_CALENDAR_CREDENTIALS);

    private final CalendarSyncService calendarSyncService;
    private final GoogleCalendarSettingsProvider googleCalendarSettingsProvider;
    private final TaskScheduler taskScheduler;
    private final Duration pollInitialDelay;

    private ScheduledFuture<?> pushJob;
    private ScheduledFuture<?> pollJob;

    public CalendarSyncJobs(CalendarSyncService calendarSyncService,
            GoogleCalendarSettingsProvider googleCalendarSettingsProvider, TaskScheduler taskScheduler,
            @Value("${task.google-calendar.poll-initial-delay:30s}") Duration pollInitialDelay) {
        this.calendarSyncService = calendarSyncService;
        this.googleCalendarSettingsProvider = googleCalendarSettingsProvider;
        this.taskScheduler = taskScheduler;
        this.pollInitialDelay = pollInitialDelay;
    }

    // System parameters are created by an application runner, so the jobs start once the application is ready
    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        // events left pending before the restart are sent right away
        this.schedulePushJob(Duration.ZERO);
        this.schedulePollJob(this.pollInitialDelay);
    }

    @Override
    public void onSystemParameterChanged(String name, String previousValue, String newValue) {
        if (!RESCHEDULING_PARAMETERS.contains(name) || Objects.equals(previousValue, newValue)) {
            return;
        }
        // the jobs read the settings from the database, so they are rescheduled once the change is committed
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    CalendarSyncJobs.this.reschedule(name);
                }
            });
        } else {
            this.reschedule(name);
        }
    }

    private void reschedule(String name) {
        switch (name) {
            case SystemParameters.GOOGLE_CALENDAR_POLL_INTERVAL -> {
                var interval = this.googleCalendarSettingsProvider.getPollInterval();
                LOG.info("Fetching changes from Google Calendar rescheduled to every {} minutes.",
                        interval.toMinutes());
                this.schedulePollJob(interval);
            }
            case SystemParameters.GOOGLE_CALENDAR_PUSH_RETRY_INTERVAL -> {
                var interval = this.googleCalendarSettingsProvider.getPushRetryInterval();
                LOG.info("Retrying events not sent to Google Calendar rescheduled to every {} minutes.",
                        interval.toMinutes());
                this.schedulePushJob(interval);
            }
            // a newly connected calendar may already hold events and local events may wait for it, so both are
            // synchronized now instead of after the interval
            default -> {
                LOG.info("Google Calendar connection changed, synchronizing events.");
                this.schedulePushJob(Duration.ZERO);
                this.schedulePollJob(Duration.ZERO);
            }
        }
    }

    private synchronized void schedulePushJob(Duration firstDelay) {
        cancel(this.pushJob);
        this.pushJob = this.taskScheduler.schedule(this::pushPendingEvents,
                delayTrigger(firstDelay, this.googleCalendarSettingsProvider::getPushRetryInterval,
                        Duration.ofMinutes(GoogleCalendarProperties.DEFAULT_PUSH_RETRY_INTERVAL)));
    }

    private synchronized void schedulePollJob(Duration firstDelay) {
        cancel(this.pollJob);
        this.pollJob = this.taskScheduler.schedule(this::pollGoogleCalendar,
                delayTrigger(firstDelay, this.googleCalendarSettingsProvider::getPollInterval,
                        Duration.ofMinutes(GoogleCalendarProperties.DEFAULT_POLL_INTERVAL)));
    }

    // A run in progress is not interrupted, its job just won't run again
    private static void cancel(ScheduledFuture<?> job) {
        if (job != null) {
            job.cancel(false);
        }
    }

    /**
     * Runs first after given delay, then waits the current interval after each finished run, like a fixed delay.
     * When the interval cannot be read, the fallback interval is used, as a failing trigger stops the job for good.
     */
    static Trigger delayTrigger(Duration firstDelay, Supplier<Duration> interval, Duration fallbackInterval) {
        var firstRun = Instant.now().plus(firstDelay);
        return triggerContext -> {
            var lastCompletion = triggerContext.lastCompletion();
            return lastCompletion == null ? firstRun : lastCompletion.plus(readInterval(interval, fallbackInterval));
        };
    }

    private static Duration readInterval(Supplier<Duration> interval, Duration fallbackInterval) {
        try {
            return interval.get();
        } catch (Exception e) {
            LOG.error("Reading interval of Google Calendar synchronization failed, {} minutes are used.",
                    fallbackInterval.toMinutes(), e);
            return fallbackInterval;
        }
    }

    void pushPendingEvents() {
        try {
            this.calendarSyncService.pushPendingEvents();
        } catch (Exception e) {
            LOG.warn("Pushing pending calendar events to Google Calendar failed.", e);
        }
    }

    void pollGoogleCalendar() {
        try {
            this.calendarSyncService.runIncrementalSync();
        } catch (Exception e) {
            LOG.warn("Synchronization with Google Calendar failed.", e);
        }
    }
}
