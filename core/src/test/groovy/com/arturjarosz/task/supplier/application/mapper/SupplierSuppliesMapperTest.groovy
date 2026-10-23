package com.arturjarosz.task.supplier.application.mapper

import com.arturjarosz.task.dto.SupplyDto
import com.arturjarosz.task.finance.domain.dto.FinancialDataDto
import com.arturjarosz.task.finance.model.SupplierSupplyDataDto
import com.arturjarosz.task.sharedkernel.testhelpers.TestUtils
import com.arturjarosz.task.systemparameter.application.TaxRatesProvider
import com.arturjarosz.task.systemparameter.application.dto.TaxRates
import spock.lang.Specification

class SupplierSuppliesMapperTest extends Specification {

    def subject = new SupplierSuppliesMapperImpl()

    def setup() {
        TestUtils.setFieldForObject(subject, "taxRatesProvider", { -> new TaxRates(new BigDecimal("0.23"), new BigDecimal("0.1")) } as TaxRatesProvider)
    }

    def "should return correctly mapped dto"() {
        given:
            var supplies = prepareSupplies(numberOfSupplies)

        when:
            def result = subject.mapToSupplierSuppliesDataDto(supplies)

        then:
            result.supplies.size() == numberOfSupplies
            result.financialData.count == numberOfSupplies
            result.averageFinancialData.netValue == averageNet
            result.averageFinancialData.grossValue == averageGross
            result.averageFinancialData.incomeTax == averageIncome
            result.averageFinancialData.vatTax == averageVat

        where:
            numberOfSupplies || averageNet | averageGross | averageIncome | averageVat
            0                || 0.0D       | 0.0D         | 0.0D          | 0.0D
            1                || 200.0D     | 246.0D       | 20.0D         | 46.0D
            2                || 250.0D     | 307.5D       | 25.0D         | 57.5D
    }

    Set<SupplierSupplyDataDto> prepareSupplies(int number) {
        var supplies = new HashSet<SupplierSupplyDataDto>()
        for (int i = 0; i < number; i++) {
            var supply = new SupplyDto()
            var financialData = new FinancialDataDto(value: (200.0D + 100.0D * i), hasInvoice: true)
            var supplyData = new SupplierSupplyDataDto(supply, financialData)
            supplies.add(supplyData)
        }
        return supplies
    }
}
