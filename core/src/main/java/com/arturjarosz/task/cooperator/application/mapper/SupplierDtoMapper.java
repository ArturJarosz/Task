package com.arturjarosz.task.cooperator.application.mapper;

import com.arturjarosz.task.cooperator.application.dto.SupplierDto;
import com.arturjarosz.task.cooperator.model.Cooperator;
import com.arturjarosz.task.cooperator.model.CooperatorCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SupplierDtoMapper {

    SupplierDtoMapper INSTANCE = Mappers.getMapper(SupplierDtoMapper.class);

    default Cooperator createSupplierDtoToCooperator(SupplierDto supplierDto) {
        return Cooperator.createSupplier(supplierDto.getName(), supplierDto.getCategory());
    }

    @Mapping(source = "name", target = "name")
    @Mapping(source = "note", target = "note")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "telephone", target = "telephone")
    @Mapping(source = "category", target = "category", qualifiedByName = "cooperatorCategoryToSupplierCategory")
    @Mapping(source = "value", target = "jobsValue")
    SupplierDto cooperatorToSupplierDto(Cooperator cooperator);

    @Named("cooperatorCategoryToSupplierCategory")
    default CooperatorCategory.SupplierCategory cooperatorCategoryToSupplierCategory(
            CooperatorCategory cooperatorCategory) {
        return CooperatorCategory.SupplierCategory.valueOf(cooperatorCategory.name());
    }

    @Mapping(source = "name", target = "name")
    @Mapping(source = "category", target = "category", qualifiedByName = "cooperatorCategoryToSupplierCategory")
    SupplierDto cooperatorToBasicSupplier(Cooperator cooperator);
}
