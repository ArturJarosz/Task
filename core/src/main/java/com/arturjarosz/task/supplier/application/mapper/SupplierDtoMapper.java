package com.arturjarosz.task.supplier.application.mapper;

import com.arturjarosz.task.dto.SupplierDto;
import com.arturjarosz.task.supplier.model.Supplier;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SupplierDtoMapper {

    SupplierDtoMapper INSTANCE = Mappers.getMapper(SupplierDtoMapper.class);

    Supplier supplierDtoToSupplier(SupplierDto supplierDto);

    @Mapping(source = "name", target = "name")
    @Mapping(source = "note", target = "note")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "telephone", target = "telephone")
    @Mapping(source = "category", target = "category")
    SupplierDto supplierToSupplierDto(Supplier supplier);

    @Mapping(source = "name", target = "name")
    @Mapping(source = "category", target = "category")
    SupplierDto supplierToBasicSupplier(Supplier supplier);
}
