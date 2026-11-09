package com.arturjarosz.task.companycost.application

import com.arturjarosz.task.companycost.domain.ChargePeriod
import com.arturjarosz.task.companycost.domain.ChargeSchedule
import com.arturjarosz.task.companycost.model.CompanyCost
import com.arturjarosz.task.companycost.model.CompanyCostRepeats
import spock.lang.Specification

import java.time.LocalDate

class CompanyCostAssemblerTest extends Specification {
    static final Long CATEGORY_ID = 1L
    static final LocalDate TODAY = LocalDate.of(2026, 11, 7)

    def companyCostAssembler = new CompanyCostAssembler()

    def "A cost with every past charge paid should have the next charge ahead and nothing overdue"() {
        given:
            def companyCost = adobe()
            payUpTo(companyCost, LocalDate.of(2026, 10, 1))
        when:
            def dto = this.companyCostAssembler.toDto(companyCost, TODAY)
        then:
            dto.nextCharge.chargeDate == LocalDate.of(2026, 11, 15)
            !dto.nextCharge.paid
            !dto.nextCharge.overdue
            dto.netValue == 289.0
            dto.overdueCharges == 0
            dto.overdueNetValue == 0.0
        and: "219 zł from 03.2024 to 12.2024, 249 zł in 2025 and the first two months of 2026, 289 zł from 03.2026"
            dto.paidThisYear == 2 * 249.0 + 8 * 289.0
            dto.paidToDate == 10 * 219.0 + 14 * 249.0 + 8 * 289.0
        and: "amounts newest first"
            dto.amounts*.validFrom == [LocalDate.of(2026, 3, 1), LocalDate.of(2025, 1, 1), LocalDate.of(2024, 3, 1)]
    }

    def "An unpaid charge due before today should be overdue and be the next charge"() {
        given:
            def companyCost = CompanyCost.recurring("Microsoft 365", CATEGORY_ID, CompanyCostRepeats.MONTHLY,
                    LocalDate.of(2026, 9, 1), null, 1, true, null, 52.40)
            companyCost.addPayment(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 1), 52.40)
        when:
            def dto = this.companyCostAssembler.toDto(companyCost, TODAY)
        then:
            dto.nextCharge.chargeDate == LocalDate.of(2026, 10, 1)
            dto.nextCharge.overdue
            dto.overdueCharges == 2
            dto.overdueNetValue == 104.80
    }

    def "An ended cost with every charge paid should have no next charge and keep its last amount"() {
        given:
            def companyCost = CompanyCost.recurring("Coworking desk", CATEGORY_ID, CompanyCostRepeats.MONTHLY,
                    LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 1), 1, true, null, 900.0)
            payUpTo(companyCost, LocalDate.of(2026, 3, 1))
        when:
            def dto = this.companyCostAssembler.toDto(companyCost, TODAY)
        then:
            dto.nextCharge == null
            dto.netValue == 900.0
            dto.paidToDate == 2700.0
    }

    def "Charges of a year should be newest first, with the paid amount for paid ones"() {
        given:
            def companyCost = adobe()
            companyCost.addPayment(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 16), 249.0)
        when:
            def charges = this.companyCostAssembler.toChargeDtos(companyCost, 2026, TODAY)
        then:
            charges.size() == 12
            charges.first().chargeDate == LocalDate.of(2026, 12, 15)
            charges.last().chargeDate == LocalDate.of(2026, 1, 15)
            def february = charges.find { it.chargeMonth == LocalDate.of(2026, 2, 1) }
            february.paid
            february.paymentDate == LocalDate.of(2026, 2, 16)
            february.netValue == 249.0
            charges.find { it.chargeMonth == LocalDate.of(2026, 3, 1) }.overdue
            !charges.find { it.chargeMonth == LocalDate.of(2026, 11, 1) }.overdue
    }

    def "With a period the dto should have the figures of its charges and of the payments made in it"() {
        given: "charges on the 15th from 03.2024, paid on their day up to 08.2026, the one of 09.2026 late on 02.10"
            def companyCost = adobe()
            payUpTo(companyCost, LocalDate.of(2026, 8, 1))
            companyCost.addPayment(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 10, 2), 289.0)
            def period = new ChargePeriod(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 11, 30))
        when:
            def dto = this.companyCostAssembler.toDto(companyCost, TODAY, period)
        then: "the charges of 09, 10 and 11, of which 10 is overdue"
            dto.periodCharges == 3
            dto.periodChargesNetValue == 3 * 289.0
            dto.periodOverdueCharges == 1
            dto.periodOverdueNetValue == 289.0
        and: "the payment of 02.10 is in the period, the one of 15.08 isn't"
            dto.periodPaidNetValue == 289.0
    }

    def "Without a period the dto should have no period figures"() {
        expect:
            this.companyCostAssembler.toDto(adobe(), TODAY).periodCharges == null
    }

    def "A paid one-time cost should have no next charge"() {
        given:
            def companyCost = CompanyCost.oneTime("Laptop", CATEGORY_ID, LocalDate.of(2026, 10, 6), true, null, 7999.0)
            companyCost.addPayment(LocalDate.of(2026, 10, 6), LocalDate.of(2026, 10, 6), 7999.0)
        when:
            def dto = this.companyCostAssembler.toDto(companyCost, TODAY)
        then:
            dto.nextCharge == null
            dto.netValue == 7999.0
            dto.paidThisYear == 7999.0
            dto.amounts.isEmpty()
    }

    private static CompanyCost adobe() {
        def companyCost = CompanyCost.recurring("Adobe Creative Cloud", CATEGORY_ID, CompanyCostRepeats.MONTHLY,
                LocalDate.of(2024, 3, 1), null, 15, true, null, 219.0)
        companyCost.addAmount(LocalDate.of(2025, 1, 1), 249.0)
        companyCost.addAmount(LocalDate.of(2026, 3, 1), 289.0)
        return companyCost
    }

    private static void payUpTo(CompanyCost companyCost, LocalDate lastMonth) {
        def schedule = ChargeSchedule.of(companyCost)
        schedule.chargeMonths().takeWhile { !it.isAfter(lastMonth) }.forEach {
            companyCost.addPayment(it, schedule.chargeDate(it), schedule.netValue(it))
        }
    }
}
