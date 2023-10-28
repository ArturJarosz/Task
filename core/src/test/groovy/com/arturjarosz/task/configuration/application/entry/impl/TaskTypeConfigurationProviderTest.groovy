package com.arturjarosz.task.configuration.application.entry.impl

import com.arturjarosz.task.dto.ApplicationConfigurationDto
import spock.lang.Specification

class TaskTypeConfigurationProviderTest extends Specification {
    def subject = new TaskTypeConfigurationProvider()

    def "addConfigurationEntry should add task types to application configuration"() {
        given:
            def configuration = new ApplicationConfigurationDto()
        when:
            def result = subject.addConfigurationEntry(configuration)
        then:
            result.taskTypes != null
            result.taskTypes[0].id != null
            result.taskTypes[0].label != null
    }

}
