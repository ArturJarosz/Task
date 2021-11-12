package com.arturjarosz.task.project.application.impl;

import com.arturjarosz.task.finance.application.ProjectFinancialDataAwareService;
import com.arturjarosz.task.finance.application.ProjectFinancialDataService;
import com.arturjarosz.task.project.application.CostApplicationService;
import com.arturjarosz.task.project.application.CostValidator;
import com.arturjarosz.task.project.application.ProjectValidator;
import com.arturjarosz.task.project.application.dto.CostDto;
import com.arturjarosz.task.project.application.mapper.CostDtoMapper;
import com.arturjarosz.task.project.infrastructure.repositor.ProjectRepository;
import com.arturjarosz.task.project.model.Cost;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.query.ProjectQueryService;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.model.AbstractEntity;
import org.springframework.beans.factory.annotation.Autowired;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationService
public class CostApplicationServiceImpl implements CostApplicationService, ProjectFinancialDataAwareService {

    private final CostValidator costValidator;
    private final ProjectValidator projectValidator;
    private final ProjectRepository projectRepository;
    private final ProjectQueryService projectQueryService;
    private final ProjectFinancialDataService projectFinancialDataService;

    @Autowired
    public CostApplicationServiceImpl(CostValidator costValidator, ProjectValidator projectValidator,
                                      ProjectRepository projectRepository, ProjectQueryService projectQueryService,
                                      ProjectFinancialDataService projectFinancialDataService) {
        this.costValidator = costValidator;
        this.projectValidator = projectValidator;
        this.projectRepository = projectRepository;
        this.projectQueryService = projectQueryService;
        this.projectFinancialDataService = projectFinancialDataService;
    }

    @Transactional
    @Override
    public CostDto createCost(Long projectId, CostDto costDto) {
        this.projectValidator.validateProjectExistence(projectId);
        this.costValidator.validateCostDto(costDto);
        Project project = this.projectRepository.load(projectId);
        Cost cost = CostDtoMapper.INSTANCE.costCreateDtoToCost(costDto);
        project.addCost(cost);
        project = this.projectRepository.save(project);
        CostDto createdCostDto = CostDtoMapper.INSTANCE.costToCostDto(cost);
        createdCostDto.setId(this.getIdForCreatedCost(project, cost));
        this.triggerProjectFinancialDataRecalculation(projectId);
        return createdCostDto;
    }

    @Override
    public CostDto getCost(Long costId) {
        Cost cost = this.projectQueryService.getCostById(costId);
        this.costValidator.validateCostExistence(cost, costId);
        return CostDtoMapper.INSTANCE.costToCostDto(cost);
    }

    @Override
    public List<CostDto> getCosts(Long projectId) {
        this.projectValidator.validateProjectExistence(projectId);
        Project project = this.projectRepository.load(projectId);
        Set<Cost> costs = project.getCosts();
        return new ArrayList<>(costs.stream()
                .map(CostDtoMapper.INSTANCE::costToCostDto)
                .collect(Collectors.toList())
        );
    }

    @Transactional
    @Override
    public void deleteCost(Long projectId, Long costId) {
        this.projectValidator.validateProjectExistence(projectId);
        this.costValidator.validateCostExistence(costId);
        Project project = this.projectRepository.load(projectId);
        project.removeCost(costId);
        this.projectRepository.save(project);
        this.triggerProjectFinancialDataRecalculation(projectId);
    }

    @Transactional
    @Override
    public CostDto updateCost(Long projectId, Long costId, CostDto costDto) {
        this.projectValidator.validateProjectExistence(projectId);
        this.costValidator.validateCostExistence(costId);
        this.costValidator.validateUpdateCostDto(costDto);
        Project project = this.projectRepository.load(projectId);
        Cost cost = project
                .updateCost(costId, costDto.getName(), costDto.getDate(), costDto.getValue(), costDto.getCategory(),
                        costDto.getNote());
        this.projectRepository.save(project);
        this.triggerProjectFinancialDataRecalculation(projectId);
        return CostDtoMapper.INSTANCE.costToCostDto(cost);
    }

    private Long getIdForCreatedCost(Project project, Cost cost) {
        return project.getCosts().stream()
                .filter(costOnProject -> costOnProject.equals(cost))
                .map(AbstractEntity::getId)
                .findFirst().orElse(null);
    }

    @Override
    public void triggerProjectFinancialDataRecalculation(long projectId) {
        this.projectFinancialDataService.recalculateProjectFinancialData(projectId);
    }
}
