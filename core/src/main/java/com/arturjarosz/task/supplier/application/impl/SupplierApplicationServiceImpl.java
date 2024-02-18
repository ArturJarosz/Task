package com.arturjarosz.task.supplier.application.impl;

import com.arturjarosz.task.dto.SupplierDto;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException;
import com.arturjarosz.task.supplier.application.SupplierApplicationService;
import com.arturjarosz.task.supplier.application.SupplierValidator;
import com.arturjarosz.task.supplier.application.mapper.SupplierDtoMapper;
import com.arturjarosz.task.supplier.infrastructure.SupplierRepository;
import com.arturjarosz.task.supplier.model.SupplierCategory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@ApplicationService
public class SupplierApplicationServiceImpl implements SupplierApplicationService {

    private final SupplierRepository supplierRepository;
    private final SupplierValidator supplierValidator;

    public SupplierApplicationServiceImpl(SupplierRepository supplierRepository, SupplierValidator supplierValidator) {
        this.supplierRepository = supplierRepository;
        this.supplierValidator = supplierValidator;
    }

    @Transactional
    @Override
    public SupplierDto createSupplier(SupplierDto supplierDto) {
        LOG.debug("Creating Supplier.");

        this.supplierValidator.validateCreateSupplierDto(supplierDto);
        var supplier = SupplierDtoMapper.INSTANCE.supplierDtoToSupplier(supplierDto);
        this.supplierRepository.save(supplier);
        LOG.debug("Supplier created.");
        return SupplierDtoMapper.INSTANCE.supplierToSupplierDto(supplier);
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
                supplierDto.getEmail(),
                supplierDto.getTelephone(), supplierDto.getNote());
        this.supplierRepository.save(supplier);

        LOG.debug("Supplier with id {} updated.", supplierId);
        return SupplierDtoMapper.INSTANCE.supplierToSupplierDto(supplier);
    }

    @Transactional
    @Override
    public void deleteSupplier(Long supplierId) {
        LOG.debug("Loading Supplier with id {}", supplierId);

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
        var supplierDto = SupplierDtoMapper.INSTANCE.supplierToSupplierDto(supplier);
        LOG.debug("Supplier with id {} loaded.", supplierId);
        return supplierDto;
    }

    @Override
    public List<SupplierDto> getBasicSuppliers() {
        LOG.debug("Loading Suppliers list");
        return this.supplierRepository.findAll()
                .stream()
                .map(SupplierDtoMapper.INSTANCE::supplierToSupplierDto)
                .toList();
    }
}
