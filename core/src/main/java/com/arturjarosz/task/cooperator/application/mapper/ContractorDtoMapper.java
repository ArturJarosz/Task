package com.arturjarosz.task.cooperator.application.mapper;

import com.arturjarosz.task.cooperator.application.dto.ContractorDto;
import com.arturjarosz.task.cooperator.model.Cooperator;
import com.arturjarosz.task.cooperator.model.CooperatorCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ContractorDtoMapper {

    ContractorDtoMapper INSTANCE = Mappers.getMapper(ContractorDtoMapper.class);

    default Cooperator createContractorDtoToContractor(ContractorDto contractorDto) {
        return Cooperator.createContractor(contractorDto.getName(), contractorDto.getCategory());
    }

    @Mapping(source = "name", target = "name")
    @Mapping(source = "note", target = "note")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "telephone", target = "telephone")
    @Mapping(source = "category", target = "category", qualifiedByName = "cooperatorCategoryToContractorCategory")
    @Mapping(source = "value", target = "jobsValue")
    ContractorDto cooperatorToContractor(Cooperator cooperator);

    @Named("cooperatorCategoryToContractorCategory")
    default CooperatorCategory.ContractorCategory cooperatorCategoryToContractorCategory(
            CooperatorCategory cooperatorCategory) {
        return CooperatorCategory.ContractorCategory.valueOf(cooperatorCategory.name());
    }

    @Mapping(source = "name", target = "name")
    @Mapping(source = "category", target = "category", qualifiedByName = "cooperatorCategoryToContractorCategory")
    ContractorDto cooperatorToBasicContractor(Cooperator cooperator);
}
