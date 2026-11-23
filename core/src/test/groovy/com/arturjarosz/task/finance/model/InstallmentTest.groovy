package com.arturjarosz.task.finance.model

import com.arturjarosz.task.dto.InstallmentDto
import spock.lang.Specification

import java.time.LocalDate

class InstallmentTest extends Specification {

    static final DUE_DATE = LocalDate.of(2026, 11, 30)
    static final PAYMENT_DATE = LocalDate.of(2026, 11, 28)

    def "Creating an installment should keep its due date and leave it unpaid"() {
        when:
            def installment = new Installment(new InstallmentDto(value: 1000, dueDate: DUE_DATE), 1L)
        then:
            installment.dueDate == DUE_DATE
            !installment.paid
            installment.paymentDate == null
    }

    def "Paying an installment should keep its due date"() {
        given:
            def installment = new Installment(new InstallmentDto(value: 1000, dueDate: DUE_DATE), 1L)
        when:
            installment.payInstallment(PAYMENT_DATE)
        then:
            installment.dueDate == DUE_DATE
            installment.paymentDate == PAYMENT_DATE
    }

    def "Updating an installment should change its due date"() {
        given:
            def installment = new Installment(new InstallmentDto(value: 1000, dueDate: DUE_DATE), 1L)
        when:
            installment.update(new InstallmentDto(value: 1000, hasInvoice: false, dueDate: DUE_DATE.plusDays(5)))
        then:
            installment.dueDate == DUE_DATE.plusDays(5)
    }
}
