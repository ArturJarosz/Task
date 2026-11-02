package com.arturjarosz.task.supplier.application.impl;

import com.arturjarosz.task.dto.SupplierCategoryDto;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.namedentry.AbstractNamedEntryApplicationService;
import com.arturjarosz.task.supplier.application.SupplierCategoryApplicationService;
import com.arturjarosz.task.supplier.application.SupplierCategoryValidator;
import com.arturjarosz.task.supplier.application.mapper.SupplierCategoryMapper;
import com.arturjarosz.task.supplier.infrastructure.SupplierCategoryRepository;
import com.arturjarosz.task.supplier.model.SupplierCategory;

@ApplicationService
public class SupplierCategoryApplicationServiceImpl extends AbstractNamedEntryApplicationService<SupplierCategory, SupplierCategoryDto>
        implements SupplierCategoryApplicationService {

    private final SupplierCategoryMapper supplierCategoryMapper;

    public SupplierCategoryApplicationServiceImpl(SupplierCategoryRepository supplierCategoryRepository, SupplierCategoryValidator supplierCategoryValidator, SupplierCategoryMapper supplierCategoryMapper) {
        super(supplierCategoryRepository, supplierCategoryValidator, "Supplier category");
        this.supplierCategoryMapper = supplierCategoryMapper;
    }

    @Override
    protected SupplierCategory createEntry(String name) {
        return new SupplierCategory(name);
    }

    @Override
    protected String nameOf(SupplierCategoryDto supplierCategoryDto) {
        return supplierCategoryDto.getName();
    }

    @Override
    protected SupplierCategoryDto toDto(SupplierCategory supplierCategory, Long numberOfUsages) {
        return this.supplierCategoryMapper.mapToDto(supplierCategory, numberOfUsages);
    }
}
