package com.arturjarosz.task.configuration.application.entry.impl

import com.arturjarosz.task.dto.ApplicationConfigurationDto
import spock.lang.Specification

class ProjectStatusConfigurationProviderTest extends Specification {

    def subject = new ProjectStatusConfigurationProvider()

    def "addConfigurationEntry should add project statuses to application configuration"() {
        given:
            def configuration = new ApplicationConfigurationDto()
        when:
            def result = subject.addConfigurationEntry(configuration)
        then:
            result.projectStatuses != null
            result.projectStatuses[0].id != null
            result.projectStatuses[0].label != null
    }
}
