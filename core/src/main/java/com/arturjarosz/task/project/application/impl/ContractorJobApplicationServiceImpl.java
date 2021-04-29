package com.arturjarosz.task.project.application.impl;

import com.arturjarosz.task.project.application.ContractorJobApplicationService;
import com.arturjarosz.task.project.application.ContractorJobValidator;
import com.arturjarosz.task.project.application.ProjectValidator;
import com.arturjarosz.task.project.application.dto.ContractorJobDto;
import com.arturjarosz.task.project.application.mapper.CooperatorJobDtoMapper;
import com.arturjarosz.task.project.infrastructure.repositor.ProjectRepository;
import com.arturjarosz.task.project.model.CooperatorJob;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.query.ProjectQueryService;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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

    @Override
    public ContractorJobDto createContractorJob(Long projectId,
                                                ContractorJobDto contractorJobDto) {
        LOG.debug("Creating ContractorJob for Project with id {}", projectId);
        this.projectValidator.validateProjectExistence(projectId);
        this.contractorJobValidator.validateCreateContractorJobDto(contractorJobDto);
        this.contractorJobValidator.validateContractorExistence(contractorJobDto.getContractorId());
        Project project = this.projectRepository.load(projectId);
        CooperatorJob cooperatorJob = CooperatorJobDtoMapper.INSTANCE
                .contractorJobCreateDtoToCooperatorJob(contractorJobDto);
        project.addCooperatorJob(cooperatorJob);
        this.projectRepository.save(project);
        return CooperatorJobDtoMapper.INSTANCE.cooperatorJobToContractorJobDto(cooperatorJob);
    }

    @Override
    public void deleteContractorJob(Long projectId, Long contractorJobId) {
        LOG.debug("Removing ContractorJob with id {} from Project with id {}", contractorJobId, projectId);
        this.projectValidator.validateProjectExistence(projectId);
        Project project = this.projectRepository.load(projectId);
        this.contractorJobValidator.validateContractorJobOnProjectExistence(project, contractorJobId);
        project.removeContractorJob(contractorJobId);
        this.projectRepository.save(project);
        LOG.debug("ContractorJob with id {} removed from Project with id {}", contractorJobId, projectId);
    }

    @Override
    public ContractorJobDto updateContractorJob(Long projectId, Long contractorJobId,
                                                ContractorJobDto contractorJobDto) {
        LOG.debug("Updating ContractorJob with id {} from Project with id {}", contractorJobId, projectId);
        this.projectValidator.validateProjectExistence(projectId);
        Project project = this.projectRepository.load(projectId);
        this.contractorJobValidator.validateContractorJobOnProjectExistence(project, contractorJobId);
        this.contractorJobValidator.validateUpdateContractorJobDto(contractorJobDto);
        CooperatorJob cooperatorJob = project
                .updateContractorJob(contractorJobId, contractorJobDto.getName(), contractorJobDto.getValue(),
                        contractorJobDto.getNote());
        this.projectRepository.save(project);
        LOG.debug("ContractorJob with id {} updated on Project with id {}", contractorJobId, projectId);
        return CooperatorJobDtoMapper.INSTANCE.cooperatorJobToContractorJobDto(cooperatorJob);
    }

    @Override
    public ContractorJobDto getContractorJob(Long projectId, Long contractorJobId) {
        LOG.debug("Loading ContractorJob with id {} for Project with id {}", contractorJobId, projectId);
        this.projectValidator.validateProjectExistence(projectId);
        Project project = this.projectRepository.load(projectId);
        this.contractorJobValidator.validateContractorJobOnProjectExistence(project, contractorJobId);
        CooperatorJob cooperatorJob = this.projectQueryService.getCooperatorJobByIdForProject(contractorJobId);
        return CooperatorJobDtoMapper.INSTANCE.cooperatorJobToContractorJobDto(cooperatorJob);
    }
}
