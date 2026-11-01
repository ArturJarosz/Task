package com.arturjarosz.task.configuration.application.entry.impl

import com.arturjarosz.task.contractor.infrastructure.ContractorCategoryRepository
import com.arturjarosz.task.contractor.model.ContractorCategory
import com.arturjarosz.task.dto.ApplicationConfigurationDto
import com.arturjarosz.task.sharedkernel.testhelpers.TestUtils
import org.springframework.data.domain.Sort
import spock.lang.Specification

class ContractorTypeConfigurationProviderTest extends Specification {

    def contractorCategoryRepository = Mock(ContractorCategoryRepository)

    def subject = new ContractorTypeConfigurationProvider(contractorCategoryRepository)

    def "addConfigurationEntry should add stored contractor categories with their id and name"() {
        given:
            def plumber = new ContractorCategory("Plumber")
            TestUtils.setFieldForObject(plumber, "id", 100L)
            this.contractorCategoryRepository.findAll(Sort.by("name")) >> [plumber]
        when:
            def result = subject.addConfigurationEntry(new ApplicationConfigurationDto())
        then:
            result.contractorTypes.size() == 1
            result.contractorTypes[0].id == "100"
            result.contractorTypes[0].label == "Plumber"
    }
}
