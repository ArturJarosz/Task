package com.arturjarosz.task.project.application;

import com.arturjarosz.task.project.application.dto.ProjectContractDto;
import com.arturjarosz.task.project.application.dto.ProjectCreateDto;
import com.arturjarosz.task.project.application.dto.ProjectDto;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.sharedkernel.exceptions.BaseValidator;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotEmpty;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

public class ProjectDtoValidator {

    private ProjectDtoValidator() {
        throw new IllegalStateException(ExceptionCodes.NOT_FOR_INSTANTIATING);
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

    public static void validateProjectDto(ProjectDto projectDto) {
        validateProjectName(projectDto.getName());
        assertIsTrue(projectDto.getProjectType() != null,
                createMessageCode(ExceptionCodes.IS_NULL, ProjectExceptionCodes.PROJECT, ProjectExceptionCodes.TYPE));
        assertIsTrue(projectDto.getClient() != null,
                createMessageCode(ExceptionCodes.IS_NULL, ProjectExceptionCodes.PROJECT, ProjectExceptionCodes.CLIENT));
        assertIsTrue(projectDto.getArchitect() != null,
                createMessageCode(ExceptionCodes.IS_NULL, ProjectExceptionCodes.PROJECT,
                        ProjectExceptionCodes.ARCHITECT));
        assertIsTrue(projectDto.getArchitect().getId() != null,
                createMessageCode(ExceptionCodes.IS_NULL, ProjectExceptionCodes.PROJECT,
                        ProjectExceptionCodes.ARCHITECT));
        assertIsTrue(projectDto.getArchitect().getId() != null,
                createMessageCode(ExceptionCodes.IS_NULL, ProjectExceptionCodes.PROJECT,
                        ProjectExceptionCodes.ARCHITECT));
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
    }

    public static void validateUpdateProjectDto(ProjectDto projectDto) {
        validateProjectName(projectDto.getName());
    }

}
