package com.arturjarosz.task.finance.domain.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class FinancialDataDto {
    public static final String HAS_INVOICE_FIELD = "hasInvoice";
    public static final String PAYABLE_FIELD = "payable";
    public static final String PAID_FIELD = "paid";
    public static final String VALUE_FIELD = "value";

    private boolean hasInvoice;
    private boolean payable;
    private boolean paid;
    private BigDecimal value;
}
