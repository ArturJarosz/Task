package com.arturjarosz.task.finance.application.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class TotalProjectFinancialSummaryDto implements Serializable {

    private static final long serialVersionUID = -5305124883533888889L;
    private Double netValue;
    private Double grossValue;
    private Double vatTax;
    private Double incomeTax;

    public TotalProjectFinancialSummaryDto(Double grossValue, Double netValue, Double vatTax, Double incomeTax) {
        this.netValue = netValue;
        this.grossValue = grossValue;
        this.vatTax = vatTax;
        this.incomeTax = incomeTax;
    }
}
