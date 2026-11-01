package com.arturjarosz.task.project.application;

import com.arturjarosz.task.dto.ProjectTypeDto;
import com.arturjarosz.task.project.infrastructure.repositor.ProjectTypeRepository;
import com.arturjarosz.task.project.model.ProjectType;
import com.arturjarosz.task.project.query.ProjectQueryService;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertEntityPresent;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsFalse;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotEmpty;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotNull;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

/**
 * Validates project type dtos and ProjectType entities.
 */
@Component
@RequiredArgsConstructor
public class ProjectTypeValidator {

    public static final String PROJECT_TYPE_WITH_PROJECTS_MESSAGE_CODE = createMessageCode(ExceptionCodes.NOT_VALID,
            ProjectExceptionCodes.PROJECT_TYPE, ProjectExceptionCodes.PROJECTS);

    private final ProjectTypeRepository projectTypeRepository;
    private final ProjectQueryService projectQueryService;

    public void validateCreateProjectTypeDto(ProjectTypeDto projectTypeDto) {
        this.validateProjectTypeDto(projectTypeDto, null);
    }

    public void validateUpdateProjectTypeDto(ProjectTypeDto projectTypeDto, Long projectTypeId) {
        this.validateProjectTypeDto(projectTypeDto, projectTypeId);
    }

    /**
     * Validates the name of a new Project type (projectTypeId null) or of the renamed one with projectTypeId. Names
     * are compared without surrounding whitespace and regardless of letter case.
     */
    private void validateProjectTypeDto(ProjectTypeDto projectTypeDto, Long projectTypeId) {
        assertNotNull(projectTypeDto, createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.PROJECT_TYPE));
        assertNotNull(projectTypeDto.getName(), createMessageCode(ExceptionCodes.NULL,
                ProjectExceptionCodes.PROJECT_TYPE, ProjectExceptionCodes.NAME));
        var name = projectTypeDto.getName().strip();
        assertNotEmpty(name, createMessageCode(ExceptionCodes.EMPTY, ProjectExceptionCodes.PROJECT_TYPE,
                ProjectExceptionCodes.NAME));
        assertIsTrue(name.length() <= ProjectType.NAME_MAX_LENGTH,
                createMessageCode(ExceptionCodes.NOT_VALID, ProjectExceptionCodes.PROJECT_TYPE,
                        ProjectExceptionCodes.NAME, ProjectExceptionCodes.TOO_LONG), ProjectType.NAME_MAX_LENGTH);
        var nameTaken = projectTypeId == null
                ? this.projectTypeRepository.existsByNameIgnoreCase(name)
                : this.projectTypeRepository.existsByNameIgnoreCaseAndIdNot(name, projectTypeId);
        assertIsFalse(nameTaken, createMessageCode(ExceptionCodes.NOT_VALID, ProjectExceptionCodes.PROJECT_TYPE,
                ProjectExceptionCodes.NAME, ProjectExceptionCodes.UNIQUE), name);
    }

    public void validateProjectTypeExistence(Optional<ProjectType> maybeProjectType, Long projectTypeId) {
        assertEntityPresent(maybeProjectType.isPresent(),
                createMessageCode(ExceptionCodes.NOT_EXIST, ProjectExceptionCodes.PROJECT_TYPE), projectTypeId);
    }

    public void validateProjectTypeExistence(Long projectTypeId) {
        this.validateProjectTypeExistence(this.projectTypeRepository.findById(projectTypeId), projectTypeId);
    }

    public void validateProjectTypeHasNoProjects(Long projectTypeId) {
        var projectCounts = this.projectQueryService.getProjectCountsByProjectTypeIds(List.of(projectTypeId));
        assertIsTrue(projectCounts.get(projectTypeId).all() == 0, PROJECT_TYPE_WITH_PROJECTS_MESSAGE_CODE);
    }
}
