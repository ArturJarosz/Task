package com.arturjarosz.task.configuration.application.entry.impl

import com.arturjarosz.task.dto.ApplicationConfigurationDto
import spock.lang.Specification

class ProjectTypeConfigurationProviderTest extends Specification {

    def subject = new ProjectTypeConfigurationProvider()

    def "addConfigurationEntry should add project types to application configuration"() {
        given:
            def configuration = new ApplicationConfigurationDto()
        when:
            def result = subject.addConfigurationEntry(configuration)
        then:
            result.projectTypes != null
            result.projectTypes[0].id != null
            result.projectTypes[0].label != null
    }
}
