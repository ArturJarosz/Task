package com.arturjarosz.task.finance.model;

import com.arturjarosz.task.sharedkernel.model.AbstractEntity;
import com.arturjarosz.task.sharedkernel.model.Money;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.math.BigDecimal;

@SequenceGenerator(name = "sequence_generator", sequenceName = "cooperator_job_sequence", allocationSize = 1)
@MappedSuperclass
@DiscriminatorColumn(name = "TYPE")
@Table(name = "COOPERATOR_JOB")
public abstract class CooperatorJob extends AbstractEntity {
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

    @Column(name = "PROJECT_FINANCIAL_DATA_ID", insertable = false, updatable = false)
    private Long projectFinancialDataId;

    protected CooperatorJob() {
        // needed by JPA
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

    long getCooperatorId() {
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
