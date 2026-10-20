package com.arturjarosz.task.configuration.application.entry.impl

import com.arturjarosz.task.dto.ApplicationConfigurationDto
import com.arturjarosz.task.dto.ProjectStatusDto
import com.arturjarosz.task.project.status.project.ProjectStatus
import com.arturjarosz.task.properties.ProjectProperties
import spock.lang.Specification

class ActiveProjectStatusesConfigurationProviderTest extends Specification {

    def "addConfigurationEntry should add active project statuses from properties to application configuration"() {
        given:
            def subject = new ActiveProjectStatusesConfigurationProvider(
                    new ProjectProperties([ProjectStatus.IN_PROGRESS, ProjectStatus.TO_DO] as Set))
            def configuration = new ApplicationConfigurationDto()
        when:
            def result = subject.addConfigurationEntry(configuration)
        then:
            result.activeProjectStatuses == [ProjectStatusDto.TO_DO, ProjectStatusDto.IN_PROGRESS]
    }

    def "addConfigurationEntry should add empty list when no active project statuses are configured"() {
        given:
            def subject = new ActiveProjectStatusesConfigurationProvider(new ProjectProperties(null))
            def configuration = new ApplicationConfigurationDto()
        when:
            def result = subject.addConfigurationEntry(configuration)
        then:
            result.activeProjectStatuses == []
    }
}
