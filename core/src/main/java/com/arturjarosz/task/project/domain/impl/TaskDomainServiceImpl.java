package com.arturjarosz.task.project.domain.impl;

import com.arturjarosz.task.project.application.dto.TaskDto;
import com.arturjarosz.task.project.application.mapper.TaskDtoMapper;
import com.arturjarosz.task.project.domain.TaskDomainService;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Task;
import com.arturjarosz.task.project.status.domain.TaskStatus;
import com.arturjarosz.task.project.status.domain.TaskStatusTransition;
import com.arturjarosz.task.project.status.domain.TaskWorkflow;
import com.arturjarosz.task.project.status.domain.TaskWorkflowService;
import com.arturjarosz.task.project.status.domain.listener.TaskStatusTransitionListener;
import com.arturjarosz.task.project.status.domain.listener.impl.CreateTaskListener;
import com.arturjarosz.task.sharedkernel.annotations.DomainService;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.util.List;

@DomainService
public class TaskDomainServiceImpl implements TaskDomainService {

    private final TaskWorkflow taskWorkflow;
    private final TaskWorkflowService taskWorkflowService;
    private final TaskStatusTransitionListener taskStatusTransitionListener;

    @Autowired
    public TaskDomainServiceImpl(TaskWorkflow taskWorkflow, TaskWorkflowService taskWorkflowService,
                                 List<TaskStatusTransitionListener> taskStatusTransitionListenerList) {
        this.taskWorkflow = taskWorkflow;
        this.taskWorkflowService = taskWorkflowService;
        this.taskStatusTransitionListener = taskStatusTransitionListenerList.stream()
                .filter(listener -> listener.getStatusTransition().equals(TaskStatusTransition.CREATE_TASK))
                .findFirst().orElse(null);
    }

    @Override
    public Task createTask(Project project, Long stageId, TaskDto taskDto) {
        return TaskDtoMapper.INSTANCE.createDtoToTask(taskDto, this.taskWorkflow);
    }

    @Override
    public void updateTaskStatus() {

    }
}
