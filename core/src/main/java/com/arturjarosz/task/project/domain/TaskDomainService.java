package com.arturjarosz.task.project.domain;

import com.arturjarosz.task.project.application.dto.TaskDto;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Task;
import com.arturjarosz.task.project.model.dto.TaskInnerDto;
import com.arturjarosz.task.project.status.task.TaskStatus;

public interface TaskDomainService {
    Task createTask(Project project, Long stageId, TaskDto taskDto);

    Task updateTask(Project project, Long stageId, Long taskId, TaskInnerDto taskInnerDto);

    void updateTaskStatus(Project project, Long stageId, Long taskId, TaskStatus status);

    void rejectTask(Project project, Long stageId, Long taskId);

    void reopenTask(Project project, Long stageId, Long taskId);

}
