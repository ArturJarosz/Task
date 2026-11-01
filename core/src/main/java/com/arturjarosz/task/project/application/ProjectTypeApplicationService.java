package com.arturjarosz.task.project.application;

import com.arturjarosz.task.dto.ProjectTypeDto;

import java.util.List;

public interface ProjectTypeApplicationService {

    /**
     * Create new ProjectType with name from ProjectTypeDto. If the name is missing, too long or already used by
     * another ProjectType, an exception is thrown.
     */
    ProjectTypeDto createProjectType(ProjectTypeDto projectTypeDto);

    /**
     * Rename ProjectType of given projectTypeId. If ProjectType does not exist or the name is not correct, an exception
     * is thrown.
     */
    ProjectTypeDto updateProjectType(Long projectTypeId, ProjectTypeDto projectTypeDto);

    /**
     * Remove ProjectType with given projectTypeId. If ProjectType does not exist or any Project is of that type,
     * an exception is thrown.
     */
    void deleteProjectType(Long projectTypeId);

    /**
     * Get ProjectType of given projectTypeId with the number of its Projects. If ProjectType does not exist,
     * an exception is thrown.
     */
    ProjectTypeDto getProjectType(Long projectTypeId);

    /**
     * Get all ProjectTypes sorted by name, each with the number of its Projects.
     */
    List<ProjectTypeDto> getProjectTypes();
}
