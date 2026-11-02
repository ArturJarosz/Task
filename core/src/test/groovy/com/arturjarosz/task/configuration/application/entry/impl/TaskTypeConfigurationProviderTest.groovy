package com.arturjarosz.task.configuration.application.entry.impl

import com.arturjarosz.task.dto.ApplicationConfigurationDto
import com.arturjarosz.task.project.infrastructure.repositor.TaskTypeRepository
import com.arturjarosz.task.project.model.TaskType
import com.arturjarosz.task.sharedkernel.testhelpers.TestUtils
import org.springframework.data.domain.Sort
import spock.lang.Specification

class TaskTypeConfigurationProviderTest extends Specification {

    def taskTypeRepository = Mock(TaskTypeRepository)

    def subject = new TaskTypeConfigurationProvider(taskTypeRepository)

    def "addConfigurationEntry should add stored task types with their id and name"() {
        given:
            def type = new TaskType("Render")
            TestUtils.setFieldForObject(type, "id", 100L)
            this.taskTypeRepository.findAll(Sort.by("name")) >> [type]
        when:
            def result = subject.addConfigurationEntry(new ApplicationConfigurationDto())
        then:
            result.taskTypes.size() == 1
            result.taskTypes[0].id == "100"
            result.taskTypes[0].label == "Render"
    }
}
