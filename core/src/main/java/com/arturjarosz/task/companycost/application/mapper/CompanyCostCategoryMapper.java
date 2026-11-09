package com.arturjarosz.task.companycost.application.mapper;

import com.arturjarosz.task.companycost.model.CompanyCostCategory;
import com.arturjarosz.task.dto.CompanyCostCategoryDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CompanyCostCategoryMapper {

    @Mapping(source = "companyCostCategory.id", target = "id")
    @Mapping(source = "companyCostCategory.name", target = "name")
    @Mapping(source = "companyCostCategory.createdDateTime", target = "createdDateTime")
    @Mapping(source = "companyCostCategory.lastModifiedDateTime", target = "lastModifiedDateTime")
    @Mapping(source = "numberOfCompanyCosts", target = "numberOfCompanyCosts")
    CompanyCostCategoryDto mapToDto(CompanyCostCategory companyCostCategory, Long numberOfCompanyCosts);
}
