package com.arturjarosz.task.configuration.application.entry.impl

import com.arturjarosz.task.dto.ApplicationConfigurationDto
import com.arturjarosz.task.project.infrastructure.repositor.ProjectTypeRepository
import com.arturjarosz.task.project.model.ProjectType
import com.arturjarosz.task.sharedkernel.testhelpers.TestUtils
import org.springframework.data.domain.Sort
import spock.lang.Specification

class ProjectTypeConfigurationProviderTest extends Specification {

    def projectTypeRepository = Mock(ProjectTypeRepository)

    def subject = new ProjectTypeConfigurationProvider(projectTypeRepository)

    def "addConfigurationEntry should add stored project types with their id and name"() {
        given:
            def concept = new ProjectType("Concept")
            TestUtils.setFieldForObject(concept, "id", 100L)
            this.projectTypeRepository.findAll(Sort.by("name")) >> [concept]
        when:
            def result = subject.addConfigurationEntry(new ApplicationConfigurationDto())
        then:
            result.projectTypes.size() == 1
            result.projectTypes[0].id == "100"
            result.projectTypes[0].label == "Concept"
    }
}
