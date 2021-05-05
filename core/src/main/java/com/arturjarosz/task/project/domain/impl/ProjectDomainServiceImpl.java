package com.arturjarosz.task.project.domain.impl;

import com.arturjarosz.task.project.application.dto.ProjectContractDto;
import com.arturjarosz.task.project.application.dto.ProjectCreateDto;
import com.arturjarosz.task.project.application.dto.ProjectDto;
import com.arturjarosz.task.project.application.mapper.ProjectDtoMapper;
import com.arturjarosz.task.project.domain.ProjectDataValidator;
import com.arturjarosz.task.project.domain.ProjectDomainService;
import com.arturjarosz.task.project.infrastructure.repositor.ProjectRepository;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.status.project.ProjectStatus;
import com.arturjarosz.task.project.status.project.ProjectWorkflow;
import com.arturjarosz.task.project.status.project.ProjectWorkflowService;
import com.arturjarosz.task.sharedkernel.annotations.DomainService;

import java.time.LocalDate;

@DomainService
public class ProjectDomainServiceImpl implements ProjectDomainService {

    private final ProjectDataValidator projectDataValidator;
    private final ProjectRepository projectRepository;
    private final ProjectWorkflow projectWorkflow;
    private final ProjectWorkflowService projectWorkflowService;

    public ProjectDomainServiceImpl(ProjectDataValidator projectDataValidator,
                                    ProjectRepository projectRepository,
                                    ProjectWorkflow projectWorkflow,
                                    ProjectWorkflowService projectWorkflowService) {
        this.projectDataValidator = projectDataValidator;
        this.projectRepository = projectRepository;
        this.projectWorkflow = projectWorkflow;
        this.projectWorkflowService = projectWorkflowService;
    }

    @Override
    public Project createProject(ProjectCreateDto projectCreateDto) {
        Project project = ProjectDtoMapper.INSTANCE.projectCreateDtoToProject(projectCreateDto, this.projectWorkflow);
        this.projectWorkflowService.changeProjectStatus(project, this.projectWorkflow.getInitialStatus());
        return project;
    }

    @Override
    public Project updateProject(Project project, ProjectDto projectDto) {
        //TODO: to think what data should be updatable on project
        project.updateProjectData(projectDto.getName(), projectDto.getNote());
        return project;
    }

    @Override
    public Project signProjectContract(Project project, ProjectContractDto projectContractDto) {
        LocalDate signingDate = projectContractDto.getSigningDate();
        LocalDate startDate = projectContractDto.getStartDate();
        LocalDate deadline = projectContractDto.getDeadline();
        //signing date can't be future date
        this.projectDataValidator.signingDateNotInFuture(signingDate);
        //start date can't be before signing date
        this.projectDataValidator.startDateNotBeforeSigningDate(startDate, signingDate);
        //deadline can't be before start date
        this.projectDataValidator.deadlineNotBeforeStartDate(startDate, deadline);
        project.signContract(signingDate, startDate, deadline);
        this.projectWorkflowService.changeProjectStatus(project, ProjectStatus.TO_DO);
        return project;
    }

    @Override
    public Project finishProject(Project project, LocalDate endDate) {
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        //end date can't be before start date
        this.projectDataValidator.endDateNotBeforeStartDate(project.getStartDate(), endDate);
        project.finishProject(endDate);
        this.projectWorkflowService.changeProjectStatus(project, ProjectStatus.COMPLETED);
        return project;
    }

    @Override
    public Project rejectProject(Project project) {
        this.projectWorkflowService.changeProjectStatus(project, ProjectStatus.REJECTED);
        return project;
    }
}
