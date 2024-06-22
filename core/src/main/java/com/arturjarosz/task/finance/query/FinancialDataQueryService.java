package com.arturjarosz.task.finance.query;

import com.arturjarosz.task.dto.ContractorJobDto;
import com.arturjarosz.task.dto.CostDto;
import com.arturjarosz.task.dto.InstallmentDto;
import com.arturjarosz.task.dto.SupplyDto;
import com.arturjarosz.task.dto.TotalProjectFinancialSummaryDto;
import com.arturjarosz.task.finance.domain.dto.FinancialDataDto;
import com.arturjarosz.task.finance.model.ProjectFinancialPartialData;
import com.arturjarosz.task.finance.model.dto.SupervisionRatesDto;
import com.arturjarosz.task.finance.model.dto.SupervisionVisitFinancialDto;

import java.util.List;

public interface FinancialDataQueryService {

    SupervisionRatesDto getSupervisionRatesDto(long supervisionId);

    List<SupervisionVisitFinancialDto> getVisitsFinancialDto(long supervisionId);

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

    ContractorJobDto getContractorJobById(long contractorJobId, long projectId);

    boolean doesInstallmentExistsByInstallmentId(long installmentId);

    boolean doesSupplyForProjectExists(long projectId, long supplyId);

    SupplyDto getSupplyById(long supplyId, long projectId);

    List<SupplyDto> getSuppliesForProject(long projectId);

    ProjectFinancialPartialData getInstallmentDataForProject(long projectId);
}
