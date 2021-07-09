package com.arturjarosz.task.finance.model;

import com.arturjarosz.task.sharedkernel.model.AbstractAggregateRoot;
import com.arturjarosz.task.sharedkernel.model.Money;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "project_financial_data_sequence", allocationSize = 1)
@Table(name = "PROJECT_FINANCIAL_DATA")
public class ProjectFinancialData extends AbstractAggregateRoot {

    private static final long serialVersionUID = 4803569322363900378L;
    @Column(name = "NET_VALUE")
    private Money netValue;

    @Column(name = "GROSS_VALUE")
    private Money grossValue;

    @Column(name = "VAT_TAX")
    private Money vatTax;

    @Column(name = "INCOME_TAX")
    private Money incomeTax;

    @Column(name = "COMMISSIONS_NET_VALUE")
    private Money commissionsNetValue;

    @Column(name = "COMMISSIONS_GROSS_VALUE")
    private Money commissionsGrossValue;

    @Column(name = "COMMISSIONS_VAT_TAX")
    private Money commissionsVatTax;

    @Column(name = "COMMISSIONS_INCOME_TAX")
    private Money commissionsIncomeTax;

    @Column(name = "COSTS_NET_VALUE")
    private Money costsNetValue;

    @Column(name = "COSTS_GROSS_VALUE")
    private Money costsGrossValue;

    @Column(name = "COSTS_VAT_TAX")
    private Money costsVatTax;

    @Column(name = "COSTS_INCOME_TAX")
    private Money costsIncomeTax;

    @Column(name = "TOTAL_NET_VALUE")
    private Money totalNetValue;

    @Column(name = "TOTAL_GROSS_VALUE")
    private Money totalGrossValue;

    @Column(name = "TOTAL_VAT_TAX")
    private Money totalVatTax;

    @Column(name = "TOTAL_INCOME_TAX")
    private Money totalIncomeTax;

    @Column(name = "PROJECT_ID", nullable = false)
    private Long projectId;

    protected ProjectFinancialData() {
        //needed by Hibernate
    }

    public ProjectFinancialData(Long projectId) {
        this.projectId = projectId;
    }

    public Money getNetValue() {
        return this.netValue;
    }

    public void setNetValue(Money netValue) {
        this.netValue = netValue;
    }

    public Money getGrossValue() {
        return this.grossValue;
    }

    public void setGrossValue(Money grossValue) {
        this.grossValue = grossValue;
    }

    public Money getVatTax() {
        return this.vatTax;
    }

    public void setVatTax(Money vatTax) {
        this.vatTax = vatTax;
    }

    public Money getIncomeTax() {
        return this.incomeTax;
    }

    public void setIncomeTax(Money incomeTax) {
        this.incomeTax = incomeTax;
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
