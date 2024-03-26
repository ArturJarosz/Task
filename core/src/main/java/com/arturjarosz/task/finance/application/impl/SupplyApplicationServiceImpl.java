package com.arturjarosz.task.finance.application.impl;

import com.arturjarosz.task.dto.SupplyDto;
import com.arturjarosz.task.finance.application.ProjectFinanceAwareObjectService;
import com.arturjarosz.task.finance.application.SupplyApplicationService;
import com.arturjarosz.task.finance.application.mapper.SupplyMapper;
import com.arturjarosz.task.finance.application.validator.SupplyValidator;
import com.arturjarosz.task.finance.infrastructure.ProjectFinancialDataRepository;
import com.arturjarosz.task.finance.query.FinancialDataQueryService;
import com.arturjarosz.task.project.application.ProjectValidator;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@ApplicationService
public class SupplyApplicationServiceImpl implements SupplyApplicationService {

    @NonNull
    private final ProjectFinanceAwareObjectService projectFinanceAwareObjectService;
    @NonNull
    private final ProjectValidator projectValidator;
    @NonNull
    private final SupplyValidator supplyValidator;
    @NonNull
    private final ProjectFinancialDataRepository projectFinancialDataRepository;
    @NonNull
    private final FinancialDataQueryService financialDataQueryService;
    @NonNull
    private final SupplyMapper supplyMapper;

    @Transactional
    @Override
    public SupplyDto createSupply(Long projectId, SupplyDto supplyDto) {
        LOG.debug("Creating Supply for Project with id {}", projectId);

        this.projectValidator.validateProjectExistence(projectId);
        this.supplyValidator.validateCreateSupplyDto(supplyDto);
        this.supplyValidator.validateSupplierExistence(supplyDto.getSupplierId());

        var financialData = this.projectFinancialDataRepository.getProjectFinancialDataByProjectId(projectId);

        var supply = this.supplyMapper.mapFromDto(supplyDto);
        financialData.addSupply(supply);

        this.projectFinanceAwareObjectService.onCreate(projectId);
        this.projectFinancialDataRepository.save(financialData);
        LOG.debug("Supply for Project with id {} created", projectId);
        return this.supplyMapper.mapToDto(supply, projectId);
    }

    @Transactional
    @Override
    public SupplyDto updateSupply(Long projectId, Long supplyId, SupplyDto supplyDto) {
        LOG.debug("Updating Supply with id {}", supplyId);

        this.projectValidator.validateProjectExistence(projectId);
        this.supplyValidator.validateSupplyOnProjectExistence(projectId, supplyId);
        this.supplyValidator.validateUpdateSupplyDto(supplyDto);

        var financialData = this.projectFinancialDataRepository.getProjectFinancialDataByProjectId(projectId);
        var supply = financialData.updateSupply(supplyId, supplyDto);

        this.projectFinanceAwareObjectService.onUpdate(projectId);
        this.projectFinancialDataRepository.save(financialData);

        LOG.debug("Supply with id {} updated", supplyId);
        return this.supplyMapper.mapToDto(supply, projectId);
    }

    @Override
    public SupplyDto getSupply(Long projectId, Long supplyId) {
        LOG.debug("Loading Supply with id {} for Project with id {}.", supplyId, projectId);
        this.projectValidator.validateProjectExistence(projectId);
        this.supplyValidator.validateSupplyOnProjectExistence(projectId, supplyId);
        return this.financialDataQueryService.getSupplyById(supplyId, projectId);
    }

    @Transactional
    @Override
    public void deleteSupply(Long projectId, Long supplyId) {
        LOG.debug("Removing Supply with id {} for Project wit id {}.", supplyId, projectId);

        this.projectValidator.validateProjectExistence(projectId);
        this.supplyValidator.validateSupplyOnProjectExistence(projectId, supplyId);

        var financialData = this.projectFinancialDataRepository.getProjectFinancialDataByProjectId(projectId);
        financialData.removeSupply(supplyId);
        this.projectFinancialDataRepository.save(financialData);
        this.projectFinanceAwareObjectService.onRemove(projectId);
        LOG.debug("Supply with id {} for Project with id {} removed.", supplyId, projectId);
    }

    @Override
    public List<SupplyDto> getSuppliesForProject(Long projectId) {
        LOG.debug("Loading list of supplies for Project with id {}", projectId);

        this.projectValidator.validateProjectExistence(projectId);
        return this.financialDataQueryService.getSuppliesForProject(projectId);
    }

}
