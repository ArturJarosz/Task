package com.arturjarosz.task.project.status.domain;

import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Task;
import com.arturjarosz.task.sharedkernel.status.WorkflowService;

public interface TaskWorkflowService extends WorkflowService<TaskStatus, TaskStatusTransition, Task> {

    /**
     * Changes status for Task with given taskId, on Stage with stageId on Project to newStatus of type TaskStatus.
     *
     * @param project
     * @param stageId
     * @param taskId
     * @param newStatus
     */
    void changeTaskStatusOnProject(Project project, Long stageId, Long taskId, TaskStatus newStatus);

}
