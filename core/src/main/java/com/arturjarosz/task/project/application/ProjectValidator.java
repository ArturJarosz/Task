package com.arturjarosz.task.project.application;

import com.arturjarosz.task.dto.ProjectCreateDto;
import com.arturjarosz.task.dto.ProjectDto;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.query.ProjectQueryService;
import com.arturjarosz.task.sharedkernel.exceptions.BaseValidator;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertEntityPresent;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotEmpty;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotNull;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

/**
 * Validates project dtos and Project Entities.
 */

@Component
public class ProjectValidator {

    private final ProjectQueryService projectQueryService;

    @Autowired
    public ProjectValidator(ProjectQueryService projectQueryService) {
        this.projectQueryService = projectQueryService;
    }

    private static void validateProjectName(String projectName) {
        assertNotNull(projectName,
                createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.PROJECT, ProjectExceptionCodes.NAME));
        assertNotEmpty(projectName,
                createMessageCode(ExceptionCodes.EMPTY, ProjectExceptionCodes.PROJECT, ProjectExceptionCodes.NAME));
    }

    public void validateProjectBasicDto(ProjectCreateDto projectCreateDto) {
        assertNotNull(projectCreateDto, createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.PROJECT));
        validateProjectName(projectCreateDto.getName());
        assertNotNull(projectCreateDto.getClientId(),
                BaseValidator.createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.PROJECT,
                        ProjectExceptionCodes.CLIENT));
        assertNotNull(projectCreateDto.getArchitectId(),
                BaseValidator.createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.PROJECT,
                        ProjectExceptionCodes.ARCHITECT));
        assertNotNull(projectCreateDto.getType(),
                BaseValidator.createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.PROJECT,
                        ProjectExceptionCodes.TYPE));
    }

    public void validateProjectExistence(Optional<Project> maybeProject, Long projectId) {
        assertEntityPresent(maybeProject.isPresent(),
                createMessageCode(ExceptionCodes.NOT_EXIST, ProjectExceptionCodes.PROJECT), projectId);
    }

    public void validateProjectExistence(Long projectId) {
        var projectExists = this.projectQueryService.doesProjectExistByProjectId(projectId);
        assertEntityPresent(projectExists, createMessageCode(ExceptionCodes.NOT_EXIST, ProjectExceptionCodes.PROJECT),
                projectId);
    }

    public void validateUpdateProjectDto(ProjectDto projectDto) {
        assertNotNull(projectDto,
                createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.PROJECT, ProjectExceptionCodes.UPDATE));
        validateProjectName(projectDto.getName());
    }

}
