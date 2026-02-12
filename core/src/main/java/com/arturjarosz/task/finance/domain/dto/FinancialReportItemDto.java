package com.arturjarosz.task.finance.domain.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class FinancialReportItemDto {
    public static final String VALUE_FIELD = "value";
    public static final String HAS_INVOICE_FIELD = "hasInvoice";
    public static final String PAYABLE_FIELD = "payable";
    public static final String PAID_FIELD = "paid";
    public static final String EFFECTIVE_DATE_FIELD = "effectiveDate";

    private BigDecimal value;
    private boolean hasInvoice;
    private boolean payable;
    private boolean paid;
    private LocalDate effectiveDate;
}
