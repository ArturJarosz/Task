package com.arturjarosz.task.configuration.application.entry.impl

import com.arturjarosz.task.dto.ApplicationConfigurationDto
import spock.lang.Specification

class ContractStatusEntryProviderTest extends Specification {

    def subject = new ContractStatusEntryProvider()

    def "addConfigurationEntry should add contractorTypes to application configuration"() {
        given:
            def configuration = new ApplicationConfigurationDto()
        when:
            def result = subject.addConfigurationEntry(configuration)
        then:
            result.contractStatuses != null
            result.contractStatuses[0].id != null
            result.contractStatuses[0].label != null
    }
}
