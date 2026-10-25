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
    def pollIntervalValidator = new GoogleCalendarPollIntervalValidator(systemParameterQueryService)
    def pushRetryIntervalValidator = new GoogleCalendarPushRetryIntervalValidator(systemParameterQueryService)

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

    def "intervals accept whole numbers of minutes from 1 to 1440"() {
        when:
            this.pollIntervalValidator.validateOnUpdate(new SystemParameterDto(value: value))
            this.pushRetryIntervalValidator.validateOnUpdate(new SystemParameterDto(value: value))
        then:
            noExceptionThrown()
        where:
            value << ["1", "15", "1440"]
    }

    def "intervals reject #value"() {
        when:
            this.pollIntervalValidator.validateOnUpdate(new SystemParameterDto(value: value))
        then:
            def exception = thrown(IllegalArgumentException)
            exception.message == "notValid.systemParameter.googleCalendarPollInterval.value"
        where:
            value << ["0", "1441", "-5", "1.5", "abc", "", " 15", "99999", null]
    }

    def "stored intervals are validated on application start"() {
        given:
            this.systemParameterQueryService.getValueByName(SystemParameters.GOOGLE_CALENDAR_PUSH_RETRY_INTERVAL) >> "0"
        when:
            this.pushRetryIntervalValidator.validate(SystemParameters.GOOGLE_CALENDAR_PUSH_RETRY_INTERVAL)
        then:
            def exception = thrown(IllegalArgumentException)
            exception.message == "notValid.systemParameter.googleCalendarPushRetryInterval.value"
    }
}
