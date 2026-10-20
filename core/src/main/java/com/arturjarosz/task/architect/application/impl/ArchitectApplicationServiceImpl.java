package com.arturjarosz.task.architect.application.impl;

import com.arturjarosz.task.architect.application.ArchitectApplicationService;
import com.arturjarosz.task.architect.application.ArchitectValidator;
import com.arturjarosz.task.architect.application.mapper.ArchitectMapper;
import com.arturjarosz.task.architect.infrastructure.repository.ArchitectRepository;
import com.arturjarosz.task.contract.query.ContractQueryService;
import com.arturjarosz.task.dto.ArchitectDto;
import com.arturjarosz.task.dto.EntityProjectsSummaryDto;
import com.arturjarosz.task.project.application.mapper.ProjectsToEntityProjectsSummaryDtoMapper;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.query.ProjectCounts;
import com.arturjarosz.task.project.query.ProjectQueryService;
import com.arturjarosz.task.properties.ProjectProperties;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static com.arturjarosz.task.architect.application.ArchitectValidator.validateArchitectDto;
import static com.arturjarosz.task.architect.application.ArchitectValidator.validateArchitectExistence;

@Slf4j
@RequiredArgsConstructor
@ApplicationService
public class ArchitectApplicationServiceImpl implements ArchitectApplicationService {

    private final ArchitectRepository architectRepository;
    private final ArchitectValidator architectValidator;
    private final ArchitectMapper architectMapper;
    private final ProjectQueryService projectQueryService;
    private final ContractQueryService contractQueryService;
    private final ProjectsToEntityProjectsSummaryDtoMapper projectsToEntityProjectsSummaryDtoMapper;
    private final ProjectProperties projectProperties;

    @Transactional
    @Override
    public ArchitectDto createArchitect(ArchitectDto architectDto) {
        LOG.info("creating architect");
        validateArchitectDto(architectDto);
        var architect = this.architectMapper.mapFromDto(architectDto);
        architect = this.architectRepository.save(architect);
        LOG.info("architect created");
        return this.architectMapper.mapToDto(architect);
    }

    @Transactional
    @Override
    public void removeArchitect(Long architectId) {
        LOG.info("removing architect");

        this.architectValidator.validateArchitectExistence(architectId);
        this.architectValidator.validateArchitectHasNoProjects(architectId);
        this.architectRepository.deleteById(architectId);

        LOG.info("architect with id {} removed", architectId);
    }

    @Override
    public ArchitectDto getArchitect(Long architectId) {
        var maybeArchitect = this.architectRepository.findById(architectId);
        validateArchitectExistence(maybeArchitect, architectId);

        LOG.info("architect with id {} loaded", architectId);
        return this.withProjectsData(List.of(this.architectMapper.mapToDto(
                maybeArchitect.orElseThrow(ResourceNotFoundException::new)))).get(0);
    }

    @Transactional
    @Override
    public ArchitectDto updateArchitect(Long architectId, ArchitectDto architectDto) {
        LOG.info("updating architect with id {}", architectId);

        var maybeArchitect = this.architectRepository.findById(architectId);
        validateArchitectExistence(maybeArchitect, architectId);
        validateArchitectDto(architectDto);
        var architect = maybeArchitect.orElseThrow(ResourceNotFoundException::new);
        architect.updateArchitectName(architectDto.getFirstName(), architectDto.getLastName());
        architect = this.architectRepository.save(architect);

        LOG.info("architect with id {} updated", architectId);

        return this.architectMapper.mapToDto(architect);
    }

    @Override
    public List<ArchitectDto> getArchitects() {
        LOG.info("Getting all architects.");
        return this.withProjectsData(this.architectRepository.findAll()
                .stream()
                .map(this.architectMapper::mapToDto)
                .toList());
    }

    @Override
    public EntityProjectsSummaryDto getProjectsSummary(Long architectId) {
        LOG.info("Getting projects summary for architect with id {}.", architectId);
        this.architectValidator.validateArchitectExistence(architectId);

        var projects = this.projectQueryService.getProjectsForArchitect(architectId);
        var contractValueByProjectId = this.contractQueryService.getContractValuesForProjects(projects.stream().map(
                Project::getId).toList());
        return this.projectsToEntityProjectsSummaryDtoMapper.mapToProjectSummaryDto(projects, contractValueByProjectId);
    }

    private List<ArchitectDto> withProjectsData(List<ArchitectDto> architectDtos) {
        var architectIds = architectDtos.stream().map(ArchitectDto::getId).toList();
        var countsByArchitectId = this.projectQueryService.getProjectCountsByArchitectIds(architectIds,
                this.projectProperties.activeStatuses());
        var valueByArchitectId = this.contractQueryService.getProjectsValueByArchitectIds(architectIds);
        architectDtos.forEach(architectDto -> {
            var counts = countsByArchitectId.getOrDefault(architectDto.getId(), ProjectCounts.NONE);
            architectDto.setNumberOfProjects((int) counts.all());
            architectDto.setNumberOfActiveProjects((int) counts.active());
            architectDto.setProjectsValue(
                    valueByArchitectId.getOrDefault(architectDto.getId(), BigDecimal.ZERO).doubleValue());
        });
        return architectDtos;
    }
}
