package com.arturjarosz.task.project.application;

import com.arturjarosz.task.project.application.dto.ProjectContractDto;
import com.arturjarosz.task.project.application.dto.ProjectCreateDto;
import com.arturjarosz.task.project.application.dto.ProjectDto;
import com.arturjarosz.task.project.infrastructure.repositor.ProjectRepository;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.sharedkernel.exceptions.BaseValidator;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import org.springframework.stereotype.Component;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotEmpty;
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
        validateProjectName(projectCreateDto.getName());
        assertIsTrue(projectCreateDto.getClientId() != null, BaseValidator
                .createMessageCode(ExceptionCodes.IS_NULL, ProjectExceptionCodes.PROJECT,
                        ProjectExceptionCodes.CLIENT));
        assertIsTrue(projectCreateDto.getArchitectId() != null, BaseValidator
                .createMessageCode(ExceptionCodes.IS_NULL, ProjectExceptionCodes.PROJECT,
                        ProjectExceptionCodes.ARCHITECT));
        assertIsTrue(projectCreateDto.getProjectType() != null, BaseValidator
                .createMessageCode(ExceptionCodes.IS_NULL, ProjectExceptionCodes.PROJECT, ProjectExceptionCodes.TYPE));
    }

    public static void validateProjectExistence(Project project, Long projectId) {
        assertIsTrue(project != null,
                createMessageCode(ExceptionCodes.NOT_EXISTS, ProjectExceptionCodes.PROJECT), projectId);
    }

    public void validateProjectExistence(Long projectId) {
        Project project = this.projectRepository.load(projectId);
        validateProjectExistence(project, projectId);
    }

    public static void validateProjectName(String projectName) {
        assertIsTrue(projectName != null,
                createMessageCode(ExceptionCodes.IS_NULL, ProjectExceptionCodes.PROJECT, ProjectExceptionCodes.NAME));
        assertNotEmpty(projectName, ExceptionCodes.EMPTY, ProjectExceptionCodes.PROJECT,
                ProjectExceptionCodes.NAME);
    }

    public static void validateProjectContractDto(ProjectContractDto projectContractDto) {
        assertIsTrue(projectContractDto != null,
                createMessageCode(ExceptionCodes.IS_NULL, ProjectExceptionCodes.CONTRACT));
        assertIsTrue(projectContractDto.getSigningDate() != null,
                createMessageCode(ExceptionCodes.IS_NULL, ProjectExceptionCodes.PROJECT,
                        ProjectExceptionCodes.SIGNING_DATE));
        assertIsTrue(projectContractDto.getSigningDate() != null,
                createMessageCode(ExceptionCodes.IS_NULL, ProjectExceptionCodes.PROJECT,
                        ProjectExceptionCodes.START_DATE));
        assertIsTrue(projectContractDto.getSigningDate() != null,
                createMessageCode(ExceptionCodes.IS_NULL, ProjectExceptionCodes.PROJECT,
                        ProjectExceptionCodes.DEADLINE));
    }

    public static void validateUpdateProjectDto(ProjectDto projectDto) {
        validateProjectName(projectDto.getName());
    }

}
