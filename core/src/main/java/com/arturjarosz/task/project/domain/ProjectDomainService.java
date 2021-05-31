package com.arturjarosz.task.project.domain;

import com.arturjarosz.task.project.application.dto.OfferDto;
import com.arturjarosz.task.project.application.dto.ProjectContractDto;
import com.arturjarosz.task.project.application.dto.ProjectCreateDto;
import com.arturjarosz.task.project.application.dto.ProjectDto;
import com.arturjarosz.task.project.model.Project;

import java.time.LocalDate;

public interface ProjectDomainService {
    /**
     * Creates new Project.
     *
     * @param projectCreateDto
     * @return Project
     */
    Project createProject(ProjectCreateDto projectCreateDto);

    /**
     * Update project with data provided in projectDto.
     *
     * @param project
     * @param projectDto
     * @return
     */
    Project updateProject(Project project, ProjectDto projectDto);

    /**
     * Accepts offer for client, fills all dates on Project and moves Project to To Do.
     *
     * @param project
     * @param projectContractDto
     * @return
     */
    Project signProjectContract(Project project, ProjectContractDto projectContractDto);

    /**
     * Finish Project and mark it as Completed.
     *
     * @param project
     * @param endDate
     * @return
     */
    Project finishProject(Project project, LocalDate endDate);

    /**
     * Reject offer for Project.
     *
     * @param project
     * @return
     */
    Project rejectProject(Project project);

    /**
     * Reopen once completed Project.
     *
     * @param project
     * @return
     */
    Project reopenProject(Project project);

    /**
     * Make a new offer for Project with value provided in OfferDto.
     *
     * @param project
     * @param offerDto
     * @return
     */
    Project makeNewOffer(Project project, OfferDto offerDto);

    /**
     * Accept offer for project. This allows to start work on Project.
     *
     * @param project
     * @return
     */
    Project acceptOffer(Project project);
}
