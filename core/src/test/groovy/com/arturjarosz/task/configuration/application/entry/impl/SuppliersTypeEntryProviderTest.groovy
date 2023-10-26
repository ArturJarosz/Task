package com.arturjarosz.task.configuration.application.entry.impl

import com.arturjarosz.task.dto.ApplicationConfigurationDto
import spock.lang.Specification

class SuppliersTypeEntryProviderTest extends Specification {
    def subject = new SuppliersTypeEntryProvider()

    def "addConfigurationEntry should add contractorTypes to application configuration"() {
        given:
            def configuration = new ApplicationConfigurationDto()
        when:
            def result = subject.addConfigurationEntry(configuration)
        then:
            result.supplierTypes != null
            result.supplierTypes[0].id != null
            result.supplierTypes[0].label != null
    }
}
