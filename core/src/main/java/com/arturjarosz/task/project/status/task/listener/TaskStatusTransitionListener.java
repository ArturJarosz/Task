package com.arturjarosz.task.project.status.task.listener;

import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.status.task.TaskStatusTransition;
import com.arturjarosz.task.sharedkernel.status.StatusTransitionListener;

/**
 * Interface for listener of status transition for Task.
 */
public interface TaskStatusTransitionListener extends StatusTransitionListener<TaskStatusTransition> {

    /**
     * When there has been a change in status of Task, this method should be fired and checked whether some changes to
     * Stage is required.
     */
    void onTaskStatusChange(Project project, Long stageId);
}
