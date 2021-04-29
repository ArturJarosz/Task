package com.arturjarosz.task.project.application.impl;

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
import org.springframework.beans.factory.annotation.Autowired;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationService
public class CostApplicationServiceImpl implements CostApplicationService {

    private CostValidator costValidator;
    private final ProjectValidator projectValidator;
    private final ProjectRepository projectRepository;
    private final ProjectQueryService projectQueryService;

    @Autowired
    public CostApplicationServiceImpl(CostValidator costValidator,
                                      ProjectValidator projectValidator,
                                      ProjectRepository projectRepository,
                                      ProjectQueryService projectQueryService) {
        this.costValidator = costValidator;
        this.projectValidator = projectValidator;
        this.projectRepository = projectRepository;
        this.projectQueryService = projectQueryService;
    }

    @Transactional
    @Override
    public CostDto createCost(Long projectId,
                              CostDto costDto) {
        this.projectValidator.validateProjectExistence(projectId);
        CostValidator.validateCostDto(costDto);
        Project project = this.projectRepository.load(projectId);
        Cost cost = CostDtoMapper.INSTANCE.costCreateDtoToCost(costDto);
        project.addCost(cost);
        project = this.projectRepository.save(project);
        return CostDtoMapper.INSTANCE.costToCostDto(cost);
    }

    @Override
    public CostDto getCost(Long costId) {
        Cost cost = this.projectQueryService.getCostById(costId);
        CostValidator.validateCostExistence(cost, costId);
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

    @Override
    public void deleteCost(Long projectId, Long costId) {
        this.projectValidator.validateProjectExistence(projectId);
        this.costValidator.validateCostExistence(costId);
        Project project = this.projectRepository.load(projectId);
        Cost cost = this.projectQueryService.getCostById(costId);
        project.getCosts().remove(cost);
        this.projectRepository.save(project);
    }

    @Override
    public CostDto updateCost(Long projectId, Long costId, CostDto costDto) {
        this.projectValidator.validateProjectExistence(projectId);
        this.costValidator.validateCostExistence(costId);
        CostValidator.validateUpdateCostDto(costDto);
        Project project = this.projectRepository.load(projectId);
        Cost cost = project
                .updateCost(costId, costDto.getName(), costDto.getDate(), costDto.getValue(), costDto.getCategory(),
                        costDto.getNote());
        this.projectRepository.save(project);
        return CostDtoMapper.INSTANCE.costToCostDto(cost);
    }
}
