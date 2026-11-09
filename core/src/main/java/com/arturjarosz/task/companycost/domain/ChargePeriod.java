package com.arturjarosz.task.companycost.domain;

import java.time.LocalDate;

/**
 * Days from startDate to endDate, both included, that the company cost list is filtered by.
 */
public record ChargePeriod(LocalDate startDate, LocalDate endDate) {

    public boolean contains(LocalDate date) {
        return !date.isBefore(this.startDate) && !date.isAfter(this.endDate);
    }
}
