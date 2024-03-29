package com.arturjarosz.task.project.domain.impl;

import com.arturjarosz.task.dto.ProjectCreateDto;
import com.arturjarosz.task.dto.ProjectDto;
import com.arturjarosz.task.project.application.mapper.ProjectMapper;
import com.arturjarosz.task.project.domain.ProjectDataValidator;
import com.arturjarosz.task.project.domain.ProjectDomainService;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.status.project.ProjectStatusTransitionService;
import com.arturjarosz.task.project.status.project.ProjectWorkflow;
import com.arturjarosz.task.sharedkernel.annotations.DomainService;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@RequiredArgsConstructor
@DomainService
public class ProjectDomainServiceImpl implements ProjectDomainService {

    private final ProjectDataValidator projectDataValidator;
    private final ProjectWorkflow projectWorkflow;
    private final ProjectStatusTransitionService projectStatusTransitionService;
    private final ProjectMapper projectMapper;

    @Override
    public Project createProject(ProjectCreateDto projectCreateDto, Long contractId) {
        var project = this.projectMapper.mapFromCreateDto(projectCreateDto, contractId,
                this.projectWorkflow);
        this.projectStatusTransitionService.create(project);
        return project;
    }

    @Override
    public Project updateProject(Project project, ProjectDto projectDto) {
        project.updateProjectData(projectDto.getName(), projectDto.getNote());
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
