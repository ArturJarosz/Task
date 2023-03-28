package com.arturjarosz.task.finance.application.dto;

import lombok.Data;

@Data
public class ProjectFinancialSummaryDto {

    private FinancialValueDto baseProjectValue;
    private FinancialValueDto totalProjectValue;
    private FinancialValueDto costsValue;
    private FinancialValueDto supervisionValue;
    private FinancialValueDto suppliesValue;
    private FinancialValueDto contractorJobsValue;

    public ProjectFinancialSummaryDto() {
        this.baseProjectValue = new FinancialValueDto();
        this.costsValue = new FinancialValueDto();
        this.contractorJobsValue = new FinancialValueDto();
        this.supervisionValue = new FinancialValueDto();
        this.suppliesValue = new FinancialValueDto();
        this.totalProjectValue = new FinancialValueDto();
    }

    public void addFinancialValues(ProjectFinancialSummaryDto financialValueDto) {
        this.baseProjectValue.addValues(financialValueDto.getBaseProjectValue());
        this.costsValue.addValues(financialValueDto.getCostsValue());
        this.contractorJobsValue.addValues(financialValueDto.getContractorJobsValue());
        this.supervisionValue.addValues(financialValueDto.getSupervisionValue());
        this.suppliesValue.addValues(financialValueDto.getSuppliesValue());
        this.totalProjectValue.addValues(financialValueDto.getTotalProjectValue());
    }
}
