package com.arturjarosz.task.finance.application.impl;

import com.arturjarosz.task.dto.InstallmentDto;
import com.arturjarosz.task.dto.InstallmentProjectDataDto;
import com.arturjarosz.task.finance.application.InstallmentApplicationService;
import com.arturjarosz.task.finance.application.ProjectFinanceAwareObjectService;
import com.arturjarosz.task.finance.application.mapper.InstallmentMapper;
import com.arturjarosz.task.finance.application.mapper.InstallmentProjectSummaryMapper;
import com.arturjarosz.task.finance.application.validator.InstallmentValidator;
import com.arturjarosz.task.finance.infrastructure.ProjectFinancialDataRepository;
import com.arturjarosz.task.finance.model.Installment;
import com.arturjarosz.task.finance.model.ProjectFinancialData;
import com.arturjarosz.task.finance.query.FinancialDataQueryService;
import com.arturjarosz.task.project.application.ProjectValidator;
import com.arturjarosz.task.project.application.StageValidator;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@ApplicationService
public class InstallmentApplicationServiceImpl implements InstallmentApplicationService {
    @NonNull
    private final ProjectValidator projectValidator;
    @NonNull
    private final StageValidator stageValidator;
    @NonNull
    private final ProjectFinancialDataRepository projectFinancialDataRepository;
    @NonNull
    private final FinancialDataQueryService financialDataQueryService;
    @NonNull
    private final InstallmentValidator installmentValidator;
    @NonNull
    private final ProjectFinanceAwareObjectService projectFinanceAwareObjectService;
    @NonNull
    private final InstallmentMapper installmentDtoMapper;
    @NonNull
    private final InstallmentProjectSummaryMapper installmentProjectSummaryMapper;

    @Transactional
    @Override
    public InstallmentDto createInstallment(Long projectId, Long stageId, InstallmentDto installmentDto) {
        LOG.debug("Creating Installment for stage with stageId {} on project with projectId {}.", stageId, projectId);

        this.projectValidator.validateProjectExistence(projectId);
        this.stageValidator.validateExistenceOfStageInProject(projectId, stageId);
        this.stageValidator.validateStageNotHavingInstallment(projectId, stageId);

        this.installmentValidator.validateCreateInstallmentDto(installmentDto);
        var installment = this.installmentDtoMapper.mapFromDto(installmentDto, stageId);

        var projectFinancialData = this.projectFinancialDataRepository.getProjectFinancialDataByProjectId(projectId);
        projectFinancialData.addInstallment(installment);
        projectFinancialData = this.projectFinancialDataRepository.save(projectFinancialData);

        LOG.debug("Installment created.");
        var createdInstallment = this.installmentDtoMapper.mapToDto(installment, "");
        createdInstallment.setId(this.getIdForCreatedInstallment(projectFinancialData, installment));
        this.projectFinanceAwareObjectService.onCreate(projectId);
        return createdInstallment;
    }

    @Transactional
    @Override
    public InstallmentDto updateInstallment(Long projectId, Long installmentId, InstallmentDto installmentDto) {
        LOG.debug("Updating Installment with installmentId {}", installmentId);

        this.projectValidator.validateProjectExistence(projectId);
        this.installmentValidator.validateInstallmentExistence(installmentId);

        var projectFinancialData = this.projectFinancialDataRepository.getProjectFinancialDataByProjectId(projectId);
        var installment = projectFinancialData.getInstallment(installmentId);
        this.installmentValidator.validateInstallmentExistence(installment, installmentId, projectId);
        this.installmentValidator.validateUpdateInstallmentDto(installmentDto, installment.isPaid());
        installment = projectFinancialData.updateInstallment(installmentId, installmentDto);
        this.projectFinancialDataRepository.save(projectFinancialData);
        this.projectFinanceAwareObjectService.onUpdate(projectId);

        LOG.debug("Installment updated");
        return this.installmentDtoMapper.mapToDto(installment, "");
    }

    @Transactional
    @Override
    public void removeInstallment(Long projectId, Long installmentId) {
        LOG.debug("Removing Installment with installmentId {}", installmentId);
        this.projectValidator.validateProjectExistence(projectId);

        var projectFinancialData = this.projectFinancialDataRepository.getProjectFinancialDataByProjectId(projectId);
        var installment = projectFinancialData.getInstallment(installmentId);
        this.installmentValidator.validateInstallmentExistence(installment, installmentId, projectId);
        this.installmentValidator.validateInstallmentNotPaid(installment);
        projectFinancialData.removeInstallment(installmentId);
        this.projectFinancialDataRepository.save(projectFinancialData);
        this.projectFinanceAwareObjectService.onRemove(projectId);

        LOG.debug("Installment removed");
    }

    @Transactional
    @Override
    public InstallmentDto payInstallment(Long projectId, Long installmentId, InstallmentDto installmentDto) {
        LOG.debug("Paying for Installment with installmentId {}", installmentId);
        this.projectValidator.validateProjectExistence(projectId);

        var projectFinancialData = this.projectFinancialDataRepository.getProjectFinancialDataByProjectId(projectId);
        var installment = projectFinancialData.getInstallment(installmentId);
        this.installmentValidator.validateInstallmentExistence(installment, installmentId, projectId);
        this.installmentValidator.validatePayInstallmentDto(installmentDto, installment.isPaid());
        installment = projectFinancialData.payInstallment(installmentId, installmentDto.getPaymentDate());
        this.projectFinancialDataRepository.save(projectFinancialData);

        LOG.debug("Payment for Installment with id {} made", installmentId);
        return this.installmentDtoMapper.mapToDto(installment, "");
    }

    @Override
    public InstallmentProjectDataDto getProjectInstallments(Long projectId) {
        LOG.debug("Getting list of Installment for project with id {}", projectId);
        this.projectValidator.validateProjectExistence(projectId);

        var installmentProjectSummary = this.financialDataQueryService.getInstallmentDataForProject(projectId);
        var installments = this.financialDataQueryService.getInstallmentsByProjectId(projectId);

        return this.installmentProjectSummaryMapper.mapToProjectFinancialPartialDataDto(installmentProjectSummary,
                installments);
    }

    @Override
    public InstallmentDto getInstallment(Long projectId, Long installmentId) {
        LOG.debug("Getting Installment with id {}", installmentId);

        this.projectValidator.validateProjectExistence(projectId);
        var projectFinancialData = this.projectFinancialDataRepository.getProjectFinancialDataByProjectId(projectId);
        var installment = projectFinancialData.getInstallment(installmentId);

        return this.installmentDtoMapper.mapToDto(installment, "");
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
