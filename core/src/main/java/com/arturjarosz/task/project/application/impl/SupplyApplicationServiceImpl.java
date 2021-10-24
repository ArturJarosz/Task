package com.arturjarosz.task.project.application.impl;

import com.arturjarosz.task.project.application.ProjectValidator;
import com.arturjarosz.task.project.application.SupplyApplicationService;
import com.arturjarosz.task.project.application.SupplyValidator;
import com.arturjarosz.task.project.application.dto.SupplyDto;
import com.arturjarosz.task.project.application.mapper.SupplyDtoMapper;
import com.arturjarosz.task.project.infrastructure.repositor.ProjectRepository;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Supply;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@ApplicationService
public class SupplyApplicationServiceImpl implements SupplyApplicationService {
    private static final Logger LOG = LoggerFactory.getLogger(SupplyApplicationServiceImpl.class);

    private final ProjectRepository projectRepository;
    private final ProjectValidator projectValidator;
    private final SupplyValidator supplyValidator;

    @Autowired
    public SupplyApplicationServiceImpl(
            ProjectRepository projectRepository,
            ProjectValidator projectValidator, SupplyValidator supplyValidator) {
        this.projectRepository = projectRepository;
        this.projectValidator = projectValidator;
        this.supplyValidator = supplyValidator;
    }

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
        LOG.debug("Supply for Project with id {} created", projectId);

        return SupplyDtoMapper.INSTANCE.supplyToSupplyDto(supply);
    }

    @Override
    public SupplyDto updateSupply(Long projectId, Long supplyId, SupplyDto supplyDto) {
        LOG.debug("Updating Supply with id {}", supplyId);

        this.projectValidator.validateProjectExistence(projectId);
        this.supplyValidator.validateSupplyOnProjectExistence(projectId, supplyId);
        this.supplyValidator.validateUpdateSupplyDto(supplyDto);
        Project project = this.projectRepository.load(projectId);
        Supply supply = project.updateSupply(supplyId, supplyDto);
        this.projectRepository.save(project);

        LOG.debug("Supply with id {} updated", supplyId);
        return SupplyDtoMapper.INSTANCE.supplyToSupplyDto(supply);
    }

    @Override
    public SupplyDto getSupply(Long projectId, Long supplyId) {
        return null;
    }

    @Override
    public void deleteSupply(Long projectId, Long supplyId) {
        // to implement
    }
}
