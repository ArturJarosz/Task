package com.arturjarosz.task.user.domain

import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException
import com.arturjarosz.task.user.model.UserConfigurationName
import spock.lang.Specification

class UserConfigurationValidatorTest extends Specification {

    def "validateConfiguration should accept a theme mode"() {
        when:
            UserConfigurationValidator.validateConfiguration(UserConfigurationName.THEME_MODE, value)
        then:
            noExceptionThrown()
        where:
            value << ["LIGHT", "DARK"]
    }

    def "validateConfiguration should accept a dashboard layout of up to 4000 characters"() {
        when:
            UserConfigurationValidator.validateConfiguration(UserConfigurationName.DASHBOARD_LAYOUT, value)
        then:
            noExceptionThrown()
        where:
            value << ['{"version":1,"rows":[]}', "x" * UserConfigurationValidator.MAX_DASHBOARD_LAYOUT_LENGTH]
    }

    def "validateConfiguration should reject a dashboard layout that is blank or longer than 4000 characters"() {
        when:
            UserConfigurationValidator.validateConfiguration(UserConfigurationName.DASHBOARD_LAYOUT, value)
        then:
            def exception = thrown(IllegalArgumentException)
            exception.message == "notValid.user.configuration.dashboardLayout"
        where:
            value << ["", "   ", "x" * (UserConfigurationValidator.MAX_DASHBOARD_LAYOUT_LENGTH + 1)]
    }
}
