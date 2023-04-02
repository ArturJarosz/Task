package com.arturjarosz.task.finance.model;


import com.arturjarosz.task.finance.application.dto.FinancialValueDto;
import com.arturjarosz.task.sharedkernel.model.AbstractEntity;
import com.arturjarosz.task.sharedkernel.model.Money;
import lombok.Data;

import javax.persistence.*;
import java.io.Serial;

@Data
@Entity
@Table(name = "PROJECT_FINANCIAL_PARTIAL_SUMMARY")
@SequenceGenerator(name = "sequence_generator", sequenceName = "project_financial_partial_summary_sequence", allocationSize = 1)
public class ProjectFinancialPartialSummary extends AbstractEntity {
    @Serial
    private static final long serialVersionUID = -2212099659638035600L;

    @Enumerated(EnumType.STRING)
    @Column(name = "DATA_TYPE", nullable = false)
    PartialFinancialDataType dataType;

    @Embedded
    @AttributeOverride(name = "value",
            column = @Column(name = "NET_VALUE", nullable = false, precision = 5, scale = 2))
    private Money netValue;

    @Embedded
    @AttributeOverride(name = "value",
            column = @Column(name = "GROSS_VALUE", nullable = false, precision = 5, scale = 2))
    private Money grossValue;

    @Embedded
    @AttributeOverride(name = "value",
            column = @Column(name = "VAT_TAX", nullable = false, precision = 5, scale = 2))
    private Money vatTax;

    @Embedded
    @AttributeOverride(name = "value",
            column = @Column(name = "INCOME_TAX", nullable = false, precision = 5, scale = 2))
    private Money incomeTax;

    @Column(name = "PROJECT_FINANCIAL_SUMMARY_ID", insertable = false, updatable = false)
    private Long projectFinancialSummaryId;

    protected ProjectFinancialPartialSummary() {
        // needed by JPA
    }

    public ProjectFinancialPartialSummary(PartialFinancialDataType dataType, FinancialValueDto financialValueDto) {
        this.dataType = dataType;
        this.netValue = new Money(financialValueDto.getNetValue());
        this.grossValue = new Money(financialValueDto.getGrossValue());
        this.vatTax = new Money(financialValueDto.getVatTax());
        this.incomeTax = new Money(financialValueDto.getIncomeTax());
    }

    public Long getProjectFinancialSummaryId() {
        return this.projectFinancialSummaryId;
    }

    public void setProjectFinancialSummaryId(Long projectFinancialSummaryId) {
        this.projectFinancialSummaryId = projectFinancialSummaryId;
    }
}
