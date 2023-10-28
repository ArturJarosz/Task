package com.arturjarosz.task.configuration.application.entry.impl

import com.arturjarosz.task.dto.ApplicationConfigurationDto
import spock.lang.Specification

class ContractStatusConfigurationProviderTest extends Specification {

    def subject = new ContractStatusConfigurationProvider()

    def "addConfigurationEntry should add contractor statuses to application configuration"() {
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
