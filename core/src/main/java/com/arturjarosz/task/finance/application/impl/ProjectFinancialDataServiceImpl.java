package com.arturjarosz.task.finance.application.impl;

import com.arturjarosz.task.dto.TotalProjectFinancialSummaryDto;
import com.arturjarosz.task.finance.application.ProjectFinancialDataService;
import com.arturjarosz.task.finance.application.dto.FinancialValueDto;
import com.arturjarosz.task.finance.domain.PartialFinancialDataService;
import com.arturjarosz.task.finance.domain.SummationStrategy;
import com.arturjarosz.task.finance.infrastructure.FinancialDataRepository;
import com.arturjarosz.task.finance.infrastructure.ProjectFinancialDataRepository;
import com.arturjarosz.task.finance.model.PartialFinancialData;
import com.arturjarosz.task.finance.model.PartialFinancialDataType;
import com.arturjarosz.task.finance.model.ProjectFinancialData;
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
public class ProjectFinancialDataServiceImpl implements ProjectFinancialDataService {

    @NonNull
    private final ProjectFinancialDataRepository projectFinancialDataRepository;
    @NonNull
    private final ProjectValidator projectValidator;
    @NonNull
    private final FinancialDataQueryServiceImpl financialDataQueryService;
    @NonNull
    private final FinancialDataRepository financialDataRepository;
    @NonNull
    private final Map<PartialFinancialDataType, PartialFinancialDataService> typeToPartialFinancialDataServices;

    @Autowired
    public ProjectFinancialDataServiceImpl(
            @NonNull ProjectFinancialDataRepository projectFinancialDataRepository,
            @NonNull ProjectValidator projectValidator,
            @NonNull FinancialDataQueryServiceImpl financialDataQueryService,
            @NonNull FinancialDataRepository financialDataRepository,
            @NonNull List<PartialFinancialDataService> partialFinancialDataServices) {
        this.projectFinancialDataRepository = projectFinancialDataRepository;
        this.projectValidator = projectValidator;
        this.financialDataQueryService = financialDataQueryService;
        this.financialDataRepository = financialDataRepository;
        this.typeToPartialFinancialDataServices = partialFinancialDataServices.stream()
                .collect(Collectors.toMap(PartialFinancialDataService::getType, Function.identity()));
    }

    @Override
    public ProjectFinancialData createProjectFinancialData(Long projectId) {
        this.projectValidator.validateProjectExistence(projectId);
        var projectFinancialData = new ProjectFinancialData(projectId);
        projectFinancialData = this.projectFinancialDataRepository.save(projectFinancialData);
        return projectFinancialData;
    }

    @Override
    public void recalculateSupervision(Long supervisionId, Long supervisionFinancialDataId) {
        var supervisionRatesDto = this.financialDataQueryService.getSupervisionRatesDto(supervisionId);
        List<SupervisionVisitFinancialDto> supervisionVisitFinancialDtos = this.financialDataQueryService.getVisitsFinancialDto(
                supervisionId);
        var financialData = this.financialDataRepository.getReferenceById(supervisionFinancialDataId);

        var value = new BigDecimal("0");
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
        var projectFinancialSummary = this.projectFinancialDataRepository.getProjectFinancialDataByProjectId(
                projectId);
        var totalFinancialData = new FinancialValueDto();

        var typeToFinancialValue = this.typeToPartialFinancialDataServices.values().stream().collect(
                Collectors.toMap(PartialFinancialDataService::getType,
                        service -> service.getPartialFinancialData(projectId)));

        typeToFinancialValue.put(PartialFinancialDataType.TOTAL,
                this.recalculateTotalProjectValue(typeToFinancialValue, totalFinancialData));

        for (Map.Entry<PartialFinancialDataType, FinancialValueDto> entry : typeToFinancialValue.entrySet()) {
            projectFinancialSummary.updatePartialData(entry.getKey(), entry.getValue());
        }

        this.projectFinancialDataRepository.save(projectFinancialSummary);
    }

    @Override
    public void removeFinancialDataForProject(Long projectId) {
        var projectFinancialSummary = this.projectFinancialDataRepository.getProjectFinancialDataByProjectId(
                projectId);
        this.projectFinancialDataRepository.deleteById(projectFinancialSummary.getId());
    }

    @Override
    public TotalProjectFinancialSummaryDto getTotalProjectFinancialData(Long projectId) {
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
