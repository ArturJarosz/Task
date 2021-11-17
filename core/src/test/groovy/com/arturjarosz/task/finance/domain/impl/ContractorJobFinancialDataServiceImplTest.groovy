package com.arturjarosz.task.finance.domain.impl

import com.arturjarosz.task.configuration.UserProperties
import com.arturjarosz.task.finance.domain.dto.FinancialDataDto
import com.arturjarosz.task.finance.query.impl.FinancialDataQueryServiceImpl
import spock.lang.Specification

import java.math.RoundingMode

class ContractorJobFinancialDataServiceImplTest extends Specification {
    private static final long PROJECT_ID = 1L;
    private static final double INCOME_TAX = 0.1;
    private static final double VAT_TAX = 0.2;
    private static final BigDecimal CONTRACTOR_JOB_VALUE_1 = new BigDecimal("20.0");
    private static final BigDecimal CONTRACTOR_JOB_VALUE_2 = new BigDecimal("30.0");
    private static final BigDecimal CONTRACTOR_JOB_VALUE_3 = new BigDecimal("40.0");

    def financialDataQueryService = Mock(FinancialDataQueryServiceImpl);
    def userProperties = Mock(UserProperties);
    def contractorJobFinancialDataService = new ContractorJobFinancialDataServiceImpl(financialDataQueryService,
            userProperties);

    def "providePartialFinancialData should return summed up all contractorJobs related financial data for given project"() {
        given:
            this.mockGetContractorsJobsFinancialData();
            this.mockUserService();
        when:
            def partialFinancialData = this.contractorJobFinancialDataService.providePartialFinancialData(PROJECT_ID);
        then:
            partialFinancialData.contractorJobsValue.getGrossValue() == new BigDecimal("90");
            partialFinancialData.contractorJobsValue.getNetValue() == new BigDecimal("81.67")
                    .setScale(2, RoundingMode.HALF_UP);
            partialFinancialData.contractorJobsValue.getVatTax() == new BigDecimal("8.33")
                    .setScale(2, RoundingMode.HALF_UP);
            partialFinancialData.contractorJobsValue.getIncomeTax() == new BigDecimal("4.17")
                    .setScale(2, RoundingMode.HALF_UP);
    }


    private void mockGetContractorsJobsFinancialData() {
        FinancialDataDto contractorJobFinancialData1 = new FinancialDataDto();
        contractorJobFinancialData1.setValue(CONTRACTOR_JOB_VALUE_1);
        contractorJobFinancialData1.setPayable(true);
        contractorJobFinancialData1.setHasInvoice(true);
        FinancialDataDto contractorJobFinancialData2 = new FinancialDataDto();
        contractorJobFinancialData2.setValue(CONTRACTOR_JOB_VALUE_2);
        contractorJobFinancialData2.setPayable(true);
        contractorJobFinancialData2.setHasInvoice(true);
        FinancialDataDto contractorJobFinancialData3 = new FinancialDataDto();
        contractorJobFinancialData3.setValue(CONTRACTOR_JOB_VALUE_3);
        contractorJobFinancialData3.setPayable(true);
        contractorJobFinancialData3.setHasInvoice(false);
        def financialDataDtos =
                Arrays.asList(contractorJobFinancialData1, contractorJobFinancialData2, contractorJobFinancialData3);
        this.financialDataQueryService.getContractorsJobsFinancialData(PROJECT_ID) >> financialDataDtos;
    }

    private void mockUserService() {
        this.userProperties.getIncomeTax() >> INCOME_TAX;
        this.userProperties.getVatTax() >> VAT_TAX;
    }
}
