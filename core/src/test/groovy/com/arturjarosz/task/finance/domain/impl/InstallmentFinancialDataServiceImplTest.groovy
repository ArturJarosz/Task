package com.arturjarosz.task.finance.domain.impl

import com.arturjarosz.task.configuration.UserProperties
import com.arturjarosz.task.finance.domain.dto.FinancialDataDto
import com.arturjarosz.task.finance.query.impl.FinancialDataQueryServiceImpl
import spock.lang.Specification

import java.math.RoundingMode

class InstallmentFinancialDataServiceImplTest extends Specification {
    private static final long PROJECT_ID = 1L
    private static final double INCOME_TAX = 0.1
    private static final double VAT_TAX = 0.2
    private static final BigDecimal INSTALLMENT_VALUE_1 = new BigDecimal("20.0")
    private static final BigDecimal INSTALLMENT_VALUE_2 = new BigDecimal("30.0")

    def financialDataQueryService = Mock(FinancialDataQueryServiceImpl)
    def userProperties = Mock(UserProperties)

    def installmentFinancialDataService = new InstallmentFinancialDataServiceImpl(financialDataQueryService, userProperties)

    def "providePartialFinancialData should return summed up all installments financial data for given project"() {
        given:
            this.mockGetInstallmentsFinancialData()
            this.mockUserService()
        when:
            def partialFinancialData = this.installmentFinancialDataService.providePartialFinancialData(PROJECT_ID)
        then:
            partialFinancialData.baseProjectValue.grossValue == new BigDecimal("60")
            partialFinancialData.baseProjectValue.netValue == new BigDecimal("50").setScale(2, RoundingMode.HALF_UP)
            partialFinancialData.baseProjectValue.vatTax == new BigDecimal("10").setScale(2, RoundingMode.HALF_UP)
            partialFinancialData.baseProjectValue.incomeTax == new BigDecimal("5").setScale(2, RoundingMode.HALF_UP)
    }

    private void mockGetInstallmentsFinancialData() {
        FinancialDataDto installmentFinancialData1 = new FinancialDataDto()
        installmentFinancialData1.value = INSTALLMENT_VALUE_1
        installmentFinancialData1.payable = true
        installmentFinancialData1.hasInvoice = true
        FinancialDataDto installmentFinancialData2 = new FinancialDataDto()
        installmentFinancialData2.value = INSTALLMENT_VALUE_2
        installmentFinancialData2.payable = true
        installmentFinancialData2.hasInvoice = true
        def financialDataDtos = Arrays.asList(installmentFinancialData1, installmentFinancialData2)
        this.financialDataQueryService.getInstallmentsFinancialData(PROJECT_ID) >> financialDataDtos
    }

    private void mockUserService() {
        this.userProperties.incomeTax >> INCOME_TAX
        this.userProperties.vatTax >> VAT_TAX
    }

}
