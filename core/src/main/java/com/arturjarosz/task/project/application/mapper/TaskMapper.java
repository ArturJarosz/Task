package com.arturjarosz.task.project.application.mapper;

import com.arturjarosz.task.dto.TaskDto;
import com.arturjarosz.task.dto.TaskStatusDto;
import com.arturjarosz.task.project.model.Task;
import com.arturjarosz.task.project.model.TaskType;
import com.arturjarosz.task.project.model.dto.TaskInnerDto;
import com.arturjarosz.task.project.status.task.TaskWorkflow;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper
public interface TaskMapper {

    default Task createDtoToTask(TaskDto taskDto, TaskWorkflow taskWorkflow) {
        return new Task(taskDto.getName(), TaskType.valueOf(taskDto.getType().name()), taskWorkflow);
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

    @Mapping(source = "task", target = "nextStatuses", qualifiedByName = "getNextStatuses")
    TaskDto taskToTaskDto(Task task);

    @Named("getNextStatuses")
    default List<TaskStatusDto> getNextStatuses(Task task) {
        return task.getStatus().getPossibleStatusTransitions().stream()
                .map(status -> TaskStatusDto.fromValue(status.getStatusName()))
                .toList();
    }
}
