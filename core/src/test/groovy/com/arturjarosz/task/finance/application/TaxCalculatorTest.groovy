package com.arturjarosz.task.finance.application

import com.arturjarosz.task.configuration.UserProperties
import com.arturjarosz.task.finance.domain.dto.FinancialDataDto
import spock.lang.Specification

class TaxCalculatorTest extends Specification {

    def "recalculateObjectTaxes should return properly recalculated data"() {
        given:
            def financialDataDto = new FinancialDataDto(value: netValue, hasInvoice: hasInvoice)
            def userProperties = new UserProperties(incomeTax: 0.1D, vatTax: 0.23D)

        when:
            def result = TaxCalculator.recalculateObjectTaxes(financialDataDto, userProperties)

        then:
            result.grossValue == expectedGross
            result.netValue == expectedNet
            result.incomeTax == expectedIncomeTax
            result.vatTax == expectedVatTax

        where:
            hasInvoice | netValue || expectedNet | expectedGross | expectedIncomeTax | expectedVatTax
            true       | 100.0D   || 100.0D      | 123.0D        | 10.0D             | 23.0D
            false      | 100.0D   || 100.0D      | 100.0D        | 0.0D              | 0.0D
    }
}
