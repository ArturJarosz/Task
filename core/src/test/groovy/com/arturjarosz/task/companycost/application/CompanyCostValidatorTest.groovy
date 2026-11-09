package com.arturjarosz.task.companycost.application

import com.arturjarosz.task.companycost.model.CompanyCost
import com.arturjarosz.task.companycost.model.CompanyCostRepeats
import com.arturjarosz.task.dto.CompanyCostAmountDto
import com.arturjarosz.task.dto.CompanyCostDto
import com.arturjarosz.task.dto.CompanyCostKindDto
import com.arturjarosz.task.dto.CompanyCostPaymentDto
import com.arturjarosz.task.dto.CompanyCostRepeatsDto
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDate

class CompanyCostValidatorTest extends Specification {
    static final Long CATEGORY_ID = 1L
    static final LocalDate TODAY = LocalDate.of(2026, 11, 7)

    def companyCostCategoryValidator = Mock(CompanyCostCategoryValidator)

    def companyCostValidator = new CompanyCostValidator(companyCostCategoryValidator)

    def "validateCreateDto should accept a recurring cost and check that its category exists"() {
        when:
            this.companyCostValidator.validateCreateDto(recurringDto(), TODAY)
        then:
            noExceptionThrown()
            1 * this.companyCostCategoryValidator.validateExistence(CATEGORY_ID)
    }

    def "validateCreateDto should accept a one-time cost paid today"() {
        given:
            def dto = new CompanyCostDto(kind: CompanyCostKindDto.ONE_TIME, name: "Laptop", categoryId: CATEGORY_ID,
                    netValue: 7999.0, date: LocalDate.of(2026, 10, 6), paid: true, paymentDate: TODAY)
        when:
            this.companyCostValidator.validateCreateDto(dto, TODAY)
        then:
            noExceptionThrown()
    }

    @Unroll
    def "validateCreateDto should refuse #description"() {
        when:
            this.companyCostValidator.validateCreateDto(dto, TODAY)
        then:
            def exception = thrown(IllegalArgumentException)
            exception.message == messageCode
        where:
            description                   | dto                                                                || messageCode
            "a cost without a kind"       | recurringDto(kind: null)                                           || "isNull.companyCost.kind"
            "a blank name"                | recurringDto(name: "  ")                                           || "isEmpty.companyCost.name"
            "a name of 256 characters"    | recurringDto(name: "a" * 256)                                      || "notValid.companyCost.name.tooLong"
            "a net value of 0"            | recurringDto(netValue: 0.0)                                        || "notValid.companyCost.netValue"
            "a recurring cost without repeats" | recurringDto(repeats: null)                                   || "isNull.companyCost.repeats"
            "a recurring cost without start"   | recurringDto(startMonth: null)                                || "isNull.companyCost.startMonth"
            "an end before the start"     | recurringDto(endMonth: LocalDate.of(2024, 2, 29))                  || "notValid.companyCost.endMonth"
            "a charge day of 0"           | recurringDto(chargeDay: 0)                                         || "notValid.companyCost.chargeDay"
            "a charge day of 32"          | recurringDto(chargeDay: 32)                                        || "notValid.companyCost.chargeDay"
            "a one-time cost without date" | new CompanyCostDto(kind: CompanyCostKindDto.ONE_TIME, name: "Laptop", categoryId: CATEGORY_ID, netValue: 1.0) || "isNull.companyCost.date"
            "a payment date in the future" | new CompanyCostDto(kind: CompanyCostKindDto.ONE_TIME, name: "Laptop", categoryId: CATEGORY_ID, netValue: 1.0, date: TODAY, paid: true, paymentDate: TODAY.plusDays(1)) || "notValid.companyCost.paymentDate"
    }

    def "validateCreateDto should accept an end month in the start month"() {
        when:
            this.companyCostValidator.validateCreateDto(recurringDto(endMonth: LocalDate.of(2024, 3, 31)), TODAY)
        then:
            noExceptionThrown()
    }

    def "validateUpdateDto should refuse a schedule that leaves paid charges outside it"() {
        given:
            def companyCost = recurringCost()
            companyCost.addPayment(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 15), 289.0)
            companyCost.addPayment(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 15), 289.0)
        when:
            this.companyCostValidator.validateUpdateDto(recurringDto(endMonth: LocalDate.of(2026, 6, 1)), companyCost)
        then:
            def exception = thrown(IllegalArgumentException)
            exception.message == "notValid.companyCost.paidCharges"
            exception.messageParameters as List == ["07.2026, 08.2026"]
    }

    def "validateUpdateDto should refuse changing how often a cost with paid charges repeats when they don't fit"() {
        given:
            def companyCost = recurringCost()
            companyCost.addPayment(LocalDate.of(2024, 4, 1), LocalDate.of(2024, 4, 15), 219.0)
        when:
            this.companyCostValidator.validateUpdateDto(recurringDto(repeats: CompanyCostRepeatsDto.YEARLY), companyCost)
        then:
            def exception = thrown(IllegalArgumentException)
            exception.message == "notValid.companyCost.paidCharges"
    }

    def "validateUpdateDto should allow another charge day for a cost with paid charges"() {
        given:
            def companyCost = recurringCost()
            companyCost.addPayment(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 15), 289.0)
        when:
            this.companyCostValidator.validateUpdateDto(recurringDto(chargeDay: 28), companyCost)
        then:
            noExceptionThrown()
    }

    @Unroll
    def "validateUpdateDto of a paid one-time cost should #description"() {
        given:
            def companyCost = CompanyCost.oneTime("Laptop", CATEGORY_ID, LocalDate.of(2026, 10, 6), true, null, 7999.0)
            companyCost.addPayment(LocalDate.of(2026, 10, 6), LocalDate.of(2026, 10, 6), 7999.0)
            def dto = new CompanyCostDto(name: "Laptop Dell XPS 15", categoryId: CATEGORY_ID, netValue: netValue, date: date)
        when:
            def refused = false
            try {
                this.companyCostValidator.validateUpdateDto(dto, companyCost)
            } catch (IllegalArgumentException exception) {
                refused = exception.message == "notValid.companyCost.paid"
            }
        then:
            refused == expectedRefused
        where:
            description                         | netValue | date                      || expectedRefused
            "allow another name and day"        | 7999.0   | LocalDate.of(2026, 10, 20) || false
            "refuse another net value"          | 8100.0   | LocalDate.of(2026, 10, 6) || true
            "refuse a date in another month"    | 7999.0   | LocalDate.of(2026, 11, 6) || true
    }

    def "validateNewPaymentDto should refuse a month without a charge and a charge already paid"() {
        given:
            def companyCost = CompanyCost.recurring("Accounting", CATEGORY_ID, CompanyCostRepeats.QUARTERLY,
                    LocalDate.of(2026, 1, 1), null, 20, true, null, 1350.0)
            companyCost.addPayment(LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 20), 1350.0)
        when:
            this.companyCostValidator.validateNewPaymentDto(new CompanyCostPaymentDto(chargeMonth: month,
                    paymentDate: TODAY), companyCost, TODAY)
        then:
            def exception = thrown(IllegalArgumentException)
            exception.message == messageCode
        where:
            month                    || messageCode
            LocalDate.of(2026, 5, 1) || "notValid.companyCost.payment.chargeMonth"
            LocalDate.of(2026, 4, 1) || "notValid.companyCost.payment.chargeMonth.paid"
    }

    def "validateNewPaymentDto should refuse a payment date in the future"() {
        when:
            this.companyCostValidator.validateNewPaymentDto(new CompanyCostPaymentDto(
                    chargeMonth: LocalDate.of(2026, 12, 1), paymentDate: TODAY.plusDays(1)), recurringCost(), TODAY)
        then:
            def exception = thrown(IllegalArgumentException)
            exception.message == "notValid.companyCost.payment.paymentDate"
    }

    def "validateNewAmountDto should refuse a one-time cost and a month outside the cost"() {
        given:
            def ended = CompanyCost.recurring("Coworking desk", CATEGORY_ID, CompanyCostRepeats.MONTHLY,
                    LocalDate.of(2025, 1, 1), LocalDate.of(2026, 6, 1), 1, true, null, 900.0)
            def oneTime = CompanyCost.oneTime("Laptop", CATEGORY_ID, TODAY, true, null, 7999.0)
            def amountDto = new CompanyCostAmountDto(validFrom: LocalDate.of(2026, 7, 1),
                    netValue: 950.0)
        when:
            this.companyCostValidator.validateNewAmountDto(amountDto, companyCost == "ended" ? ended : oneTime)
        then:
            def exception = thrown(IllegalArgumentException)
            exception.message == messageCode
        where:
            companyCost || messageCode
            "ended"     || "notValid.companyCost.amount.validFrom"
            "one-time"  || "notValid.companyCost.amount.oneTime"
    }

    @Unroll
    def "validatePeriod should #description"() {
        when:
            def refused = null
            try {
                this.companyCostValidator.validatePeriod(startDate, endDate)
            } catch (IllegalArgumentException exception) {
                refused = exception.message
            }
        then:
            refused == messageCode
        where:
            description                  | startDate | endDate            || messageCode
            "accept no period"           | null      | null               || null
            "accept a one-day period"    | TODAY     | TODAY              || null
            "refuse a start without end" | TODAY     | null               || "notValid.companyCost.period"
            "refuse an end before start" | TODAY     | TODAY.minusDays(1) || "notValid.companyCost.period.startDate"
    }

    private static CompanyCost recurringCost() {
        return CompanyCost.recurring("Adobe Creative Cloud", CATEGORY_ID, CompanyCostRepeats.MONTHLY,
                LocalDate.of(2024, 3, 1), null, 15, true, null, 219.0)
    }

    private static CompanyCostDto recurringDto(Map overrides = [:]) {
        def properties = [kind      : CompanyCostKindDto.RECURRING, name: "Adobe Creative Cloud", categoryId: CATEGORY_ID,
                          repeats   : CompanyCostRepeatsDto.MONTHLY, startMonth: LocalDate.of(2024, 3, 1),
                          chargeDay : 15, netValue: 219.0, hasInvoice: true] + overrides
        return new CompanyCostDto(properties)
    }
}
