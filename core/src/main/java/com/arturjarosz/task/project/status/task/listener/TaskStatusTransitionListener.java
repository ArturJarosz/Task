package com.arturjarosz.task.project.status.task.listener;

import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.status.task.TaskStatusTransition;
import com.arturjarosz.task.sharedkernel.status.StatusChangeListener;

public interface TaskStatusTransitionListener extends StatusChangeListener<TaskStatusTransition> {

    void onTaskStatusChange(Project project, Long stageId);
}
