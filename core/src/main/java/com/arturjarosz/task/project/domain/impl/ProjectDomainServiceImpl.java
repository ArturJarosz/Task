package com.arturjarosz.task.project.domain.impl;

import com.arturjarosz.task.project.application.dto.ProjectContractDto;
import com.arturjarosz.task.project.application.dto.ProjectDto;
import com.arturjarosz.task.project.domain.ProjectDataValidator;
import com.arturjarosz.task.project.domain.ProjectDomainService;
import com.arturjarosz.task.project.infrastructure.repositor.ProjectRepository;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.sharedkernel.annotations.DomainService;

import java.time.LocalDate;

@DomainService
public class ProjectDomainServiceImpl implements ProjectDomainService {

    private ProjectDataValidator projectDataValidator;
    private ProjectRepository projectRepository;

    public ProjectDomainServiceImpl(ProjectDataValidator projectDataValidator,
                                    ProjectRepository projectRepository) {
        this.projectDataValidator = projectDataValidator;
        this.projectRepository = projectRepository;
    }

    @Override
    public void updateProject(Project project, ProjectDto projectDto) {
        project.updateProjectData(projectDto.getName(), projectDto.getNote());

    }

    @Override
    public void signProjectContract(Project project,
                                    ProjectContractDto projectContractDto) {
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
    }

    @Override
    public void finishProject(Long projectId, LocalDate endDate) {
        Project project = this.projectRepository.load(projectId);
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        //end date can't be before start date
        this.projectDataValidator.endDateNotBeforeStartDate(project.getStartDate(), endDate);
        project.finishProject(endDate);
    }
}
