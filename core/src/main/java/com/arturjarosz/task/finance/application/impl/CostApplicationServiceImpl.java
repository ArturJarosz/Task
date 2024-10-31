package com.arturjarosz.task.finance.application.impl;

import com.arturjarosz.task.dto.CostDto;
import com.arturjarosz.task.dto.CostProjectDataDto;
import com.arturjarosz.task.finance.application.CostApplicationService;
import com.arturjarosz.task.finance.application.ProjectFinanceAwareObjectService;
import com.arturjarosz.task.finance.application.mapper.CostMapper;
import com.arturjarosz.task.finance.application.mapper.CostProjectDataMapper;
import com.arturjarosz.task.finance.application.validator.CostValidator;
import com.arturjarosz.task.finance.infrastructure.ProjectFinancialDataRepository;
import com.arturjarosz.task.finance.model.Cost;
import com.arturjarosz.task.finance.model.CostCategory;
import com.arturjarosz.task.finance.model.PartialFinancialDataType;
import com.arturjarosz.task.finance.model.ProjectFinancialData;
import com.arturjarosz.task.finance.query.FinancialDataQueryService;
import com.arturjarosz.task.project.application.ProjectValidator;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@ApplicationService
public class CostApplicationServiceImpl implements CostApplicationService {
    private final CostValidator costValidator;
    private final ProjectValidator projectValidator;
    private final ProjectFinanceAwareObjectService projectFinanceAwareObjectService;
    private final ProjectFinancialDataRepository projectFinancialDataRepository;
    private final FinancialDataQueryService financialDataQueryService;
    private final CostMapper costMapper;
    private final CostProjectDataMapper costProjectDataMapper;

    @Transactional
    @Override
    public CostDto createCost(Long projectId, CostDto costDto) {

        this.projectValidator.validateProjectExistence(projectId);
        this.costValidator.validateCostDto(costDto);

        var cost = this.costMapper.mapFromDto(costDto);
        var financialData = this.projectFinancialDataRepository.getProjectFinancialDataByProjectId(projectId);
        financialData.addCost(cost);
        financialData = this.projectFinancialDataRepository.save(financialData);

        CostDto createdCostDto = this.costMapper.mapToDto(cost);
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

        var projectFinancialData = this.projectFinancialDataRepository.getProjectFinancialDataByProjectId(projectId);
        var cost = projectFinancialData.updateCost(costId, costDto.getName(), costDto.getDate(), costDto.getValue(),
                CostCategory.valueOf(costDto.getCategory().name()), costDto.getNote());

        this.projectFinancialDataRepository.save(projectFinancialData);
        this.projectFinanceAwareObjectService.onUpdate(projectId);

        return this.costMapper.mapToDto(cost);
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

        var projectFinancialData = this.projectFinancialDataRepository.getProjectFinancialDataByProjectId(projectId);
        projectFinancialData.removeCost(costId);

        this.projectFinancialDataRepository.save(projectFinancialData);
        this.projectFinanceAwareObjectService.onRemove(projectId);
    }

    @Override
    public CostProjectDataDto getProjectCostData(Long projectId) {
        LOG.debug("Loading cost project data for project with it: {}", projectId);
        this.projectValidator.validateProjectExistence(projectId);

        var projectCostFinancialData = this.financialDataQueryService.getProjectPartialFinancialDataByType(projectId,
                PartialFinancialDataType.COST);
        var costs = this.financialDataQueryService.getCostsByProjectId(projectId);

        return this.costProjectDataMapper.map(projectCostFinancialData, costs);
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
