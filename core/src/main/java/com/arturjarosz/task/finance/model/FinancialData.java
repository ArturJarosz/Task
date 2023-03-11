package com.arturjarosz.task.finance.model;

import com.arturjarosz.task.sharedkernel.model.AbstractAggregateRoot;
import com.arturjarosz.task.sharedkernel.model.Money;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.time.LocalDate;

/**
 * Object that represents financial data in every Entity that stores Money value and participates in Project value.
 */

@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "financial_data_sequence", allocationSize = 1)
@Table(name = "FINANCIAL_DATA")
public class FinancialData extends AbstractAggregateRoot {
    private static final long serialVersionUID = -7882045222253776404L;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "VALUE", precision = 5, scale = 2))
    private Money value;

    @Column(name = "HAS_INVOICE", nullable = false)
    private boolean hasInvoice;

    @Column(name = "PAYABLE", nullable = false)
    private boolean payable;

    @Column(name = "PAID")
    private boolean paid;

    @Column(name = "PAYMENT_DATE")
    private LocalDate paymentDate;

    protected FinancialData() {
        // needed by JPA
    }

    public FinancialData(Money value, boolean hasInvoice, boolean payable) {
        this.value = value;
        this.hasInvoice = hasInvoice;
        this.payable = payable;
        this.paid = false;
    }

    public FinancialData(Money value, boolean hasInvoice, boolean payable, boolean paid) {
        this.value = value;
        this.hasInvoice = hasInvoice;
        this.payable = payable;
        this.paid = paid;
    }

    public void pay(LocalDate paymentDate) {
        this.paid = true;
        this.paymentDate = paymentDate;
    }

    public Money getValue() {
        return this.value;
    }

    public boolean isHasInvoice() {
        return this.hasInvoice;
    }

    public boolean isPayable() {
        return this.payable;
    }

    public void setHasInvoice(boolean hasInvoice) {
        this.hasInvoice = hasInvoice;
    }

    public void setValue(Money value) {
        this.value = value;
    }

    public LocalDate getPaymentDate() {
        return this.paymentDate;
    }

    public boolean isPaid() {
        return this.paid;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public void setPayable(boolean payable) {
        this.payable = payable;
    }
}
