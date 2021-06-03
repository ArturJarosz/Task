package com.arturjarosz.task.sharedkernel.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

/**
 * Object that represents financial data in every Entity that stores Money value and participates in Project value.
 */

@Embeddable
public class FinancialData {

    @Embedded
    private Money value;

    @Column(name = "HAS_INVOICE")
    private boolean hasInvoice;

    protected FinancialData() {

    }

    public FinancialData(double value, boolean hasInvoice) {
        this.value = new Money(value);
        this.hasInvoice = hasInvoice;
    }

    public Money getValue() {
        return this.value;
    }

    public void setValue(Money value) {
        this.value = value;
    }

    public boolean isHasInvoice() {
        return this.hasInvoice;
    }

    public void setHasInvoice(boolean hasInvoice) {
        this.hasInvoice = hasInvoice;
    }
}
