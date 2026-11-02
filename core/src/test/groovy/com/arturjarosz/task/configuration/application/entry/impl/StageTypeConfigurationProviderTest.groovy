package com.arturjarosz.task.configuration.application.entry.impl

import com.arturjarosz.task.dto.ApplicationConfigurationDto
import com.arturjarosz.task.project.infrastructure.repositor.StageTypeRepository
import com.arturjarosz.task.project.model.StageType
import com.arturjarosz.task.sharedkernel.testhelpers.TestUtils
import org.springframework.data.domain.Sort
import spock.lang.Specification

class StageTypeConfigurationProviderTest extends Specification {

    def stageTypeRepository = Mock(StageTypeRepository)

    def subject = new StageTypeConfigurationProvider(stageTypeRepository)

    def "addConfigurationEntry should add stored stage types with their id and name"() {
        given:
            def type = new StageType("Visualisations")
            TestUtils.setFieldForObject(type, "id", 100L)
            this.stageTypeRepository.findAll(Sort.by("name")) >> [type]
        when:
            def result = subject.addConfigurationEntry(new ApplicationConfigurationDto())
        then:
            result.stageTypes.size() == 1
            result.stageTypes[0].id == "100"
            result.stageTypes[0].label == "Visualisations"
    }
}
