package com.arturjarosz.task.project.application.mapper;

import com.arturjarosz.task.project.application.dto.TaskDto;
import com.arturjarosz.task.project.model.Task;
import com.arturjarosz.task.project.model.dto.TaskInnerDto;
import com.arturjarosz.task.project.status.task.TaskWorkflow;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TaskDtoMapper {

    TaskDtoMapper INSTANCE = Mappers.getMapper(TaskDtoMapper.class);

    default Task createDtoToTask(TaskDto taskDto, TaskWorkflow taskWorkflow) {
        return new Task(taskDto.getName(), taskDto.getType(), taskWorkflow);
    }

    @Mapping(source = "name", target = "name")
    @Mapping(source = "type", target = "taskType")
    @Mapping(source = "startDate", target = "startDate")
    @Mapping(source = "endDate", target = "endDate")
    @Mapping(source = "note", target = "note")
    TaskInnerDto updateDtoToInnerDto(TaskDto taskDto);

    @Mapping(source = "name", target = "name")
    @Mapping(source = "type", target = "type")
    @Mapping(source = "status", target = "status")
    @Mapping(target = "note", ignore = true)
    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "endDate", ignore = true)
    TaskDto taskToTaskBasicDto(Task task);

    TaskDto taskToTaskDto(Task task);
}
