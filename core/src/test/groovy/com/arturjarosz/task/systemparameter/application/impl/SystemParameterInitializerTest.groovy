package com.arturjarosz.task.systemparameter.application.impl

import com.arturjarosz.task.configuration.UserProperties
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException
import com.arturjarosz.task.systemparameter.domain.SystemParameterValidatorService
import com.arturjarosz.task.systemparameter.domain.SystemParameters
import com.arturjarosz.task.systemparameter.infrastructure.repository.SystemParameterRepository
import com.arturjarosz.task.systemparameter.model.SystemParameter
import com.arturjarosz.task.systemparameter.model.SystemParameterType
import spock.lang.Specification

class SystemParameterInitializerTest extends Specification {

    def systemParameterRepository = Mock(SystemParameterRepository)
    def systemParameterValidatorService = Mock(SystemParameterValidatorService)
    def userProperties = new UserProperties(vatTax: 0.23D, incomeTax: 0.18D, themeColor: "#465fff")

    def systemParameterInitializer = new SystemParameterInitializer(systemParameterRepository,
            systemParameterValidatorService, userProperties)

    def "run should save default parameters when they are not in the database"() {
        given:
            this.systemParameterRepository.existsByName(_ as String) >> false
            List<SystemParameter> saved = []
        when:
            this.systemParameterInitializer.run(null)
        then:
            3 * this.systemParameterRepository.save(_ as SystemParameter) >> { SystemParameter parameter ->
                saved << parameter
                parameter
            }
        and:
            saved.collectEntries { [(it.name): it.value] } == [(SystemParameters.VAT_TAX)    : "0.23",
                                                               (SystemParameters.INCOME_TAX) : "0.18",
                                                               (SystemParameters.THEME_COLOR): "#465fff"]
            saved.collectEntries { [(it.name): it.type] } ==
                    [(SystemParameters.VAT_TAX)    : SystemParameterType.DECIMAL_NUMBER,
                     (SystemParameters.INCOME_TAX) : SystemParameterType.DECIMAL_NUMBER,
                     (SystemParameters.THEME_COLOR): SystemParameterType.TEXT]
            saved.every { it.defaultValue == it.value && it.singleValue }
    }

    def "run should not override parameters that are already in the database"() {
        given:
            this.systemParameterRepository.existsByName(SystemParameters.VAT_TAX) >> true
            this.systemParameterRepository.existsByName(SystemParameters.INCOME_TAX) >> false
            this.systemParameterRepository.existsByName(SystemParameters.THEME_COLOR) >> true
        when:
            this.systemParameterInitializer.run(null)
        then:
            1 * this.systemParameterRepository.save({ SystemParameter parameter ->
                parameter.name == SystemParameters.INCOME_TAX
            })
            0 * this.systemParameterRepository.save({ SystemParameter parameter ->
                parameter.name == SystemParameters.VAT_TAX
            })
    }

    def "run should validate default value before saving it"() {
        given:
            this.systemParameterRepository.existsByName(_ as String) >> false
            this.systemParameterValidatorService.validateOnUpdate(_) >> { throw new IllegalArgumentException("") }
        when:
            this.systemParameterInitializer.run(null)
        then:
            thrown(IllegalArgumentException)
            0 * this.systemParameterRepository.save(_)
    }
}
