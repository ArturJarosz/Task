package com.arturjarosz.task.project.application.impl;

import com.arturjarosz.task.finance.application.ProjectFinancialDataAwareService;
import com.arturjarosz.task.finance.application.ProjectFinancialDataService;
import com.arturjarosz.task.project.application.ProjectValidator;
import com.arturjarosz.task.project.application.SupplyApplicationService;
import com.arturjarosz.task.project.application.SupplyValidator;
import com.arturjarosz.task.project.application.dto.SupplyDto;
import com.arturjarosz.task.project.application.mapper.SupplyDtoMapper;
import com.arturjarosz.task.project.infrastructure.repositor.ProjectRepository;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Supply;
import com.arturjarosz.task.project.query.ProjectQueryService;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.transaction.Transactional;

@ApplicationService
public class SupplyApplicationServiceImpl implements SupplyApplicationService, ProjectFinancialDataAwareService {
    private static final Logger LOG = LoggerFactory.getLogger(SupplyApplicationServiceImpl.class);

    private final ProjectFinancialDataService projectFinancialDataService;
    private final ProjectQueryService projectQueryService;
    private final ProjectRepository projectRepository;
    private final ProjectValidator projectValidator;
    private final SupplyValidator supplyValidator;

    @Autowired
    public SupplyApplicationServiceImpl(ProjectFinancialDataService projectFinancialDataService, ProjectQueryService projectQueryService,
                                        ProjectRepository projectRepository, ProjectValidator projectValidator,
                                        SupplyValidator supplyValidator) {
        this.projectFinancialDataService = projectFinancialDataService;
        this.projectQueryService = projectQueryService;
        this.projectRepository = projectRepository;
        this.projectValidator = projectValidator;
        this.supplyValidator = supplyValidator;
    }

    @Transactional
    @Override
    public SupplyDto createSupply(Long projectId, SupplyDto supplyDto) {
        LOG.debug("Creating Supply for Project with id {}", projectId);

        this.projectValidator.validateProjectExistence(projectId);
        this.supplyValidator.validateCreateSupplyDto(supplyDto);
        this.supplyValidator.validateSupplierExistence(supplyDto.getSupplierId());
        Project project = this.projectRepository.load(projectId);
        Supply supply = SupplyDtoMapper.INSTANCE.supplyDtoToSupply(supplyDto);
        project.addSupply(supply);
        this.projectRepository.save(project);
        this.triggerProjectFinancialDataRecalculation(projectId);
        LOG.debug("Supply for Project with id {} created", projectId);
        SupplyDto createdSupplyDto = SupplyDtoMapper.INSTANCE.supplyToSupplyDto(supply, projectId);
        createdSupplyDto.setId(this.getCreatedSupply(project, supply).getId());
        return createdSupplyDto;
    }

    @Transactional
    @Override
    public SupplyDto updateSupply(Long projectId, Long supplyId, SupplyDto supplyDto) {
        LOG.debug("Updating Supply with id {}", supplyId);

        this.projectValidator.validateProjectExistence(projectId);
        this.supplyValidator.validateSupplyOnProjectExistence(projectId, supplyId);
        this.supplyValidator.validateUpdateSupplyDto(supplyDto);
        Project project = this.projectRepository.load(projectId);
        Supply supply = project.updateSupply(supplyId, supplyDto);
        this.triggerProjectFinancialDataRecalculation(projectId);

        LOG.debug("Supply with id {} updated", supplyId);
        return SupplyDtoMapper.INSTANCE.supplyToSupplyDto(supply, projectId);
    }

    @Override
    public SupplyDto getSupply(Long projectId, Long supplyId) {
        LOG.debug("Loading Supply with id {} for Project with id {}.", supplyId, projectId);
        this.projectValidator.validateProjectExistence(projectId);
        this.supplyValidator.validateSupplyOnProjectExistence(projectId, supplyId);
        return this.projectQueryService.getSupplyForProject(supplyId, projectId);
    }

    @Transactional
    @Override
    public void deleteSupply(Long projectId, Long supplyId) {
        LOG.debug("Removing Supply with id {} for Project wit id {}.", supplyId, projectId);
        this.projectValidator.validateProjectExistence(projectId);
        this.supplyValidator.validateSupplyOnProjectExistence(projectId, supplyId);
        Project project = this.projectRepository.load(projectId);
        project.removeSupply(supplyId);
        this.projectRepository.save(project);
        this.triggerProjectFinancialDataRecalculation(projectId);
        LOG.debug("Supply with id {} for Project with id {} removed.", supplyId, projectId);
    }

    private Supply getCreatedSupply(Project project, Supply supply) {
        return project.getSupplies().stream()
                .filter(supplyOnProject -> (supplyOnProject).equals(supply)).findFirst()
                .orElse(null);
    }

    @Override
    public void triggerProjectFinancialDataRecalculation(long projectId) {
        this.projectFinancialDataService.recalculateProjectFinancialData(projectId);
    }
}
