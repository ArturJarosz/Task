package com.arturjarosz.task.configuration.application.entry.impl

import com.arturjarosz.task.dto.ApplicationConfigurationDto
import spock.lang.Specification

class SuppliersTypeConfigurationProviderTest extends Specification {
    def subject = new SuppliersTypeConfigurationProvider()

    def "addConfigurationEntry should add suppliers types to application configuration"() {
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
