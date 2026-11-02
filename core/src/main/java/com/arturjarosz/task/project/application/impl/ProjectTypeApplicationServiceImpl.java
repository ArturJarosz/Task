package com.arturjarosz.task.project.application.impl;

import com.arturjarosz.task.dto.ProjectTypeDto;
import com.arturjarosz.task.project.application.ProjectTypeApplicationService;
import com.arturjarosz.task.project.application.ProjectTypeValidator;
import com.arturjarosz.task.project.application.mapper.ProjectTypeMapper;
import com.arturjarosz.task.project.infrastructure.repositor.ProjectTypeRepository;
import com.arturjarosz.task.project.model.ProjectType;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.namedentry.AbstractNamedEntryApplicationService;

@ApplicationService
public class ProjectTypeApplicationServiceImpl extends AbstractNamedEntryApplicationService<ProjectType, ProjectTypeDto>
        implements ProjectTypeApplicationService {

    private final ProjectTypeMapper projectTypeMapper;

    public ProjectTypeApplicationServiceImpl(ProjectTypeRepository projectTypeRepository, ProjectTypeValidator projectTypeValidator, ProjectTypeMapper projectTypeMapper) {
        super(projectTypeRepository, projectTypeValidator, "Project type");
        this.projectTypeMapper = projectTypeMapper;
    }

    @Override
    protected ProjectType createEntry(String name) {
        return new ProjectType(name);
    }

    @Override
    protected String nameOf(ProjectTypeDto projectTypeDto) {
        return projectTypeDto.getName();
    }

    @Override
    protected ProjectTypeDto toDto(ProjectType projectType, Long numberOfUsages) {
        return this.projectTypeMapper.mapToDto(projectType, numberOfUsages);
    }
}
