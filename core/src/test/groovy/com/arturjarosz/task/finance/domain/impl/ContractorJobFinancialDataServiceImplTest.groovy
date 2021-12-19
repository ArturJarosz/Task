package com.arturjarosz.task.finance.domain.impl

import com.arturjarosz.task.configuration.UserProperties
import com.arturjarosz.task.finance.domain.dto.FinancialDataDto
import com.arturjarosz.task.finance.query.impl.FinancialDataQueryServiceImpl
import spock.lang.Specification

import java.math.RoundingMode

class ContractorJobFinancialDataServiceImplTest extends Specification {
    private static final long PROJECT_ID = 1L
    private static final double INCOME_TAX = 0.1
    private static final double VAT_TAX = 0.2
    private static final BigDecimal CONTRACTOR_JOB_VALUE_1 = new BigDecimal("20.0")
    private static final BigDecimal CONTRACTOR_JOB_VALUE_2 = new BigDecimal("30.0")
    private static final BigDecimal CONTRACTOR_JOB_VALUE_3 = new BigDecimal("40.0")

    def financialDataQueryService = Mock(FinancialDataQueryServiceImpl)
    def userProperties = Mock(UserProperties)
    def contractorJobFinancialDataService = new ContractorJobFinancialDataServiceImpl(financialDataQueryService,
            userProperties)

    def "providePartialFinancialData should return summed up all contractorJobs related financial data for given project"() {
        given:
            this.mockGetContractorsJobsFinancialData()
            this.mockUserService()
        when:
            def partialFinancialData = this.contractorJobFinancialDataService.providePartialFinancialData(PROJECT_ID)
        then:
            partialFinancialData.contractorJobsValue.grossValue == new BigDecimal("100")
            partialFinancialData.contractorJobsValue.netValue == new BigDecimal("90")
                    .setScale(2, RoundingMode.HALF_UP)
            partialFinancialData.contractorJobsValue.vatTax == new BigDecimal("10")
                    .setScale(2, RoundingMode.HALF_UP)
            partialFinancialData.contractorJobsValue.incomeTax == new BigDecimal("5")
                    .setScale(2, RoundingMode.HALF_UP)
    }

    private void mockGetContractorsJobsFinancialData() {
        FinancialDataDto contractorJobFinancialData1 = new FinancialDataDto(value: CONTRACTOR_JOB_VALUE_1,
                payable: true, hasInvoice: true)
        FinancialDataDto contractorJobFinancialData2 = new FinancialDataDto(value: CONTRACTOR_JOB_VALUE_2,
                payable: true, hasInvoice: true)
        FinancialDataDto contractorJobFinancialData3 = new FinancialDataDto(value: CONTRACTOR_JOB_VALUE_3,
                payable: true, hasInvoice: false)
        def financialDataDtos =
                Arrays.asList(contractorJobFinancialData1, contractorJobFinancialData2, contractorJobFinancialData3)
        this.financialDataQueryService.getContractorsJobsFinancialData(PROJECT_ID) >> financialDataDtos
    }

    private void mockUserService() {
        this.userProperties.incomeTax >> INCOME_TAX
        this.userProperties.vatTax >> VAT_TAX
    }
}
