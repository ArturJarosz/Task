package com.arturjarosz.task.project.application;

import com.arturjarosz.task.project.application.dto.ProjectContractDto;
import com.arturjarosz.task.project.application.dto.ProjectCreateDto;
import com.arturjarosz.task.project.application.dto.ProjectDto;
import com.arturjarosz.task.project.infrastructure.repositor.ProjectRepository;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.sharedkernel.exceptions.BaseValidator;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import org.springframework.stereotype.Component;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotEmpty;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotNull;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

/**
 * Validates project dtos and Project Entities.
 */

@Component
public class ProjectValidator {

    private final ProjectRepository projectRepository;

    public ProjectValidator(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public static void validateProjectBasicDto(ProjectCreateDto projectCreateDto) {
        assertNotNull(projectCreateDto, createMessageCode(ExceptionCodes.IS_NULL, ProjectExceptionCodes.PROJECT));
        validateProjectName(projectCreateDto.getName());
        assertNotNull(projectCreateDto.getClientId(), BaseValidator.createMessageCode(ExceptionCodes.IS_NULL,
                ProjectExceptionCodes.PROJECT, ProjectExceptionCodes.CLIENT));
        assertNotNull(projectCreateDto.getArchitectId(), BaseValidator.createMessageCode(ExceptionCodes.IS_NULL,
                ProjectExceptionCodes.PROJECT, ProjectExceptionCodes.ARCHITECT));
        assertNotNull(projectCreateDto.getProjectType(), BaseValidator.createMessageCode(ExceptionCodes.IS_NULL,
                ProjectExceptionCodes.PROJECT, ProjectExceptionCodes.TYPE));
    }

    public static void validateProjectExistence(Project project, Long projectId) {
        assertNotNull(project, createMessageCode(ExceptionCodes.NOT_EXISTS, ProjectExceptionCodes.PROJECT), projectId);
    }

    public void validateProjectExistence(Long projectId) {
        Project project = this.projectRepository.load(projectId);
        validateProjectExistence(project, projectId);
    }

    private static void validateProjectName(String projectName) {
        assertNotNull(projectName, createMessageCode(ExceptionCodes.IS_NULL, ProjectExceptionCodes.PROJECT,
                ProjectExceptionCodes.NAME));
        assertNotEmpty(projectName, createMessageCode(ExceptionCodes.EMPTY, ProjectExceptionCodes.PROJECT,
                ProjectExceptionCodes.NAME));
    }

    public static void validateProjectContractDto(ProjectContractDto projectContractDto) {
        assertNotNull(projectContractDto, createMessageCode(ExceptionCodes.IS_NULL, ProjectExceptionCodes.CONTRACT));
        assertNotNull(projectContractDto.getSigningDate(), createMessageCode(ExceptionCodes.IS_NULL,
                ProjectExceptionCodes.PROJECT, ProjectExceptionCodes.SIGNING_DATE));
        assertNotNull(projectContractDto.getStartDate(), createMessageCode(ExceptionCodes.IS_NULL,
                ProjectExceptionCodes.PROJECT, ProjectExceptionCodes.START_DATE));
        assertNotNull(projectContractDto.getDeadline(), createMessageCode(ExceptionCodes.IS_NULL,
                ProjectExceptionCodes.PROJECT, ProjectExceptionCodes.DEADLINE));
    }

    public static void validateUpdateProjectDto(ProjectDto projectDto) {
        assertNotNull(projectDto, createMessageCode(ExceptionCodes.IS_NULL,
                ProjectExceptionCodes.PROJECT, ProjectExceptionCodes.UPDATE));
        validateProjectName(projectDto.getName());
    }

}
