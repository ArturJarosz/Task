package com.arturjarosz.task.configuration.application.entry.impl

import com.arturjarosz.task.dto.ApplicationConfigurationDto
import spock.lang.Specification

class TaskStatusConfigurationProviderTest extends Specification {
    def subject = new TaskStatusConfigurationProvider()

    def "addConfigurationEntry should add task statuses to application configuration"() {
        given:
            def configuration = new ApplicationConfigurationDto()
        when:
            def result = subject.addConfigurationEntry(configuration)
        then:
            result.taskStatuses != null
            result.taskStatuses[0].id != null
            result.taskStatuses[0].label != null
    }
}
