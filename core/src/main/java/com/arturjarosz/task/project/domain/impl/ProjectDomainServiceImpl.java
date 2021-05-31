package com.arturjarosz.task.project.domain.impl;

import com.arturjarosz.task.project.application.dto.OfferDto;
import com.arturjarosz.task.project.application.dto.ProjectContractDto;
import com.arturjarosz.task.project.application.dto.ProjectCreateDto;
import com.arturjarosz.task.project.application.dto.ProjectDto;
import com.arturjarosz.task.project.application.mapper.ProjectDtoMapper;
import com.arturjarosz.task.project.domain.ProjectDataValidator;
import com.arturjarosz.task.project.domain.ProjectDomainService;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.status.project.ProjectStatusTransitionService;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.project.status.project.ProjectStatus;
import com.arturjarosz.task.project.status.project.ProjectWorkflow;
import com.arturjarosz.task.project.status.project.ProjectWorkflowService;
import com.arturjarosz.task.project.status.stage.StageStatus;
import com.arturjarosz.task.sharedkernel.annotations.DomainService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@DomainService
public class ProjectDomainServiceImpl implements ProjectDomainService {

    private final ProjectDataValidator projectDataValidator;
    private final ProjectWorkflow projectWorkflow;
    private final ProjectStatusTransitionService projectStatusTransitionService;

    @Autowired
    public ProjectDomainServiceImpl(ProjectDataValidator projectDataValidator,
                                    ProjectWorkflow projectWorkflow,
                                    ProjectStatusTransitionService projectStatusTransitionService) {
        this.projectDataValidator = projectDataValidator;
        this.projectWorkflow = projectWorkflow;
        this.projectStatusTransitionService = projectStatusTransitionService;
    }

    @Override
    public Project createProject(ProjectCreateDto projectCreateDto) {
        Project project = ProjectDtoMapper.INSTANCE.projectCreateDtoToProject(projectCreateDto, this.projectWorkflow);
        this.projectStatusTransitionService.create(project);
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
        this.projectDataValidator.allDatesPresent(signingDate, startDate, deadline);
        //signing date can't be future date
        this.projectDataValidator.signingDateNotInFuture(signingDate);
        //start date can't be before signing date
        this.projectDataValidator.startDateNotBeforeSigningDate(startDate, signingDate);
        //deadline can't be before start date
        this.projectDataValidator.deadlineNotBeforeStartDate(startDate, deadline);
        if (!project.isOfferAccepted()) {
            project = this.acceptOffer(project);
        }
        project.signContract(signingDate, startDate, deadline);
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
        // TODO: what does it mean to finish project ? is there a need to have action for that ?
        //this.projectStatusTransitionService.completeWork(project);
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

    @Override
    public Project makeNewOffer(Project project, OfferDto offerDto) {
        if (project.getStatus() != null) {
            this.projectStatusTransitionService.makeNewOffer(project);
        }
        project.makeNewOffer(offerDto.getOfferValue());
        return project;
    }

    @Override
    public Project acceptOffer(Project project) {
        project.acceptOffer();
        this.projectStatusTransitionService.acceptOffer(project);
        return project;
    }
}
