package com.arturjarosz.task.companycost.domain;

import com.arturjarosz.task.companycost.model.CompanyCost;
import com.arturjarosz.task.companycost.model.CompanyCostAmount;
import com.arturjarosz.task.companycost.model.CompanyCostRepeats;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * The charges of a company cost, worked out from its schedule and amounts; nothing about a charge is stored until it
 * is paid. A charge is identified by its month (the first day of it).
 */
public final class ChargeSchedule {
    // Charges fall due on calendar days in Poland, where the app is used; the server's own time zone can be behind it
    public static final ZoneId ZONE = ZoneId.of("Europe/Warsaw");

    private final LocalDate oneTimeDate;
    private final CompanyCostRepeats repeats;
    private final LocalDate startMonth;
    private final LocalDate endMonth;
    private final int chargeDay;
    private final NavigableMap<LocalDate, BigDecimal> netValuesByMonth = new TreeMap<>();

    private ChargeSchedule(LocalDate oneTimeDate, CompanyCostRepeats repeats, LocalDate startMonth,
            LocalDate endMonth, int chargeDay, List<CompanyCostAmount> amounts) {
        this.oneTimeDate = oneTimeDate;
        this.repeats = repeats;
        this.startMonth = CompanyCost.monthOf(startMonth);
        this.endMonth = CompanyCost.monthOf(endMonth);
        this.chargeDay = chargeDay;
        amounts.forEach(amount -> this.netValuesByMonth.put(amount.getValidFrom(), amount.getNetValue()));
    }

    public static ChargeSchedule of(CompanyCost companyCost) {
        if (companyCost.isRecurring()) {
            return recurring(companyCost.getRepeats(), companyCost.getStartMonth(), companyCost.getEndMonth(),
                    companyCost.getChargeDay(), companyCost.getAmounts());
        }
        return oneTime(companyCost.getDate(), companyCost.getAmounts());
    }

    public static ChargeSchedule oneTime(LocalDate date, List<CompanyCostAmount> amounts) {
        return new ChargeSchedule(date, null, null, null, 0, amounts);
    }

    public static ChargeSchedule recurring(CompanyCostRepeats repeats, LocalDate startMonth, LocalDate endMonth,
            int chargeDay, List<CompanyCostAmount> amounts) {
        return new ChargeSchedule(null, repeats, startMonth, endMonth, chargeDay, amounts);
    }

    public static LocalDate today() {
        return LocalDate.now(ZONE);
    }

    public boolean hasCharge(LocalDate chargeMonth) {
        var month = CompanyCost.monthOf(chargeMonth);
        if (this.oneTimeDate != null) {
            return month.equals(CompanyCost.monthOf(this.oneTimeDate));
        }
        return !month.isBefore(this.startMonth) && (this.endMonth == null || !month.isAfter(this.endMonth))
                && ChronoUnit.MONTHS.between(this.startMonth, month) % this.repeats.getMonths() == 0;
    }

    /**
     * The day a charge is due: the date of a one-time cost, else the charge day of its month, or the last day of a
     * shorter month.
     */
    public LocalDate chargeDate(LocalDate chargeMonth) {
        if (this.oneTimeDate != null) {
            return this.oneTimeDate;
        }
        return chargeMonth.withDayOfMonth(Math.min(this.chargeDay, chargeMonth.lengthOfMonth()));
    }

    /**
     * Net value of the amount valid in the month of the charge.
     */
    public BigDecimal netValue(LocalDate chargeMonth) {
        var entry = this.netValuesByMonth.floorEntry(CompanyCost.monthOf(chargeMonth));
        return entry != null ? entry.getValue() : this.netValuesByMonth.firstEntry().getValue();
    }

    /**
     * Months of all charges in order. Without an end month the stream has no end, so it has to be limited.
     */
    public Stream<LocalDate> chargeMonths() {
        if (this.oneTimeDate != null) {
            return Stream.of(CompanyCost.monthOf(this.oneTimeDate));
        }
        return Stream.iterate(this.startMonth, month -> this.endMonth == null || !month.isAfter(this.endMonth),
                month -> month.plusMonths(this.repeats.getMonths()));
    }

    /**
     * Months of the charges due before the given day, in order.
     */
    public Stream<LocalDate> chargeMonthsBefore(LocalDate day) {
        return this.chargeMonths().takeWhile(month -> this.chargeDate(month).isBefore(day));
    }

    /**
     * Months of the charges due in the period, in order.
     */
    public List<LocalDate> chargeMonthsDueIn(ChargePeriod period) {
        var lastMonth = CompanyCost.monthOf(period.endDate());
        return this.chargeMonths()
                .takeWhile(month -> !month.isAfter(lastMonth))
                .filter(month -> period.contains(this.chargeDate(month)))
                .toList();
    }

    /**
     * Months of the charges due in the given year, in order.
     */
    public List<LocalDate> chargeMonthsIn(int year) {
        return this.chargeMonths()
                .dropWhile(month -> month.getYear() < year)
                .takeWhile(month -> month.getYear() == year)
                .toList();
    }
}
