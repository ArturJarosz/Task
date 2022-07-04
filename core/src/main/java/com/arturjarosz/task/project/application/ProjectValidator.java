package com.arturjarosz.task.project.application;

import com.arturjarosz.task.project.application.dto.ProjectCreateDto;
import com.arturjarosz.task.project.application.dto.ProjectDto;
import com.arturjarosz.task.project.infrastructure.repositor.ProjectRepository;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.sharedkernel.exceptions.BaseValidator;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotEmpty;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotNull;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

/**
 * Validates project dtos and Project Entities.
 */

@Component
public class ProjectValidator {

    private final ProjectRepository projectRepository;

    @Autowired
    public ProjectValidator(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
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
        assertNotNull(projectCreateDto.getProjectType(),
                BaseValidator.createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.PROJECT,
                        ProjectExceptionCodes.TYPE));
    }

    public void validateProjectExistence(Optional<Project> maybeProject, Long projectId) {
        assertIsTrue(maybeProject.isPresent(),
                createMessageCode(ExceptionCodes.NOT_EXIST, ProjectExceptionCodes.PROJECT), projectId);
    }

    public void validateProjectExistence(Long projectId) {
        Optional<Project> maybeProject = this.projectRepository.findById(projectId);
        this.validateProjectExistence(maybeProject, projectId);
    }

    public void validateUpdateProjectDto(ProjectDto projectDto) {
        assertNotNull(projectDto,
                createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.PROJECT, ProjectExceptionCodes.UPDATE));
        validateProjectName(projectDto.getName());
    }

}
