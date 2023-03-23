package com.arturjarosz.task.finance.application.impl;

import com.arturjarosz.task.finance.application.InstallmentApplicationService;
import com.arturjarosz.task.finance.application.InstallmentValidator;
import com.arturjarosz.task.finance.application.dto.InstallmentDto;
import com.arturjarosz.task.finance.application.mapper.InstallmentDtoMapper;
import com.arturjarosz.task.finance.infrastructure.ProjectFinancialDataRepository;
import com.arturjarosz.task.finance.model.Installment;
import com.arturjarosz.task.finance.model.ProjectFinancialData;
import com.arturjarosz.task.finance.query.FinancialDataQueryService;
import com.arturjarosz.task.project.application.ProjectValidator;
import com.arturjarosz.task.project.application.StageValidator;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@ApplicationService
public class InstallmentApplicationServiceImpl implements InstallmentApplicationService {
    private static final Logger LOG = LoggerFactory.getLogger(InstallmentApplicationServiceImpl.class);

    private final ProjectValidator projectValidator;
    private final StageValidator stageValidator;
    private final ProjectFinancialDataRepository projectFinancialDataRepository;
    private final FinancialDataQueryService financialDataQueryService;
    private final InstallmentValidator installmentValidator;

    @Autowired
    public InstallmentApplicationServiceImpl(ProjectValidator projectValidator, StageValidator stageValidator,
            ProjectFinancialDataRepository projectFinancialDataRepository,
            FinancialDataQueryService financialDataQueryService, InstallmentValidator installmentValidator) {
        this.projectValidator = projectValidator;
        this.stageValidator = stageValidator;
        this.projectFinancialDataRepository = projectFinancialDataRepository;
        this.financialDataQueryService = financialDataQueryService;
        this.installmentValidator = installmentValidator;
    }

    @Transactional
    @Override
    public InstallmentDto createInstallment(Long projectId, Long stageId, InstallmentDto installmentDto) {
        LOG.debug("Creating Installment for stage with stageId {} on project with projectId {}.", stageId, projectId);

        this.projectValidator.validateProjectExistence(projectId);
        this.stageValidator.validateExistenceOfStageInProject(projectId, stageId);
        this.stageValidator.validateStageNotHavingInstallment(projectId, stageId);

        this.installmentValidator.validateCreateInstallmentDto(installmentDto);
        Installment installment = InstallmentDtoMapper.INSTANCE.installmentDtoToInstallment(installmentDto, stageId);

        ProjectFinancialData projectFinancialData = this.projectFinancialDataRepository.getProjectFinancialDataByProjectId(
                projectId);
        projectFinancialData.addInstallment(installment);
        projectFinancialData = this.projectFinancialDataRepository.save(projectFinancialData);

        LOG.debug("Installment created.");
        InstallmentDto createdInstallment = InstallmentDtoMapper.INSTANCE.installmentToInstallmentDto(installment);
        createdInstallment.setId(this.getIdForCreatedInstallment(projectFinancialData, installment));
        return createdInstallment;
    }

    @Transactional
    @Override
    public InstallmentDto updateInstallment(Long projectId, Long installmentId, InstallmentDto installmentDto) {
        LOG.debug("Updating Installment with installmentId {}", installmentId);

        this.projectValidator.validateProjectExistence(projectId);
        this.installmentValidator.validateInstallmentExistence(installmentId);

        ProjectFinancialData projectFinancialData = this.projectFinancialDataRepository.getProjectFinancialDataByProjectId(
                projectId);
        Installment installment = projectFinancialData.getInstallment(installmentId);
        this.installmentValidator.validateUpdateInstallmentDto(installmentDto, installment.isPaid());
        installment = projectFinancialData.updateInstallment(installmentId, installmentDto);
        this.projectFinancialDataRepository.save(projectFinancialData);

        LOG.debug("Installment updated");
        return InstallmentDtoMapper.INSTANCE.installmentToInstallmentDto(installment);
    }

    @Transactional
    @Override
    public void removeInstallment(Long projectId, Long installmentId) {
        LOG.debug("Removing Installment with installmentId {}", installmentId);
        this.projectValidator.validateProjectExistence(projectId);

        ProjectFinancialData projectFinancialData = this.projectFinancialDataRepository.getProjectFinancialDataByProjectId(
                projectId);
        projectFinancialData.removeInstallment(installmentId);
        this.projectFinancialDataRepository.save(projectFinancialData);

        LOG.debug("Installment removed");
    }

    @Transactional
    @Override
    public InstallmentDto payInstallment(Long projectId, Long installmentId, InstallmentDto installmentDto) {
        LOG.debug("Paying for Installment with installmentId {}", installmentId);
        this.projectValidator.validateProjectExistence(projectId);

        ProjectFinancialData projectFinancialData = this.projectFinancialDataRepository.getProjectFinancialDataByProjectId(
                projectId);
        Installment installment = projectFinancialData.getInstallment(installmentId);
        this.installmentValidator.validatePayInstallmentDto(installmentDto, installment.isPaid());
        projectFinancialData.payInstallment(installmentId, installmentDto.getPaymentDate());
        this.projectFinancialDataRepository.save(projectFinancialData);

        LOG.debug("Payment for Installment with id {} made", installmentId);
        return InstallmentDtoMapper.INSTANCE.installmentToInstallmentDto(installment);
    }

    @Override
    public List<InstallmentDto> getProjectInstallments(Long projectId) {
        LOG.debug("Getting list of Installment for project with id {}", projectId);
        this.projectValidator.validateProjectExistence(projectId);

        return this.financialDataQueryService.getInstallmentsByProjectId(projectId);
    }

    @Override
    public InstallmentDto getInstallment(Long projectId, Long installmentId) {
        LOG.debug("Getting Installment with id {}", installmentId);

        this.projectValidator.validateProjectExistence(projectId);
        ProjectFinancialData projectFinancialData = this.projectFinancialDataRepository.getProjectFinancialDataByProjectId(
                projectId);
        Installment installment = projectFinancialData.getInstallment(installmentId);

        return InstallmentDtoMapper.INSTANCE.installmentToInstallmentDto(installment);
    }

    private Long getIdForCreatedInstallment(ProjectFinancialData financialData, Installment installment) {
        return financialData.getInstallments()
                .stream()
                .filter(installmentOnStage -> installmentOnStage.equals(installment))
                .map(Installment::getId)
                .findFirst()
                .orElse(null);
    }
}
