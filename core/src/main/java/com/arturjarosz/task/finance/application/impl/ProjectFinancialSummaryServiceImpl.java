package com.arturjarosz.task.finance.application.impl;

import com.arturjarosz.task.finance.application.ProjectFinancialSummaryService;
import com.arturjarosz.task.finance.application.dto.ProjectFinancialSummaryDto;
import com.arturjarosz.task.finance.application.dto.TotalProjectFinancialSummaryDto;
import com.arturjarosz.task.finance.domain.PartialFinancialDataService;
import com.arturjarosz.task.finance.infrastructure.FinancialDataRepository;
import com.arturjarosz.task.finance.infrastructure.ProjectFinancialSummaryRepository;
import com.arturjarosz.task.finance.model.FinancialData;
import com.arturjarosz.task.finance.model.ProjectFinancialSummary;
import com.arturjarosz.task.finance.model.dto.SupervisionRatesDto;
import com.arturjarosz.task.finance.model.dto.SupervisionVisitFinancialDto;
import com.arturjarosz.task.finance.query.impl.FinancialDataQueryServiceImpl;
import com.arturjarosz.task.project.application.ProjectValidator;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.model.Money;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@RequiredArgsConstructor
@ApplicationService
public class ProjectFinancialSummaryServiceImpl implements ProjectFinancialSummaryService {

    @NonNull
    private final ProjectFinancialSummaryRepository projectFinancialSummaryRepository;
    @NonNull
    private final ProjectValidator projectValidator;
    @NonNull
    private final FinancialDataQueryServiceImpl financialDataQueryService;
    @NonNull
    private final FinancialDataRepository financialDataRepository;
    @NonNull
    private final List<PartialFinancialDataService> partialFinancialDataServices;

    @Override
    public ProjectFinancialSummary createProjectFinancialSummary(Long projectId) {
        this.projectValidator.validateProjectExistence(projectId);
        ProjectFinancialSummary projectFinancialSummary = new ProjectFinancialSummary(projectId);
        projectFinancialSummary = this.projectFinancialSummaryRepository.save(projectFinancialSummary);
        return projectFinancialSummary;
    }

    @Override
    public void recalculateSupervision(Long supervisionId, Long supervisionFinancialDataId) {
        SupervisionRatesDto supervisionRatesDto = this.financialDataQueryService.getSupervisionRatesDto(supervisionId);
        List<SupervisionVisitFinancialDto> supervisionVisitFinancialDtos = this.financialDataQueryService.getVisitsFinancialDto(
                supervisionId);
        FinancialData financialData = this.financialDataRepository.getById(supervisionFinancialDataId);

        BigDecimal value = new BigDecimal("0");
        value = value.add(BigDecimal.valueOf(supervisionRatesDto.getBaseNetRate().doubleValue()));

        if (supervisionVisitFinancialDtos != null) {
            // Adding hours value and rate per visit
            for (SupervisionVisitFinancialDto supervisionVisit : supervisionVisitFinancialDtos) {
                if (supervisionVisit.isPayable()) {
                    BigDecimal hoursValue = BigDecimal.valueOf(
                            supervisionVisit.getHoursCount() * supervisionRatesDto.getHourlyNetRate().doubleValue());
                    value = value.add(hoursValue);
                    value = value.add(supervisionRatesDto.getVisitNetRate());
                }
            }
        }

        financialData.setValue(new Money(value));
        this.financialDataRepository.save(financialData);
    }

    @Override
    public void recalculateProjectFinancialSummary(long projectId) {
        ProjectFinancialSummary projectFinancialSummary = this.projectFinancialSummaryRepository.findProjectFinancialSummaryByProjectId(
                projectId);
        ProjectFinancialSummaryDto summedUpFinancialData = new ProjectFinancialSummaryDto();
        for (PartialFinancialDataService partialFinancialDataService : this.partialFinancialDataServices) {
            summedUpFinancialData.addFinancialValues(
                    partialFinancialDataService.providePartialFinancialData(projectFinancialSummary.getId()));
        }
        this.recalculateTotalProjectValue(summedUpFinancialData);
        projectFinancialSummary.updateWithPartialData(summedUpFinancialData);
        this.projectFinancialSummaryRepository.save(projectFinancialSummary);
    }

    @Override
    public void removeFinancialSummaryForProject(Long projectId) {
        ProjectFinancialSummary projectFinancialSummary = this.projectFinancialSummaryRepository.findProjectFinancialSummaryByProjectId(
                projectId);
        this.projectFinancialSummaryRepository.deleteById(projectFinancialSummary.getId());
    }

    @Override
    public TotalProjectFinancialSummaryDto getTotalProjectFinancialSummary(Long projectId) {
        this.projectValidator.validateProjectExistence(projectId);
        return this.financialDataQueryService.getTotalProjectFinancialSummary(projectId);
    }

    private void recalculateTotalProjectValue(ProjectFinancialSummaryDto projectFinancialSummaryDto) {
        projectFinancialSummaryDto.getTotalProjectValue().addValues(projectFinancialSummaryDto.getSuppliesValue());
        projectFinancialSummaryDto.getTotalProjectValue().addValues(projectFinancialSummaryDto.getSupervisionValue());
        projectFinancialSummaryDto.getTotalProjectValue().addValues(projectFinancialSummaryDto.getContractorJobsValue());
        projectFinancialSummaryDto.getTotalProjectValue().subtractValues(projectFinancialSummaryDto.getCostsValue());
    }
}
