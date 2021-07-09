package com.arturjarosz.task.finance.model;

import com.arturjarosz.task.sharedkernel.model.AbstractAggregateRoot;
import com.arturjarosz.task.sharedkernel.model.Money;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * Object that represents financial data in every Entity that stores Money value and participates in Project value.
 */

@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "financial_data_sequence", allocationSize = 1)
@Table(name = "FINACIAL_DATA")
public class FinancialData extends AbstractAggregateRoot {
    private static final long serialVersionUID = -7882045222253776404L;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "VALUE"))
    private Money value;

    @Column(name = "HAS_INVOICE", nullable = false)
    private boolean hasInvoice;

    @Column(name = "PAYABLE", nullable = false)
    private boolean payable;

    @Column(name = "IS_PAID")
    private boolean isPaid;

    protected FinancialData() {
        // Needed by Hibernate
    }

    public FinancialData(Money value, boolean hasInvoice, boolean payable) {
        this.value = value;
        this.hasInvoice = hasInvoice;
        this.payable = payable;
    }

    public void pay() {
        this.isPaid = true;
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
        return this.isPaid;
    }
}
