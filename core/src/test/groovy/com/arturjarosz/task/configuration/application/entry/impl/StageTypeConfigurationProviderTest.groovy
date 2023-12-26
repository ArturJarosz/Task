package com.arturjarosz.task.configuration.application.entry.impl

import com.arturjarosz.task.dto.ApplicationConfigurationDto
import spock.lang.Specification

class StageTypeConfigurationProviderTest extends Specification {

    def subject = new StageTypeConfigurationProvider()

    def "addConfigurationEntry should add stage types to application configuration"() {
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
