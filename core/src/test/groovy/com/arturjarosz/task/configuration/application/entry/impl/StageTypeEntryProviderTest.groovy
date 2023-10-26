package com.arturjarosz.task.configuration.application.entry.impl

import com.arturjarosz.task.dto.ApplicationConfigurationDto
import spock.lang.Specification

class StageTypeEntryProviderTest extends Specification {

    def subject = new StageTypeEntryProvider()

    def "addConfigurationEntry should add contractorTypes to application configuration"() {
        given:
            def configuration = new ApplicationConfigurationDto()
        when:
            def result = subject.addConfigurationEntry(configuration)
        then:
            result.stageTypes != null
            result.stageTypes[0].id != null
            result.stageTypes[0].label != null
    }
}
