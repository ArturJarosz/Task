package com.arturjarosz.task.systemparameter.application.impl

import com.arturjarosz.task.dto.SystemParameterDto
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException
import com.arturjarosz.task.systemparameter.domain.SystemParameters
import com.arturjarosz.task.systemparameter.query.SystemParameterQueryService
import spock.lang.Specification

class TaxRatesProviderImplTest extends Specification {

    def systemParameterQueryService = Mock(SystemParameterQueryService)

    def taxRatesProvider = new TaxRatesProviderImpl(systemParameterQueryService)

    def "getTaxRates should return tax rates stored as system parameters"() {
        given:
            this.systemParameterQueryService.getSystemPropertyByName(SystemParameters.VAT_TAX) >>
                    new SystemParameterDto(name: SystemParameters.VAT_TAX, value: "0.08")
            this.systemParameterQueryService.getSystemPropertyByName(SystemParameters.INCOME_TAX) >>
                    new SystemParameterDto(name: SystemParameters.INCOME_TAX, value: "0.12")
        when:
            def result = this.taxRatesProvider.getTaxRates()
        then:
            result.vatTax() == new BigDecimal("0.08")
            result.incomeTax() == new BigDecimal("0.12")
    }

    def "getTaxRates should throw exception when tax system parameter does not exist"() {
        given:
            this.systemParameterQueryService.getSystemPropertyByName(SystemParameters.VAT_TAX) >> null
        when:
            this.taxRatesProvider.getTaxRates()
        then:
            thrown(IllegalArgumentException)
    }
}
