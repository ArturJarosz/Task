package com.arturjarosz.task.finance.application.impl;

import com.arturjarosz.task.finance.application.ProjectFinancialSummaryService;
import com.arturjarosz.task.finance.application.dto.FinancialValueDto;
import com.arturjarosz.task.finance.application.dto.TotalProjectFinancialSummaryDto;
import com.arturjarosz.task.finance.domain.PartialFinancialDataService;
import com.arturjarosz.task.finance.domain.SummationStrategy;
import com.arturjarosz.task.finance.infrastructure.FinancialDataRepository;
import com.arturjarosz.task.finance.infrastructure.ProjectFinancialSummaryRepository;
import com.arturjarosz.task.finance.model.FinancialData;
import com.arturjarosz.task.finance.model.PartialFinancialData;
import com.arturjarosz.task.finance.model.PartialFinancialDataType;
import com.arturjarosz.task.finance.model.ProjectFinancialSummary;
import com.arturjarosz.task.finance.model.dto.SupervisionRatesDto;
import com.arturjarosz.task.finance.model.dto.SupervisionVisitFinancialDto;
import com.arturjarosz.task.finance.query.impl.FinancialDataQueryServiceImpl;
import com.arturjarosz.task.project.application.ProjectValidator;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.model.Money;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Transactional
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
    private final Map<PartialFinancialDataType, PartialFinancialDataService> typeToPartialFinancialDataServices;

    @Autowired
    public ProjectFinancialSummaryServiceImpl(ProjectFinancialSummaryRepository projectFinancialSummaryRepository,
            ProjectValidator projectValidator, FinancialDataQueryServiceImpl financialDataQueryService,
            FinancialDataRepository financialDataRepository,
            List<PartialFinancialDataService> partialFinancialDataServices) {
        this.projectFinancialSummaryRepository = projectFinancialSummaryRepository;
        this.projectValidator = projectValidator;
        this.financialDataQueryService = financialDataQueryService;
        this.financialDataRepository = financialDataRepository;
        this.typeToPartialFinancialDataServices = partialFinancialDataServices.stream()
                .collect(Collectors.toMap(PartialFinancialDataService::getType, Function.identity()));
    }

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
        FinancialValueDto totalFinancialData = new FinancialValueDto();

        Map<PartialFinancialDataType, FinancialValueDto> typeToFinancialValue = this.typeToPartialFinancialDataServices.values()
                .stream()
                .collect(Collectors.toMap(PartialFinancialDataService::getType,
                        service -> service.getPartialFinancialData(projectId)));

        typeToFinancialValue.put(PartialFinancialDataType.TOTAL, this.recalculateTotalProjectValue(typeToFinancialValue,
                totalFinancialData));

        for (Map.Entry<PartialFinancialDataType, FinancialValueDto> entry : typeToFinancialValue.entrySet()) {
            projectFinancialSummary.updatePartialData(entry.getKey(), entry.getValue());
        }

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

    private FinancialValueDto recalculateTotalProjectValue(
            Map<PartialFinancialDataType, FinancialValueDto> typeToFinancialValue,
            FinancialValueDto totalFinancialData) {
        for (Map.Entry<PartialFinancialDataType, FinancialValueDto> entry : typeToFinancialValue.entrySet()) {
            PartialFinancialDataService<PartialFinancialData> partialFinancialDataService = this.typeToPartialFinancialDataServices.get(
                    entry.getKey());
            if (partialFinancialDataService.getSummationStrategy() == SummationStrategy.ADD) {
                totalFinancialData.addValues(entry.getValue());
            } else {
                totalFinancialData.subtractValues(entry.getValue());
            }
        }
        return totalFinancialData;
    }
}
