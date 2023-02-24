package com.arturjarosz.task.finance.application.dto;

import java.io.Serializable;

public class TotalProjectFinancialSummaryDto implements Serializable {

    private static final long serialVersionUID = -5305124883533888889L;
    private Double netValue;
    private Double grossValue;
    private Double vatTax;
    private Double incomeTax;

    public TotalProjectFinancialSummaryDto() {
    }

    public TotalProjectFinancialSummaryDto(Double grossValue, Double netValue, Double vatTax, Double incomeTax) {
        this.netValue = netValue;
        this.grossValue = grossValue;
        this.vatTax = vatTax;
        this.incomeTax = incomeTax;
    }

    public Double getNetValue() {
        return this.netValue;
    }

    public void setNetValue(Double netValue) {
        this.netValue = netValue;
    }

    public Double getGrossValue() {
        return this.grossValue;
    }

    public void setGrossValue(Double grossValue) {
        this.grossValue = grossValue;
    }

    public Double getVatTax() {
        return this.vatTax;
    }

    public void setVatTax(Double vatTax) {
        this.vatTax = vatTax;
    }

    public Double getIncomeTax() {
        return this.incomeTax;
    }

    public void setIncomeTax(Double incomeTax) {
        this.incomeTax = incomeTax;
    }
}
