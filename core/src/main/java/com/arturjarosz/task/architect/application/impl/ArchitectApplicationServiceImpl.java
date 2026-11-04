package com.arturjarosz.task.architect.application.impl;

import com.arturjarosz.task.architect.application.ArchitectApplicationService;
import com.arturjarosz.task.architect.application.ArchitectValidator;
import com.arturjarosz.task.architect.application.mapper.ArchitectMapper;
import com.arturjarosz.task.architect.infrastructure.repository.ArchitectRepository;
import com.arturjarosz.task.architect.model.Architect;
import com.arturjarosz.task.contract.query.ContractQueryService;
import com.arturjarosz.task.dto.ArchitectDto;
import com.arturjarosz.task.dto.ArchitectUserLinkDto;
import com.arturjarosz.task.dto.EntityProjectsSummaryDto;
import com.arturjarosz.task.project.application.mapper.ProjectsToEntityProjectsSummaryDtoMapper;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.query.ProjectCounts;
import com.arturjarosz.task.project.query.ProjectQueryService;
import com.arturjarosz.task.properties.ProjectProperties;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException;
import com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException;
import com.arturjarosz.task.user.application.UserApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.arturjarosz.task.architect.application.ArchitectValidator.validateArchitectDto;
import static com.arturjarosz.task.architect.application.ArchitectValidator.validateArchitectExistence;
import static com.arturjarosz.task.architect.application.ArchitectValidator.validateArchitectUserLinkDto;
import static com.arturjarosz.task.architect.application.ArchitectValidator.validateUserExistence;

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
    private final UserApplicationService userApplicationService;

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

    @Override
    public ArchitectDto getArchitectWithLinkedUser(Long architectId) {
        var maybeArchitect = this.architectRepository.findById(architectId);
        validateArchitectExistence(maybeArchitect, architectId);

        LOG.info("architect with id {} and its user loaded", architectId);
        return this.withProjectsData(this.mapWithLinkedUsers(
                List.of(maybeArchitect.orElseThrow(ResourceNotFoundException::new)))).get(0);
    }

    @Override
    public Map<Long, ArchitectDto> getArchitectsBasicData(Collection<Long> architectIds) {
        LOG.info("Getting basic data for {} architects.", architectIds.size());
        var existingIds = architectIds.stream().filter(Objects::nonNull).toList();
        if (existingIds.isEmpty()) {
            return Map.of();
        }
        return this.architectRepository.findAllById(existingIds).stream()
                .map(this.architectMapper::mapToDto)
                .collect(Collectors.toMap(ArchitectDto::getId, Function.identity()));
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

        return this.mapWithLinkedUsers(List.of(architect)).get(0);
    }

    @Override
    public List<ArchitectDto> getArchitects() {
        LOG.info("Getting all architects.");
        return this.withProjectsData(this.mapWithLinkedUsers(this.architectRepository.findAll(Sort.by("id"))));
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

    @Transactional
    @Override
    public ArchitectDto linkUser(Long architectId, ArchitectUserLinkDto architectUserLinkDto) {
        LOG.info("Linking user with architect with id {}.", architectId);

        var maybeArchitect = this.architectRepository.findById(architectId);
        validateArchitectExistence(maybeArchitect, architectId);
        validateArchitectUserLinkDto(architectUserLinkDto);
        var userId = architectUserLinkDto.getUserId();
        validateUserExistence(this.userApplicationService.getUserSummaries(List.of(userId)).containsKey(userId),
                userId);
        this.architectValidator.validateUserNotLinkedToOtherArchitect(userId, architectId);
        var architect = maybeArchitect.orElseThrow(ResourceNotFoundException::new);
        architect.linkUser(userId);
        architect = this.saveWithUniqueUser(architect, userId);

        LOG.info("User with id {} linked with architect with id {}.", userId, architectId);
        return this.withProjectsData(this.mapWithLinkedUsers(List.of(architect))).get(0);
    }

    @Transactional
    @Override
    public ArchitectDto unlinkUser(Long architectId) {
        LOG.info("Unlinking user from architect with id {}.", architectId);

        var maybeArchitect = this.architectRepository.findById(architectId);
        validateArchitectExistence(maybeArchitect, architectId);
        var architect = maybeArchitect.orElseThrow(ResourceNotFoundException::new);
        architect.unlinkUser();
        architect = this.architectRepository.save(architect);

        LOG.info("User unlinked from architect with id {}.", architectId);
        return this.withProjectsData(this.mapWithLinkedUsers(List.of(architect))).get(0);
    }

    /**
     * Saves and flushes, so a user linked with another architect in the meantime (after the validator checked it) fails
     * with the same message as the validator.
     */
    private Architect saveWithUniqueUser(Architect architect, Long userId) {
        try {
            return this.architectRepository.saveAndFlush(architect);
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalArgumentException(ArchitectValidator.getUserAlreadyLinkedMessageCode(), exception, userId);
        }
    }

    private List<ArchitectDto> mapWithLinkedUsers(List<Architect> architects) {
        var usersById = this.userApplicationService.getUserSummaries(
                architects.stream().map(Architect::getUserId).filter(Objects::nonNull).toList());
        return architects.stream().map(architect -> {
            var architectDto = this.architectMapper.mapToDto(architect);
            if (architect.getUserId() != null) {
                architectDto.setLinkedUser(usersById.get(architect.getUserId()));
            }
            return architectDto;
        }).toList();
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
