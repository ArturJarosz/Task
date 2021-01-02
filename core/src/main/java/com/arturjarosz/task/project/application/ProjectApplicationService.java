package com.arturjarosz.task.project.application;

import com.arturjarosz.task.project.application.dto.ProjectContractDto;
import com.arturjarosz.task.project.application.dto.ProjectCreateDto;
import com.arturjarosz.task.project.application.dto.ProjectDto;
import com.arturjarosz.task.sharedkernel.model.CreatedEntityDto;

import java.util.List;

public interface ProjectApplicationService {

    /**
     * Creates {@link com.arturjarosz.task.project.model.Project} from given {@link ProjectCreateDto}.
     * When not all data provided, then {@link com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException}
     * is thrown.
     *
     * @param projectCreateDto
     * @return CreatedEntityDto with newly created Project id.
     */
    CreatedEntityDto createProject(ProjectCreateDto projectCreateDto);

    /**
     * Load all {@link com.arturjarosz.task.project.model.Project} data.
     * When not all data provided, then {@link com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException}
     * is thrown.
     *
     * @param projectId
     * @return
     */
    ProjectDto getProject(Long projectId);

    /**
     * Updates {@link com.arturjarosz.task.project.model.Project} of given Id by ProjectDto data.
     * When not all data provided, then {@link com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException}
     * is thrown.
     *
     * @param projectId
     * @param projectDto
     */
    void updateProject(Long projectId, ProjectDto projectDto);

    /**
     * Removes {@link com.arturjarosz.task.project.model.Project} of given Id.
     * When not all data provided, then {@link com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException}
     * is thrown.
     *
     * @param projectId
     */
    void removeProject(Long projectId);

    /**
     * Changes project status to Signed and updates data on the project based on the {@link ProjectContractDto}.
     * When not all data provided, then {@link com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException}
     * is thrown.
     *
     * @param projectId
     * @param projectContractDto
     */
    void singProjectContract(Long projectId, ProjectContractDto projectContractDto);

    /**
     * Finishes {@link com.arturjarosz.task.project.model.Project}.
     *
     * @param projectId
     * @param projectContractDto
     */
    void finishProject(Long projectId,
                       ProjectContractDto projectContractDto);

    /**
     * Loads list of all Projects Data.
     *
     * @return
     */
    List<ProjectDto> getProjects();
}
