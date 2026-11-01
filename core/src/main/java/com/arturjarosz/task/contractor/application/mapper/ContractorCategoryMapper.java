package com.arturjarosz.task.contractor.application.mapper;

import com.arturjarosz.task.dto.ContractorCategoryDto;
import com.arturjarosz.task.contractor.model.ContractorCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ContractorCategoryMapper {

    @Mapping(source = "contractorCategory.id", target = "id")
    @Mapping(source = "contractorCategory.name", target = "name")
    @Mapping(source = "contractorCategory.createdDateTime", target = "createdDateTime")
    @Mapping(source = "contractorCategory.lastModifiedDateTime", target = "lastModifiedDateTime")
    @Mapping(source = "numberOfContractors", target = "numberOfContractors")
    ContractorCategoryDto mapToDto(ContractorCategory contractorCategory, Long numberOfContractors);
}
