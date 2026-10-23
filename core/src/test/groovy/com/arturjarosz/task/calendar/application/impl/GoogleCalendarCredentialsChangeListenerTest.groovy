package com.arturjarosz.task.calendar.application.impl

import com.arturjarosz.task.calendar.application.CalendarSyncService
import com.arturjarosz.task.calendar.domain.GoogleCalendarService
import com.arturjarosz.task.systemparameter.domain.SystemParameters
import spock.lang.Specification

class GoogleCalendarCredentialsChangeListenerTest extends Specification {
    static final OLD_KEY = "old key"
    static final NEW_KEY = "new key"
    static final OLD_ACCOUNT = "old@project.iam.gserviceaccount.com"
    static final NEW_ACCOUNT = "new@project.iam.gserviceaccount.com"

    def googleCalendarService = Mock(GoogleCalendarService)
    def calendarSyncService = Mock(CalendarSyncService)

    def subject = new GoogleCalendarCredentialsChangeListener(googleCalendarService, calendarSyncService)

    def "should pass both service accounts to the synchronization"() {
        given:
            this.googleCalendarService.readServiceAccountEmail(OLD_KEY) >> Optional.of(OLD_ACCOUNT)
            this.googleCalendarService.readServiceAccountEmail(NEW_KEY) >> Optional.of(NEW_ACCOUNT)
        when:
            this.subject.onSystemParameterChanged(SystemParameters.GOOGLE_CALENDAR_CREDENTIALS, OLD_KEY, NEW_KEY)
        then:
            1 * this.calendarSyncService.resetSynchronizationIfAccountChanged(OLD_ACCOUNT, NEW_ACCOUNT)
    }

    def "should pass no previous account when the first key is added"() {
        given:
            this.googleCalendarService.readServiceAccountEmail("") >> Optional.empty()
            this.googleCalendarService.readServiceAccountEmail(NEW_KEY) >> Optional.of(NEW_ACCOUNT)
        when:
            this.subject.onSystemParameterChanged(SystemParameters.GOOGLE_CALENDAR_CREDENTIALS, "", NEW_KEY)
        then:
            1 * this.calendarSyncService.resetSynchronizationIfAccountChanged(null, NEW_ACCOUNT)
    }

    def "should keep synchronization when the key is removed"() {
        given:
            this.googleCalendarService.readServiceAccountEmail(OLD_KEY) >> Optional.of(OLD_ACCOUNT)
            this.googleCalendarService.readServiceAccountEmail("") >> Optional.empty()
        when:
            this.subject.onSystemParameterChanged(SystemParameters.GOOGLE_CALENDAR_CREDENTIALS, OLD_KEY, "")
        then:
            0 * this.calendarSyncService._
    }

    def "should ignore other system parameters"() {
        when:
            this.subject.onSystemParameterChanged(SystemParameters.GOOGLE_CALENDAR_NAME, "Task", "Office")
        then:
            0 * this.googleCalendarService.readServiceAccountEmail(_)
            0 * this.calendarSyncService._
    }
}
