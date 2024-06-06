package com.arturjarosz.task.finance.model;


import com.arturjarosz.task.finance.application.dto.FinancialValueDto;
import com.arturjarosz.task.sharedkernel.model.AbstractEntity;
import com.arturjarosz.task.sharedkernel.model.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Entity
@Getter
@Table(name = "PROJECT_FINANCIAL_PARTIAL_DATA")
@SequenceGenerator(name = "sequence_generator", sequenceName = "project_financial_partial_data_sequence", allocationSize = 1)
public class ProjectFinancialPartialData extends AbstractEntity {
    @Serial
    private static final long serialVersionUID = -2212099659638035600L;

    @Enumerated(EnumType.STRING)
    @Column(name = "DATA_TYPE", nullable = false)
    PartialFinancialDataType dataType;

    @Setter
    @Embedded
    @AttributeOverride(name = "value",
            column = @Column(name = "NET_VALUE", nullable = false, precision = 5, scale = 2))
    private Money netValue;

    @Setter
    @Embedded
    @AttributeOverride(name = "value",
            column = @Column(name = "GROSS_VALUE", nullable = false, precision = 5, scale = 2))
    private Money grossValue;

    @Setter
    @Embedded
    @AttributeOverride(name = "value",
            column = @Column(name = "VAT_TAX", nullable = false, precision = 5, scale = 2))
    private Money vatTax;

    @Setter
    @Embedded
    @AttributeOverride(name = "value",
            column = @Column(name = "INCOME_TAX", nullable = false, precision = 5, scale = 2))
    private Money incomeTax;

    @Column(name = "PROJECT_FINANCIAL_DATA_ID", insertable = false, updatable = false)
    private Long projectFinancialDataId;

    protected ProjectFinancialPartialData() {
        // needed by JPA
    }

    public ProjectFinancialPartialData(PartialFinancialDataType dataType, FinancialValueDto financialValueDto) {
        this.dataType = dataType;
        this.netValue = new Money(financialValueDto.getNetValue());
        this.grossValue = new Money(financialValueDto.getGrossValue());
        this.vatTax = new Money(financialValueDto.getVatTax());
        this.incomeTax = new Money(financialValueDto.getIncomeTax());
    }
}
