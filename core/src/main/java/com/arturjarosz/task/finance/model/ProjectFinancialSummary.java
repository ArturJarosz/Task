package com.arturjarosz.task.finance.model;

import com.arturjarosz.task.finance.application.dto.ProjectFinancialSummaryDto;
import com.arturjarosz.task.sharedkernel.model.AbstractAggregateRoot;
import com.arturjarosz.task.sharedkernel.model.Money;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "project_financial_summary_sequence", allocationSize = 1)
@Table(name = "PROJECT_FINANCIAL_SUMMARY")
public class ProjectFinancialSummary extends AbstractAggregateRoot {
    private static final long serialVersionUID = 4803569322363900378L;

    @Embedded
    @AttributeOverride(name = "value",
            column = @Column(name = "BASE_NET_VALUE", nullable = false, precision = 5, scale = 2))
    private Money baseNetValue;

    @Embedded
    @AttributeOverride(name = "value",
            column = @Column(name = "BASE_GROSS_VALUE", nullable = false, precision = 5, scale = 2))
    private Money baseGrossValue;

    @Embedded
    @AttributeOverride(name = "value",
            column = @Column(name = "BASE_VAT_TAX", nullable = false, precision = 5, scale = 2))
    private Money baseVatTax;

    @Embedded
    @AttributeOverride(name = "value",
            column = @Column(name = "BASE_INCOME_TAX", nullable = false, precision = 5, scale = 2))
    private Money baseIncomeTax;

    @Embedded
    @AttributeOverride(name = "value",
            column = @Column(name = "CONTRACTORS_JOBS_NET_VALUE", nullable = false, precision = 5, scale = 2))
    private Money contractorsJobsNetValue;

    @Embedded
    @AttributeOverride(name = "value",
            column = @Column(name = "CONTRACTORS_JOBS_GROSS_VALUE", nullable = false, precision = 5, scale = 2))
    private Money contractorsJobsGrossValue;

    @Embedded
    @AttributeOverride(name = "value",
            column = @Column(name = "CONTRACTORS_JOBS_VAT_TAX", nullable = false, precision = 5, scale = 2))
    private Money contractorsJobsVatTax;

    @Embedded
    @AttributeOverride(name = "value",
            column = @Column(name = "CONTRACTORS_JOBS_INCOME_TAX", nullable = false, precision = 5, scale = 2))
    private Money contractorsJobsIncomeTax;

    @Embedded
    @AttributeOverride(name = "value",
            column = @Column(name = "COSTS_NET_VALUE", nullable = false, precision = 5, scale = 2))
    private Money costsNetValue;

    @Embedded
    @AttributeOverride(name = "value",
            column = @Column(name = "COSTS_GROSS_VALUE", nullable = false, precision = 5, scale = 2))
    private Money costsGrossValue;

    @Embedded
    @AttributeOverride(name = "value",
            column = @Column(name = "COSTS_VAT_TAX", nullable = false, precision = 5, scale = 2))
    private Money costsVatTax;

    @Embedded
    @AttributeOverride(name = "value",
            column = @Column(name = "COSTS_INCOME_TAX", nullable = false, precision = 5, scale = 2))
    private Money costsIncomeTax;

    @Embedded
    @AttributeOverride(name = "value",
            column = @Column(name = "SUPPLIES_NET_VALUE", nullable = false, precision = 5, scale = 2))
    private Money suppliesNetValue;

    @Embedded
    @AttributeOverride(name = "value",
            column = @Column(name = "SUPPLIES_GROSS_VALUE", nullable = false, precision = 5, scale = 2))
    private Money suppliesGrossValue;

    @Embedded
    @AttributeOverride(name = "value",
            column = @Column(name = "SUPPLIES_VAT_TAX", nullable = false, precision = 5, scale = 2))
    private Money suppliesVatTax;

    @Embedded
    @AttributeOverride(name = "value",
            column = @Column(name = "SUPPLIES_INCOME_TAX", nullable = false, precision = 5, scale = 2))
    private Money suppliesIncomeTax;

    @Embedded
    @AttributeOverride(name = "value",
            column = @Column(name = "TOTAL_NET_VALUE", nullable = false, precision = 5, scale = 2))
    private Money totalNetValue;

    @Embedded
    @AttributeOverride(name = "value",
            column = @Column(name = "TOTAL_GROSS_VALUE", nullable = false, precision = 5, scale = 2))
    private Money totalGrossValue;

    @Embedded
    @AttributeOverride(name = "value",
            column = @Column(name = "TOTAL_VAT_TAX", nullable = false, precision = 5, scale = 2))
    private Money totalVatTax;

    @Embedded
    @AttributeOverride(name = "value",
            column = @Column(name = "TOTAL_INCOME_TAX", nullable = false, precision = 5, scale = 2))
    private Money totalIncomeTax;

    @Column(name = "PROJECT_ID", nullable = false)
    private Long projectId;

    protected ProjectFinancialSummary() {
        // needed by JPA
    }

    public ProjectFinancialSummary(Long projectId) {
        this.projectId = projectId;
        this.initiateProjectFinancialSummary();
    }

    public void initiateProjectFinancialSummary() {
        this.baseGrossValue = new Money(0);
        this.baseNetValue = new Money(0);
        this.baseIncomeTax = new Money(0);
        this.baseVatTax = new Money(0);
        this.contractorsJobsGrossValue = new Money(0);
        this.contractorsJobsNetValue = new Money(0);
        this.contractorsJobsIncomeTax = new Money(0);
        this.contractorsJobsVatTax = new Money(0);
        this.costsGrossValue = new Money(0);
        this.costsNetValue = new Money(0);
        this.costsIncomeTax = new Money(0);
        this.costsVatTax = new Money(0);
        this.suppliesGrossValue = new Money(0);
        this.suppliesNetValue = new Money(0);
        this.suppliesVatTax = new Money(0);
        this.suppliesIncomeTax = new Money(0);
        this.totalGrossValue = new Money(0);
        this.totalNetValue = new Money(0);
        this.totalIncomeTax = new Money(0);
        this.totalVatTax = new Money(0);

    }

    public Money getBaseNetValue() {
        return this.baseNetValue;
    }

    public Money getBaseGrossValue() {
        return this.baseGrossValue;
    }

    public Money getBaseVatTax() {
        return this.baseVatTax;
    }

    public Money getBaseIncomeTax() {
        return this.baseIncomeTax;
    }

    public Money getContractorsJobsNetValue() {
        return this.contractorsJobsNetValue;
    }

    public Money getContractorsJobsGrossValue() {
        return this.contractorsJobsGrossValue;
    }

    public Money getContractorsJobsVatTax() {
        return this.contractorsJobsVatTax;
    }

    public Money getContractorsJobsIncomeTax() {
        return this.contractorsJobsIncomeTax;
    }


    public Money getCostsNetValue() {
        return this.costsNetValue;
    }

    public Money getCostsGrossValue() {
        return this.costsGrossValue;
    }

    public Money getCostsVatTax() {
        return this.costsVatTax;
    }

    public Money getCostsIncomeTax() {
        return this.costsIncomeTax;
    }


    public Money getTotalNetValue() {
        return this.totalNetValue;
    }


    public Money getTotalGrossValue() {
        return this.totalGrossValue;
    }


    public Money getTotalVatTax() {
        return this.totalVatTax;
    }


    public Money getTotalIncomeTax() {
        return this.totalIncomeTax;
    }


    public Money getSuppliesNetValue() {
        return this.suppliesNetValue;
    }


    public Money getSuppliesGrossValue() {
        return this.suppliesGrossValue;
    }


    public Money getSuppliesVatTax() {
        return this.suppliesVatTax;
    }


    public Money getSuppliesIncomeTax() {
        return this.suppliesIncomeTax;
    }


    public Long getProjectId() {
        return this.projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    // TODO: figure out the better way to do it instead of ifs
    public void updateWithPartialData(ProjectFinancialSummaryDto projectFinancialSummaryDto) {
        if (projectFinancialSummaryDto.getCostsValue() != null) {
            this.costsGrossValue.setValue(projectFinancialSummaryDto.getCostsValue().getGrossValue());
            this.costsNetValue.setValue(projectFinancialSummaryDto.getCostsValue().getNetValue());
            this.costsIncomeTax.setValue(projectFinancialSummaryDto.getCostsValue().getIncomeTax());
            this.costsVatTax.setValue(projectFinancialSummaryDto.getCostsValue().getVatTax());
        }

        if (projectFinancialSummaryDto.getContractorJobsValue() != null) {
            this.contractorsJobsGrossValue.setValue(
                    projectFinancialSummaryDto.getContractorJobsValue().getGrossValue());
            this.contractorsJobsNetValue.setValue(projectFinancialSummaryDto.getContractorJobsValue().getNetValue());
            this.contractorsJobsIncomeTax.setValue(projectFinancialSummaryDto.getContractorJobsValue().getIncomeTax());
            this.contractorsJobsVatTax.setValue(projectFinancialSummaryDto.getContractorJobsValue().getVatTax());
        }

        if (projectFinancialSummaryDto.getSuppliesValue() != null) {
            this.suppliesGrossValue.setValue(projectFinancialSummaryDto.getSuppliesValue().getGrossValue());
            this.suppliesNetValue.setValue(projectFinancialSummaryDto.getSuppliesValue().getNetValue());
            this.suppliesIncomeTax.setValue(projectFinancialSummaryDto.getSuppliesValue().getIncomeTax());
            this.suppliesVatTax.setValue(projectFinancialSummaryDto.getSuppliesValue().getVatTax());
        }

        if (projectFinancialSummaryDto.getTotalProjectValue() != null) {
            this.totalGrossValue.setValue(projectFinancialSummaryDto.getTotalProjectValue().getGrossValue());
            this.totalNetValue.setValue(projectFinancialSummaryDto.getTotalProjectValue().getNetValue());
            this.totalIncomeTax.setValue(projectFinancialSummaryDto.getTotalProjectValue().getIncomeTax());
            this.totalVatTax.setValue(projectFinancialSummaryDto.getTotalProjectValue().getVatTax());
        }
    }

}
