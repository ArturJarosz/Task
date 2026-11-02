package com.arturjarosz.task.project.application.mapper;

import com.arturjarosz.task.dto.TaskTypeDto;
import com.arturjarosz.task.project.model.TaskType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TaskTypeMapper {

    @Mapping(source = "taskType.id", target = "id")
    @Mapping(source = "taskType.name", target = "name")
    @Mapping(source = "taskType.createdDateTime", target = "createdDateTime")
    @Mapping(source = "taskType.lastModifiedDateTime", target = "lastModifiedDateTime")
    @Mapping(source = "numberOfTasks", target = "numberOfTasks")
    TaskTypeDto mapToDto(TaskType taskType, Long numberOfTasks);
}
