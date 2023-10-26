package com.arturjarosz.task.configuration.application.entry.impl

import com.arturjarosz.task.dto.ApplicationConfigurationDto
import spock.lang.Specification

class StageStatusEntryProviderTest extends Specification {
    def subject = new StageStatusEntryProvider()

    def "addConfigurationEntry should add contractorTypes to application configuration"() {
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
