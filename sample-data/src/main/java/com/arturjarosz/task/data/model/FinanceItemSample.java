package com.arturjarosz.task.data.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Common data of financial objects (costs, contractor jobs, supplies) of sample Project.
 */
@Getter
@Setter
public abstract class FinanceItemSample {
    private String name;
    private BigDecimal value;
    private String note;
    private boolean hasInvoice;
    // financial_data table has a constraint requiring payable to be true
    private boolean payable = true;
    private boolean paid;
    private Integer paymentDaysAgo;
}
