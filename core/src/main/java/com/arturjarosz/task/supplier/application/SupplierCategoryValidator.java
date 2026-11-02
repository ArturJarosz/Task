package com.arturjarosz.task.supplier.application;

import com.arturjarosz.task.sharedkernel.namedentry.NamedEntryValidator;
import com.arturjarosz.task.supplier.domain.SupplierExceptionCodes;
import com.arturjarosz.task.supplier.infrastructure.SupplierCategoryRepository;
import com.arturjarosz.task.supplier.model.SupplierCategory;
import com.arturjarosz.task.supplier.query.SupplierQueryService;
import org.springframework.stereotype.Component;

/**
 * Validates supplier category dtos and SupplierCategory entities.
 */
@Component
public class SupplierCategoryValidator extends NamedEntryValidator<SupplierCategory> {

    public SupplierCategoryValidator(SupplierCategoryRepository supplierCategoryRepository, SupplierQueryService supplierQueryService) {
        super(supplierCategoryRepository, SupplierExceptionCodes.SUPPLIER_CATEGORY, SupplierExceptionCodes.SUPPLIERS, supplierQueryService::getNumberOfSuppliersByCategoryIds);
    }
}
