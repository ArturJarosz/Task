package com.arturjarosz.task.configuration.application.impl

import com.arturjarosz.task.configuration.application.entry.ConfigurationProvider
import com.arturjarosz.task.dto.ApplicationConfigurationDto
import com.arturjarosz.task.dto.ConfigurationEntryDto
import spock.lang.Specification

class ConfigurationServiceImplTest extends Specification {

    def subject = new ConfigurationServiceImpl([new TestProvider1(), new TestProvider2()])

    def "getApplicationConfiguration should return configuration with all fields set"() {
        given:
        when:
            def result = subject.getApplicationConfiguration()
        then:
            result.contractorTypes.size() == 2
            result.supplierTypes.size() == 2
    }
}

class TestProvider1 implements ConfigurationProvider {
    static final CONTRACTOR_TYPE_1 = "Contractor Type 1"
    static final CONTRACTOR_TYPE_2 = "Contractor Type 2"

    @Override
    ApplicationConfigurationDto addConfigurationEntry(ApplicationConfigurationDto configurationDto) {
        def contractorType1 = new ConfigurationEntryDto().id(CONTRACTOR_TYPE_1)
        def contractorType2 = new ConfigurationEntryDto().id(CONTRACTOR_TYPE_2)
        return configurationDto.setContractorTypes([contractorType1, contractorType2])
    }
}

class TestProvider2 implements ConfigurationProvider {
    static final SUPPLIER_TYPE_1 = "Supplier Type 1"
    static final SUPPLIER_TYPE_2 = "Supplier Type 2"

    @Override
    ApplicationConfigurationDto addConfigurationEntry(ApplicationConfigurationDto configurationDto) {
        def supplierType1 = new ConfigurationEntryDto().id(SUPPLIER_TYPE_1)
        def supplierType2 = new ConfigurationEntryDto().id(SUPPLIER_TYPE_2)
        return configurationDto.setSupplierTypes([supplierType1, supplierType2])
    }
}
