package com.arturjarosz.task.calendar.application.impl

import com.arturjarosz.task.calendar.application.CalendarSyncService
import com.arturjarosz.task.calendar.domain.GoogleCalendarService
import com.arturjarosz.task.dto.GoogleCalendarConnectionStatusDto
import com.arturjarosz.task.systemparameter.application.GoogleCalendarSettingsProvider
import com.arturjarosz.task.systemparameter.application.dto.GoogleCalendarSettings
import spock.lang.Specification

class GoogleCalendarConnectionApplicationServiceImplTest extends Specification {
    static final KEY = '{"type": "service_account"}'
    static final SERVICE_ACCOUNT_EMAIL = "task@project.iam.gserviceaccount.com"
    static final GOOGLE_CALENDAR_ID = "googleCalendar123"

    def googleCalendarSettingsProvider = Mock(GoogleCalendarSettingsProvider)
    def googleCalendarService = Mock(GoogleCalendarService) {
        readServiceAccountEmail(KEY) >> Optional.of(SERVICE_ACCOUNT_EMAIL)
    }
    def calendarSyncService = Mock(CalendarSyncService)

    def subject = new GoogleCalendarConnectionApplicationServiceImpl(googleCalendarSettingsProvider,
            googleCalendarService, calendarSyncService)

    def "should return NOT_CONFIGURED when no key is stored"() {
        given:
            this.googleCalendarSettingsProvider.getSettings() >> new GoogleCalendarSettings(true, "", "Task", "Task")
        when:
            def result = this.subject.getConnectionStatus()
        then:
            result.status == GoogleCalendarConnectionStatusDto.NOT_CONFIGURED
            result.checkedDateTime != null
            0 * this.googleCalendarService.checkConnection(_, _)
    }

    def "should return DISABLED with the key's account when sync is off"() {
        given:
            this.googleCalendarSettingsProvider.getSettings() >> new GoogleCalendarSettings(false, KEY, "Task", "Task")
        when:
            def result = this.subject.getConnectionStatus()
        then:
            result.status == GoogleCalendarConnectionStatusDto.DISABLED
            result.serviceAccountEmail == SERVICE_ACCOUNT_EMAIL
            0 * this.googleCalendarService.checkConnection(_, _)
    }

    def "should check the connection with stored settings and the synchronized calendar"() {
        given:
            def settings = new GoogleCalendarSettings(true, KEY, "Task", "Task")
            this.googleCalendarSettingsProvider.getSettings() >> settings
            this.calendarSyncService.getGoogleCalendarId() >> Optional.of(GOOGLE_CALENDAR_ID)
        when:
            def result = this.subject.getConnectionStatus()
        then:
            1 * this.googleCalendarService.checkConnection(settings, GOOGLE_CALENDAR_ID) >>
                    new GoogleCalendarService.ConnectionCheckResult(
                            GoogleCalendarConnectionStatusDto.PERMISSION_DENIED, "Calendar API is disabled")
            result.status == GoogleCalendarConnectionStatusDto.PERMISSION_DENIED
            result.message == "Calendar API is disabled"
            result.serviceAccountEmail == SERVICE_ACCOUNT_EMAIL
    }
}
