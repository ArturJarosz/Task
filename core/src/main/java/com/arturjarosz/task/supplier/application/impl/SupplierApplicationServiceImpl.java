package com.arturjarosz.task.supplier.application.impl;

import com.arturjarosz.task.dto.SupplierDto;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException;
import com.arturjarosz.task.supplier.application.SupplierApplicationService;
import com.arturjarosz.task.supplier.application.SupplierValidator;
import com.arturjarosz.task.supplier.application.mapper.SupplierMapper;
import com.arturjarosz.task.supplier.infrastructure.SupplierRepository;
import com.arturjarosz.task.supplier.model.SupplierCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@ApplicationService
public class SupplierApplicationServiceImpl implements SupplierApplicationService {

    private final SupplierRepository supplierRepository;
    private final SupplierValidator supplierValidator;
    private final SupplierMapper supplierMapper;

    @Transactional
    @Override
    public SupplierDto createSupplier(SupplierDto supplierDto) {
        LOG.debug("Creating Supplier.");

        this.supplierValidator.validateCreateSupplierDto(supplierDto);
        var supplier = this.supplierMapper.mapFromDto(supplierDto);
        this.supplierRepository.save(supplier);
        LOG.debug("Supplier created.");
        return this.supplierMapper.mapToDto(supplier);
    }

    @Transactional
    @Override
    public SupplierDto updateSupplier(Long supplierId, SupplierDto supplierDto) {
        LOG.debug("Updating Supplier with id {}.", supplierId);

        var maybeSupplier = this.supplierRepository.findById(supplierId);
        this.supplierValidator.validateSupplierExistence(maybeSupplier, supplierId);
        this.supplierValidator.validateUpdateSupplierDto(supplierDto);
        var supplier = maybeSupplier.orElseThrow(ResourceNotFoundException::new);
        supplier.update(supplierDto.getName(), SupplierCategory.valueOf(supplierDto.getCategory().name()),
                supplierDto.getEmail(), supplierDto.getTelephone(), supplierDto.getNote());
        supplier = this.supplierRepository.save(supplier);

        LOG.debug("Supplier with id {} updated.", supplierId);
        return this.supplierMapper.mapToDto(supplier);
    }

    @Transactional
    @Override
    public void deleteSupplier(Long supplierId) {
        LOG.debug("Deleting Supplier with id {}", supplierId);

        this.supplierValidator.validateSupplierExistence(supplierId);
        this.supplierValidator.validateSupplierHasNoSupply(supplierId);
        this.supplierRepository.deleteById(supplierId);

        LOG.debug("Supplier with id {} deleted.", supplierId);
    }

    @Override
    public SupplierDto getSupplier(Long supplierId) {
        LOG.debug("Loading Supplier with id {}", supplierId);

        var maybeSupplier = this.supplierRepository.findById(supplierId);
        this.supplierValidator.validateSupplierExistence(supplierId);
        var supplier = maybeSupplier.orElseThrow(ResourceNotFoundException::new);
        var supplierDto = this.supplierMapper.mapToDto(supplier);
        LOG.debug("Supplier with id {} loaded.", supplierId);
        return supplierDto;
    }

    @Override
    public List<SupplierDto> getBasicSuppliers() {
        LOG.debug("Loading Suppliers list");
        return this.supplierRepository.findAll().stream().map(this.supplierMapper::mapToDto).toList();
    }
}
