package com.arturjarosz.task.finance.application.mapper;

import com.arturjarosz.task.dto.CostCategoryDto;
import com.arturjarosz.task.finance.model.CostCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CostCategoryMapper {

    @Mapping(source = "costCategory.id", target = "id")
    @Mapping(source = "costCategory.name", target = "name")
    @Mapping(source = "costCategory.createdDateTime", target = "createdDateTime")
    @Mapping(source = "costCategory.lastModifiedDateTime", target = "lastModifiedDateTime")
    @Mapping(source = "numberOfCosts", target = "numberOfCosts")
    CostCategoryDto mapToDto(CostCategory costCategory, Long numberOfCosts);
}
