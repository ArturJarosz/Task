package com.arturjarosz.task.sharedkernel.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

@Embeddable
public class PayableFinancialData {

    @Embedded
    private FinancialData financialData;

    @Column(name = "IS_PAID")
    private boolean paid;

    public PayableFinancialData() {
        //needed by Hibernate
    }

    public PayableFinancialData(boolean paid, double value, boolean hasInvoice) {
        this.financialData = new FinancialData(value, hasInvoice);
        this.paid = paid;
    }

    public boolean isPaid() {
        return this.paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public Money getValue() {
        return this.financialData.getValue();
    }

    public void setValue(double value) {
        this.financialData.setValue(new Money(value));
    }

    public boolean hasInvoice() {
        return this.financialData.isHasInvoice();
    }

    public void setHasInvoice(boolean hasInvoice) {
        this.financialData.setHasInvoice(hasInvoice);
    }
}
