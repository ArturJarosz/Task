package com.arturjarosz.task.project.application.impl;

import com.arturjarosz.task.project.application.ContractorJobApplicationService;
import com.arturjarosz.task.project.application.ContractorJobValidator;
import com.arturjarosz.task.project.application.ProjectValidator;
import com.arturjarosz.task.project.application.dto.ContractorJobDto;
import com.arturjarosz.task.project.application.mapper.ContractorJobDtoMapper;
import com.arturjarosz.task.project.infrastructure.repositor.ProjectRepository;
import com.arturjarosz.task.project.model.ContractorJob;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.query.ProjectQueryService;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.transaction.Transactional;

@ApplicationService
public class ContractorJobApplicationServiceImpl implements ContractorJobApplicationService {
    private static final Logger LOG = LoggerFactory.getLogger(ContractorJobApplicationServiceImpl.class);
    private final ContractorJobValidator contractorJobValidator;
    private final ProjectQueryService projectQueryService;
    private final ProjectRepository projectRepository;
    private final ProjectValidator projectValidator;

    @Autowired
    public ContractorJobApplicationServiceImpl(ContractorJobValidator contractorJobValidator,
                                               ProjectQueryService projectQueryService,
                                               ProjectRepository projectRepository,
                                               ProjectValidator projectValidator) {
        this.contractorJobValidator = contractorJobValidator;
        this.projectQueryService = projectQueryService;
        this.projectRepository = projectRepository;
        this.projectValidator = projectValidator;
    }

    @Transactional
    @Override
    public ContractorJobDto createContractorJob(Long projectId, ContractorJobDto contractorJobDto) {
        LOG.debug("Creating ContractorJob for Project with id {}", projectId);

        this.projectValidator.validateProjectExistence(projectId);
        this.contractorJobValidator.validateCreateContractorJobDto(contractorJobDto);
        this.contractorJobValidator.validateContractorExistence(contractorJobDto.getContractorId());
        Project project = this.projectRepository.load(projectId);
        ContractorJob contractorJob = ContractorJobDtoMapper.INSTANCE.contractorJobDtoToContractorJob(contractorJobDto);
        project.addContractorJob(contractorJob);
        this.projectRepository.save(project);
        LOG.debug("ContractorJob for Project with id {} created", projectId);
        ContractorJobDto createdContractorJobDto = ContractorJobDtoMapper.INSTANCE.contractorJobToContractorJobDto(
                contractorJob, projectId);
        createdContractorJobDto.setId(this.getCreatedContractorJob(project, contractorJob).getId());
        return createdContractorJobDto;
    }

    @Transactional
    @Override
    public ContractorJobDto updateContractorJob(Long projectId, Long contractorJobId,
                                                ContractorJobDto contractorJobDto) {
        LOG.debug("Updating ContractorJob with id {} from Project with id {}", contractorJobId, projectId);

        this.projectValidator.validateProjectExistence(projectId);
        this.contractorJobValidator.validateContractorJobOnProjectExistence(projectId, contractorJobId);
        this.contractorJobValidator.validateUpdateContractorJobDto(contractorJobDto);
        Project project = this.projectRepository.load(projectId);
        ContractorJob contractorJob = project.updateContractorJob(contractorJobId, contractorJobDto);
        this.projectRepository.save(project);

        LOG.debug("ContractorJob with id {} updated on Project with id {}", contractorJobId, projectId);
        return ContractorJobDtoMapper.INSTANCE.contractorJobToContractorJobDto(contractorJob);
    }

    @Override
    public ContractorJobDto getContractorJob(Long projectId, Long contractorJobId) {
        LOG.debug("Loading ContractorJob with id {} for Project with id {}", contractorJobId, projectId);
        this.projectValidator.validateProjectExistence(projectId);
        this.contractorJobValidator.validateContractorJobOnProjectExistence(projectId, contractorJobId);
        return this.projectQueryService.getContractorJobForProject(contractorJobId, projectId);
    }

    @Transactional
    @Override
    public void deleteContractorJob(Long projectId, Long contractorJobId) {
        LOG.debug("Removing ContractorJob with id {} from Project with id {}", contractorJobId, projectId);
        this.projectValidator.validateProjectExistence(projectId);
        this.contractorJobValidator.validateContractorJobOnProjectExistence(projectId, contractorJobId);
        Project project = this.projectRepository.load(projectId);
        project.removeContractorJob(contractorJobId);
        this.projectRepository.save(project);
        LOG.debug("ContractorJob with id {} removed from Project with id {}", contractorJobId, projectId);
    }

    private ContractorJob getCreatedContractorJob(Project project, ContractorJob contractorJob) {
        return project.getContractorJobs().stream()
                .filter(contractorJobOnProject -> contractorJobOnProject.equals(contractorJob)).findFirst()
                .orElse(null);
    }
}
