package com.arturjarosz.task.finance.application.mapper

import com.arturjarosz.task.common.mapper.MoneyMapperImpl
import com.arturjarosz.task.dto.InstallmentDto
import com.arturjarosz.task.finance.application.dto.FinancialValueDto
import com.arturjarosz.task.finance.model.PartialFinancialDataType
import com.arturjarosz.task.finance.model.ProjectFinancialPartialData
import spock.lang.Specification

class InstallmentProjectDataMapperTest extends Specification {
    def moneyMapper = new MoneyMapperImpl()
    def subject = new InstallmentProjectDataMapperImpl(moneyMapper)

    def "mapToProjectFinancialPartialDataDto should return correct dto"() {
        given:
            def financialValueDto = new FinancialValueDto()
            financialValueDto.netValue = BigDecimal.valueOf(100.0D)
            financialValueDto.grossValue = BigDecimal.valueOf(123.45D)
            financialValueDto.vatTax = BigDecimal.valueOf(23.45D)
            financialValueDto.incomeTax = BigDecimal.valueOf(20.0D)
            def partialData = new ProjectFinancialPartialData(PartialFinancialDataType.INSTALLMENT, financialValueDto)
            def installment1 = new InstallmentDto()
            def installment2 = new InstallmentDto()
            def stagesWithoutInstallmentIds = [100L, 101L]

        when:
            def result = subject.mapToProjectFinancialPartialDataDto(partialData, [installment1, installment2],
                    stagesWithoutInstallmentIds)

        then:
            result.stagesWithoutInstallment.size() == 2
            result.installments.size() == 2
            with(result.financialData) {
                count == 2
                netValue == 100.0D
                grossValue == 123.45D
                vatTax == 23.45D
                incomeTax == 20.0D
            }
    }
}
