package com.arturjarosz.task.calendar.application.impl

import com.arturjarosz.task.calendar.application.CalendarSyncService
import com.arturjarosz.task.calendar.domain.GoogleCalendarService
import com.arturjarosz.task.dto.CalendarShareDto
import com.arturjarosz.task.dto.CalendarShareRoleDto
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException
import com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException
import com.arturjarosz.task.systemparameter.application.GoogleCalendarSettingsProvider
import com.arturjarosz.task.systemparameter.application.dto.GoogleCalendarSettings
import spock.lang.Specification

class GoogleCalendarShareApplicationServiceImplTest extends Specification {
    static final KEY = '{"type": "service_account"}'
    static final SERVICE_ACCOUNT_EMAIL = "task@project.iam.gserviceaccount.com"
    static final GOOGLE_CALENDAR_ID = "4d195577ab@group.calendar.google.com"
    static final EMAIL = "john.doe@gmail.com"
    static final SETTINGS = new GoogleCalendarSettings(true, KEY, "Task", "Task")

    def googleCalendarSettingsProvider = Mock(GoogleCalendarSettingsProvider)
    def googleCalendarService = Mock(GoogleCalendarService) {
        readServiceAccountEmail(KEY) >> Optional.of(SERVICE_ACCOUNT_EMAIL)
    }
    def calendarSyncService = Mock(CalendarSyncService)

    def subject = new GoogleCalendarShareApplicationServiceImpl(googleCalendarSettingsProvider,
            googleCalendarService, calendarSyncService)

    def "getShares should list shares without the service account and the calendar's own address"() {
        given:
            this.mockActiveCalendar()
            this.googleCalendarService.listShares(GOOGLE_CALENDAR_ID) >> [
                    share(SERVICE_ACCOUNT_EMAIL, CalendarShareRoleDto.OWNER),
                    share(GOOGLE_CALENDAR_ID, CalendarShareRoleDto.OWNER),
                    share(EMAIL, CalendarShareRoleDto.READER),
                    share("anna@gmail.com", CalendarShareRoleDto.WRITER)]
        when:
            def result = this.subject.getShares()
        then:
            result*.email == [EMAIL, "anna@gmail.com"]
            result*.role == [CalendarShareRoleDto.READER, CalendarShareRoleDto.WRITER]
    }

    def "getShares should throw an exception when sync is turned off"() {
        given:
            this.googleCalendarSettingsProvider.getActiveSettings() >> Optional.empty()
        when:
            this.subject.getShares()
        then:
            def exception = thrown(IllegalArgumentException)
            exception.message == "forbidden.calendarShare.sync"
            0 * this.googleCalendarService.listShares(_)
    }

    def "getShares should throw an exception when the calendar is not created yet"() {
        given:
            this.googleCalendarSettingsProvider.getActiveSettings() >> Optional.of(SETTINGS)
            this.calendarSyncService.getGoogleCalendarId() >> Optional.empty()
        when:
            this.subject.getShares()
        then:
            def exception = thrown(IllegalArgumentException)
            exception.message == "notExist.calendarShare.googleCalendar"
    }

    def "addShare should share the calendar with trimmed lower case email"() {
        given:
            this.mockActiveCalendar()
            this.googleCalendarService.listShares(GOOGLE_CALENDAR_ID) >> []
        when:
            def result = this.subject.addShare(share(" John.Doe@Gmail.com ", CalendarShareRoleDto.WRITER))
        then:
            1 * this.googleCalendarService.shareCalendar(GOOGLE_CALENDAR_ID, EMAIL, CalendarShareRoleDto.WRITER) >>
                    share(EMAIL, CalendarShareRoleDto.WRITER)
            result.email == EMAIL
            result.role == CalendarShareRoleDto.WRITER
    }

    def "addShare should throw an exception when the email already has access"() {
        given:
            this.mockActiveCalendar()
            this.googleCalendarService.listShares(GOOGLE_CALENDAR_ID) >> [share(EMAIL, CalendarShareRoleDto.READER)]
        when:
            this.subject.addShare(share(EMAIL, CalendarShareRoleDto.WRITER))
        then:
            def exception = thrown(IllegalArgumentException)
            exception.message == "alreadySet.calendarShare.email"
            0 * this.googleCalendarService.shareCalendar(_, _, _)
    }

    def "addShare should accept email with #description"() {
        given:
            this.mockActiveCalendar()
            this.googleCalendarService.listShares(GOOGLE_CALENDAR_ID) >> []
        when:
            this.subject.addShare(share(email, CalendarShareRoleDto.READER))
        then:
            1 * this.googleCalendarService.shareCalendar(GOOGLE_CALENDAR_ID, email, CalendarShareRoleDto.READER) >>
                    share(email, CalendarShareRoleDto.READER)
        where:
            description            | email
            "subdomain"            | "jan@mail.firma.pl"
            "hyphen in the domain" | "anna@my-company.com"
    }

    def "addShare should not change access of the #description"() {
        given:
            this.mockActiveCalendar()
            this.googleCalendarService.listShares(GOOGLE_CALENDAR_ID) >>
                    [share(SERVICE_ACCOUNT_EMAIL, CalendarShareRoleDto.OWNER),
                     share(GOOGLE_CALENDAR_ID, CalendarShareRoleDto.OWNER)]
        when:
            this.subject.addShare(share(email, CalendarShareRoleDto.READER))
        then:
            def exception = thrown(IllegalArgumentException)
            exception.message == "alreadySet.calendarShare.email"
            0 * this.googleCalendarService.shareCalendar(_, _, _)
        where:
            description       | email
            "service account" | SERVICE_ACCOUNT_EMAIL
            "calendar itself" | GOOGLE_CALENDAR_ID
    }

    def "addShare should throw an exception for #description"() {
        when:
            this.subject.addShare(calendarShare)
        then:
            def exception = thrown(IllegalArgumentException)
            exception.message == messageCode
            0 * this.googleCalendarService.shareCalendar(_, _, _)
        where:
            description       | calendarShare                                         || messageCode
            "missing share"   | null                                                  || "isNull.calendarShare"
            "empty email"     | share(" ", CalendarShareRoleDto.READER)               || "isEmpty.calendarShare.email"
            "invalid email"   | share("john.doe", CalendarShareRoleDto.READER)        || "notValid.email"
            "missing role"    | share(EMAIL, null)                                    || "isNull.calendarShare.role"
            "owner role"      | share(EMAIL, CalendarShareRoleDto.OWNER)              || "notValid.calendarShare.role"
            "free busy role"  | share(EMAIL, CalendarShareRoleDto.FREE_BUSY_READER)   || "notValid.calendarShare.role"
    }

    def "removeShare should revoke access of a person the calendar is shared with"() {
        given:
            this.mockActiveCalendar()
            this.googleCalendarService.listShares(GOOGLE_CALENDAR_ID) >> [share(EMAIL, CalendarShareRoleDto.READER)]
        when:
            this.subject.removeShare(EMAIL)
        then:
            1 * this.googleCalendarService.revokeShare(GOOGLE_CALENDAR_ID, EMAIL)
    }

    def "removeShare should revoke access with trimmed lower case email"() {
        given:
            this.mockActiveCalendar()
            this.googleCalendarService.listShares(GOOGLE_CALENDAR_ID) >> [share(EMAIL, CalendarShareRoleDto.READER)]
        when:
            this.subject.removeShare(" John.Doe@Gmail.com ")
        then:
            1 * this.googleCalendarService.revokeShare(GOOGLE_CALENDAR_ID, EMAIL)
    }

    def "removeShare should throw an exception for the service account, the calendar and emails without access"() {
        given:
            this.mockActiveCalendar()
            this.googleCalendarService.listShares(GOOGLE_CALENDAR_ID) >>
                    [share(SERVICE_ACCOUNT_EMAIL, CalendarShareRoleDto.OWNER),
                     share(GOOGLE_CALENDAR_ID, CalendarShareRoleDto.OWNER)]
        when:
            this.subject.removeShare(email)
        then:
            def exception = thrown(ResourceNotFoundException)
            exception.message == "notExist.calendarShare.email"
            0 * this.googleCalendarService.revokeShare(_, _)
        where:
            email << [SERVICE_ACCOUNT_EMAIL, GOOGLE_CALENDAR_ID, EMAIL]
    }

    private void mockActiveCalendar() {
        this.googleCalendarSettingsProvider.getActiveSettings() >> Optional.of(SETTINGS)
        this.calendarSyncService.getGoogleCalendarId() >> Optional.of(GOOGLE_CALENDAR_ID)
    }

    private static CalendarShareDto share(String email, CalendarShareRoleDto role) {
        return new CalendarShareDto(email: email, role: role)
    }
}
