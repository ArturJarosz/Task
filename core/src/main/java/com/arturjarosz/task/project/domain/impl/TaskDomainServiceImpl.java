package com.arturjarosz.task.project.domain.impl;

import com.arturjarosz.task.project.application.dto.TaskDto;
import com.arturjarosz.task.project.application.mapper.TaskDtoMapper;
import com.arturjarosz.task.project.domain.TaskDomainService;
import com.arturjarosz.task.project.model.Task;
import com.arturjarosz.task.sharedkernel.annotations.DomainService;
import com.arturjarosz.task.status.domain.TaskWorkflow;

@DomainService
public class TaskDomainServiceImpl implements TaskDomainService {

    private final TaskWorkflow taskWorkflow;

    public TaskDomainServiceImpl(TaskWorkflow taskWorkflow) {
        this.taskWorkflow = taskWorkflow;
    }

    @Override
    public Task createTask(TaskDto taskDto) {
        Task task = TaskDtoMapper.INSTANCE.createDtoToTask(taskDto, this.taskWorkflow);
        return task;
    }

    @Override
    public void updateTaskStatus() {

    }
}
