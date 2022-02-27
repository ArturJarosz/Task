package com.arturjarosz.task.supplier.application;

import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.model.CreatedEntityDto;
import com.arturjarosz.task.supplier.application.dto.SupplierDto;
import com.arturjarosz.task.supplier.infrastructure.SupplierRepository;
import com.arturjarosz.task.supplier.model.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationService
public class SupplierApplicationServiceImpl implements SupplierApplicationService {
    private static final Logger LOG = LoggerFactory.getLogger(SupplierApplicationServiceImpl.class);

    private final SupplierRepository supplierRepository;
    private final SupplierValidator supplierValidator;

    public SupplierApplicationServiceImpl(SupplierRepository supplierRepository, SupplierValidator supplierValidator) {
        this.supplierRepository = supplierRepository;
        this.supplierValidator = supplierValidator;
    }

    @Transactional
    @Override
    public CreatedEntityDto createSupplier(SupplierDto supplierDto) {
        LOG.debug("Creating Supplier.");
        this.supplierValidator.validateCreateSupplierDto(supplierDto);
        Supplier supplier = SupplierDtoMapper.INSTANCE.supplierDtoToSupplier(supplierDto);
        this.supplierRepository.save(supplier);
        LOG.debug("Supplier created.");
        return new CreatedEntityDto(supplier.getId());
    }

    @Transactional
    @Override
    public void updateSupplier(Long supplierId, SupplierDto supplierDto) {
        LOG.debug("Updating Supplier with id {}.", supplierId);
        this.supplierValidator.validateSupplierExistence(supplierId);
        this.supplierValidator.validateUpdateSupplierDto(supplierDto);
        Supplier supplier = this.supplierRepository.load(supplierId);
        supplier.update(supplierDto.getName(), supplierDto.getCategory(), supplierDto.getEmail(),
                supplierDto.getTelephone(), supplierDto.getNote());
        this.supplierRepository.save(supplier);
        LOG.debug("Supplier with id {} updated.", supplierId);
    }

    @Transactional
    @Override
    public void deleteSupplier(Long supplierId) {
        LOG.debug("Loading Supplier with id {}", supplierId);
        this.supplierValidator.validateSupplierExistence(supplierId);
        this.supplierValidator.validateSupplierHasNoSupply(supplierId);
        this.supplierRepository.remove(supplierId);
        LOG.debug("Supplier with id {} deleted.", supplierId);
    }

    @Override
    public SupplierDto getSupplier(Long supplierId) {
        LOG.debug("Loading Supplier with id {}", supplierId);
        this.supplierValidator.validateSupplierExistence(supplierId);
        Supplier supplier = this.supplierRepository.load(supplierId);
        SupplierDto supplierDto = SupplierDtoMapper.INSTANCE.supplierToSupplierDto(supplier);
        LOG.debug("Supplier with id {} loaded.", supplierId);
        return supplierDto;
    }

    @Override
    public List<SupplierDto> getBasicSuppliers() {
        LOG.debug("Loading Suppliers list");
        return this.supplierRepository.loadAll().stream().map(SupplierDtoMapper.INSTANCE::supplierToBasicSupplier)
                .collect(Collectors.toList());
    }
}
