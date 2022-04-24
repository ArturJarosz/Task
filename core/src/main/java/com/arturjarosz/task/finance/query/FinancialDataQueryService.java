package com.arturjarosz.task.finance.query;

import com.arturjarosz.task.finance.application.dto.TotalProjectFinancialDataDto;
import com.arturjarosz.task.finance.domain.dto.FinancialDataDto;
import com.arturjarosz.task.finance.model.dto.SupervisionRatesDto;
import com.arturjarosz.task.finance.model.dto.SupervisionVisitFinancialDto;

import java.util.List;

public interface FinancialDataQueryService {

    SupervisionRatesDto getSupervisionRatesDto(long supervisionId);

    List<SupervisionVisitFinancialDto> getVisitsFinancialDto(Long supervisionId);

    List<FinancialDataDto> getCostsFinancialData(long projectId);

    List<FinancialDataDto> getSuppliesFinancialData(long projectId);

    List<FinancialDataDto> getContractorsJobsFinancialData(long projectId);

    FinancialDataDto getSupervisionFinancialData(long projectId);

    List<FinancialDataDto> getInstallmentsFinancialData(long projectId);

    TotalProjectFinancialDataDto getTotalProjectFinancialData(long projectId);
}
