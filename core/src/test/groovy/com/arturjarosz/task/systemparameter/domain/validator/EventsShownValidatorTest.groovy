package com.arturjarosz.task.systemparameter.domain.validator

import com.arturjarosz.task.dto.SystemParameterDto
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException
import com.arturjarosz.task.systemparameter.query.SystemParameterQueryService
import spock.lang.Specification

class EventsShownValidatorTest extends Specification {
    def systemParameterQueryService = Mock(SystemParameterQueryService)

    def "validateOnUpdate should accept #value"() {
        when:
            new UpcomingEventsShownValidator(this.systemParameterQueryService)
                    .validateOnUpdate(new SystemParameterDto(value: value))
        then:
            noExceptionThrown()
        where:
            value << ["1", "10", "100"]
    }

    def "validateOnUpdate should not accept #value"() {
        when:
            new PastEventsShownValidator(this.systemParameterQueryService)
                    .validateOnUpdate(new SystemParameterDto(value: value))
        then:
            def exception = thrown(IllegalArgumentException)
            exception.message == "notValid.systemParameter.pastEventsShown.value"
        where:
            value << [null, "", "0", "101", "-1", "1.5", "abc"]
    }
}
