package com.arturjarosz.task.architect.application.mapper;

import com.arturjarosz.task.architect.model.Architect;
import com.arturjarosz.task.dto.ArchitectDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ArchitectMapper {

    @Mapping(source = "personName.firstName", target = "firstName")
    @Mapping(source = "personName.lastName", target = "lastName")
    // The stored PROJECTS_VALUE column is never updated; the application service sums the contract values instead
    @Mapping(target = "projectsValue", ignore = true)
    ArchitectDto mapToDto(Architect architect);

    @Mapping(source = "firstName", target = "personName.firstName")
    @Mapping(source = "lastName", target = "personName.lastName")
    @Mapping(target = "projectsValue", ignore = true)
    Architect mapFromDto(ArchitectDto architectDto);

}
