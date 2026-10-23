package com.arturjarosz.task.calendar.domain.validator

import com.arturjarosz.task.calendar.domain.GoogleCalendarService
import com.arturjarosz.task.dto.SystemParameterDto
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException
import com.arturjarosz.task.systemparameter.domain.SystemParameters
import com.arturjarosz.task.systemparameter.query.SystemParameterQueryService
import spock.lang.Specification

class GoogleCalendarParameterValidatorsTest extends Specification {
    static final KEY = '{"type": "service_account"}'

    def systemParameterQueryService = Mock(SystemParameterQueryService)
    def googleCalendarService = Mock(GoogleCalendarService)

    def syncValidator = new GoogleCalendarSyncValidator(systemParameterQueryService)
    def credentialsValidator = new GoogleCalendarCredentialsValidator(systemParameterQueryService,
            googleCalendarService)
    def calendarNameValidator = new GoogleCalendarNameValidator(systemParameterQueryService)

    def "sync can be turned on when a key is stored"() {
        given:
            this.systemParameterQueryService.getValueByName(SystemParameters.GOOGLE_CALENDAR_CREDENTIALS) >> KEY
        when:
            this.syncValidator.validateOnUpdate(new SystemParameterDto(value: "true"))
        then:
            noExceptionThrown()
    }

    def "sync cannot be turned on without a stored key"() {
        given:
            this.systemParameterQueryService.getValueByName(SystemParameters.GOOGLE_CALENDAR_CREDENTIALS) >> credentials
        when:
            this.syncValidator.validateOnUpdate(new SystemParameterDto(value: "true"))
        then:
            thrown(IllegalArgumentException)
        where:
            credentials << [null, "", " "]
    }

    def "sync can be turned off without a key"() {
        when:
            this.syncValidator.validateOnUpdate(new SystemParameterDto(value: "false"))
        then:
            noExceptionThrown()
            0 * this.systemParameterQueryService.getValueByName(_)
    }

    def "sync accepts only true or false"() {
        when:
            this.syncValidator.validateOnUpdate(new SystemParameterDto(value: value))
        then:
            thrown(IllegalArgumentException)
        where:
            value << ["TRUE", "yes", "", null]
    }

    def "credentials can be empty or a service account key when sync is turned off"() {
        given:
            this.systemParameterQueryService.getValueByName(SystemParameters.GOOGLE_CALENDAR_SYNC) >> "false"
            this.googleCalendarService.readServiceAccountEmail(KEY) >> Optional.of("task@project.iam.gserviceaccount.com")
        when:
            this.credentialsValidator.validateOnUpdate(new SystemParameterDto(value: value))
        then:
            noExceptionThrown()
        where:
            value << [null, "", KEY]
    }

    def "credentials cannot be removed while sync is turned on"() {
        given:
            this.systemParameterQueryService.getValueByName(SystemParameters.GOOGLE_CALENDAR_SYNC) >> "true"
        when:
            this.credentialsValidator.validateOnUpdate(new SystemParameterDto(value: value))
        then:
            thrown(IllegalArgumentException)
        where:
            value << [null, "", " "]
    }

    def "credentials cannot be text that is not a service account key"() {
        given:
            this.googleCalendarService.readServiceAccountEmail("not a key") >> Optional.empty()
        when:
            this.credentialsValidator.validateOnUpdate(new SystemParameterDto(value: "not a key"))
        then:
            thrown(IllegalArgumentException)
    }

    def "calendar name can be up to 255 characters"() {
        when:
            this.calendarNameValidator.validateOnUpdate(new SystemParameterDto(value: value))
        then:
            noExceptionThrown()
        where:
            value << ["Office", "x" * 255]
    }

    def "calendar name cannot be blank or longer than 255 characters"() {
        when:
            this.calendarNameValidator.validateOnUpdate(new SystemParameterDto(value: value))
        then:
            thrown(IllegalArgumentException)
        where:
            value << ["x" * 256, " ", "", null]
    }
}
