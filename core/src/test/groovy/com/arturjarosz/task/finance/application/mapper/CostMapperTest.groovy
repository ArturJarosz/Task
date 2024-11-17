package com.arturjarosz.task.finance.application.mapper

import com.arturjarosz.task.dto.CostCategoryDto
import com.arturjarosz.task.finance.model.Cost
import com.arturjarosz.task.finance.model.CostCategory
import spock.lang.Specification

import java.time.LocalDate

class CostMapperTest extends Specification {
    static final NAME = "name"
    static final VALUE = 200.0D
    static final CATEGORY = CostCategory.FUEL
    static final DATE = LocalDate.of(2022, 01, 02)
    static final NOTE = "some note"
    static final HAS_INVOICE = true
    static final PAYABLE = true
    static final PAID = true
    static final PAYMENT_DATE = LocalDate.of(2022, 03, 04)

    def subject = new CostMapperImpl()

    def "mapToDto should return properly mapped dto"() {
        given:
            def cost = new Cost(NAME, BigDecimal.valueOf(VALUE), CATEGORY, DATE, NOTE, HAS_INVOICE, PAYABLE, givenPaid, givenPaymentDate)
        when:
            def result = subject.mapToDto(cost)
        then:
            result.name == NAME
            result.value == BigDecimal.valueOf(VALUE)
            result.category == CostCategoryDto.valueOf(CATEGORY.name())
            result.date == DATE
            result.note == NOTE
            result.hasInvoice
            result.paid == resultPaid
            result.paymentDate == resultPaymentDate
        where:
            givenPaid | givenPaymentDate || resultPaid | resultPaymentDate
            false     | PAYMENT_DATE     || false      | null
            true      | PAYMENT_DATE     || true       | PAYMENT_DATE

    }
}
