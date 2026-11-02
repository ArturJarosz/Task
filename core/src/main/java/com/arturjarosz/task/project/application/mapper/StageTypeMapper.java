package com.arturjarosz.task.project.application.mapper;

import com.arturjarosz.task.dto.StageTypeDto;
import com.arturjarosz.task.project.model.StageType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StageTypeMapper {

    @Mapping(source = "stageType.id", target = "id")
    @Mapping(source = "stageType.name", target = "name")
    @Mapping(source = "stageType.createdDateTime", target = "createdDateTime")
    @Mapping(source = "stageType.lastModifiedDateTime", target = "lastModifiedDateTime")
    @Mapping(source = "numberOfStages", target = "numberOfStages")
    StageTypeDto mapToDto(StageType stageType, Long numberOfStages);
}
