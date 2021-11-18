package com.arturjarosz.task.finance.domain.impl

import com.arturjarosz.task.configuration.UserProperties
import com.arturjarosz.task.finance.domain.dto.FinancialDataDto
import com.arturjarosz.task.finance.query.impl.FinancialDataQueryServiceImpl
import spock.lang.Specification

import java.math.RoundingMode

class InstallmentFinancialDataServiceImplTest extends Specification {
    private static final long PROJECT_ID = 1L;
    private static final double INCOME_TAX = 0.1;
    private static final double VAT_TAX = 0.2;
    private static final BigDecimal INSTALLMENT_VALUE_1 = new BigDecimal("20.0");
    private static final BigDecimal INSTALLMENT_VALUE_2 = new BigDecimal("30.0");

    def financialDataQueryService = Mock(FinancialDataQueryServiceImpl);
    def userProperties = Mock(UserProperties);

    def installmentFinancialDataService = new InstallmentFinancialDataServiceImpl(financialDataQueryService, userProperties);

    def "providePartialFinancialData should return summed up all installments financial data for given project"() {
        given:
            this.mockGetInstallmentsFinancialData();
            this.mockUserService();
        when:
            def partialFinancialData = this.installmentFinancialDataService.providePartialFinancialData(PROJECT_ID);
        then:
            partialFinancialData.baseProjectValue.getGrossValue() == new BigDecimal("60");
            partialFinancialData.baseProjectValue.getNetValue() == new BigDecimal("50").setScale(2, RoundingMode.HALF_UP);
            partialFinancialData.baseProjectValue.getVatTax() == new BigDecimal("10").setScale(2, RoundingMode.HALF_UP);
            partialFinancialData.baseProjectValue.getIncomeTax() == new BigDecimal("5").setScale(2, RoundingMode.HALF_UP);
    }

    private void mockGetInstallmentsFinancialData() {
        FinancialDataDto installmentFinancialData1 = new FinancialDataDto();
        installmentFinancialData1.setValue(INSTALLMENT_VALUE_1);
        installmentFinancialData1.setPayable(true);
        installmentFinancialData1.setHasInvoice(true);
        FinancialDataDto installmentFinancialData2 = new FinancialDataDto();
        installmentFinancialData2.setValue(INSTALLMENT_VALUE_2);
        installmentFinancialData2.setPayable(true);
        installmentFinancialData2.setHasInvoice(true);
        def financialDataDtos = Arrays.asList(installmentFinancialData1, installmentFinancialData2);
        this.financialDataQueryService.getInstallmentsFinancialData(PROJECT_ID) >> financialDataDtos;
    }

    private void mockUserService() {
        this.userProperties.getIncomeTax() >> INCOME_TAX;
        this.userProperties.getVatTax() >> VAT_TAX;
    }

}
