package com.arturjarosz.task.configuration.application.entry.impl

import com.arturjarosz.task.dto.ApplicationConfigurationDto
import com.arturjarosz.task.sharedkernel.testhelpers.TestUtils
import com.arturjarosz.task.supplier.infrastructure.SupplierCategoryRepository
import com.arturjarosz.task.supplier.model.SupplierCategory
import org.springframework.data.domain.Sort
import spock.lang.Specification

class SuppliersTypeConfigurationProviderTest extends Specification {

    def supplierCategoryRepository = Mock(SupplierCategoryRepository)

    def subject = new SuppliersTypeConfigurationProvider(supplierCategoryRepository)

    def "addConfigurationEntry should add stored supplier categories with their id and name"() {
        given:
            def paintShop = new SupplierCategory("Paint shop")
            TestUtils.setFieldForObject(paintShop, "id", 100L)
            this.supplierCategoryRepository.findAll(Sort.by("name")) >> [paintShop]
        when:
            def result = subject.addConfigurationEntry(new ApplicationConfigurationDto())
        then:
            result.supplierTypes.size() == 1
            result.supplierTypes[0].id == "100"
            result.supplierTypes[0].label == "Paint shop"
    }
}
