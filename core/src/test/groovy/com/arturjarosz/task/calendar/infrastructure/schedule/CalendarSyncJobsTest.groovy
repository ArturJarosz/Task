package com.arturjarosz.task.calendar.infrastructure.schedule

import com.arturjarosz.task.calendar.application.CalendarSyncService
import com.arturjarosz.task.systemparameter.application.GoogleCalendarSettingsProvider
import com.arturjarosz.task.systemparameter.domain.SystemParameters
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.Trigger
import org.springframework.scheduling.TriggerContext
import org.springframework.scheduling.support.SimpleTriggerContext
import spock.lang.Specification

import java.time.Duration
import java.time.Instant
import java.util.concurrent.ScheduledFuture

class CalendarSyncJobsTest extends Specification {
    def calendarSyncService = Mock(CalendarSyncService)
    def googleCalendarSettingsProvider = Mock(GoogleCalendarSettingsProvider) {
        getPollInterval() >> Duration.ofMinutes(15)
        getPushRetryInterval() >> Duration.ofMinutes(1)
    }
    def taskScheduler = Mock(TaskScheduler)

    def subject = new CalendarSyncJobs(calendarSyncService, googleCalendarSettingsProvider, taskScheduler,
            Duration.ofSeconds(30))

    def "trigger should run after the first delay, then wait the current interval after each run"() {
        given:
            def interval = Duration.ofMinutes(15)
            def trigger = CalendarSyncJobs.delayTrigger(Duration.ofSeconds(30), { interval }, Duration.ofMinutes(15))
            def lastCompletion = Instant.parse("2026-10-24T10:00:00Z")
        when:
            def firstRun = trigger.nextExecution(context(null))
        then:
            Duration.between(Instant.now(), firstRun).toSeconds() >= 28L
            Duration.between(Instant.now(), firstRun).toSeconds() <= 30L
        when:
            interval = Duration.ofMinutes(3)
        then: "a changed interval is used for the next run"
            trigger.nextExecution(context(lastCompletion)) == lastCompletion.plus(Duration.ofMinutes(3))
    }

    def "trigger should use the fallback interval when the interval cannot be read"() {
        given:
            def trigger = CalendarSyncJobs.delayTrigger(Duration.ofSeconds(30),
                    { throw new IllegalStateException("Database is down") }, Duration.ofMinutes(15))
            def lastCompletion = Instant.parse("2026-10-24T10:00:00Z")
        expect:
            trigger.nextExecution(context(lastCompletion)) == lastCompletion.plus(Duration.ofMinutes(15))
    }

    def "start should schedule both jobs"() {
        when:
            this.subject.start()
        then:
            2 * this.taskScheduler.schedule(_ as Runnable, _ as Trigger) >> Mock(ScheduledFuture)
    }

    def "changed poll interval should replace the scheduled poll job"() {
        given:
            def pushJob = Mock(ScheduledFuture)
            def pollJob = Mock(ScheduledFuture)
            this.taskScheduler.schedule(_ as Runnable, _ as Trigger) >>> [pushJob, pollJob, Mock(ScheduledFuture)]
            this.subject.start()
        when:
            this.subject.onSystemParameterChanged(SystemParameters.GOOGLE_CALENDAR_POLL_INTERVAL, "15", "5")
        then:
            1 * pollJob.cancel(false)
            0 * pushJob.cancel(_)
    }

    def "saving unchanged interval should not reschedule the jobs"() {
        when:
            this.subject.onSystemParameterChanged(SystemParameters.GOOGLE_CALENDAR_POLL_INTERVAL, "15", "15")
        then:
            0 * this.taskScheduler.schedule(_, _ as Trigger)
    }

    def "other parameters should not reschedule the jobs"() {
        when:
            this.subject.onSystemParameterChanged(SystemParameters.GOOGLE_CALENDAR_NAME, "Task", "Office")
        then:
            0 * this.taskScheduler.schedule(_, _ as Trigger)
    }

    def "failing synchronization should not stop the job"() {
        when:
            this.subject.pollGoogleCalendar()
            this.subject.pushPendingEvents()
        then:
            1 * this.calendarSyncService.runIncrementalSync() >> { throw new IllegalStateException("Google is down") }
            1 * this.calendarSyncService.pushPendingEvents() >> { throw new IllegalStateException("Google is down") }
            noExceptionThrown()
    }

    private static TriggerContext context(Instant lastCompletion) {
        return new SimpleTriggerContext(lastCompletion as Instant, lastCompletion as Instant, lastCompletion as Instant)
    }
}
