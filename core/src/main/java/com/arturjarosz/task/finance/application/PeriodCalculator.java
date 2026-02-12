package com.arturjarosz.task.finance.application;

import com.arturjarosz.task.dto.PeriodTypeDto;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import static com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes.NOT_FOR_INSTANTIATING;

public final class PeriodCalculator {

    private PeriodCalculator() {
        throw new IllegalStateException(NOT_FOR_INSTANTIATING);
    }

    public static List<DatePeriod> calculatePeriods(LocalDate start, LocalDate end, PeriodTypeDto periodType) {
        List<DatePeriod> periods = new ArrayList<>();
        LocalDate periodStart = start;

        while (!periodStart.isAfter(end)) {
            LocalDate periodEnd = calculatePeriodEnd(periodStart, periodType);
            if (periodEnd.isAfter(end)) {
                periodEnd = end;
            }
            periods.add(new DatePeriod(periodStart, periodEnd));
            periodStart = periodEnd.plusDays(1);
        }

        return periods;
    }

    private static LocalDate calculatePeriodEnd(LocalDate periodStart, PeriodTypeDto periodType) {
        return switch (periodType) {
            case MONTHLY -> periodStart.with(TemporalAdjusters.lastDayOfMonth());
            case QUARTERLY -> {
                int quarterMonth = ((periodStart.getMonthValue() - 1) / 3 + 1) * 3;
                yield periodStart.withMonth(quarterMonth).with(TemporalAdjusters.lastDayOfMonth());
            }
            case YEARLY -> periodStart.with(TemporalAdjusters.lastDayOfYear());
        };
    }
}
