package com.arturjarosz.task.project.status.task;

import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Task;
import com.arturjarosz.task.sharedkernel.status.WorkflowService;

public interface TaskWorkflowService extends WorkflowService<TaskStatus, Task> {

    /**
     * Changes status for Task with given taskId, on Stage with stageId on Project to newStatus.
     *
     * @param project
     * @param stageId
     * @param taskId
     * @param newStatus
     */
    void changeTaskStatusOnProject(Project project, Long stageId, Long taskId, TaskStatus newStatus);

    /**
     * Method contains logic that should be executed before status transition is executed, such as validators.
     *
     * @param project
     * @param task
     * @param stageId
     * @param statusTransition
     */
    void beforeStatusChange(Project project, Task task, Long stageId, TaskStatusTransition statusTransition);

    /**
     * Contains logic that should be executed after successful execution of status transition, like listeners
     * or loggers.
     *
     * @param project
     * @param stageId
     * @param statusTransition
     */
    void afterStatusChange(Project project, Long stageId, TaskStatusTransition statusTransition);
}
