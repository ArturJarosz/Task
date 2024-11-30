package com.arturjarosz.task.supplier.application.mapper;

import com.arturjarosz.task.dto.SupplierDto;
import com.arturjarosz.task.supplier.model.Supplier;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SupplierMapper {

    @Mapping(source = "name", target = "name")
    @Mapping(source = "note", target = "note")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "telephone", target = "telephone")
    @Mapping(source = "category", target = "category")
    Supplier mapFromDto(SupplierDto supplierDto);

    @Mapping(source = "supplier.name", target = "name")
    @Mapping(source = "supplier.note", target = "note")
    @Mapping(source = "supplier.email", target = "email")
    @Mapping(source = "supplier.telephone", target = "telephone")
    @Mapping(source = "supplier.category", target = "category")
    @Mapping(source = "numberOfSupplies", target = "numberOfSupplies")
    SupplierDto mapToDto(Supplier supplier, Long numberOfSupplies);

    @Mapping(source = "supplier.name", target = "name")
    @Mapping(source = "supplier.note", target = "note")
    @Mapping(source = "supplier.email", target = "email")
    @Mapping(source = "supplier.telephone", target = "telephone")
    @Mapping(source = "supplier.category", target = "category")
    SupplierDto mapToDto(Supplier supplier);
}
