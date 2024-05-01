package com.arturjarosz.task.project.domain;

import com.arturjarosz.task.dto.TaskDto;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Task;
import com.arturjarosz.task.project.model.dto.TaskInnerDto;
import com.arturjarosz.task.project.status.task.TaskStatus;

public interface TaskDomainService {

    /**
     * Create new Task on Stage with given stageId on given Project according to data provided in TaskDto.
     */
    Task createTask(Project project, Long stageId, TaskDto taskDto);

    /**
     * Update Task with given taskId on given Stage with stageId for Project according to data passed in TaskInnerDto.
     */
    Task updateTask(Project project, Long stageId, Long taskId, TaskInnerDto taskInnerDto);

    /**
     * Update Task with given taskId on given Stage with stageId for Project according to status passed in TaskStatus.
     */
    void updateTaskStatus(Project project, Long stageId, Long taskId, TaskStatus status);

}
