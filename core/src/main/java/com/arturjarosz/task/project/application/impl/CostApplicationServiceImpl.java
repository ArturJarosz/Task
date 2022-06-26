package com.arturjarosz.task.project.application.impl;

import com.arturjarosz.task.finance.application.ProjectFinanceAwareObjectService;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@ApplicationService
public class CostApplicationServiceImpl implements CostApplicationService {

    private final CostValidator costValidator;
    private final ProjectValidator projectValidator;
    private final ProjectRepository projectRepository;
    private final ProjectQueryService projectQueryService;
    private final ProjectFinanceAwareObjectService projectFinanceAwareObjectService;

    @Autowired
    public CostApplicationServiceImpl(CostValidator costValidator, ProjectValidator projectValidator,
            ProjectRepository projectRepository, ProjectQueryService projectQueryService,
            ProjectFinanceAwareObjectService projectFinanceAwareObjectService) {
        this.costValidator = costValidator;
        this.projectValidator = projectValidator;
        this.projectRepository = projectRepository;
        this.projectQueryService = projectQueryService;
        this.projectFinanceAwareObjectService = projectFinanceAwareObjectService;
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
        this.projectFinanceAwareObjectService.onCreate(projectId);
        return createdCostDto;
    }

    @Transactional
    @Override
    public CostDto updateCost(Long projectId, Long costId, CostDto costDto) {
        this.projectValidator.validateProjectExistence(projectId);
        this.costValidator.validateCostExistence(costId);
        this.costValidator.validateUpdateCostDto(costDto);
        Project project = this.projectRepository.load(projectId);
        Cost cost = project.updateCost(costId, costDto.getName(), costDto.getDate(), costDto.getValue(),
                costDto.getCategory(), costDto.getNote());
        this.projectRepository.save(project);
        this.projectFinanceAwareObjectService.onUpdate(projectId);
        return CostDtoMapper.INSTANCE.costToCostDto(cost);
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
        return new ArrayList<>(costs.stream().map(CostDtoMapper.INSTANCE::costToCostDto).toList());
    }

    @Transactional
    @Override
    public void deleteCost(Long projectId, Long costId) {
        this.projectValidator.validateProjectExistence(projectId);
        this.costValidator.validateCostExistence(costId);
        Project project = this.projectRepository.load(projectId);
        project.removeCost(costId);
        this.projectRepository.save(project);
        this.projectFinanceAwareObjectService.onRemove(projectId);
    }

    private Long getIdForCreatedCost(Project project, Cost cost) {
        return project.getCosts().stream().filter(costOnProject -> costOnProject.equals(cost))
                .map(AbstractEntity::getId).findFirst().orElse(null);
    }

}
