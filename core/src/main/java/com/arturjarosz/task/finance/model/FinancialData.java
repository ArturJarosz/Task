package com.arturjarosz.task.finance.model;

import com.arturjarosz.task.sharedkernel.model.AbstractAggregateRoot;
import com.arturjarosz.task.sharedkernel.model.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

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

    @Setter
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "VALUE", precision = 5, scale = 2))
    private Money value;

    @Setter
    @Column(name = "HAS_INVOICE", nullable = false)
    private boolean hasInvoice;

    @Setter
    @Column(name = "PAYABLE", nullable = false)
    private boolean payable;

    @Column(name = "PAID")
    private boolean paid;

    @Getter
    @Setter
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

    public FinancialData(Money value, boolean hasInvoice, boolean payable, boolean paid, LocalDate paymentDate) {
        this(value, hasInvoice, payable);
        if (paid) {
            this.pay(paymentDate);
        }
    }

    public void pay(LocalDate paymentDate) {
        this.paid = true;
        this.paymentDate = paymentDate;
    }

    public void unpay() {
        this.paid = false;
        this.paymentDate = null;
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

    public boolean isPaid() {
        return this.paid;
    }
}
