package com.arturjarosz.task.architect.application.mapper;

import com.arturjarosz.task.architect.model.Architect;
import com.arturjarosz.task.dto.ArchitectDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ArchitectDtoMapper {

    ArchitectDtoMapper INSTANCE = Mappers.getMapper(ArchitectDtoMapper.class);

    @Mapping(source = "personName.firstName", target = "firstName")
    @Mapping(source = "personName.lastName", target = "lastName")
    ArchitectDto architectToArchitectDto(Architect architect);

    @Mapping(source = "firstName", target = "personName.firstName")
    @Mapping(source = "lastName", target = "personName.lastName")
    Architect architectDtoToArchitect(ArchitectDto architectDto);

}
