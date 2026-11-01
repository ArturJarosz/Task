package com.arturjarosz.task.project.application.impl;

import com.arturjarosz.task.dto.ProjectTypeDto;
import com.arturjarosz.task.project.application.ProjectTypeApplicationService;
import com.arturjarosz.task.project.application.ProjectTypeValidator;
import com.arturjarosz.task.project.application.mapper.ProjectTypeMapper;
import com.arturjarosz.task.project.infrastructure.repositor.ProjectTypeRepository;
import com.arturjarosz.task.project.model.ProjectType;
import com.arturjarosz.task.project.query.ProjectQueryService;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException;
import com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@ApplicationService
public class ProjectTypeApplicationServiceImpl implements ProjectTypeApplicationService {

    private final ProjectTypeRepository projectTypeRepository;
    private final ProjectQueryService projectQueryService;
    private final ProjectTypeValidator projectTypeValidator;
    private final ProjectTypeMapper projectTypeMapper;

    @Transactional
    @Override
    public ProjectTypeDto createProjectType(ProjectTypeDto projectTypeDto) {
        LOG.info("Creating Project type.");

        this.projectTypeValidator.validateCreateProjectTypeDto(projectTypeDto);
        var projectType = this.projectTypeRepository.save(new ProjectType(projectTypeDto.getName().strip()));

        LOG.info("Project type with id {} created.", projectType.getId());
        return this.projectTypeMapper.mapToDto(projectType, 0L);
    }

    @Transactional
    @Override
    public ProjectTypeDto updateProjectType(Long projectTypeId, ProjectTypeDto projectTypeDto) {
        LOG.info("Updating Project type with id {}.", projectTypeId);

        var maybeProjectType = this.projectTypeRepository.findById(projectTypeId);
        this.projectTypeValidator.validateProjectTypeExistence(maybeProjectType, projectTypeId);
        this.projectTypeValidator.validateUpdateProjectTypeDto(projectTypeDto, projectTypeId);
        var projectType = maybeProjectType.orElseThrow(ResourceNotFoundException::new);
        projectType.rename(projectTypeDto.getName().strip());
        projectType = this.projectTypeRepository.save(projectType);

        LOG.info("Project type with id {} updated.", projectTypeId);
        return this.projectTypeMapper.mapToDto(projectType, this.getNumberOfProjects(projectTypeId));
    }

    @Transactional
    @Override
    public void deleteProjectType(Long projectTypeId) {
        LOG.info("Deleting Project type with id {}.", projectTypeId);

        this.projectTypeValidator.validateProjectTypeExistence(projectTypeId);
        this.projectTypeValidator.validateProjectTypeHasNoProjects(projectTypeId);
        try {
            this.projectTypeRepository.deleteById(projectTypeId);
            // flushed here, so a project given this type after the check above fails the delete with the same message
            this.projectTypeRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalArgumentException(ProjectTypeValidator.PROJECT_TYPE_WITH_PROJECTS_MESSAGE_CODE, exception);
        }

        LOG.info("Project type with id {} deleted.", projectTypeId);
    }

    @Override
    public ProjectTypeDto getProjectType(Long projectTypeId) {
        LOG.info("Loading Project type with id {}.", projectTypeId);

        var maybeProjectType = this.projectTypeRepository.findById(projectTypeId);
        this.projectTypeValidator.validateProjectTypeExistence(maybeProjectType, projectTypeId);
        var projectType = maybeProjectType.orElseThrow(ResourceNotFoundException::new);
        return this.projectTypeMapper.mapToDto(projectType, this.getNumberOfProjects(projectTypeId));
    }

    @Override
    public List<ProjectTypeDto> getProjectTypes() {
        LOG.info("Loading Project types list.");

        var projectTypes = this.projectTypeRepository.findAll(Sort.by("name"));
        var projectCountsByProjectTypeId = this.projectQueryService.getProjectCountsByProjectTypeIds(
                projectTypes.stream().map(ProjectType::getId).toList());
        return projectTypes.stream()
                .map(projectType -> this.projectTypeMapper.mapToDto(projectType,
                        projectCountsByProjectTypeId.get(projectType.getId()).all()))
                .toList();
    }

    private Long getNumberOfProjects(Long projectTypeId) {
        return this.projectQueryService.getProjectCountsByProjectTypeIds(List.of(projectTypeId))
                .get(projectTypeId)
                .all();
    }
}
