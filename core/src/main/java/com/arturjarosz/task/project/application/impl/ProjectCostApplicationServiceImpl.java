package com.arturjarosz.task.project.application.impl;

import com.arturjarosz.task.project.application.ProjectCostApplicationService;
import com.arturjarosz.task.project.application.ProjectCostValidator;
import com.arturjarosz.task.project.application.ProjectValidator;
import com.arturjarosz.task.project.application.dto.CostDto;
import com.arturjarosz.task.project.infrastructure.repositor.ProjectRepository;
import com.arturjarosz.task.project.model.Cost;
import com.arturjarosz.task.project.model.CostDtoMapper;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.query.ProjectQueryService;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.model.AbstractEntity;
import com.arturjarosz.task.sharedkernel.model.CreatedEntityDto;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationService
public class ProjectCostApplicationServiceImpl implements ProjectCostApplicationService {

    private final ProjectValidator projectValidator;
    private final ProjectRepository projectRepository;
    private final ProjectQueryService projectQueryService;

    public ProjectCostApplicationServiceImpl(ProjectValidator projectValidator,
                                             ProjectRepository projectRepository,
                                             ProjectQueryService projectQueryService) {
        this.projectValidator = projectValidator;
        this.projectRepository = projectRepository;
        this.projectQueryService = projectQueryService;
    }

    @Transactional
    @Override
    public CreatedEntityDto createCost(Long projectId,
                                       CostDto costDto) {
        this.projectValidator.validateProjectExistence(projectId);
        ProjectCostValidator.validateCostDto(costDto);
        Project project = this.projectRepository.load(projectId);
        Cost cost = CostDtoMapper.INSTANCE.costCreateDtoToCost(costDto);
        project.addCost(cost);
        project = this.projectRepository.save(project);
        return new CreatedEntityDto(this.getIdForCreatedCost(project, cost));
    }

    @Override
    public CostDto getCost(Long costId) {
        Cost cost = this.projectQueryService.getCostById(costId);
        ProjectCostValidator.validateCostExistence(cost, costId);
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

    /**
     * Retrieve id of given cost in Project. When Cost is added to the project it does not have any id yet.
     * After it is saved by repository to the database the Id is generated.
     *
     * @param project
     * @param cost
     * @return id of Cost
     */
    private Long getIdForCreatedCost(Project project, Cost cost) {
        return project.getCosts().stream()
                .filter(projectCost -> projectCost.equals(cost))
                .findFirst()
                .map(AbstractEntity::getId
                ).orElseThrow();
    }
}