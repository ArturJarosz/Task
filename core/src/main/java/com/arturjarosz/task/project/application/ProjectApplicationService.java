package com.arturjarosz.task.project.application;

import com.arturjarosz.task.dto.ProjectCreateDto;
import com.arturjarosz.task.dto.ProjectDto;

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
     * Updates {@link com.arturjarosz.task.project.model.Project} of given projectId by ProjectDto data.
     * When not all data provided, then {@link com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException}
     * is thrown.
     */
    ProjectDto updateProject(Long projectId, ProjectDto projectDto);

    /**
     * Removes {@link com.arturjarosz.task.project.model.Project} of given projectId.
     * When not all data provided, then {@link com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException}
     * is thrown.
     */
    void removeProject(Long projectId);

    /**
     * Finishes {@link com.arturjarosz.task.project.model.Project}.
     */
    ProjectDto finishProject(Long projectId, ProjectDto projectContractDto);

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

}
