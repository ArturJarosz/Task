package com.arturjarosz.task.project.domain.impl;

import com.arturjarosz.task.dto.ProjectCreateDto;
import com.arturjarosz.task.dto.ProjectDto;
import com.arturjarosz.task.project.application.mapper.ProjectMapper;
import com.arturjarosz.task.project.domain.ProjectDataValidator;
import com.arturjarosz.task.project.domain.ProjectDomainService;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.ProjectType;
import com.arturjarosz.task.project.status.project.ProjectStatus;
import com.arturjarosz.task.project.status.project.ProjectStatusTransitionService;
import com.arturjarosz.task.project.status.project.ProjectWorkflow;
import com.arturjarosz.task.sharedkernel.annotations.DomainService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiConsumer;

@DomainService
public class ProjectDomainServiceImpl implements ProjectDomainService {

    private final ProjectDataValidator projectDataValidator;
    private final ProjectWorkflow projectWorkflow;
    private final ProjectStatusTransitionService projectStatusTransitionService;
    private final ProjectMapper projectMapper;

    private Map<ProjectStatus, BiConsumer<Project, ProjectDto>> statusToUpdater;

    @Autowired
    public ProjectDomainServiceImpl(ProjectDataValidator projectDataValidator, ProjectWorkflow projectWorkflow,
            ProjectStatusTransitionService projectStatusTransitionService, ProjectMapper projectMapper) {
        this.projectDataValidator = projectDataValidator;
        this.projectWorkflow = projectWorkflow;
        this.projectStatusTransitionService = projectStatusTransitionService;
        this.projectMapper = projectMapper;
        this.prepareUpdaters();
    }

    private void prepareUpdaters() {
        this.statusToUpdater = new EnumMap<>(ProjectStatus.class);
        this.statusToUpdater.put(ProjectStatus.TO_DO, (project, projectDto) -> {
            project.updateProjectBasicData(projectDto.getName(), projectDto.getNote());
            project.setArchitectId(projectDto.getArchitect().getId());
            project.setProjectType(ProjectType.valueOf(projectDto.getType().getValue()));
            project.setEndDate(null);
        });
        this.statusToUpdater.put(ProjectStatus.IN_PROGRESS, (project, projectDto) -> {
            project.updateProjectBasicData(projectDto.getName(), projectDto.getNote());
            project.setArchitectId(projectDto.getArchitect().getId());
            project.setProjectType(ProjectType.valueOf(projectDto.getType().getValue()));
            project.setStartDate(projectDto.getStartDate());
            project.setEndDate(null);
        });
        this.statusToUpdater.put(ProjectStatus.DONE, (project, projectDto) -> {
            project.updateProjectBasicData(projectDto.getName(), projectDto.getNote());
            project.setArchitectId(projectDto.getArchitect().getId());
            project.setStartDate(projectDto.getStartDate());
        });
        this.statusToUpdater.put(ProjectStatus.COMPLETED, (project, projectDto) -> {
            project.updateProjectBasicData(projectDto.getName(), projectDto.getNote());
            project.setArchitectId(projectDto.getArchitect().getId());
            project.setStartDate(projectDto.getStartDate());
        });
        this.statusToUpdater.put(ProjectStatus.REJECTED, (project, projectDto) -> {
            project.updateProjectBasicData(projectDto.getName(), projectDto.getNote());
            project.setArchitectId(projectDto.getArchitect().getId());
            project.setProjectType(ProjectType.valueOf(projectDto.getType().getValue()));
            project.setStartDate(projectDto.getStartDate());
            project.setEndDate(null);
        });

    }

    @Override
    public Project createProject(ProjectCreateDto projectCreateDto, Long contractId) {
        var project = this.projectMapper.mapFromCreateDto(projectCreateDto, contractId, this.projectWorkflow);
        this.projectStatusTransitionService.create(project);
        return project;
    }

    @Override
    public Project updateProject(Project project, ProjectDto projectDto) {
        var updater = this.statusToUpdater.get(project.getStatus());
        updater.accept(project, projectDto);
        return project;
    }

    @Override
    public Project finishProject(Project project, LocalDate endDate) {
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        this.projectDataValidator.startDatePresent(project.getStartDate());
        //end date can't be before start date
        this.projectDataValidator.endDateNotBeforeStartDate(project.getStartDate(), endDate);
        project.finishProject(endDate);
        // TODO TA-194: what does it mean to finish project ? is there a need to have action for that ?
        this.projectStatusTransitionService.complete(project);
        return project;
    }

    @Override
    public Project rejectProject(Project project) {
        this.projectStatusTransitionService.reject(project);
        return project;
    }

    @Override
    public Project reopenProject(Project project) {
        this.projectStatusTransitionService.reopen(project);
        return project;
    }
}
