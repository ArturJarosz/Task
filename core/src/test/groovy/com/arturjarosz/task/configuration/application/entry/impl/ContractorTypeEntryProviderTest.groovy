package com.arturjarosz.task.configuration.application.entry.impl

import com.arturjarosz.task.dto.ApplicationConfigurationDto
import spock.lang.Specification

class ContractorTypeEntryProviderTest extends Specification {

    def subject = new ContractorTypeEntryProvider()

    def "addConfigurationEntry should add contractorTypes to application configuration"() {
        given:
            def configuration = new ApplicationConfigurationDto()
        when:
            def result = subject.addConfigurationEntry(configuration)
        then:
            result.contractorTypes != null
            result.contractorTypes[0].id != null
            result.contractorTypes[0].label != null
    }
}
