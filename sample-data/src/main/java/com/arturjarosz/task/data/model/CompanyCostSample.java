package com.arturjarosz.task.data.model;

import com.arturjarosz.task.dto.CompanyCostKindDto;
import com.arturjarosz.task.dto.CompanyCostRepeatsDto;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Sample Company cost. The category is referenced by its name, and dates are counted back from today, so the sample
 * stays current: months back from this month, days back from today.
 */
@Getter
@Setter
public class CompanyCostSample {
    private String name;
    // name of the Company cost category
    private String category;
    private CompanyCostKindDto kind;
    private BigDecimal netValue;
    private boolean hasInvoice;
    private String note;

    // one-time cost: its date, and the payment (not paid without paymentDaysAgo)
    private Integer dateDaysAgo;
    private Integer paymentDaysAgo;

    // recurring cost: its schedule (no end month without endMonthsAgo) and later amounts
    private CompanyCostRepeatsDto repeats;
    private Integer startMonthsAgo;
    private Integer endMonthsAgo;
    private Integer chargeDay;
    private List<AmountSample> amounts = new ArrayList<>();
    // charges due before today are paid paymentDelayDays after their charge date, except the last unpaidCharges ones,
    // which stay overdue
    private int paymentDelayDays;
    private int unpaidCharges;

    @Getter
    @Setter
    public static class AmountSample {
        private int fromMonthsAgo;
        private BigDecimal netValue;
    }
}
