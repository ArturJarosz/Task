package com.arturjarosz.task.project.status.domain.listener;

import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.status.domain.TaskStatusTransition;
import com.arturjarosz.task.sharedkernel.status.StatusChangeListener;

public interface TaskStatusTransitionListener extends StatusChangeListener<TaskStatusTransition> {

    void onTaskStatusChange(Project project, Long stageId);
}
