package com.arturjarosz.task.project.domain;

import com.arturjarosz.task.contract.application.dto.ContractDto;
import com.arturjarosz.task.project.application.dto.ProjectContractDto;
import com.arturjarosz.task.project.application.dto.ProjectCreateDto;
import com.arturjarosz.task.project.application.dto.ProjectDto;
import com.arturjarosz.task.project.model.Project;

import java.time.LocalDate;

public interface ProjectDomainService {
    /**
     * Creates new Project.
     */
    Project createProject(ProjectCreateDto projectCreateDto, Long contractId);

    /**
     * Update project with data provided in projectDto.
     */
    Project updateProject(Project project, ProjectDto projectDto);

    /**
     * Accepts offer for client, fills all dates on Project and moves Project to 'To Do'.
     */
    Project signProjectContract(Project project, ProjectContractDto projectContractDto);

    /**
     * Finish Project and mark it as Completed.
     */
    Project finishProject(Project project, LocalDate endDate);

    /**
     * Reject offer for Project. Work on rejected Project cannot be continued. It is not possible to add Stage or
     * Task on rejected Project as well as changing their statuses.
     */
    Project rejectProject(Project project);

    /**
     * Reopen rejected Project to continue or plan work on it.
     */
    Project reopenProject(Project project);

    /**
     * Make a new offer for Project with value provided in OfferDto.
     */
    Project makeNewOffer(Project project, ContractDto contractDto);

    /**
     * Accept offer for project. This allows to start work on Project.
     */
    Project acceptOffer(Project project);
}
