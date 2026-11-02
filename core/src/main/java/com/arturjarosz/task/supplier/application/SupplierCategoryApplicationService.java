package com.arturjarosz.task.supplier.application;

import com.arturjarosz.task.dto.SupplierCategoryDto;
import com.arturjarosz.task.sharedkernel.namedentry.NamedEntryApplicationService;

/**
 * Supplier categorys configured by users, each with the number of items using it.
 */
public interface SupplierCategoryApplicationService extends NamedEntryApplicationService<SupplierCategoryDto> {
}
