package com.arturjarosz.task.data.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class InstallmentSample {
    private BigDecimal value;
    private String note;
    private boolean hasInvoice;
    private boolean paid;
    private Integer paymentDaysAgo;
}
