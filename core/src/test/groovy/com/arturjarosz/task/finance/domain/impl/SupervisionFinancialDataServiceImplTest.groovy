package com.arturjarosz.task.finance.domain.impl

import com.arturjarosz.task.finance.domain.SummationStrategy
import com.arturjarosz.task.finance.domain.dto.FinancialDataDto
import com.arturjarosz.task.finance.model.PartialFinancialDataType
import com.arturjarosz.task.finance.query.impl.FinancialDataQueryServiceImpl
import com.arturjarosz.task.systemparameter.application.TaxRatesProvider
import com.arturjarosz.task.systemparameter.application.dto.TaxRates
import spock.lang.Specification

class SupervisionFinancialDataServiceImplTest extends Specification {
    private static final long PROJECT_ID = 1L
    private static final double INCOME_TAX = 0.1
    private static final double VAT_TAX = 0.2
    private static final BigDecimal SUPERVISION_VALUE_1 = new BigDecimal("10.0")

    def financialDataQueryService = Mock(FinancialDataQueryServiceImpl)
    def taxRatesProvider = Mock(TaxRatesProvider)
    def supervisionFinancialDataService = new SupervisionFinancialDataServiceImpl(financialDataQueryService,
            taxRatesProvider)

    def "providePartialFinancialData should return supervision financial data for given project"() {
        given:
            this.mockGetSupervisionFinancialData()
            this.mockUserService()
        when:
            def partialFinancialData = this.supervisionFinancialDataService.getPartialFinancialData(PROJECT_ID)
        then:
            partialFinancialData.grossValue == new BigDecimal("12")
            partialFinancialData.netValue == new BigDecimal("10")
            partialFinancialData.vatTax == new BigDecimal("2")
            partialFinancialData.incomeTax == new BigDecimal("1")
    }

    def "getSummationStrategy should return correct strategy"() {
        given:
        when:
            def strategy = this.supervisionFinancialDataService.getSummationStrategy()
        then:
            strategy == SummationStrategy.ADD
    }

    def "getType should return correct type"() {
        given:
        when:
            def type = this.supervisionFinancialDataService.getType()
        then:
            type == PartialFinancialDataType.SUPERVISION
    }

    private void mockGetSupervisionFinancialData() {
        FinancialDataDto supervisionFinancialData1 = new FinancialDataDto(value: SUPERVISION_VALUE_1, payable: true,
                hasInvoice: true)

        def financialDataDto = supervisionFinancialData1
        this.financialDataQueryService.getSupervisionFinancialData(PROJECT_ID) >> financialDataDto
    }

    private void mockUserService() {
        this.taxRatesProvider.getTaxRates() >> new TaxRates(BigDecimal.valueOf(VAT_TAX),
                BigDecimal.valueOf(INCOME_TAX))
    }
}
