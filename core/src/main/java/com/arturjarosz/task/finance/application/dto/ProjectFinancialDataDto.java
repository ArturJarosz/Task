package com.arturjarosz.task.finance.application.dto;

public class ProjectFinancialDataDto {
    private FinancialValueDto totalProjectValue;
    private FinancialValueDto costsValue;
    private FinancialValueDto supervisionValue;
    private FinancialValueDto suppliesValue;
    private FinancialValueDto contractorJobsValue;

    public ProjectFinancialDataDto() {
    }

    public FinancialValueDto getTotalProjectValue() {
        return this.totalProjectValue;
    }

    public void setTotalProjectValue(FinancialValueDto totalProjectValue) {
        this.totalProjectValue = totalProjectValue;
    }

    public FinancialValueDto getCostsValue() {
        return this.costsValue;
    }

    public void setCostsValue(FinancialValueDto costsValue) {
        this.costsValue = costsValue;
    }

    public FinancialValueDto getSupervisionValue() {
        return this.supervisionValue;
    }

    public void setSupervisionValue(FinancialValueDto supervisionValue) {
        this.supervisionValue = supervisionValue;
    }

    public FinancialValueDto getSuppliesValue() {
        return this.suppliesValue;
    }

    public void setSuppliesValue(FinancialValueDto suppliesValue) {
        this.suppliesValue = suppliesValue;
    }

    public FinancialValueDto getContractorJobsValue() {
        return this.contractorJobsValue;
    }

    public void setContractorJobsValue(FinancialValueDto contractorJobsValue) {
        this.contractorJobsValue = contractorJobsValue;
    }
}
