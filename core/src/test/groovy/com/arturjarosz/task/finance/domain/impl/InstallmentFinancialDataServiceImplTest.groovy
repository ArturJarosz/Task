package com.arturjarosz.task.finance.domain.impl

import com.arturjarosz.task.finance.domain.SummationStrategy
import com.arturjarosz.task.finance.domain.dto.FinancialDataDto
import com.arturjarosz.task.finance.model.PartialFinancialDataType
import com.arturjarosz.task.finance.query.impl.FinancialDataQueryServiceImpl
import com.arturjarosz.task.systemparameter.application.TaxRatesProvider
import com.arturjarosz.task.systemparameter.application.dto.TaxRates
import java.math.RoundingMode
import spock.lang.Specification

class InstallmentFinancialDataServiceImplTest extends Specification {
    private static final long PROJECT_ID = 1L
    private static final double INCOME_TAX = 0.1
    private static final double VAT_TAX = 0.2
    private static final BigDecimal INSTALLMENT_VALUE_1 = new BigDecimal("20.0")
    private static final BigDecimal INSTALLMENT_VALUE_2 = new BigDecimal("30.0")

    def financialDataQueryService = Mock(FinancialDataQueryServiceImpl)
    def taxRatesProvider = Mock(TaxRatesProvider)

    def installmentFinancialDataService = new InstallmentFinancialDataServiceImpl(financialDataQueryService, taxRatesProvider)

    def "providePartialFinancialData should return summed up all installments financial data for given project"() {
        given:
            this.mockGetInstallmentsFinancialData()
            this.mockUserService()
        when:
            def partialFinancialData = this.installmentFinancialDataService.getPartialFinancialData(PROJECT_ID)
        then:
            partialFinancialData.grossValue == new BigDecimal("60")
            partialFinancialData.netValue == new BigDecimal("50").setScale(2, RoundingMode.HALF_UP)
            partialFinancialData.vatTax == new BigDecimal("10").setScale(2, RoundingMode.HALF_UP)
            partialFinancialData.incomeTax == new BigDecimal("5").setScale(2, RoundingMode.HALF_UP)
    }

    def "getSummationStrategy should return correct strategy"() {
        given:
        when:
            def strategy = this.installmentFinancialDataService.getSummationStrategy()
        then:
            strategy == SummationStrategy.ADD
    }

    def "getType should return correct type"() {
        given:
        when:
            def type = this.installmentFinancialDataService.getType()
        then:
            type == PartialFinancialDataType.INSTALLMENT
    }

    private void mockGetInstallmentsFinancialData() {
        FinancialDataDto installmentFinancialData1 = new FinancialDataDto(value: INSTALLMENT_VALUE_1, payable: true, hasInvoice: true)
        FinancialDataDto installmentFinancialData2 = new FinancialDataDto(value: INSTALLMENT_VALUE_2, payable: true, hasInvoice: true)
        def financialDataDtos = Arrays.asList(installmentFinancialData1, installmentFinancialData2)
        this.financialDataQueryService.getInstallmentsFinancialData(PROJECT_ID) >> financialDataDtos
    }

    private void mockUserService() {
        this.taxRatesProvider.getTaxRates() >> new TaxRates(BigDecimal.valueOf(VAT_TAX),
                BigDecimal.valueOf(INCOME_TAX))
    }

}
