package com.arturjarosz.task.companycost.domain

import com.arturjarosz.task.companycost.model.CompanyCost
import com.arturjarosz.task.companycost.model.CompanyCostRepeats
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDate

class ChargeScheduleTest extends Specification {
    static final Long CATEGORY_ID = 1L

    @Unroll
    def "A #repeats cost starting in 03.2026 should have a charge in #month: #expected"() {
        given:
            def schedule = ChargeSchedule.of(recurring(repeats, LocalDate.of(2026, 3, 1), null, 15))
        expect:
            schedule.hasCharge(month) == expected
        where:
            repeats                      | month                    || expected
            CompanyCostRepeats.MONTHLY   | LocalDate.of(2026, 2, 1) || false
            CompanyCostRepeats.MONTHLY   | LocalDate.of(2026, 3, 1) || true
            CompanyCostRepeats.MONTHLY   | LocalDate.of(2030, 7, 20) || true
            CompanyCostRepeats.QUARTERLY | LocalDate.of(2026, 6, 1) || true
            CompanyCostRepeats.QUARTERLY | LocalDate.of(2026, 7, 1) || false
            CompanyCostRepeats.YEARLY    | LocalDate.of(2027, 3, 1) || true
            CompanyCostRepeats.YEARLY    | LocalDate.of(2027, 4, 1) || false
    }

    def "A cost with an end month should have no charge after it"() {
        given:
            def schedule = ChargeSchedule.of(recurring(CompanyCostRepeats.MONTHLY, LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 6, 1), 1))
        expect:
            schedule.hasCharge(LocalDate.of(2026, 6, 1))
            !schedule.hasCharge(LocalDate.of(2026, 7, 1))
            schedule.chargeMonths().toList().size() == 6
    }

    @Unroll
    def "A charge on day 31 should fall on #expected in #month"() {
        given:
            def schedule = ChargeSchedule.of(recurring(CompanyCostRepeats.MONTHLY, LocalDate.of(2024, 1, 1), null, 31))
        expect:
            schedule.chargeDate(month) == expected
        where:
            month                    || expected
            LocalDate.of(2026, 2, 1) || LocalDate.of(2026, 2, 28)
            LocalDate.of(2024, 2, 1) || LocalDate.of(2024, 2, 29)
            LocalDate.of(2026, 4, 1) || LocalDate.of(2026, 4, 30)
            LocalDate.of(2026, 5, 1) || LocalDate.of(2026, 5, 31)
    }

    def "Each charge should take the amount valid in its month"() {
        given:
            def companyCost = recurring(CompanyCostRepeats.MONTHLY, LocalDate.of(2024, 3, 1), null, 15, 219.0)
            companyCost.addAmount(LocalDate.of(2025, 1, 20), 249.0)
            companyCost.addAmount(LocalDate.of(2026, 3, 1), 289.0)
            def schedule = ChargeSchedule.of(companyCost)
        expect:
            schedule.netValue(LocalDate.of(2024, 12, 1)) == 219.0
            schedule.netValue(LocalDate.of(2025, 1, 1)) == 249.0
            schedule.netValue(LocalDate.of(2026, 2, 1)) == 249.0
            schedule.netValue(LocalDate.of(2026, 11, 1)) == 289.0
    }

    def "chargeMonthsIn should list the charges of a quarterly cost due in the year"() {
        given:
            def schedule = ChargeSchedule.of(recurring(CompanyCostRepeats.QUARTERLY, LocalDate.of(2025, 11, 1), null, 5))
        expect:
            schedule.chargeMonthsIn(2026) == [LocalDate.of(2026, 2, 1), LocalDate.of(2026, 5, 1),
                                              LocalDate.of(2026, 8, 1), LocalDate.of(2026, 11, 1)]
            schedule.chargeMonthsIn(2024) == []
    }

    def "chargeMonthsBefore should list the charges due before the day, not the one due on it"() {
        given:
            def schedule = ChargeSchedule.of(recurring(CompanyCostRepeats.MONTHLY, LocalDate.of(2026, 9, 1), null, 15))
        expect:
            schedule.chargeMonthsBefore(LocalDate.of(2026, 11, 15)).toList() ==
                    [LocalDate.of(2026, 9, 1), LocalDate.of(2026, 10, 1)]
    }

    def "chargeMonthsDueIn should list the charges whose date falls in the period"() {
        given:
            def schedule = ChargeSchedule.of(recurring(CompanyCostRepeats.MONTHLY, LocalDate.of(2025, 1, 1), null, 15))
        expect: "the charge of 15.01 is before the period, the one of 15.06 after it"
            schedule.chargeMonthsDueIn(new ChargePeriod(LocalDate.of(2026, 1, 16), LocalDate.of(2026, 6, 14))) ==
                    (2..5).collect { LocalDate.of(2026, it, 1) }
        and: "a yearly cost starting later has none"
            ChargeSchedule.of(recurring(CompanyCostRepeats.YEARLY, LocalDate.of(2026, 8, 1), null, 1))
                    .chargeMonthsDueIn(new ChargePeriod(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30))).isEmpty()
    }

    def "A one-time cost should have one charge on its date"() {
        given:
            def schedule = ChargeSchedule.of(CompanyCost.oneTime("Laptop", CATEGORY_ID, LocalDate.of(2026, 10, 6),
                    true, null, 7999.0))
        expect:
            schedule.chargeMonths().toList() == [LocalDate.of(2026, 10, 1)]
            schedule.hasCharge(LocalDate.of(2026, 10, 31))
            !schedule.hasCharge(LocalDate.of(2026, 11, 1))
            schedule.chargeDate(LocalDate.of(2026, 10, 1)) == LocalDate.of(2026, 10, 6)
            schedule.netValue(LocalDate.of(2026, 10, 1)) == 7999.0
    }

    private static CompanyCost recurring(CompanyCostRepeats repeats, LocalDate startMonth, LocalDate endMonth,
            int chargeDay, BigDecimal netValue = 100.0) {
        return CompanyCost.recurring("Subscription", CATEGORY_ID, repeats, startMonth, endMonth, chargeDay, true, null,
                netValue)
    }
}
