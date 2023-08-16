package com.arturjarosz.task.finance.model;

import com.arturjarosz.task.sharedkernel.model.AbstractEntity;
import com.arturjarosz.task.sharedkernel.model.Money;
import jakarta.persistence.*;

import java.io.Serial;
import java.math.BigDecimal;

@SuppressWarnings("java:S2160") // equality is tested on uuid value, no need to override with same code
@SequenceGenerator(name = "sequence_generator", sequenceName = "cooperator_job_sequence", allocationSize = 1)
@MappedSuperclass
@DiscriminatorColumn(name = "TYPE")
@Table(name = "COOPERATOR_JOB")
public abstract class CooperatorJob extends AbstractEntity {
    @Serial
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

    protected CooperatorJob(String name, Long cooperatorId, CooperatorJobType cooperatorJobType, BigDecimal value,
            boolean hasInvoice, boolean payable) {
        this.name = name;
        this.cooperatorId = cooperatorId;
        this.type = cooperatorJobType;
        this.financialData = new FinancialData(new Money(value), hasInvoice, payable);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getValue() {
        return this.financialData.getValue().getValue();
    }

    public void setValue(BigDecimal value) {
        this.financialData.setValue(new Money(value));
    }

    public String getNote() {
        return this.note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    long getCooperatorId() {
        return this.cooperatorId;
    }

    public CooperatorJobType getType() {
        return this.type;
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
