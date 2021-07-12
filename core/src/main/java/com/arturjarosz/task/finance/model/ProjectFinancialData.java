package com.arturjarosz.task.finance.model;

import com.arturjarosz.task.sharedkernel.model.AbstractAggregateRoot;
import com.arturjarosz.task.sharedkernel.model.Money;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "project_financial_data_sequence", allocationSize = 1)
@Table(name = "PROJECT_FINANCIAL_DATA")
public class ProjectFinancialData extends AbstractAggregateRoot {
    private static final long serialVersionUID = 4803569322363900378L;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "BASE_NET_VALUE", nullable = false))
    private Money baseNetValue;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "BASE_GROSS_VALUE", nullable = false))
    private Money baseGrossValue;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "BASE_VAT_TAX", nullable = false))
    private Money baseVatTax;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "BASE_INCOME_TAX", nullable = false))
    private Money baseIncomeTax;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "COMMISSIONS_NET_VALUE", nullable = false))
    private Money commissionsNetValue;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "COMMISSIONS_GROSS_VALUE", nullable = false))
    private Money commissionsGrossValue;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "COMMISSIONS_VAT_TAX", nullable = false))
    private Money commissionsVatTax;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "COMMISSIONS_INCOME_TAX", nullable = false))
    private Money commissionsIncomeTax;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "COSTS_NET_VALUE", nullable = false))
    private Money costsNetValue;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "COSTS_GROSS_VALUE", nullable = false))
    private Money costsGrossValue;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "COSTS_VAT_TAX", nullable = false))
    private Money costsVatTax;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "COSTS_INCOME_TAX", nullable = false))
    private Money costsIncomeTax;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "TOTAL_NET_VALUE", nullable = false))
    private Money totalNetValue;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "TOTAL_GROSS_VALUE", nullable = false))
    private Money totalGrossValue;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "TOTAL_VAT_TAX", nullable = false))
    private Money totalVatTax;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "TOTAL_INCOME_TAX", nullable = false))
    private Money totalIncomeTax;

    @Column(name = "PROJECT_ID", nullable = false)
    private Long projectId;

    protected ProjectFinancialData() {
        //needed by Hibernate
    }

    public ProjectFinancialData(Long projectId) {
        this.projectId = projectId;
    }

    public void initiateProjectFinancialData() {
        Money zeroValue = new Money(0);
        this.baseGrossValue = zeroValue;
        this.baseNetValue = zeroValue;
        this.baseIncomeTax = zeroValue;
        this.baseVatTax = zeroValue;
        this.commissionsGrossValue = zeroValue;
        this.commissionsNetValue = zeroValue;
        this.commissionsIncomeTax = zeroValue;
        this.commissionsVatTax = zeroValue;
        this.costsGrossValue = zeroValue;
        this.costsNetValue = zeroValue;
        this.costsIncomeTax = zeroValue;
        this.costsVatTax = zeroValue;
        this.totalGrossValue = zeroValue;
        this.totalNetValue = zeroValue;
        this.totalIncomeTax = zeroValue;
        this.totalVatTax = zeroValue;

    }

    public Money getBaseNetValue() {
        return this.baseNetValue;
    }

    public void setBaseNetValue(Money netValue) {
        this.baseNetValue = netValue;
    }

    public Money getBaseGrossValue() {
        return this.baseGrossValue;
    }

    public void setBaseGrossValue(Money grossValue) {
        this.baseGrossValue = grossValue;
    }

    public Money getBaseVatTax() {
        return this.baseVatTax;
    }

    public void setBaseVatTax(Money vatTax) {
        this.baseVatTax = vatTax;
    }

    public Money getBaseIncomeTax() {
        return this.baseIncomeTax;
    }

    public void setBaseIncomeTax(Money incomeTax) {
        this.baseIncomeTax = incomeTax;
    }

    public Money getCommissionsNetValue() {
        return this.commissionsNetValue;
    }

    public void setCommissionsNetValue(Money commissionsNetValue) {
        this.commissionsNetValue = commissionsNetValue;
    }

    public Money getCommissionsGrossValue() {
        return this.commissionsGrossValue;
    }

    public void setCommissionsGrossValue(Money commissionsGrossValue) {
        this.commissionsGrossValue = commissionsGrossValue;
    }

    public Money getCommissionsVatTax() {
        return this.commissionsVatTax;
    }

    public void setCommissionsVatTax(Money commissionsVatTax) {
        this.commissionsVatTax = commissionsVatTax;
    }

    public Money getCommissionsIncomeTax() {
        return this.commissionsIncomeTax;
    }

    public void setCommissionsIncomeTax(Money commissionsIncomeTax) {
        this.commissionsIncomeTax = commissionsIncomeTax;
    }

    public Money getCostsNetValue() {
        return this.costsNetValue;
    }

    public void setCostsNetValue(Money costsNetValue) {
        this.costsNetValue = costsNetValue;
    }

    public Money getCostsGrossValue() {
        return this.costsGrossValue;
    }

    public void setCostsGrossValue(Money costsGrossValue) {
        this.costsGrossValue = costsGrossValue;
    }

    public Money getCostsVatTax() {
        return this.costsVatTax;
    }

    public void setCostsVatTax(Money costsVatTax) {
        this.costsVatTax = costsVatTax;
    }

    public Money getCostsIncomeTax() {
        return this.costsIncomeTax;
    }

    public void setCostsIncomeTax(Money costsIncomeTax) {
        this.costsIncomeTax = costsIncomeTax;
    }

    public Money getTotalNetValue() {
        return this.totalNetValue;
    }

    public void setTotalNetValue(Money totalNetValue) {
        this.totalNetValue = totalNetValue;
    }

    public Money getTotalGrossValue() {
        return this.totalGrossValue;
    }

    public void setTotalGrossValue(Money totalGrossValue) {
        this.totalGrossValue = totalGrossValue;
    }

    public Money getTotalVatTax() {
        return this.totalVatTax;
    }

    public void setTotalVatTax(Money totalVatTax) {
        this.totalVatTax = totalVatTax;
    }

    public Money getTotalIncomeTax() {
        return this.totalIncomeTax;
    }

    public void setTotalIncomeTax(Money totalIncomeTax) {
        this.totalIncomeTax = totalIncomeTax;
    }

    public Long getProjectId() {
        return this.projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
}
