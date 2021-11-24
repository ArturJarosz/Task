package com.arturjarosz.task.cooperator.application.impl;

import com.arturjarosz.task.cooperator.application.SupplierApplicationService;
import com.arturjarosz.task.cooperator.application.SupplierValidator;
import com.arturjarosz.task.cooperator.application.dto.SupplierDto;
import com.arturjarosz.task.cooperator.application.mapper.SupplierDtoMapper;
import com.arturjarosz.task.cooperator.infrastructure.CooperatorRepository;
import com.arturjarosz.task.cooperator.model.Cooperator;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.model.CreatedEntityDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationService
public class SupplierApplicationServiceImpl implements SupplierApplicationService {
    private static final Logger LOG = LoggerFactory.getLogger(SupplierApplicationServiceImpl.class);

    private final CooperatorRepository cooperatorRepository;
    private final SupplierValidator supplierValidator;

    public SupplierApplicationServiceImpl(CooperatorRepository cooperatorRepository,
                                          SupplierValidator supplierValidator) {
        this.cooperatorRepository = cooperatorRepository;
        this.supplierValidator = supplierValidator;
    }

    @Transactional
    @Override
    public CreatedEntityDto createSupplier(SupplierDto supplierDto) {
        LOG.debug("Creating Supplier.");
        SupplierValidator.validateCreateSupplierDto(supplierDto);
        Cooperator cooperator = SupplierDtoMapper.INSTANCE.createSupplierDtoToCooperator(supplierDto);
        this.cooperatorRepository.save(cooperator);
        LOG.debug("Supplier created.");
        return new CreatedEntityDto(cooperator.getId());
    }

    @Transactional
    @Override
    public void updateSupplier(Long supplierId, SupplierDto supplierDto) {
        LOG.debug("Updating Supplier with id {}.", supplierId);
        this.supplierValidator.validateSupplierExistence(supplierId);
        SupplierValidator.validateUpdateSupplierDto(supplierDto);
        Cooperator cooperator = this.cooperatorRepository.load(supplierId);
        cooperator.update(supplierDto.getName(), supplierDto.getCategory().asCooperatorCategory(),
                supplierDto.getEmail(), supplierDto.getTelephone(), supplierDto.getNote());
        this.cooperatorRepository.save(cooperator);
        LOG.debug("Supplier with id {} updated.", supplierId);
    }

    @Transactional
    @Override
    public void deleteSupplier(Long supplierId) {
        LOG.debug("Loading Supplier with id {}", supplierId);
        this.supplierValidator.validateSupplierExistence(supplierId);
        this.supplierValidator.validateSupplierHasNoSupply(supplierId);
        this.cooperatorRepository.remove(supplierId);
        LOG.debug("Supplier with id {} deleted.", supplierId);
    }

    @Override
    public SupplierDto getSupplier(Long supplierId) {
        LOG.debug("Loading Supplier with id {}", supplierId);
        this.supplierValidator.validateSupplierExistence(supplierId);
        Cooperator cooperator = this.cooperatorRepository.load(supplierId);
        SupplierDto supplierDto = SupplierDtoMapper.INSTANCE.cooperatorToSupplierDto(cooperator);
        LOG.debug("Supplier with id {} loaded.", supplierId);
        return supplierDto;
    }

    @Override
    public List<SupplierDto> getBasicSuppliers() {
        LOG.debug("Loading Suppliers list");
        return this.cooperatorRepository.loadAll().stream().map(SupplierDtoMapper.INSTANCE::cooperatorToBasicSupplier)
                .collect(Collectors.toList());
    }
}
