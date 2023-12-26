package com.arturjarosz.task.contractor.application.mapper;

import com.arturjarosz.task.contractor.model.Contractor;
import com.arturjarosz.task.dto.ContractorDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ContractorDtoMapper {

    ContractorDtoMapper INSTANCE = Mappers.getMapper(ContractorDtoMapper.class);

    Contractor createContractorDtoToContractor(ContractorDto contractorDto);

    @Mapping(source = "name", target = "name")
    @Mapping(source = "note", target = "note")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "telephone", target = "telephone")
    @Mapping(source = "category", target = "category")
    ContractorDto contractorToContractorDto(Contractor contractor);

    @Mapping(source = "name", target = "name")
    @Mapping(source = "category", target = "category")
    ContractorDto contractorToBasicContractor(Contractor contractor);
}
