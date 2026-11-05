package com.arturjarosz.task.project.domain

import spock.lang.Specification
import spock.lang.Unroll

class HourlyRateCalculatorTest extends Specification {

    @Unroll
    def "ratePerHour of #value over #minutes minutes should be #rate"() {
        expect:
            HourlyRateCalculator.ratePerHour(new BigDecimal(value), minutes) == (rate == null ? null : new BigDecimal(rate))
        where:
            value      | minutes || rate
            "18400.00" | 5190    || "212.72"
            "24000.00" | 5190    || "277.46"
            "-1000.00" | 600     || "-100.00"
            "100.00"   | 60      || "100.00"
            "0.00"     | 90      || "0.00"
            "10.00"    | 0       || null
    }

    def "ratePerHour should round half up to 2 decimal places"() {
        expect:
            // 0.125 per hour
            HourlyRateCalculator.ratePerHour(new BigDecimal("0.125"), 60) == new BigDecimal("0.13")
    }
}
