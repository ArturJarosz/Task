package com.arturjarosz.task.project.domain.impl

import com.arturjarosz.task.finance.model.FinancialData
import com.arturjarosz.task.project.application.dto.InstallmentDto
import com.arturjarosz.task.project.model.Installment
import com.arturjarosz.task.project.model.Stage
import com.arturjarosz.task.project.model.StageType
import com.arturjarosz.task.project.status.stage.StageWorkflow
import com.arturjarosz.task.project.utils.InstallmentBuilder
import com.arturjarosz.task.sharedkernel.model.Money
import com.arturjarosz.task.supervision.utils.FinancialDataBuilder
import spock.lang.Specification

import java.time.LocalDate

class InstallmentDomainServiceImplTest extends Specification {

    private static final String STAGE_NAME = "stage"
    private static final StageType STAGE_TYPE = StageType.AUTHORS_SUPERVISION
    private static final BigDecimal OLD_AMOUNT = new BigDecimal("1.00")
    private static final BigDecimal NEW_AMOUNT = new BigDecimal("2.00")
    private static final LocalDate OLD_DATE = LocalDate.now().minusDays(10)
    private static final LocalDate NEW_DATE = LocalDate.now()
    private static final LocalDate FUTURE_DATE = LocalDate.now().plusDays(10)
    private static final String NEW_NOTE = "some note"
    private static final String OLD_NOTE = "old note"

    def installmentDomainService = new InstallmentDomainServiceImpl()

    def "updateInstallment should update value and note when updating not paid installment"() {
        given:
            Stage stage = new Stage(STAGE_NAME, STAGE_TYPE, new StageWorkflow())
            FinancialData financialData = new FinancialDataBuilder()
                    .withValue(new Money(OLD_AMOUNT))
                    .withPaid(false)
                    .withHasInvoice(true)
                    .build()
            Installment installment = new InstallmentBuilder()
                    .withFinancialData(financialData)
                    .withNote(OLD_NOTE)
                    .build()
            stage.installment = installment
            InstallmentDto installmentDto = new InstallmentDto()
            installmentDto.value = NEW_AMOUNT
            installmentDto.paymentDate = OLD_DATE
            installmentDto.note = NEW_NOTE
            installmentDto.hasInvoice = true
        when:
            Installment updatedInstallment = this.installmentDomainService.updateInstallment(installment,
                    installmentDto)
        then:
            updatedInstallment.amount.hasSameValueAs(new Money(NEW_AMOUNT))
            updatedInstallment.note == NEW_NOTE
            updatedInstallment.paymentDate == null
    }

    def "updateInstallment should update value, note and date when updating paid installment"() {
        given:
            Stage stage = new Stage(STAGE_NAME, STAGE_TYPE, new StageWorkflow())
            FinancialData financialData = new FinancialDataBuilder().withValue(new Money(OLD_AMOUNT))
                    .withPaid(true)
                    .withPaymentDate(OLD_DATE)
                    .withHasInvoice(true)
                    .build()
            Installment installment = new InstallmentBuilder()
                    .withNote(OLD_NOTE)
                    .withFinancialData(financialData)
                    .build()
            stage.installment = installment
            InstallmentDto installmentDto = new InstallmentDto()
            installmentDto.value = NEW_AMOUNT
            installmentDto.paymentDate = NEW_DATE
            installmentDto.note = NEW_NOTE
            installmentDto.hasInvoice = true
        when:
            Installment updatedInstallment = this.installmentDomainService.updateInstallment(installment,
                    installmentDto)
        then:
            updatedInstallment.amount.hasSameValueAs(new Money(NEW_AMOUNT))
            updatedInstallment.note == NEW_NOTE
            updatedInstallment.paymentDate == NEW_DATE
    }

    def "payForInstallment should throw an exception if dto date is in future"() {
        given:
            Stage stage = new Stage(STAGE_NAME, STAGE_TYPE, new StageWorkflow())
            FinancialData financialData = new FinancialDataBuilder()
                    .withValue(new Money(OLD_AMOUNT))
                    .withPaid(false)
                    .withHasInvoice(true)
                    .build()
            Installment installment = new InstallmentBuilder()
                    .withFinancialData(financialData)
                    .build()
            stage.installment = installment
        when:
            this.installmentDomainService.payInstallment(stage, FUTURE_DATE)
        then:
            Exception exception = thrown()
            exception.message == "notValid.installment.payDate"
    }

    def "payForInstallment should throw an exception if installment is already paid"() {
        given:
            Stage stage = new Stage(STAGE_NAME, STAGE_TYPE, new StageWorkflow())
            FinancialData financialData = new FinancialDataBuilder()
                    .withValue(new Money(OLD_AMOUNT))
                    .withPaid(true)
                    .withHasInvoice(true)
                    .build()
            Installment installment = new InstallmentBuilder()
                    .withFinancialData(financialData)
                    .build()
            stage.installment = installment
        when:
            this.installmentDomainService.payInstallment(stage, NEW_DATE)
        then:
            Exception exception = thrown()
            exception.message == "notValid.installment.paid"
    }

    def "payForInstallment should set payment day for today is date is not given and set isPaid to true"() {
        given:
            Stage stage = new Stage(STAGE_NAME, STAGE_TYPE, new StageWorkflow())
            FinancialData financialData = new FinancialDataBuilder()
                    .withValue(new Money(OLD_AMOUNT))
                    .withPaid(false)
                    .withHasInvoice(true)
                    .build()
            Installment installment = new InstallmentBuilder()
                    .withFinancialData(financialData)
                    .build()
            stage.installment = installment
        when:
            Installment paidInstallment = this.installmentDomainService.payInstallment(stage, null)
        then:
            noExceptionThrown()
            paidInstallment.paymentDate == LocalDate.now()
            paidInstallment.paid
    }

    def "payForInstallment should set payment day for given date and set isPaid to true"() {
        given:
            Stage stage = new Stage(STAGE_NAME, STAGE_TYPE, new StageWorkflow())
            FinancialData financialData = new FinancialDataBuilder()
                    .withValue(new Money(OLD_AMOUNT))
                    .withPaid(false)
                    .withHasInvoice(true)
                    .build()
            Installment installment = new InstallmentBuilder()
                    .withFinancialData(financialData)
                    .build()
            stage.installment = installment
        when:
            Installment paidInstallment = this.installmentDomainService.payInstallment(stage, NEW_DATE)
        then:
            noExceptionThrown()
            paidInstallment.paymentDate == NEW_DATE
            paidInstallment.paid
    }
}
