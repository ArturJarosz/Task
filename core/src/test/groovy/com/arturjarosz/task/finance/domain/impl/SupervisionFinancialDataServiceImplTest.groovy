package com.arturjarosz.task.finance.domain.impl

import com.arturjarosz.task.configuration.UserProperties
import com.arturjarosz.task.finance.domain.dto.FinancialDataDto
import com.arturjarosz.task.finance.query.impl.FinancialDataQueryServiceImpl
import spock.lang.Specification

class SupervisionFinancialDataServiceImplTest extends Specification {
    private static final long PROJECT_ID = 1L;
    private static final double INCOME_TAX = 0.1;
    private static final double VAT_TAX = 0.2;
    private static final BigDecimal SUPERVISION_VALUE_1 = new BigDecimal("10.0");

    def financialDataQueryService = Mock(FinancialDataQueryServiceImpl);
    def userProperties = Mock(UserProperties);
    def supervisionFinancialDataService = new SupervisionFinancialDataServiceImpl(financialDataQueryService,
            userProperties);

    def "providePartialFinancialData should return supervision financial data for given project"() {
        given:
            this.mockGetSupervisionFinancialData();
            this.mockUserService();
        when:
            def partialFinancialData = this.supervisionFinancialDataService.providePartialFinancialData(PROJECT_ID);
        then:
            partialFinancialData.getSupervisionValue().getGrossValue() == new BigDecimal("10.0");
            partialFinancialData.getSupervisionValue().getNetValue() == new BigDecimal("8.33");
            partialFinancialData.getSupervisionValue().getVatTax() == new BigDecimal("1.67");
            partialFinancialData.getSupervisionValue().getIncomeTax() == new BigDecimal("0.83");
    }

    private void mockGetSupervisionFinancialData() {
        FinancialDataDto supervisionFinancialData1 = new FinancialDataDto();
        supervisionFinancialData1.setValue(SUPERVISION_VALUE_1);
        supervisionFinancialData1.setPayable(true);
        supervisionFinancialData1.setHasInvoice(true);

        def financialDataDto = supervisionFinancialData1;
        this.financialDataQueryService.getSupervisionFinancialData(PROJECT_ID) >> financialDataDto;
    }

    private void mockUserService() {
        this.userProperties.getIncomeTax() >> INCOME_TAX;
        this.userProperties.getVatTax() >> VAT_TAX;
    }
}
