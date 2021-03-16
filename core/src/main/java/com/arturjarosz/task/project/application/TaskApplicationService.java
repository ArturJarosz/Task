package com.arturjarosz.task.project.application;

import com.arturjarosz.task.project.application.dto.TaskDto;
import com.arturjarosz.task.sharedkernel.model.CreatedEntityDto;

public interface TaskApplicationService {

    /**
     * Creates Task for Stage of given stageId on Project of given projectId according to data in DaskDto.
     * If project or stage doesn't exist, or data provided in TaskDto does not meet required criteria for creating
     * Task, then new exception is thrown and Stage is not created.
     *
     * @param projectId
     * @param stageId
     * @param taskDto
     * @return
     */
    CreatedEntityDto createTask(Long projectId, Long stageId, TaskDto taskDto);

    /**
     * Delete Task with given id on Stage with stageId, for Project with given projectId. If Task, Stage or Project
     * does not exists, then exception is thrown.
     *
     * @param projectId
     * @param stageId
     * @param taskId
     */
    void deleteTask(Long projectId, Long stageId, Long taskId);

    /**
     * Update Task with given id on stage with stageId for Project with projectId based on data from taskDto. If
     * Project, Stage or Task does not exist, new exception is thrown.
     *
     * @param projectId
     * @param stageId
     * @param taskId
     * @param taskDto
     */
    void updateTask(Long projectId, Long stageId, Long taskId, TaskDto taskDto);

    /**
     * Update Status of Task with given taskId, on Stage with stageId on Project with projectId. If Project, Stage or
     * Status does not exist or Stage is not valid Status on used Workflow an exception will be thrown.
     *
     * @param projectId
     * @param stageId
     * @param taskId
     * @param taskDto
     */
    void updateTaskStatus(Long projectId, Long stageId, Long taskId, TaskDto taskDto);
}
