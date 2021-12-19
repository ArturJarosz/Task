package com.arturjarosz.task.finance.domain.impl

import com.arturjarosz.task.configuration.UserProperties
import com.arturjarosz.task.finance.domain.dto.FinancialDataDto
import com.arturjarosz.task.finance.query.impl.FinancialDataQueryServiceImpl
import spock.lang.Specification

import java.math.RoundingMode

class SupplyFinancialDataServiceImplTest extends Specification {
    private static final long PROJECT_ID = 1L
    private static final double INCOME_TAX = 0.1
    private static final double VAT_TAX = 0.2
    private static final BigDecimal SUPPLY_VALUE_1 = new BigDecimal("20.0")
    private static final BigDecimal SUPPLY_VALUE_2 = new BigDecimal("30.0")
    private static final BigDecimal SUPPLY_VALUE_3 = new BigDecimal("40.0")

    def financialDataQueryService = Mock(FinancialDataQueryServiceImpl)
    def userProperties = Mock(UserProperties)

    def suppliesFinancialDataService = new SupplyFinancialDataServiceImpl(financialDataQueryService, userProperties)

    def "providePartialFinancialData should return summed up all supplies related financial data for given project"() {
        given:
            this.mockGetSuppliesFinancialData()
            this.mockUserService()
        when:
            def partialFinancialData = this.suppliesFinancialDataService.providePartialFinancialData(PROJECT_ID)
        then:
            partialFinancialData.suppliesValue.grossValue == new BigDecimal("100")
            partialFinancialData.suppliesValue.netValue == new BigDecimal("90").setScale(2, RoundingMode.HALF_UP)
            partialFinancialData.suppliesValue.vatTax == new BigDecimal("10").setScale(2, RoundingMode.HALF_UP)
            partialFinancialData.suppliesValue.incomeTax == new BigDecimal("5").setScale(2, RoundingMode.HALF_UP)
    }

    private void mockGetSuppliesFinancialData() {
        FinancialDataDto supplyFinancialData1 = new FinancialDataDto()
        supplyFinancialData1.value = SUPPLY_VALUE_1
        supplyFinancialData1.payable = true
        supplyFinancialData1.hasInvoice = true
        FinancialDataDto supplyFinancialData2 = new FinancialDataDto()
        supplyFinancialData2.value = SUPPLY_VALUE_2
        supplyFinancialData2.payable = true
        supplyFinancialData2.hasInvoice = true
        FinancialDataDto supplyFinancialData3 = new FinancialDataDto()
        supplyFinancialData3.value = SUPPLY_VALUE_3
        supplyFinancialData3.payable = true
        supplyFinancialData3.hasInvoice = false
        def financialDataDtos = Arrays.asList(supplyFinancialData1, supplyFinancialData2, supplyFinancialData3)
        this.financialDataQueryService.getSuppliesFinancialData(PROJECT_ID) >> financialDataDtos
    }

    private void mockUserService() {
        this.userProperties.incomeTax >> INCOME_TAX
        this.userProperties.vatTax >> VAT_TAX
    }
}
