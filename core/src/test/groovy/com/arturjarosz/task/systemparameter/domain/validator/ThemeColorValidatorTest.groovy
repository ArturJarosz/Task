package com.arturjarosz.task.systemparameter.domain.validator

import com.arturjarosz.task.dto.SystemParameterDto
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException
import com.arturjarosz.task.systemparameter.domain.SystemParameters
import com.arturjarosz.task.systemparameter.query.SystemParameterQueryService
import spock.lang.Specification

class ThemeColorValidatorTest extends Specification {

    def systemParameterQueryService = Mock(SystemParameterQueryService)

    def themeColorValidator = new ThemeColorValidator(systemParameterQueryService)

    def "validateOnUpdate should accept six digit hex color"() {
        when:
            this.themeColorValidator.validateOnUpdate(new SystemParameterDto(name: SystemParameters.THEME_COLOR,
                    value: value))
        then:
            noExceptionThrown()
        where:
            value << ["#465fff", "#000000", "#ABCDEF"]
    }

    def "validateOnUpdate should throw exception for value that is not six digit hex color"() {
        when:
            this.themeColorValidator.validateOnUpdate(new SystemParameterDto(name: SystemParameters.THEME_COLOR,
                    value: value))
        then:
            thrown(IllegalArgumentException)
        where:
            value << [null, "", "465fff", "#fff", "#465fffaa", "blue", "#gggggg"]
    }

    def "validate should throw exception when stored theme color is not valid"() {
        given:
            this.systemParameterQueryService.getSystemPropertyByName(SystemParameters.THEME_COLOR) >>
                    new SystemParameterDto(name: SystemParameters.THEME_COLOR, value: "red")
        when:
            this.themeColorValidator.validate(SystemParameters.THEME_COLOR)
        then:
            thrown(IllegalArgumentException)
    }
}
