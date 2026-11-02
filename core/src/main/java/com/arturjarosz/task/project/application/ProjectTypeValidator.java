package com.arturjarosz.task.project.application;

import com.arturjarosz.task.project.infrastructure.repositor.ProjectTypeRepository;
import com.arturjarosz.task.project.model.ProjectType;
import com.arturjarosz.task.project.query.ProjectQueryService;
import com.arturjarosz.task.sharedkernel.namedentry.NamedEntryValidator;
import org.springframework.stereotype.Component;

/**
 * Validates project type dtos and ProjectType entities.
 */
@Component
public class ProjectTypeValidator extends NamedEntryValidator<ProjectType> {

    public ProjectTypeValidator(ProjectTypeRepository projectTypeRepository, ProjectQueryService projectQueryService) {
        super(projectTypeRepository, ProjectExceptionCodes.PROJECT_TYPE, ProjectExceptionCodes.PROJECTS, projectQueryService::getNumberOfProjectsByProjectTypeIds);
    }
}
