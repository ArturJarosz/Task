package com.arturjarosz.task.finance.query;

import com.arturjarosz.task.finance.application.dto.ContractorJobDto;
import com.arturjarosz.task.finance.application.dto.InstallmentDto;
import com.arturjarosz.task.finance.application.dto.SupplyDto;
import com.arturjarosz.task.finance.application.dto.TotalProjectFinancialSummaryDto;
import com.arturjarosz.task.finance.domain.dto.FinancialDataDto;
import com.arturjarosz.task.finance.model.dto.SupervisionRatesDto;
import com.arturjarosz.task.finance.model.dto.SupervisionVisitFinancialDto;
import com.arturjarosz.task.project.application.dto.CostDto;

import java.util.List;

public interface FinancialDataQueryService {

    SupervisionRatesDto getSupervisionRatesDto(long supervisionId);

    List<SupervisionVisitFinancialDto> getVisitsFinancialDto(Long supervisionId);

    List<FinancialDataDto> getCostsFinancialData(long projectId);

    List<FinancialDataDto> getSuppliesFinancialData(long projectId);

    List<FinancialDataDto> getContractorsJobsFinancialData(long projectId);

    FinancialDataDto getSupervisionFinancialData(long projectId);

    List<FinancialDataDto> getInstallmentsFinancialData(long projectId);

    TotalProjectFinancialSummaryDto getTotalProjectFinancialSummary(long projectId);

    Boolean doesCostExistByCostId(long costId);

    CostDto getCostById(long costId);

    List<CostDto> getCostsByProjectId(long projectId);

    List<InstallmentDto> getInstallmentsByProjectId(long projectId);

    ContractorJobDto getContractorJobById(long contractorJobId);

    boolean doesInstallmentExistsByInstallmentId(long installmentId);

    boolean doesSupplyForProjectExists(long projectId, long supplyId);

    SupplyDto getSupplyById(Long supplyId);

    List<SupplyDto> getSuppliesForProject(Long projectId);
}
