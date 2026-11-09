package com.arturjarosz.task.companycost.model

import spock.lang.Specification

import java.time.LocalDate

class CompanyCostTest extends Specification {
    static final Long CATEGORY_ID = 1L

    def "A recurring cost should start with one amount in its start month, stored as the first day"() {
        when:
            def companyCost = recurring(LocalDate.of(2024, 3, 15), null)
        then:
            companyCost.startMonth == LocalDate.of(2024, 3, 1)
            companyCost.amounts*.validFrom == [LocalDate.of(2024, 3, 1)]
            companyCost.amounts*.netValue == [219.0]
    }

    def "addAmount should replace the amount starting in the same month"() {
        given:
            def companyCost = recurring(LocalDate.of(2024, 3, 1), null)
            companyCost.addAmount(LocalDate.of(2025, 1, 1), 249.0)
        when:
            companyCost.addAmount(LocalDate.of(2025, 1, 20), 259.0)
        then:
            companyCost.amounts*.validFrom == [LocalDate.of(2024, 3, 1), LocalDate.of(2025, 1, 1)]
            companyCost.amounts*.netValue == [219.0, 259.0]
    }

    def "Moving the start later should move the amount valid then to it and drop the earlier ones"() {
        given:
            def companyCost = recurring(LocalDate.of(2024, 3, 1), null)
            companyCost.addAmount(LocalDate.of(2025, 1, 1), 249.0)
            companyCost.addAmount(LocalDate.of(2026, 3, 1), 289.0)
        when:
            companyCost.updateRecurring(CompanyCostRepeats.MONTHLY, LocalDate.of(2025, 6, 1), null, 15)
        then:
            companyCost.amounts*.validFrom == [LocalDate.of(2025, 6, 1), LocalDate.of(2026, 3, 1)]
            companyCost.amounts*.netValue == [249.0, 289.0]
    }

    def "Moving the start earlier should move the first amount with it"() {
        given:
            def companyCost = recurring(LocalDate.of(2024, 3, 1), null)
            companyCost.addAmount(LocalDate.of(2025, 1, 1), 249.0)
        when:
            companyCost.updateRecurring(CompanyCostRepeats.MONTHLY, LocalDate.of(2023, 9, 1), null, 15)
        then:
            companyCost.amounts*.validFrom == [LocalDate.of(2023, 9, 1), LocalDate.of(2025, 1, 1)]
            companyCost.amounts*.netValue == [219.0, 249.0]
    }

    def "Ending the cost should drop the amounts that start after its end month"() {
        given:
            def companyCost = recurring(LocalDate.of(2024, 3, 1), null)
            companyCost.addAmount(LocalDate.of(2026, 12, 1), 319.0)
        when:
            companyCost.updateRecurring(CompanyCostRepeats.MONTHLY, LocalDate.of(2024, 3, 1),
                    LocalDate.of(2026, 11, 30), 15)
        then:
            companyCost.endMonth == LocalDate.of(2026, 11, 1)
            companyCost.amounts*.validFrom == [LocalDate.of(2024, 3, 1)]
    }

    def "Changing a one-time cost should move its only amount to the month of the new date"() {
        given:
            def companyCost = CompanyCost.oneTime("BIM course", CATEGORY_ID, LocalDate.of(2026, 9, 14), false, null,
                    1200.0)
        when:
            companyCost.updateOneTime(LocalDate.of(2026, 11, 20), 1300.0)
        then:
            companyCost.date == LocalDate.of(2026, 11, 20)
            companyCost.amounts*.validFrom == [LocalDate.of(2026, 11, 1)]
            companyCost.amounts*.netValue == [1300.0]
    }

    def "A payment should be keyed by the month of its charge"() {
        given:
            def companyCost = recurring(LocalDate.of(2024, 3, 1), null)
        when:
            companyCost.addPayment(LocalDate.of(2026, 10, 15), LocalDate.of(2026, 10, 16), 289.0)
        then:
            companyCost.findPaymentOfCharge(LocalDate.of(2026, 10, 1)).get().paymentDate == LocalDate.of(2026, 10, 16)
            companyCost.payments*.chargeMonth == [LocalDate.of(2026, 10, 1)]
    }

    private static CompanyCost recurring(LocalDate startMonth, LocalDate endMonth) {
        return CompanyCost.recurring("Adobe Creative Cloud", CATEGORY_ID, CompanyCostRepeats.MONTHLY, startMonth,
                endMonth, 15, true, null, 219.0)
    }
}
