package com.arturjarosz.task.companycost.model;

import lombok.Getter;

/**
 * How often a recurring company cost is charged, counted in months from its start month.
 */
@Getter
public enum CompanyCostRepeats {
    MONTHLY(1),
    QUARTERLY(3),
    YEARLY(12);

    private final int months;

    CompanyCostRepeats(int months) {
        this.months = months;
    }
}
