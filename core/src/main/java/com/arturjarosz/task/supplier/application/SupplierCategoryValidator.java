package com.arturjarosz.task.supplier.application;

import com.arturjarosz.task.dto.SupplierCategoryDto;
import com.arturjarosz.task.supplier.domain.SupplierExceptionCodes;
import com.arturjarosz.task.supplier.infrastructure.SupplierCategoryRepository;
import com.arturjarosz.task.supplier.model.SupplierCategory;
import com.arturjarosz.task.supplier.query.SupplierQueryService;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertEntityPresent;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsFalse;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotEmpty;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotNull;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

/**
 * Validates supplier category dtos and SupplierCategory entities.
 */
@Component
@RequiredArgsConstructor
public class SupplierCategoryValidator {

    public static final String SUPPLIER_CATEGORY_WITH_SUPPLIERS_MESSAGE_CODE = createMessageCode(ExceptionCodes.NOT_VALID,
            SupplierExceptionCodes.SUPPLIER_CATEGORY, SupplierExceptionCodes.SUPPLIERS);
    public static final String SUPPLIER_CATEGORY_NAME_UNIQUE_MESSAGE_CODE = createMessageCode(ExceptionCodes.NOT_VALID,
            SupplierExceptionCodes.SUPPLIER_CATEGORY, SupplierExceptionCodes.NAME, SupplierExceptionCodes.UNIQUE);

    private final SupplierCategoryRepository supplierCategoryRepository;
    private final SupplierQueryService supplierQueryService;

    public void validateCreateSupplierCategoryDto(SupplierCategoryDto supplierCategoryDto) {
        this.validateSupplierCategoryDto(supplierCategoryDto, null);
    }

    public void validateUpdateSupplierCategoryDto(SupplierCategoryDto supplierCategoryDto, Long supplierCategoryId) {
        this.validateSupplierCategoryDto(supplierCategoryDto, supplierCategoryId);
    }

    /**
     * Validates the name of a new Supplier category (supplierCategoryId null) or of the renamed one with
     * supplierCategoryId. Names are compared without surrounding whitespace and regardless of letter case.
     */
    private void validateSupplierCategoryDto(SupplierCategoryDto supplierCategoryDto, Long supplierCategoryId) {
        assertNotNull(supplierCategoryDto, createMessageCode(ExceptionCodes.NULL, SupplierExceptionCodes.SUPPLIER_CATEGORY));
        assertNotNull(supplierCategoryDto.getName(), createMessageCode(ExceptionCodes.NULL,
                SupplierExceptionCodes.SUPPLIER_CATEGORY, SupplierExceptionCodes.NAME));
        var name = supplierCategoryDto.getName().strip();
        assertNotEmpty(name, createMessageCode(ExceptionCodes.EMPTY, SupplierExceptionCodes.SUPPLIER_CATEGORY,
                SupplierExceptionCodes.NAME));
        assertIsTrue(name.length() <= SupplierCategory.NAME_MAX_LENGTH,
                createMessageCode(ExceptionCodes.NOT_VALID, SupplierExceptionCodes.SUPPLIER_CATEGORY,
                        SupplierExceptionCodes.NAME, SupplierExceptionCodes.TOO_LONG), SupplierCategory.NAME_MAX_LENGTH);
        var nameTaken = supplierCategoryId == null
                ? this.supplierCategoryRepository.existsByNameIgnoreCase(name)
                : this.supplierCategoryRepository.existsByNameIgnoreCaseAndIdNot(name, supplierCategoryId);
        assertIsFalse(nameTaken, SUPPLIER_CATEGORY_NAME_UNIQUE_MESSAGE_CODE, name);
    }

    public void validateSupplierCategoryExistence(Optional<SupplierCategory> maybeSupplierCategory, Long supplierCategoryId) {
        assertEntityPresent(maybeSupplierCategory.isPresent(),
                createMessageCode(ExceptionCodes.NOT_EXIST, SupplierExceptionCodes.SUPPLIER_CATEGORY), supplierCategoryId);
    }

    public void validateSupplierCategoryExistence(Long supplierCategoryId) {
        this.validateSupplierCategoryExistence(this.supplierCategoryRepository.findById(supplierCategoryId), supplierCategoryId);
    }

    public void validateSupplierCategoryHasNoSuppliers(Long supplierCategoryId) {
        var suppliersCount = this.supplierQueryService.getNumberOfSuppliersByCategoryIds(List.of(supplierCategoryId));
        assertIsTrue(suppliersCount.get(supplierCategoryId) == 0, SUPPLIER_CATEGORY_WITH_SUPPLIERS_MESSAGE_CODE);
    }
}
