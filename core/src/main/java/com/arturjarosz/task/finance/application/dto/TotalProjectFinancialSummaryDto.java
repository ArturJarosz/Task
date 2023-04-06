package com.arturjarosz.task.finance.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TotalProjectFinancialSummaryDto implements Serializable {
    @Serial
    private static final long serialVersionUID = -5305124883533888889L;

    private Double netValue;
    private Double grossValue;
    private Double vatTax;
    private Double incomeTax;

}
