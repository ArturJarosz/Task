package com.arturjarosz.task.project.domain.impl;

import com.arturjarosz.task.project.application.dto.TaskDto;
import com.arturjarosz.task.project.application.mapper.TaskDtoMapper;
import com.arturjarosz.task.project.domain.TaskDomainService;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Task;
import com.arturjarosz.task.project.model.dto.TaskInnerDto;
import com.arturjarosz.task.project.status.task.TaskStatus;
import com.arturjarosz.task.project.status.task.TaskStatusTransitionService;
import com.arturjarosz.task.project.status.task.TaskWorkflow;
import com.arturjarosz.task.sharedkernel.annotations.DomainService;
import org.springframework.beans.factory.annotation.Autowired;

@DomainService
public class TaskDomainServiceImpl implements TaskDomainService {

    private final TaskWorkflow taskWorkflow;
    private final TaskStatusTransitionService taskStatusTransitionService;

    @Autowired
    public TaskDomainServiceImpl(TaskWorkflow taskWorkflow, TaskStatusTransitionService taskStatusTransitionService) {
        this.taskWorkflow = taskWorkflow;
        this.taskStatusTransitionService = taskStatusTransitionService;
    }

    @Override
    public Task createTask(Project project, Long stageId, TaskDto taskDto) {
        Task task = TaskDtoMapper.INSTANCE.createDtoToTask(taskDto, this.taskWorkflow);
        project.addTaskToStage(stageId, task);
        this.taskStatusTransitionService.createTask(project, stageId, task.getId());
        return TaskDtoMapper.INSTANCE.createDtoToTask(taskDto, this.taskWorkflow);
    }

    @Override
    public Task updateTask(Project project, Long stageId, Long taskId, TaskInnerDto taskInnerDto) {
        return project.updateTaskOnStage(stageId, taskId, taskInnerDto);
    }

    @Override
    public void updateTaskStatus(Project project, Long stageId, Long taskId, TaskStatus status) {
        this.taskStatusTransitionService.changeTaskStatus(project, stageId, taskId, status);
    }

    @Override
    public void rejectTask(Project project, Long stageId, Long taskId) {
        this.taskStatusTransitionService.reject(project, stageId, taskId);
    }

    @Override
    public void reopenTask(Project project, Long stageId, Long taskId) {
        this.taskStatusTransitionService.reopen(project, stageId, taskId);
    }
}
