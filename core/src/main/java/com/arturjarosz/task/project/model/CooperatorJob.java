package com.arturjarosz.task.project.model;

import com.arturjarosz.task.finance.model.FinancialData;
import com.arturjarosz.task.sharedkernel.model.AbstractEntity;
import com.arturjarosz.task.sharedkernel.model.Money;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import java.math.BigDecimal;

@MappedSuperclass
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "TYPE")
public class CooperatorJob extends AbstractEntity {
    private static final long serialVersionUID = -2817735161319438104L;

    @Column(name = "NAME", nullable = false)
    String name;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "FINANCIAL_DATA_ID", referencedColumnName = "ID", nullable = false)
    FinancialData financialData;

    @Column(name = "NOTE")
    String note;

    @Column(name = "COOPERATOR_ID", nullable = false)
    long cooperatorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE", nullable = false)
    CooperatorJobType type;

    protected CooperatorJob() {
        // Needed by Hibernate
    }

    public CooperatorJob(String name, Long cooperatorId, CooperatorJobType cooperatorJobType, BigDecimal value,
                         boolean hasInvoice, boolean payable) {
        this.name = name;
        this.cooperatorId = cooperatorId;
        this.type = cooperatorJobType;
        this.financialData = new FinancialData(new Money(value), hasInvoice, payable);
    }

    public String getName() {
        return this.name;
    }

    public BigDecimal getValue() {
        return this.financialData.getValue().getValue();
    }

    public String getNote() {
        return this.note;
    }

    public long getCooperatorId() {
        return this.cooperatorId;
    }

    public CooperatorJobType getType() {
        return this.type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(BigDecimal value) {
        this.financialData.setValue(new Money(value));
    }

    public void setNote(String note) {
        this.note = note;
    }


    public boolean isHasInvoice() {
        return this.financialData.isHasInvoice();
    }

    public boolean isPayable() {
        return this.financialData.isPayable();
    }

    public boolean isPaid() {
        return this.financialData.isPaid();
    }

}
