package com.arturjarosz.task.project.application;

import com.arturjarosz.task.project.application.dto.ProjectCreateDto;
import com.arturjarosz.task.project.application.dto.ProjectDto;
import com.arturjarosz.task.project.infrastructure.repositor.ProjectRepository;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.query.ProjectQueryService;
import com.arturjarosz.task.sharedkernel.exceptions.BaseValidator;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
    private final ProjectQueryService projectQueryService;

    @Autowired
    public ProjectValidator(ProjectRepository projectRepository, ProjectQueryService projectQueryService) {
        this.projectRepository = projectRepository;
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
        assertNotNull(projectCreateDto.getProjectType(),
                BaseValidator.createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.PROJECT,
                        ProjectExceptionCodes.TYPE));
    }

    public void validateProjectExistence(Project project, Long projectId) {
        assertNotNull(project, createMessageCode(ExceptionCodes.NOT_EXIST, ProjectExceptionCodes.PROJECT), projectId);
    }

    public void validateProjectExistence(Long projectId) {
        Project project = this.projectRepository.load(projectId);
        this.validateProjectExistence(project, projectId);
    }

    public void validateUpdateProjectDto(ProjectDto projectDto) {
        assertNotNull(projectDto,
                createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.PROJECT, ProjectExceptionCodes.UPDATE));
        validateProjectName(projectDto.getName());
    }

    public void validateProjectNotSigned(Project project) {
        assertIsTrue(!project.isContractSigned(),
                createMessageCode(ExceptionCodes.ALREADY_SET, ProjectExceptionCodes.PROJECT,
                        ProjectExceptionCodes.SIGN));
    }

}
