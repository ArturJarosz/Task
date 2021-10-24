package com.arturjarosz.task.project.model;

import com.arturjarosz.task.finance.model.FinancialData;
import com.arturjarosz.task.sharedkernel.model.AbstractEntity;
import com.arturjarosz.task.sharedkernel.model.Money;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "cooperator_job", allocationSize = 1)
@Table(name = "COOPERATOR_JOB")
public class CooperatorJob extends AbstractEntity {
    private static final long serialVersionUID = -2817735161319438104L;

    @Column(name = "NAME", nullable = false)
    private String name;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "FINANCIAL_DATA_ID", referencedColumnName = "ID", nullable = false)
    private FinancialData financialData;

    @Column(name = "NOTE")
    private String note;

    @Column(name = "COOPERATOR_ID", nullable = false)
    private Long cooperatorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE", nullable = false)
    private CooperatorJobType type;

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

    public Long getCooperatorId() {
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

}
