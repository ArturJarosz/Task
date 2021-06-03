package com.arturjarosz.task.project.status.task.impl;

import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.status.task.TaskStatus;
import com.arturjarosz.task.project.status.task.TaskStatusTransitionService;
import com.arturjarosz.task.project.status.task.TaskWorkflowService;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;

@ApplicationService
public class TaskStatusTransitionServiceImpl implements TaskStatusTransitionService {
    private final TaskWorkflowService taskWorkflowService;

    @Autowired
    public TaskStatusTransitionServiceImpl(TaskWorkflowService taskWorkflowService) {
        this.taskWorkflowService = taskWorkflowService;
    }

    @Override
    public void createTask(Project project, Long stageId, Long taskId) {
        this.taskWorkflowService.changeTaskStatusOnProject(project, stageId, taskId, TaskStatus.TO_DO);
    }

    @Override
    public void startProgress(Project project, Long stageId, Long taskId) {
        this.taskWorkflowService.changeTaskStatusOnProject(project, stageId, taskId, TaskStatus.IN_PROGRESS);
    }

    @Override
    public void rejectFromToDo(Project project, Long stageId, Long taskId) {
        this.taskWorkflowService.changeTaskStatusOnProject(project, stageId, taskId, TaskStatus.REJECTED);
    }

    @Override
    public void completeWork(Project project, Long stageId, Long taskId) {
        this.taskWorkflowService.changeTaskStatusOnProject(project, stageId, taskId, TaskStatus.COMPLETED);
    }

    @Override
    public void rejectFromInProgress(Project project, Long stageId, Long taskId) {
        this.taskWorkflowService.changeTaskStatusOnProject(project, stageId, taskId, TaskStatus.REJECTED);
    }

    @Override
    public void backToToDo(Project project, Long stageId, Long taskId) {
        this.taskWorkflowService.changeTaskStatusOnProject(project, stageId, taskId, TaskStatus.TO_DO);
    }

    @Override
    public void reopenTask(Project project, Long stageId, Long taskId) {
        this.taskWorkflowService.changeTaskStatusOnProject(project, stageId, taskId, TaskStatus.TO_DO);
    }

    @Override
    public void backToInProgress(Project project, Long stageId, Long taskId) {
        this.taskWorkflowService.changeTaskStatusOnProject(project, stageId, taskId, TaskStatus.IN_PROGRESS);
    }

    @Override
    public void reopen(Project project, Long stageId, Long taskId) {
        this.taskWorkflowService.changeTaskStatusOnProject(project, stageId, taskId, TaskStatus.TO_DO);
    }

    @Override
    public void reject(Project project, Long stageId, Long taskId) {
        this.taskWorkflowService.changeTaskStatusOnProject(project, stageId, taskId, TaskStatus.REJECTED);
    }

    @Override
    public void changeTaskStatus(Project project, Long stageId, Long taskId, TaskStatus newStatus) {
        this.taskWorkflowService.changeTaskStatusOnProject(project, stageId, taskId, newStatus);
    }
}
