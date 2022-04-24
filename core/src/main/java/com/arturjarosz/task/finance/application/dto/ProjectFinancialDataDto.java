package com.arturjarosz.task.finance.application.dto;

public class ProjectFinancialDataDto {

    private FinancialValueDto baseProjectValue;
    private FinancialValueDto totalProjectValue;
    private FinancialValueDto costsValue;
    private FinancialValueDto supervisionValue;
    private FinancialValueDto suppliesValue;
    private FinancialValueDto contractorJobsValue;

    public ProjectFinancialDataDto() {
        this.baseProjectValue = new FinancialValueDto();
        this.costsValue = new FinancialValueDto();
        this.contractorJobsValue = new FinancialValueDto();
        this.supervisionValue = new FinancialValueDto();
        this.suppliesValue = new FinancialValueDto();
        this.totalProjectValue = new FinancialValueDto();
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

    public FinancialValueDto getBaseProjectValue() {
        return this.baseProjectValue;
    }

    public void setBaseProjectValue(FinancialValueDto baseProjectValue) {
        this.baseProjectValue = baseProjectValue;
    }

    public void addFinancialValues(ProjectFinancialDataDto financialValueDto) {
        this.baseProjectValue.addValues(financialValueDto.getBaseProjectValue());
        this.costsValue.addValues(financialValueDto.getCostsValue());
        this.contractorJobsValue.addValues(financialValueDto.getContractorJobsValue());
        this.supervisionValue.addValues(financialValueDto.getSupervisionValue());
        this.suppliesValue.addValues(financialValueDto.getSuppliesValue());
        this.totalProjectValue.addValues(financialValueDto.getTotalProjectValue());
    }
}
