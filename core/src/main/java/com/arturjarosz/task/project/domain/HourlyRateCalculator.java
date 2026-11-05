package com.arturjarosz.task.project.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Calculates what a value earned per hour of work.
 */
public final class HourlyRateCalculator {

    private static final BigDecimal MINUTES_IN_HOUR = BigDecimal.valueOf(60);
    private static final int SCALE = 2;

    private HourlyRateCalculator() {
    }

    /**
     * Returns value divided by the given minutes in hours, rounded half up to 2 decimal places, or null when no minutes
     * were spent, as there is no rate then.
     */
    public static BigDecimal ratePerHour(BigDecimal value, long minutes) {
        if (minutes <= 0) {
            return null;
        }
        return value.multiply(MINUTES_IN_HOUR).divide(BigDecimal.valueOf(minutes), SCALE, RoundingMode.HALF_UP);
    }
}
