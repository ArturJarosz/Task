package com.arturjarosz.task.finance.model;

import com.arturjarosz.task.finance.application.dto.FinancialValueDto;
import com.arturjarosz.task.sharedkernel.model.AbstractAggregateRoot;
import com.arturjarosz.task.sharedkernel.model.Money;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "project_financial_summary_sequence", allocationSize = 1)
@Table(name = "PROJECT_FINANCIAL_SUMMARY")
public class ProjectFinancialSummary extends AbstractAggregateRoot {
    private static final long serialVersionUID = 4803569322363900378L;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "PROJECT_FINANCIAL_SUMMARY_ID", nullable = false)
    @MapKeyColumn(name = "DATA_TYPE")
    @MapKeyEnumerated(EnumType.STRING)
    private Map<PartialFinancialDataType, ProjectFinancialPartialSummary> partialSummaries;

    @Column(name = "PROJECT_ID", nullable = false)
    private Long projectId;

    protected ProjectFinancialSummary() {
        // needed by JPA
    }

    public ProjectFinancialSummary(Long projectId) {
        this.projectId = projectId;
    }


    public Long getProjectId() {
        return this.projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public void updatePartialData(PartialFinancialDataType type, FinancialValueDto financialValueDto) {
        if (this.partialSummaries == null) {
            this.partialSummaries = new HashMap<>();
        }
        if (this.partialSummaries.containsKey(type)) {
            ProjectFinancialPartialSummary partialSummary = this.partialSummaries.get(type);
            partialSummary.setGrossValue(new Money(financialValueDto.getGrossValue()));
            partialSummary.setNetValue(new Money(financialValueDto.getNetValue()));
            partialSummary.setIncomeTax(new Money(financialValueDto.getIncomeTax()));
            partialSummary.setVatTax(new Money(financialValueDto.getVatTax()));
        } else {
            this.partialSummaries.put(type, new ProjectFinancialPartialSummary(type, financialValueDto));
        }
    }

    public Map<PartialFinancialDataType, ProjectFinancialPartialSummary> getPartialSummaries() {
        return this.partialSummaries;
    }
}
