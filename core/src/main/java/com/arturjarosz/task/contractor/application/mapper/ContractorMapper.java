package com.arturjarosz.task.contractor.application.mapper;

import com.arturjarosz.task.contractor.model.Contractor;
import com.arturjarosz.task.dto.ContractorDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ContractorMapper {

    @Mapping(source = "name", target = "name")
    @Mapping(source = "note", target = "note")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "telephone", target = "telephone")
    @Mapping(source = "category", target = "category")
    Contractor mapFromDto(ContractorDto contractorDto);

    @Mapping(source = "name", target = "name")
    @Mapping(source = "note", target = "note")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "telephone", target = "telephone")
    @Mapping(source = "category", target = "category")
    ContractorDto mapToDto(Contractor contractor);

    @Mapping(source = "contractor.name", target = "name")
    @Mapping(source = "contractor.note", target = "note")
    @Mapping(source = "contractor.email", target = "email")
    @Mapping(source = "contractor.telephone", target = "telephone")
    @Mapping(source = "contractor.category", target = "category")
    @Mapping(source = "count", target = "numberOfContractorJobs")
    ContractorDto mapToDto(Contractor contractor, Long count);

}
