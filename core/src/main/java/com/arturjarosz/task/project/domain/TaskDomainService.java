package com.arturjarosz.task.project.domain;

import com.arturjarosz.task.project.application.dto.TaskDto;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Task;
import com.arturjarosz.task.project.model.dto.TaskInnerDto;
import com.arturjarosz.task.project.status.task.TaskStatus;

public interface TaskDomainService {

    /**
     * Create new Task on Stage with given stageId on given Project according to data provided in TaskDto.
     *
     * @param project
     * @param stageId
     * @param taskDto
     * @return
     */
    Task createTask(Project project, Long stageId, TaskDto taskDto);

    /**
     * Update Task with given taskId on given Stage with stageId for Project according to data passed in TaskInnerDto.
     *
     * @param project
     * @param stageId
     * @param taskId
     * @param taskInnerDto
     * @return
     */
    Task updateTask(Project project, Long stageId, Long taskId, TaskInnerDto taskInnerDto);

    /**
     * Update Task with given taskId on given Stage with stageId for Project according to status passed in TaskStatus.
     *
     * @param project
     * @param stageId
     * @param taskId
     * @param status
     */
    void updateTaskStatus(Project project, Long stageId, Long taskId, TaskStatus status);

    /**
     * Mark Task with given taskId on given Stage with stageId for Project as Rejected.
     *
     * @param project
     * @param stageId
     * @param taskId
     */
    void rejectTask(Project project, Long stageId, Long taskId);

    /**
     * Reopen rejected Task with given taskId on given Stage with stageId for Project to work.
     *
     * @param project
     * @param stageId
     * @param taskId
     */
    void reopenTask(Project project, Long stageId, Long taskId);

}
