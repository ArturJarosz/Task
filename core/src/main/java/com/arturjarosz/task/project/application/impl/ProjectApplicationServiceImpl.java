package com.arturjarosz.task.project.application.impl;

import com.arturjarosz.task.architect.application.ArchitectApplicationService;
import com.arturjarosz.task.architect.application.ArchitectValidator;
import com.arturjarosz.task.client.application.ClientApplicationService;
import com.arturjarosz.task.client.application.ClientValidator;
import com.arturjarosz.task.contract.application.ContractService;
import com.arturjarosz.task.contract.application.mapper.ContractMapper;
import com.arturjarosz.task.dto.ProjectCreateDto;
import com.arturjarosz.task.dto.ProjectDto;
import com.arturjarosz.task.finance.application.CostApplicationService;
import com.arturjarosz.task.finance.application.ProjectFinancialDataService;
import com.arturjarosz.task.project.application.ProjectApplicationService;
import com.arturjarosz.task.project.application.ProjectValidator;
import com.arturjarosz.task.project.application.mapper.ProjectMapper;
import com.arturjarosz.task.project.domain.ProjectDomainService;
import com.arturjarosz.task.project.infrastructure.repositor.ProjectRepository;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@ApplicationService
public class ProjectApplicationServiceImpl implements ProjectApplicationService {

    @NonNull
    private final ClientApplicationService clientApplicationService;
    @NonNull
    private final ClientValidator clientValidator;
    @NonNull
    private final ArchitectApplicationService architectApplicationService;
    @NonNull
    private final ArchitectValidator architectValidator;
    @NonNull
    private final ProjectRepository projectRepository;
    @NonNull
    private final ProjectDomainService projectDomainService;
    @NonNull
    private final ProjectValidator projectValidator;
    @NonNull
    private final ProjectFinancialDataService projectFinancialDataService;
    @NonNull
    private final ContractService contractService;
    @NonNull
    private final ContractMapper contractMapper;
    @NonNull
    private final ProjectMapper projectMapper;
    @NonNull
    private final CostApplicationService costApplicationService;

    @Transactional
    @Override
    public ProjectDto createProject(ProjectCreateDto projectCreateDto) {
        LOG.debug("Creating Project.");

        this.projectValidator.validateProjectBasicDto(projectCreateDto);
        this.architectValidator.validateArchitectExistence(projectCreateDto.getArchitectId());
        this.clientValidator.validateClientExistence(projectCreateDto.getClientId());

        var contractDto = this.contractMapper.projectDtoToContractDto(projectCreateDto);
        var contract = this.contractService.createContract(contractDto);
        var project = this.projectDomainService.createProject(projectCreateDto, contract.getId());
        project = this.projectRepository.save(project);

        this.projectFinancialDataService.createProjectFinancialData(project.getId());

        LOG.debug("Project created.");
        return this.projectMapper.mapToDto(project, contract);
    }

    @Override
    public ProjectDto getProject(Long projectId) {
        LOG.debug("Loading Project with id {}.", projectId);

        var maybeProject = this.projectRepository.findById(projectId);
        this.projectValidator.validateProjectExistence(maybeProject, projectId);
        var project = maybeProject.orElseThrow(ResourceNotFoundException::new);
        var clientDto = this.clientApplicationService.getClientBasicData(project.getClientId());
        var architectDto = this.architectApplicationService.getArchitect(project.getArchitectId());
        var contractDto = this.contractService.getContractForProject(project.getContractId());
        var projectCosts = this.costApplicationService.getCosts(projectId);

        LOG.debug("Project with id {} loaded.", projectId);
        return this.projectMapper.mapToDto(clientDto, architectDto, project, contractDto, projectCosts);
    }

    @Transactional
    @Override
    public ProjectDto updateProject(Long projectId, ProjectDto projectDto) {
        LOG.debug("Updating Project with id {}.", projectId);

        var maybeProject = this.projectRepository.findById(projectId);
        this.projectValidator.validateProjectExistence(maybeProject, projectId);
        this.projectValidator.validateUpdateProjectDto(projectDto);
        var project = maybeProject.orElseThrow(ResourceNotFoundException::new);
        project = this.projectDomainService.updateProject(project, projectDto);
        project = this.projectRepository.save(project);

        LOG.debug("Project with id {} updated", projectId);
        return this.projectMapper.mapToDto(project);
    }

    @Transactional
    @Override
    public void removeProject(Long projectId) {
        LOG.debug("Removing Project with id {}.", projectId);

        this.projectValidator.validateProjectExistence(projectId);
        this.projectFinancialDataService.removeFinancialDataForProject(projectId);
        this.projectRepository.deleteById(projectId);

        LOG.debug("Project with id {} removed.", projectId);
    }

    @Transactional
    @Override
    public ProjectDto finishProject(Long projectId, ProjectDto projectContractDto) {
        LOG.debug("Finishing Project with id {}.", projectId);

        var maybeProject = this.projectRepository.findById(projectId);
        this.projectValidator.validateProjectExistence(maybeProject, projectId);
        var project = maybeProject.orElseThrow(ResourceNotFoundException::new);
        project = this.projectDomainService.finishProject(project, projectContractDto.getEndDate());
        this.projectRepository.save(project);

        LOG.debug("Project with id {} is finished.", projectId);
        return this.projectMapper.mapToDto(project);
    }

    @Override
    public List<ProjectDto> getProjects() {
        LOG.debug("Loading list of projects.");

        return this.projectRepository.findAll().stream().map(project -> {
            var clientDto = this.clientApplicationService.getClientBasicData(project.getClientId());
            var architectDto = this.architectApplicationService.getArchitect(project.getArchitectId());
            return this.projectMapper.mapToDto(clientDto, architectDto, project);
        }).toList();
    }

    @Transactional
    @Override
    public ProjectDto rejectProject(Long projectId) {
        LOG.debug("Rejecting Project with id {}.", projectId);

        var maybeProject = this.projectRepository.findById(projectId);
        this.projectValidator.validateProjectExistence(maybeProject, projectId);
        var project = maybeProject.orElseThrow(ResourceNotFoundException::new);
        project = this.projectDomainService.rejectProject(project);
        this.projectRepository.save(project);

        return this.projectMapper.mapToDto(project);
    }

    @Transactional
    @Override
    public ProjectDto reopenProject(Long projectId) {
        LOG.debug("Reopening Project with id {}.", projectId);

        var maybeProject = this.projectRepository.findById(projectId);
        this.projectValidator.validateProjectExistence(maybeProject, projectId);
        var project = maybeProject.orElseThrow(ResourceNotFoundException::new);
        project = this.projectDomainService.reopenProject(project);
        this.projectRepository.save(project);

        return this.projectMapper.mapToDto(project);
    }
}
