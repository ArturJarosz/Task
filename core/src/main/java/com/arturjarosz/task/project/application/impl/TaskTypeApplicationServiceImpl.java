package com.arturjarosz.task.project.application.impl;

import com.arturjarosz.task.dto.TaskTypeDto;
import com.arturjarosz.task.project.application.TaskTypeApplicationService;
import com.arturjarosz.task.project.application.TaskTypeValidator;
import com.arturjarosz.task.project.application.mapper.TaskTypeMapper;
import com.arturjarosz.task.project.infrastructure.repositor.TaskTypeRepository;
import com.arturjarosz.task.project.model.TaskType;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.namedentry.AbstractNamedEntryApplicationService;

@ApplicationService
public class TaskTypeApplicationServiceImpl extends AbstractNamedEntryApplicationService<TaskType, TaskTypeDto>
        implements TaskTypeApplicationService {

    private final TaskTypeMapper taskTypeMapper;

    public TaskTypeApplicationServiceImpl(TaskTypeRepository taskTypeRepository, TaskTypeValidator taskTypeValidator, TaskTypeMapper taskTypeMapper) {
        super(taskTypeRepository, taskTypeValidator, "Task type");
        this.taskTypeMapper = taskTypeMapper;
    }

    @Override
    protected TaskType createEntry(String name) {
        return new TaskType(name);
    }

    @Override
    protected String nameOf(TaskTypeDto taskTypeDto) {
        return taskTypeDto.getName();
    }

    @Override
    protected TaskTypeDto toDto(TaskType taskType, Long numberOfUsages) {
        return this.taskTypeMapper.mapToDto(taskType, numberOfUsages);
    }
}
