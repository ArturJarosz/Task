package com.arturjarosz.task.project.domain;

import com.arturjarosz.task.project.application.dto.TaskDto;
import com.arturjarosz.task.project.model.Task;

public interface TaskDomainService {
    Task createTask(TaskDto taskDto);

    void updateTaskStatus();

}
