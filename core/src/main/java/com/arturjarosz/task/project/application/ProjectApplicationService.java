package com.arturjarosz.task.project.application;

import com.arturjarosz.task.contract.application.dto.ContractDto;
import com.arturjarosz.task.project.application.dto.ProjectContractDto;
import com.arturjarosz.task.project.application.dto.ProjectCreateDto;
import com.arturjarosz.task.project.application.dto.ProjectDto;

import java.util.List;

public interface ProjectApplicationService {

    /**
     * Creates {@link com.arturjarosz.task.project.model.Project} from given {@link ProjectCreateDto}, and creates
     * Contract connected to that Project. When not all data provided, then {@link com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException}
     * is thrown.
     */
    ProjectDto createProject(ProjectCreateDto projectCreateDto);

    /**
     * Load all {@link com.arturjarosz.task.project.model.Project} data.
     * When not all data provided, then {@link com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException}
     * is thrown.
     */
    ProjectDto getProject(Long projectId);

    /**
     * Updates {@link com.arturjarosz.task.project.model.Project} of given Id by ProjectDto data.
     * When not all data provided, then {@link com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException}
     * is thrown.
     */
    ProjectDto updateProject(Long projectId, ProjectDto projectDto);

    /**
     * Removes {@link com.arturjarosz.task.project.model.Project} of given Id.
     * When not all data provided, then {@link com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException}
     * is thrown.
     */
    void removeProject(Long projectId);

    /**
     * Changes project status to Signed and updates data on the project based on the {@link ProjectContractDto}.
     * When not all data provided, then {@link com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException}
     * is thrown.
     */
    ProjectDto signProjectContract(Long projectId, ProjectContractDto projectContractDto);

    /**
     * Finishes {@link com.arturjarosz.task.project.model.Project}.
     */
    ProjectDto finishProject(Long projectId, ProjectContractDto projectContractDto);

    /**
     * Loads list of all Projects Data.
     */
    List<ProjectDto> getProjects();

    /**
     * Set Project with projectId as rejected. If Project does not exist, new exception will be thrown.
     */
    ProjectDto rejectProject(Long projectId);

    /**
     * Reopens once rejected project.
     */
    ProjectDto reopenProject(Long projectId);

    /**
     * Make new offer to Project for value in offerDto.
     */
    ProjectDto makeNewOffer(Long projectId, ContractDto contractDto);

    /**
     * Mark offer of Project with projectId as accepted and
     */
    ProjectDto acceptOffer(Long projectId);
}
