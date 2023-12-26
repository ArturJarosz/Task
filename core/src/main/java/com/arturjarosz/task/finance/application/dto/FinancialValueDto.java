package com.arturjarosz.task.finance.application.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FinancialValueDto {
    private BigDecimal netValue;
    private BigDecimal grossValue;
    private BigDecimal vatTax;
    private BigDecimal incomeTax;

    public FinancialValueDto() {
        this.initFinancialValue();
    }

    private void initFinancialValue() {
        this.netValue = new BigDecimal("0.0");
        this.grossValue = new BigDecimal("0.0");
        this.vatTax = new BigDecimal("0.0");
        this.incomeTax = new BigDecimal("0.0");
    }

    public void copyValues(FinancialValueDto financialValueDto) {
        this.netValue = financialValueDto.getNetValue();
        this.grossValue = financialValueDto.getGrossValue();
        this.incomeTax = financialValueDto.getIncomeTax();
        this.vatTax = financialValueDto.getVatTax();
    }

    public void addValues(FinancialValueDto financialValueDto) {
        this.netValue = this.netValue.add(financialValueDto.getNetValue());
        this.grossValue = this.grossValue.add(financialValueDto.getGrossValue());
        this.incomeTax = this.incomeTax.add(financialValueDto.getIncomeTax());
        this.vatTax = this.vatTax.add(financialValueDto.getVatTax());
    }

    public void subtractValues(FinancialValueDto financialValueDto) {
        this.netValue = this.netValue.subtract(financialValueDto.getNetValue());
        this.grossValue = this.grossValue.subtract(financialValueDto.getGrossValue());
        this.incomeTax = this.incomeTax.subtract(financialValueDto.getIncomeTax());
        this.vatTax = this.vatTax.subtract(financialValueDto.getVatTax());
    }

    public void addGross(BigDecimal value) {
        this.grossValue = this.grossValue.add(value);
    }

    public void addNet(BigDecimal value) {
        this.netValue = this.netValue.add(value);
    }

    public void addVatTax(BigDecimal value) {
        this.vatTax = this.vatTax.add(value);
    }

    public void addIncomeTax(BigDecimal value) {
        this.incomeTax = this.incomeTax.add(value);
    }

    @Override
    public String toString() {
        return "FinancialValueDto{" + "netValue=" + this.netValue + ", grossValue=" + this.grossValue + ", vatTax=" + this.vatTax + ", incomeTax=" + this.incomeTax + '}';
    }
}
