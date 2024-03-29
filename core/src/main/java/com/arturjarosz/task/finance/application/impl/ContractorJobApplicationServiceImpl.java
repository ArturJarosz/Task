package com.arturjarosz.task.finance.application.impl;

import com.arturjarosz.task.dto.ContractorJobDto;
import com.arturjarosz.task.finance.application.ContractorJobApplicationService;
import com.arturjarosz.task.finance.application.mapper.ContractorJobMapper;
import com.arturjarosz.task.finance.application.validator.ContractorJobValidator;
import com.arturjarosz.task.finance.infrastructure.ProjectFinancialDataRepository;
import com.arturjarosz.task.finance.query.FinancialDataQueryService;
import com.arturjarosz.task.project.application.ProjectValidator;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@ApplicationService
public class ContractorJobApplicationServiceImpl implements ContractorJobApplicationService {

    @NonNull
    private final ContractorJobValidator contractorJobValidator;
    @NonNull
    private final ProjectFinanceAwareObjectServiceImpl projectFinanceAwareObjectService;
    @NonNull
    private final ProjectValidator projectValidator;
    @NonNull
    private final ProjectFinancialDataRepository projectFinancialDataRepository;
    @NonNull
    private final FinancialDataQueryService financialDataQueryService;
    @NonNull
    private final ContractorJobMapper contractorJobMapper;

    @Transactional
    @Override
    public ContractorJobDto createContractorJob(Long projectId, ContractorJobDto contractorJobDto) {
        LOG.debug("Creating ContractorJob for Project with id {}", projectId);

        this.projectValidator.validateProjectExistence(projectId);
        this.contractorJobValidator.validateCreateContractorJobDto(contractorJobDto);
        this.contractorJobValidator.validateContractorExistence(contractorJobDto.getContractorId());

        var financialData = this.projectFinancialDataRepository.getProjectFinancialDataByProjectId(projectId);

        var contractorJob = this.contractorJobMapper.mapFromDto(contractorJobDto);
        financialData.addContractorJob(contractorJob);

        this.projectFinanceAwareObjectService.onCreate(projectId);
        this.projectFinancialDataRepository.save(financialData);
        LOG.debug("ContractorJob for Project with id {} created", projectId);

        return this.contractorJobMapper.mapToDto(contractorJob, projectId);
    }

    @Transactional
    @Override
    public ContractorJobDto updateContractorJob(Long projectId, Long contractorJobId,
            ContractorJobDto contractorJobDto) {
        LOG.debug("Updating ContractorJob with id {} from Project with id {}", contractorJobId, projectId);

        this.projectValidator.validateProjectExistence(projectId);
        this.contractorJobValidator.validateContractorJobOnProjectExistence(projectId, contractorJobId);
        this.contractorJobValidator.validateUpdateContractorJobDto(contractorJobDto);
        var financialData = this.projectFinancialDataRepository.getProjectFinancialDataByProjectId(projectId);

        var contractorJob = financialData.updateContractorJob(contractorJobId, contractorJobDto);
        this.projectFinancialDataRepository.save(financialData);
        this.projectFinanceAwareObjectService.onUpdate(projectId);

        LOG.debug("ContractorJob with id {} updated on Project with id {}", contractorJobId, projectId);
        return this.contractorJobMapper.mapToDto(contractorJob, projectId);
    }

    @Override
    public ContractorJobDto getContractorJob(Long projectId, Long contractorJobId) {
        LOG.debug("Loading ContractorJob with id {} for Project with id {}", contractorJobId, projectId);
        this.projectValidator.validateProjectExistence(projectId);
        var contractorJobDto = this.financialDataQueryService.getContractorJobById(contractorJobId, projectId);
        this.contractorJobValidator.validateContractorJobExistence(contractorJobDto, projectId, contractorJobId);
        return contractorJobDto;
    }

    @Transactional
    @Override
    public void deleteContractorJob(Long projectId, Long contractorJobId) {
        LOG.debug("Removing ContractorJob with id {} from Project with id {}", contractorJobId, projectId);

        this.projectValidator.validateProjectExistence(projectId);
        this.contractorJobValidator.validateContractorJobOnProjectExistence(projectId, contractorJobId);
        var financialData = this.projectFinancialDataRepository.getProjectFinancialDataByProjectId(projectId);

        financialData.removeContractorJob(contractorJobId);
        this.projectFinancialDataRepository.save(financialData);
        this.projectFinanceAwareObjectService.onRemove(projectId);
        LOG.debug("ContractorJob with id {} removed from Project with id {}", contractorJobId, projectId);
    }
}
