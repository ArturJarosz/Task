package com.arturjarosz.task.project.application;

import com.arturjarosz.task.project.application.dto.TaskDto;
import com.arturjarosz.task.sharedkernel.model.CreatedEntityDto;

import java.util.List;

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

    /**
     * Return Task as TaskDto for Task on Stage on Project with given taskId, stageId and projectId. If Project,
     * Stage or Task does not exist, new Exception will be thrown.
     *
     * @param projectId
     * @param stageId
     * @param taskId
     * @return
     */
    TaskDto getTask(Long projectId, Long stageId, Long taskId);

    /**
     * Return list of Tasks as TasksDtos for Stage on Project with stageId and ProjectId. If Project or Stage
     * does not exist, then new Exception will be thrown.
     *
     * @param projectId
     * @param stageId
     * @return
     */
    List<TaskDto> getTaskList(Long projectId, Long stageId);

    /**
     * Set Task with taskId as rejected for Stage with stageId on Project with ProjectId. If Project, Stage or Task
     * does not exist, new exception will be thrown.
     *
     * @param projectId
     * @param stageId
     * @param taskId
     */
    void rejectTask(Long projectId, Long stageId, Long taskId);

    /**
     * Allows to continue work on Task, changing its status to To Do. If Project, Stage or Task do not exist,
     * new exception will be thrown.
     *
     * @param projectId
     * @param stageId
     * @param taskId
     */
    void reopenTask(Long projectId, Long stageId, Long taskId);
}
