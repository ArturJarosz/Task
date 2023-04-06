package com.arturjarosz.task.finance.model;

import com.arturjarosz.task.sharedkernel.model.AbstractEntity;
import com.arturjarosz.task.sharedkernel.model.Money;

import javax.persistence.*;
import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDate;

@SuppressWarnings("java:S2160") // equality is tested on uuid value, no need to override with same code
@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "cost_sequence", allocationSize = 1)
@Table(name = "COST")
public class Cost extends AbstractEntity implements PartialFinancialData {
    @Serial
    private static final long serialVersionUID = 4833869293487851155L;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "CATEGORY", nullable = false)
    @Enumerated(EnumType.STRING)
    private CostCategory category;

    @Column(name = "DATE")
    private LocalDate date;

    @Column(name = "NOTE")
    private String note;

    @Column(name = "PROJECT_FINANCIAL_DATA_ID", insertable = false, updatable = false)
    private Long projectFinancialDataId;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "FINANCIAL_DATA_ID", referencedColumnName = "ID", nullable = false)
    private FinancialData financialData;

    protected Cost() {
        // needed by JPA
    }

    public Cost(String name, BigDecimal value, CostCategory category, LocalDate date, String note, boolean hasInvoice,
            boolean payable) {
        this.name = name;
        this.category = category;
        this.date = date;
        this.note = note;
        this.financialData = new FinancialData(new Money(value), hasInvoice, payable);
    }

    public String getName() {
        return this.name;
    }

    public BigDecimal getValue() {
        return this.financialData.getValue().getValue();
    }

    public void setValue(BigDecimal value) {
        this.financialData.setValue(new Money(value));
    }

    public CostCategory getCategory() {
        return this.category;
    }

    public LocalDate getDate() {
        return this.date;
    }

    public String getNote() {
        return this.note;
    }

    public void updateCost(String name, BigDecimal value, LocalDate date, String note, CostCategory category) {
        this.name = name;
        this.financialData.setValue(new Money(value));
        this.date = date;
        this.note = note;
        this.category = category;
    }

    public Long getProjectFinancialDataId() {
        return this.projectFinancialDataId;
    }

    public void setProjectFinancialDataId(Long projectFinancialDataId) {
        this.projectFinancialDataId = projectFinancialDataId;
    }
}
