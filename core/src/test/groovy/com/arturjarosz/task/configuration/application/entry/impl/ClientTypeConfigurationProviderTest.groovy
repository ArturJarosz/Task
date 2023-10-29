package com.arturjarosz.task.configuration.application.entry.impl

import com.arturjarosz.task.dto.ApplicationConfigurationDto
import spock.lang.Specification

class ClientTypeConfigurationProviderTest extends Specification {
    def subject = new ClientTypeConfigurationProvider()

    def "addConfigurationEntry should add clientTypes to application configuration"() {
        given:
            def configuration = new ApplicationConfigurationDto()
        when:
            def result = subject.addConfigurationEntry(configuration)
        then:
            result.clientTypes != null
            result.clientTypes[0].id != null
            result.clientTypes[0].label != null
    }
}
