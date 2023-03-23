package com.arturjarosz.task.finance.application.impl;

import com.arturjarosz.task.finance.application.ContractorJobApplicationService;
import com.arturjarosz.task.finance.application.ContractorJobValidator;
import com.arturjarosz.task.finance.application.dto.ContractorJobDto;
import com.arturjarosz.task.finance.application.mapper.ContractorJobDtoMapper;
import com.arturjarosz.task.finance.infrastructure.ProjectFinancialDataRepository;
import com.arturjarosz.task.finance.model.ContractorJob;
import com.arturjarosz.task.finance.model.ProjectFinancialData;
import com.arturjarosz.task.finance.query.FinancialDataQueryService;
import com.arturjarosz.task.project.application.ProjectValidator;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@ApplicationService
public class ContractorJobApplicationServiceImpl implements ContractorJobApplicationService {
    private static final Logger LOG = LoggerFactory.getLogger(ContractorJobApplicationServiceImpl.class);
    private final ContractorJobValidator contractorJobValidator;
    private final ProjectFinanceAwareObjectServiceImpl projectFinanceAwareObjectService;
    private final ProjectValidator projectValidator;
    private final ProjectFinancialDataRepository projectFinancialDataRepository;
    private final FinancialDataQueryService financialDataQueryService;

    @Autowired
    public ContractorJobApplicationServiceImpl(ContractorJobValidator contractorJobValidator,
            ProjectFinanceAwareObjectServiceImpl projectFinanceAwareObjectService, ProjectValidator projectValidator,
            ProjectFinancialDataRepository projectFinancialDataRepository,
            FinancialDataQueryService financialDataQueryService) {
        this.contractorJobValidator = contractorJobValidator;
        this.projectFinanceAwareObjectService = projectFinanceAwareObjectService;
        this.projectValidator = projectValidator;
        this.projectFinancialDataRepository = projectFinancialDataRepository;
        this.financialDataQueryService = financialDataQueryService;
    }

    @Transactional
    @Override
    public ContractorJobDto createContractorJob(Long projectId, ContractorJobDto contractorJobDto) {
        LOG.debug("Creating ContractorJob for Project with id {}", projectId);

        this.projectValidator.validateProjectExistence(projectId);
        this.contractorJobValidator.validateCreateContractorJobDto(contractorJobDto);
        this.contractorJobValidator.validateContractorExistence(contractorJobDto.getContractorId());

        ProjectFinancialData financialData = this.projectFinancialDataRepository.getProjectFinancialDataByProjectId(
                projectId);

        ContractorJob contractorJob = ContractorJobDtoMapper.INSTANCE.contractorJobDtoToContractorJob(contractorJobDto);
        financialData.addContractorJob(contractorJob);

        this.projectFinanceAwareObjectService.onCreate(projectId);
        this.projectFinancialDataRepository.save(financialData);
        LOG.debug("ContractorJob for Project with id {} created", projectId);
        ContractorJobDto createdContractorJobDto = ContractorJobDtoMapper.INSTANCE.contractorJobToContractorJobDto(
                contractorJob, projectId);
        createdContractorJobDto.setId(this.getCreatedContractorJob(financialData, contractorJob).getId());
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
        ProjectFinancialData financialData = this.projectFinancialDataRepository.getProjectFinancialDataByProjectId(
                projectId);

        ContractorJob contractorJob = financialData.updateContractorJob(contractorJobId, contractorJobDto);
        this.projectFinancialDataRepository.save(financialData);
        this.projectFinanceAwareObjectService.onUpdate(projectId);

        LOG.debug("ContractorJob with id {} updated on Project with id {}", contractorJobId, projectId);
        return ContractorJobDtoMapper.INSTANCE.contractorJobToContractorJobDto(contractorJob, projectId);
    }

    @Override
    public ContractorJobDto getContractorJob(Long projectId, Long contractorJobId) {
        LOG.debug("Loading ContractorJob with id {} for Project with id {}", contractorJobId, projectId);
        this.projectValidator.validateProjectExistence(projectId);
        ContractorJobDto contractorJobDto = this.financialDataQueryService.getContractorJobById(contractorJobId);
        this.contractorJobValidator.validateContractorJobExistence(contractorJobDto, projectId, contractorJobId);
        return contractorJobDto;
    }

    @Transactional
    @Override
    public void deleteContractorJob(Long projectId, Long contractorJobId) {
        LOG.debug("Removing ContractorJob with id {} from Project with id {}", contractorJobId, projectId);

        this.projectValidator.validateProjectExistence(projectId);
        this.contractorJobValidator.validateContractorJobOnProjectExistence(projectId, contractorJobId);
        ProjectFinancialData financialData = this.projectFinancialDataRepository.getProjectFinancialDataByProjectId(
                projectId);

        financialData.removeContractorJob(contractorJobId);
        this.projectFinancialDataRepository.save(financialData);
        this.projectFinanceAwareObjectService.onRemove(projectId);
        LOG.debug("ContractorJob with id {} removed from Project with id {}", contractorJobId, projectId);
    }

    private ContractorJob getCreatedContractorJob(ProjectFinancialData financialData, ContractorJob contractorJob) {
        return financialData.getContractorJobs()
                .stream()
                .filter(contractorJobOnProject -> contractorJobOnProject.equals(contractorJob))
                .findFirst()
                .orElse(null);
    }
}
