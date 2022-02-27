package com.arturjarosz.task.supplier.application;

import com.arturjarosz.task.supplier.application.dto.SupplierDto;
import com.arturjarosz.task.supplier.model.Supplier;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SupplierDtoMapper {

    SupplierDtoMapper INSTANCE = Mappers.getMapper(SupplierDtoMapper.class);

    Supplier supplierDtoToSupplier(SupplierDto supplierDto);

    @Mapping(source = "name", target = "name")
    @Mapping(source = "note", target = "note")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "telephone", target = "telephone")
    @Mapping(source = "category", target = "category")
    SupplierDto cooperatorToSupplierDto(Supplier supplier);

    @Mapping(source = "name", target = "name")
    @Mapping(source = "category", target = "category")
    SupplierDto supplierToBasicSupplier(Supplier supplier);
}
