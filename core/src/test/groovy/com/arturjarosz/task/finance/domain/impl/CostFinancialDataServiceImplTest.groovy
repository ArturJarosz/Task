package com.arturjarosz.task.finance.domain.impl

import com.arturjarosz.task.configuration.UserProperties
import com.arturjarosz.task.finance.domain.SummationStrategy
import com.arturjarosz.task.finance.domain.dto.FinancialDataDto
import com.arturjarosz.task.finance.model.PartialFinancialDataType
import com.arturjarosz.task.finance.query.impl.FinancialDataQueryServiceImpl
import spock.lang.Specification

class CostFinancialDataServiceImplTest extends Specification {
    private static final long PROJECT_ID = 1L
    private static final double INCOME_TAX = 0.1
    private static final double VAT_TAX = 0.2
    private static final BigDecimal COST_VALUE_1 = new BigDecimal("10.0")
    private static final BigDecimal COST_VALUE_2 = new BigDecimal("20.0")
    private static final BigDecimal COST_VALUE_3 = new BigDecimal("30.0")

    def financialDataQueryService = Mock(FinancialDataQueryServiceImpl)
    def userProperties = Mock(UserProperties)
    def costFinancialDataService = new CostFinancialDataServiceImpl(financialDataQueryService, userProperties)

    def "providePartialFinancialData should return summed up all cost related financial data for given project"() {
        given:
            this.mockGetCostsFinancialData()
            this.mockUserService()
        when:
            def partialFinancialData = this.costFinancialDataService.getPartialFinancialData(PROJECT_ID)
        then:
            partialFinancialData.grossValue == new BigDecimal("66")
            partialFinancialData.netValue == new BigDecimal("60")
            partialFinancialData.vatTax == new BigDecimal("6")
            partialFinancialData.incomeTax == new BigDecimal("3")
    }

    def "getSummationStrategy should return correct strategy"() {
        given:
        when:
            def strategy = this.costFinancialDataService.getSummationStrategy();
        then:
            strategy == SummationStrategy.SUBTRACT
    }

    def "getType should return correct type"() {
        given:
        when:
            def type = this.costFinancialDataService.getType();
        then:
            type == PartialFinancialDataType.COST
    }

    private void mockGetCostsFinancialData() {
        FinancialDataDto costFinancialData1 = new FinancialDataDto(value: COST_VALUE_1, payable: true, hasInvoice: true)
        FinancialDataDto costFinancialData2 = new FinancialDataDto(value: COST_VALUE_2, payable: true, hasInvoice: true)
        FinancialDataDto costFinancialData3 = new FinancialDataDto(value: COST_VALUE_3, payable: true,
                hasInvoice: false)
        def financialDataDtos = Arrays.asList(costFinancialData1, costFinancialData2, costFinancialData3)
        this.financialDataQueryService.getCostsFinancialData(PROJECT_ID) >> financialDataDtos
    }

    private void mockUserService() {
        this.userProperties.incomeTax >> INCOME_TAX
        this.userProperties.vatTax >> VAT_TAX
    }
}
