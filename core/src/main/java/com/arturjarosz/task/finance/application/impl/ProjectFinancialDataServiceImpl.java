package com.arturjarosz.task.finance.application.impl;

import com.arturjarosz.task.finance.application.ProjectFinancialDataService;
import com.arturjarosz.task.finance.application.dto.ProjectFinancialDataDto;
import com.arturjarosz.task.finance.application.dto.TotalProjectFinancialDataDto;
import com.arturjarosz.task.finance.domain.PartialFinancialDataService;
import com.arturjarosz.task.finance.infrastructure.FinancialDataRepository;
import com.arturjarosz.task.finance.infrastructure.ProjectFinancialDataRepository;
import com.arturjarosz.task.finance.model.FinancialData;
import com.arturjarosz.task.finance.model.ProjectFinancialData;
import com.arturjarosz.task.finance.model.dto.SupervisionRatesDto;
import com.arturjarosz.task.finance.model.dto.SupervisionVisitFinancialDto;
import com.arturjarosz.task.finance.query.impl.FinancialDataQueryServiceImpl;
import com.arturjarosz.task.project.application.ProjectValidator;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.model.Money;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

@ApplicationService
public class ProjectFinancialDataServiceImpl implements ProjectFinancialDataService {

    private final ProjectFinancialDataRepository projectFinancialDataRepository;
    private final ProjectValidator projectValidator;
    private final FinancialDataQueryServiceImpl financialDataQueryService;
    private final FinancialDataRepository financialDataRepository;
    private final List<PartialFinancialDataService> partialFinancialDataServices;

    @Autowired
    public ProjectFinancialDataServiceImpl(ProjectFinancialDataRepository projectFinancialDataRepository,
            ProjectValidator projectValidator, FinancialDataQueryServiceImpl financialDataQueryService,
            FinancialDataRepository financialDataRepository,
            List<PartialFinancialDataService> partialFinancialDataServices) {
        this.projectFinancialDataRepository = projectFinancialDataRepository;
        this.projectValidator = projectValidator;
        this.financialDataQueryService = financialDataQueryService;
        this.financialDataRepository = financialDataRepository;
        this.partialFinancialDataServices = partialFinancialDataServices;
    }

    @Override
    public ProjectFinancialData createProjectFinancialData(Long projectId) {
        this.projectValidator.validateProjectExistence(projectId);
        ProjectFinancialData projectFinancialData = new ProjectFinancialData(projectId);
        projectFinancialData = this.projectFinancialDataRepository.save(projectFinancialData);
        return projectFinancialData;
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
    public void recalculateProjectFinancialData(long projectId) {
        ProjectFinancialData projectFinancialData = this.projectFinancialDataRepository.findProjectFinancialDataByProjectId(
                projectId);
        ProjectFinancialDataDto summedUpFinancialData = new ProjectFinancialDataDto();
        for (PartialFinancialDataService partialFinancialDataService : this.partialFinancialDataServices) {
            summedUpFinancialData.addFinancialValues(
                    partialFinancialDataService.providePartialFinancialData(projectFinancialData.getId()));
        }
        this.recalculateTotalProjectValue(summedUpFinancialData);
        projectFinancialData.updateWithPartialData(summedUpFinancialData);
        this.projectFinancialDataRepository.save(projectFinancialData);
    }

    @Override
    public void removeFinancialDataForProject(Long projectId) {
        ProjectFinancialData projectFinancialData = this.projectFinancialDataRepository.findProjectFinancialDataByProjectId(
                projectId);
        this.projectFinancialDataRepository.deleteById(projectFinancialData.getId());
    }

    @Override
    public TotalProjectFinancialDataDto getTotalProjectFinancialData(Long projectId) {
        this.projectValidator.validateProjectExistence(projectId);
        return this.financialDataQueryService.getTotalProjectFinancialData(projectId);
    }

    private void recalculateTotalProjectValue(ProjectFinancialDataDto projectFinancialDataDto) {
        projectFinancialDataDto.getTotalProjectValue().addValues(projectFinancialDataDto.getSuppliesValue());
        projectFinancialDataDto.getTotalProjectValue().addValues(projectFinancialDataDto.getSupervisionValue());
        projectFinancialDataDto.getTotalProjectValue().addValues(projectFinancialDataDto.getContractorJobsValue());
        projectFinancialDataDto.getTotalProjectValue().subtractValues(projectFinancialDataDto.getCostsValue());
    }
}
