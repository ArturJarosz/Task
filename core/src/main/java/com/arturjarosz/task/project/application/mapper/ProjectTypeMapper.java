package com.arturjarosz.task.project.application.mapper;

import com.arturjarosz.task.dto.ProjectTypeDto;
import com.arturjarosz.task.project.model.ProjectType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProjectTypeMapper {

    @Mapping(source = "projectType.id", target = "id")
    @Mapping(source = "projectType.name", target = "name")
    @Mapping(source = "projectType.createdDateTime", target = "createdDateTime")
    @Mapping(source = "projectType.lastModifiedDateTime", target = "lastModifiedDateTime")
    @Mapping(source = "numberOfProjects", target = "numberOfProjects")
    ProjectTypeDto mapToDto(ProjectType projectType, Long numberOfProjects);
}
