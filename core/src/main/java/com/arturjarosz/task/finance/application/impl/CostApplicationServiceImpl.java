package com.arturjarosz.task.finance.application.impl;

import com.arturjarosz.task.finance.application.CostApplicationService;
import com.arturjarosz.task.finance.application.CostValidator;
import com.arturjarosz.task.finance.application.ProjectFinanceAwareObjectService;
import com.arturjarosz.task.finance.application.mapper.CostDtoMapper;
import com.arturjarosz.task.finance.infrastructure.ProjectFinancialDataRepository;
import com.arturjarosz.task.finance.model.Cost;
import com.arturjarosz.task.finance.model.ProjectFinancialData;
import com.arturjarosz.task.finance.query.FinancialDataQueryService;
import com.arturjarosz.task.project.application.ProjectValidator;
import com.arturjarosz.task.project.application.dto.CostDto;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@ApplicationService
public class CostApplicationServiceImpl implements CostApplicationService {

    private final CostValidator costValidator;
    private final ProjectValidator projectValidator;
    private final ProjectFinanceAwareObjectService projectFinanceAwareObjectService;
    private final ProjectFinancialDataRepository projectFinancialDataRepository;
    private final FinancialDataQueryService financialDataQueryService;

    @Autowired
    public CostApplicationServiceImpl(CostValidator costValidator, ProjectValidator projectValidator,
            ProjectFinanceAwareObjectService projectFinanceAwareObjectService,
            ProjectFinancialDataRepository projectFinancialDataRepository,
            FinancialDataQueryService financialDataQueryService) {
        this.costValidator = costValidator;
        this.projectValidator = projectValidator;
        this.projectFinanceAwareObjectService = projectFinanceAwareObjectService;
        this.projectFinancialDataRepository = projectFinancialDataRepository;
        this.financialDataQueryService = financialDataQueryService;
    }

    @Transactional
    @Override
    public CostDto createCost(Long projectId, CostDto costDto) {

        this.projectValidator.validateProjectExistence(projectId);
        this.costValidator.validateCostDto(costDto);

        Cost cost = CostDtoMapper.INSTANCE.costCreateDtoToCost(costDto);
        ProjectFinancialData financialData = this.projectFinancialDataRepository.getProjectFinancialDataByProjectId(
                projectId);
        financialData.addCost(cost);
        financialData = this.projectFinancialDataRepository.save(financialData);

        CostDto createdCostDto = CostDtoMapper.INSTANCE.costToCostDto(cost);
        createdCostDto.setId(this.getIdForCreatedCost(financialData, cost));
        this.projectFinanceAwareObjectService.onCreate(projectId);
        return createdCostDto;
    }

    @Transactional
    @Override
    public CostDto updateCost(Long projectId, Long costId, CostDto costDto) {

        this.projectValidator.validateProjectExistence(projectId);
        this.costValidator.validateCostExistence(costId);
        this.costValidator.validateUpdateCostDto(costDto);

        ProjectFinancialData projectFinancialData = this.projectFinancialDataRepository.getProjectFinancialDataByProjectId(
                projectId);
        Cost cost = projectFinancialData.updateCost(costId, costDto.getName(), costDto.getDate(), costDto.getValue(),
                costDto.getCategory(), costDto.getNote());

        this.projectFinancialDataRepository.save(projectFinancialData);
        this.projectFinanceAwareObjectService.onUpdate(projectId);

        return CostDtoMapper.INSTANCE.costToCostDto(cost);
    }

    @Override
    public CostDto getCost(Long costId) {
        this.costValidator.validateCostExistence(costId);
        return this.financialDataQueryService.getCostById(costId);
    }

    @Override
    public List<CostDto> getCosts(Long projectId) {
        this.projectValidator.validateProjectExistence(projectId);
        return this.financialDataQueryService.getCostsByProjectId(projectId);
    }

    @Transactional
    @Override
    public void deleteCost(Long projectId, Long costId) {
        this.projectValidator.validateProjectExistence(projectId);
        this.costValidator.validateCostExistence(costId);

        ProjectFinancialData projectFinancialData = this.projectFinancialDataRepository.getProjectFinancialDataByProjectId(
                projectId);
        projectFinancialData.removeCost(costId);

        this.projectFinancialDataRepository.save(projectFinancialData);
        this.projectFinanceAwareObjectService.onRemove(projectId);
    }

    private Long getIdForCreatedCost(ProjectFinancialData financialData, Cost cost) {
        return financialData.getCosts()
                .stream()
                .filter(costOnProject -> costOnProject.equals(cost))
                .map(Cost::getId)
                .findFirst()
                .orElse(null);
    }

}
