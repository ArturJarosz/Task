package com.arturjarosz.task.supplier.application.mapper;

import com.arturjarosz.task.dto.SupplierCategoryDto;
import com.arturjarosz.task.supplier.model.SupplierCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SupplierCategoryMapper {

    @Mapping(source = "supplierCategory.id", target = "id")
    @Mapping(source = "supplierCategory.name", target = "name")
    @Mapping(source = "supplierCategory.createdDateTime", target = "createdDateTime")
    @Mapping(source = "supplierCategory.lastModifiedDateTime", target = "lastModifiedDateTime")
    @Mapping(source = "numberOfSuppliers", target = "numberOfSuppliers")
    SupplierCategoryDto mapToDto(SupplierCategory supplierCategory, Long numberOfSuppliers);
}
