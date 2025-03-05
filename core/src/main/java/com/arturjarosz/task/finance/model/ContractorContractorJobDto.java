package com.arturjarosz.task.finance.model;

import com.arturjarosz.task.dto.ContractorJobDto;
import com.arturjarosz.task.finance.domain.dto.FinancialDataDto;

public record ContractorContractorJobDto(ContractorJobDto contractorJob, FinancialDataDto financialData) {
}
