package com.arturjarosz.task.configuration.application.entry.impl

import com.arturjarosz.task.dto.ApplicationConfigurationDto
import spock.lang.Specification

class StageStatusConfigurationProviderTest extends Specification {
    def subject = new StageStatusConfigurationProvider()

    def "addConfigurationEntry should add stage statuses to application configuration"() {
        given:
            def configuration = new ApplicationConfigurationDto()
        when:
            def result = subject.addConfigurationEntry(configuration)
        then:
            result.stageStatuses != null
            result.stageStatuses[0].id != null
            result.stageStatuses[0].label != null
    }
}
