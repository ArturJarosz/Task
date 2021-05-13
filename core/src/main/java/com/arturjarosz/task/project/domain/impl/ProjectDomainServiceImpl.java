package com.arturjarosz.task.project.domain.impl;

import com.arturjarosz.task.project.application.dto.OfferDto;
import com.arturjarosz.task.project.application.dto.ProjectContractDto;
import com.arturjarosz.task.project.application.dto.ProjectCreateDto;
import com.arturjarosz.task.project.application.dto.ProjectDto;
import com.arturjarosz.task.project.application.mapper.ProjectDtoMapper;
import com.arturjarosz.task.project.domain.ProjectDataValidator;
import com.arturjarosz.task.project.domain.ProjectDomainService;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.project.status.project.ProjectStatus;
import com.arturjarosz.task.project.status.project.ProjectWorkflow;
import com.arturjarosz.task.project.status.project.ProjectWorkflowService;
import com.arturjarosz.task.project.status.stage.StageStatus;
import com.arturjarosz.task.sharedkernel.annotations.DomainService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@DomainService
public class ProjectDomainServiceImpl implements ProjectDomainService {

    private final ProjectDataValidator projectDataValidator;
    private final ProjectWorkflow projectWorkflow;
    private final ProjectWorkflowService projectWorkflowService;

    public ProjectDomainServiceImpl(ProjectDataValidator projectDataValidator,
                                    ProjectWorkflow projectWorkflow,
                                    ProjectWorkflowService projectWorkflowService) {
        this.projectDataValidator = projectDataValidator;
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

    @Override
    public Project reopenProject(Project project) {
        if (this.hasStagesOnlyInRejectedAndToDoStatus(project)) {
            this.projectWorkflowService.changeProjectStatus(project, ProjectStatus.TO_DO);
        } else {
            this.projectWorkflowService.changeProjectStatus(project, ProjectStatus.IN_PROGRESS);
        }
        return project;
    }

    private boolean hasStagesOnlyInRejectedAndToDoStatus(Project project) {
        List<Stage> allStages = new ArrayList<>(project.getStages());
        //we are removing Stages in Rejected status, because they should not be taken into account
        allStages.removeIf(stage -> stage.getStatus().equals(StageStatus.REJECTED));
        allStages.removeIf(stage -> stage.getStatus().equals(StageStatus.TO_DO));
        return allStages.isEmpty();
    }

    @Override
    public Project makeNewOffer(Project project, OfferDto offerDto) {
        project.makeNewOffer(offerDto.getOfferValue());
        this.projectWorkflowService.changeProjectStatus(project, ProjectStatus.OFFER);
        return project;
    }
}
