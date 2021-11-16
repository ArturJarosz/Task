package com.arturjarosz.task.finance.domain.impl

import com.arturjarosz.task.configuration.UserProperties
import com.arturjarosz.task.finance.domain.dto.FinancialDataDto
import com.arturjarosz.task.finance.query.impl.FinancialDataQueryServiceImpl
import spock.lang.Specification

class CostFinancialDataServiceImplTest extends Specification {
    private static final long PROJECT_ID = 1L;
    private static final double INCOME_TAX = 0.1;
    private static final double VAT_TAX = 0.2;
    private static final BigDecimal COST_VALUE_1 = new BigDecimal("10.0");
    private static final BigDecimal COST_VALUE_2 = new BigDecimal("20.0");
    private static final BigDecimal COST_VALUE_3 = new BigDecimal("30.0");

    def financialDataQueryService = Mock(FinancialDataQueryServiceImpl);
    def userProperties = Mock(UserProperties);
    def costFinancialDataService = new CostFinancialDataServiceImpl(financialDataQueryService, userProperties);

    def "providePartialFinancialData should return summed up all cost related financial data for given project"() {
        given:
            this.mockGetCostsFinancialData();
            this.mockUserService();
        when:
            def partialFinancialData = this.costFinancialDataService.providePartialFinancialData(PROJECT_ID);
        then:
            partialFinancialData.costsValue.getGrossValue() == new BigDecimal("60");
            partialFinancialData.costsValue.getNetValue() == new BigDecimal("55");
            partialFinancialData.costsValue.getVatTax() == new BigDecimal("5");
            partialFinancialData.costsValue.getIncomeTax() == new BigDecimal("2.5");
    }

    private void mockGetCostsFinancialData() {
        FinancialDataDto costFinancialData1 = new FinancialDataDto();
        costFinancialData1.setValue(COST_VALUE_1);
        costFinancialData1.setPayable(true);
        costFinancialData1.setHasInvoice(true);
        FinancialDataDto costFinancialData2 = new FinancialDataDto();
        costFinancialData2.setValue(COST_VALUE_2);
        costFinancialData2.setPayable(true);
        costFinancialData2.setHasInvoice(true);
        FinancialDataDto costFinancialData3 = new FinancialDataDto();
        costFinancialData3.setValue(COST_VALUE_3);
        costFinancialData3.setPayable(true);
        costFinancialData3.setHasInvoice(false);
        def financialDataDtos = Arrays.asList(costFinancialData1, costFinancialData2, costFinancialData3);
        this.financialDataQueryService.getCostsFinancialData(PROJECT_ID) >> financialDataDtos;
    }

    private void mockUserService() {
        this.userProperties.getIncomeTax() >> INCOME_TAX;
        this.userProperties.getVatTax() >> VAT_TAX;
    }
}
