package com.arturjarosz.task.supplier.application;

import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import com.arturjarosz.task.supplier.application.dto.SupplierDto;
import com.arturjarosz.task.supplier.domain.SupplierExceptionCodes;
import com.arturjarosz.task.supplier.intrastructure.SupplierRepository;
import com.arturjarosz.task.supplier.model.Supplier;
import org.springframework.stereotype.Component;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotEmpty;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotNull;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

@Component
public class SupplierValidator {

    private final SupplierRepository supplierRepository;

    public SupplierValidator(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public static void validateCreateSupplierDto(SupplierDto supplierDto) {
        assertNotNull(supplierDto, createMessageCode(ExceptionCodes.NULL, SupplierExceptionCodes.SUPPLIER));
        assertNotNull(supplierDto.getName(),
                createMessageCode(ExceptionCodes.NULL, SupplierExceptionCodes.SUPPLIER, SupplierExceptionCodes.NAME));
        assertNotEmpty(supplierDto.getName(),
                createMessageCode(ExceptionCodes.EMPTY, SupplierExceptionCodes.SUPPLIER, SupplierExceptionCodes.NAME));
        assertNotNull(supplierDto.getCategory(), createMessageCode(ExceptionCodes.NULL, SupplierExceptionCodes.SUPPLIER,
                SupplierExceptionCodes.CATEGORY));
    }

    public static void validateUpdateSupplierDto(SupplierDto supplierDto) {
        assertNotNull(supplierDto, createMessageCode(ExceptionCodes.NULL, SupplierExceptionCodes.SUPPLIER));
        assertNotNull(supplierDto.getName(),
                createMessageCode(ExceptionCodes.NULL, SupplierExceptionCodes.SUPPLIER, SupplierExceptionCodes.NAME));
        assertNotEmpty(supplierDto.getName(),
                createMessageCode(ExceptionCodes.EMPTY, SupplierExceptionCodes.SUPPLIER, SupplierExceptionCodes.NAME));
        assertNotNull(supplierDto.getCategory(), createMessageCode(ExceptionCodes.NULL, SupplierExceptionCodes.SUPPLIER,
                SupplierExceptionCodes.CATEGORY));
    }

    public void validateSupplierExistence(Long supplierId) {
        Supplier supplier = this.supplierRepository.load(supplierId);
        assertNotNull(supplier, createMessageCode(ExceptionCodes.NOT_EXIST, SupplierExceptionCodes.SUPPLIER));
    }

    public void validateSupplierHasNoSupply(Long supplierId) {
        //TODO: to implemented when SupplierJob is ready
    }
}
