package com.arturjarosz.task.configuration.application.entry.impl

import com.arturjarosz.task.finance.infrastructure.CostCategoryRepository
import com.arturjarosz.task.finance.model.CostCategory
import com.arturjarosz.task.dto.ApplicationConfigurationDto
import com.arturjarosz.task.sharedkernel.testhelpers.TestUtils
import org.springframework.data.domain.Sort
import spock.lang.Specification

class CostCategoryConfigurationProviderTest extends Specification {

    def costCategoryRepository = Mock(CostCategoryRepository)

    def subject = new CostCategoryConfigurationProvider(costCategoryRepository)

    def "addConfigurationEntry should add stored cost categories with their id and name"() {
        given:
            def fuel = new CostCategory("Fuel")
            TestUtils.setFieldForObject(fuel, "id", 100L)
            this.costCategoryRepository.findAll(Sort.by("name")) >> [fuel]
        when:
            def result = subject.addConfigurationEntry(new ApplicationConfigurationDto())
        then:
            result.costCategories.size() == 1
            result.costCategories[0].id == "100"
            result.costCategories[0].label == "Fuel"
    }
}
