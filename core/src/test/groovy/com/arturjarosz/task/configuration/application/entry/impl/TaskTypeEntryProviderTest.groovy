package com.arturjarosz.task.configuration.application.entry.impl

import com.arturjarosz.task.dto.ApplicationConfigurationDto
import spock.lang.Specification

class TaskTypeEntryProviderTest extends Specification {
    def subject = new TaskTypeEntryProvider()

    def "addConfigurationEntry should add contractorTypes to application configuration"() {
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
