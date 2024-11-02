package com.arturjarosz.task.finance.application.impl;

import com.arturjarosz.task.dto.SupplyDto;
import com.arturjarosz.task.dto.SupplyProjectDataDto;
import com.arturjarosz.task.finance.application.ProjectFinanceAwareObjectService;
import com.arturjarosz.task.finance.application.SupplyApplicationService;
import com.arturjarosz.task.finance.application.mapper.SupplyMapper;
import com.arturjarosz.task.finance.application.mapper.SupplyProjectDataMapper;
import com.arturjarosz.task.finance.application.validator.SupplyValidator;
import com.arturjarosz.task.finance.infrastructure.ProjectFinancialDataRepository;
import com.arturjarosz.task.finance.model.PartialFinancialDataType;
import com.arturjarosz.task.finance.query.FinancialDataQueryService;
import com.arturjarosz.task.project.application.ProjectValidator;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@ApplicationService
public class SupplyApplicationServiceImpl implements SupplyApplicationService {

    private final ProjectFinanceAwareObjectService projectFinanceAwareObjectService;
    private final ProjectValidator projectValidator;
    private final SupplyValidator supplyValidator;
    private final ProjectFinancialDataRepository projectFinancialDataRepository;
    private final FinancialDataQueryService financialDataQueryService;
    private final SupplyMapper supplyMapper;
    private final SupplyProjectDataMapper supplyProjectDataMapper;

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
    public SupplyProjectDataDto getProjectSuppliesData(Long projectId) {
        LOG.debug("Loading supplies data for Project with id {}", projectId);
        this.projectValidator.validateProjectExistence(projectId);

        var projectFinancialData = this.financialDataQueryService.getProjectPartialFinancialDataByType(projectId,
                PartialFinancialDataType.SUPPLY);
        var supplies = this.financialDataQueryService.getSuppliesForProject(projectId);

        return this.supplyProjectDataMapper.map(projectFinancialData, supplies);
    }

}
