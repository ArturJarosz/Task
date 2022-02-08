package com.arturjarosz.task.project.status.task;

import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Task;
import com.arturjarosz.task.sharedkernel.status.WorkflowService;

public interface TaskWorkflowService extends WorkflowService<TaskStatus, Task> {

    /**
     * Changes status for Task with given taskId, on Stage with stageId on Project to newStatus.
     * Runs validators to check, whether Project and Stage statuses are allowing for status change.
     * After that check if the transition from current TaskStatus for target one can be done. If status transition is
     * not possible, new Exception will be thrown.
     *
     * Method is also responsible for triggering action that should be run before status transition and after status
     * transition is made.
     */
    void changeTaskStatusOnProject(Project project, Long stageId, Long taskId, TaskStatus newStatus);

    /**
     * Method contains logic that should be executed before status transition is executed, such as validators.
     */
    void beforeStatusChange(Project project, Task task, Long stageId, TaskStatusTransition statusTransition);

    /**
     * Contains logic that should be executed after successful execution of status transition, like listeners
     * or loggers.
     */
    void afterStatusChange(Project project, Long stageId, TaskStatusTransition statusTransition);
}
