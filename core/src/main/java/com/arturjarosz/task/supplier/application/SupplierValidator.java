package com.arturjarosz.task.supplier.application;

import com.arturjarosz.task.dto.SupplierDto;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import com.arturjarosz.task.supplier.domain.SupplierExceptionCodes;
import com.arturjarosz.task.supplier.infrastructure.SupplierCategoryRepository;
import com.arturjarosz.task.supplier.infrastructure.SupplierRepository;
import com.arturjarosz.task.supplier.model.Supplier;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertEntityPresent;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotEmpty;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotNull;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

@Component
public class SupplierValidator {

    private final SupplierRepository supplierRepository;
    private final SupplierCategoryRepository supplierCategoryRepository;

    public SupplierValidator(SupplierRepository supplierRepository,
            SupplierCategoryRepository supplierCategoryRepository) {
        this.supplierRepository = supplierRepository;
        this.supplierCategoryRepository = supplierCategoryRepository;
    }

    public void validateCreateSupplierDto(SupplierDto supplierDto) {
        assertNotNull(supplierDto, createMessageCode(ExceptionCodes.NULL, SupplierExceptionCodes.SUPPLIER));
        assertNotNull(supplierDto.getName(),
                createMessageCode(ExceptionCodes.NULL, SupplierExceptionCodes.SUPPLIER, SupplierExceptionCodes.NAME));
        assertNotEmpty(supplierDto.getName(),
                createMessageCode(ExceptionCodes.EMPTY, SupplierExceptionCodes.SUPPLIER, SupplierExceptionCodes.NAME));
        assertNotNull(supplierDto.getCategoryId(), createMessageCode(ExceptionCodes.NULL,
                SupplierExceptionCodes.SUPPLIER, SupplierExceptionCodes.CATEGORY));
        assertEntityPresent(this.supplierCategoryRepository.existsById(supplierDto.getCategoryId()),
                createMessageCode(ExceptionCodes.NOT_EXIST, SupplierExceptionCodes.SUPPLIER_CATEGORY),
                supplierDto.getCategoryId());
    }

    public void validateUpdateSupplierDto(SupplierDto supplierDto) {
        this.validateCreateSupplierDto(supplierDto);
    }

    public void validateSupplierExistence(Long supplierId) {
        Optional<Supplier> maybeSupplier = this.supplierRepository.findById(supplierId);
        assertIsTrue(maybeSupplier.isPresent(),
                createMessageCode(ExceptionCodes.NOT_EXIST, SupplierExceptionCodes.SUPPLIER), supplierId);
    }

    public void validateSupplierHasNoSupply(Long supplierId) {
        //TODO: to implemented when SupplierJob is ready
    }

    public void validateSupplierExistence(Optional<Supplier> maybeSupplier, Long supplierId) {
        assertIsTrue(maybeSupplier.isPresent(),
                createMessageCode(ExceptionCodes.NOT_EXIST, SupplierExceptionCodes.SUPPLIER), supplierId);
    }
}
