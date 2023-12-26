package com.arturjarosz.task.finance.model;

import com.arturjarosz.task.sharedkernel.model.AbstractAggregateRoot;
import com.arturjarosz.task.sharedkernel.model.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.io.Serial;
import java.time.LocalDate;

/**
 * Object that represents financial data in every Entity that stores Money value and participates in Project value.
 */

@SuppressWarnings("java:S2160") // equality is tested on uuid value, no need to override with same code
@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "financial_data_sequence", allocationSize = 1)
@Table(name = "FINANCIAL_DATA")
public class FinancialData extends AbstractAggregateRoot {
    @Serial
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

    public void setValue(Money value) {
        this.value = value;
    }

    public boolean isHasInvoice() {
        return this.hasInvoice;
    }

    public void setHasInvoice(boolean hasInvoice) {
        this.hasInvoice = hasInvoice;
    }

    public boolean isPayable() {
        return this.payable;
    }

    public void setPayable(boolean payable) {
        this.payable = payable;
    }

    public LocalDate getPaymentDate() {
        return this.paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public boolean isPaid() {
        return this.paid;
    }
}
