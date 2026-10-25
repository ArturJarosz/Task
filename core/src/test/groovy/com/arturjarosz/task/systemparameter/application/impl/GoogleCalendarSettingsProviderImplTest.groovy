package com.arturjarosz.task.systemparameter.application.impl

import com.arturjarosz.task.systemparameter.domain.SystemParameters
import com.arturjarosz.task.systemparameter.query.SystemParameterQueryService
import spock.lang.Specification

import java.time.Duration

class GoogleCalendarSettingsProviderImplTest extends Specification {
    def systemParameterQueryService = Mock(SystemParameterQueryService)

    def subject = new GoogleCalendarSettingsProviderImpl(systemParameterQueryService)

    def "should return intervals stored in minutes"() {
        given:
            this.systemParameterQueryService.getValueByName(SystemParameters.GOOGLE_CALENDAR_POLL_INTERVAL) >> "5"
            this.systemParameterQueryService.getValueByName(SystemParameters.GOOGLE_CALENDAR_PUSH_RETRY_INTERVAL) >> "2"
        expect:
            this.subject.getPollInterval() == Duration.ofMinutes(5)
            this.subject.getPushRetryInterval() == Duration.ofMinutes(2)
    }

    def "should fall back to default intervals when stored value is #value"() {
        given:
            this.systemParameterQueryService.getValueByName(_ as String) >> value
        expect:
            this.subject.getPollInterval() == Duration.ofMinutes(15)
            this.subject.getPushRetryInterval() == Duration.ofMinutes(1)
        where:
            value << [null, "", "abc", "0", "-3", "1441", "900000"]
    }
}
