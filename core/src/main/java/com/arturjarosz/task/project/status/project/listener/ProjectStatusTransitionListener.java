package com.arturjarosz.task.project.status.project.listener;

import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.status.project.ProjectStatusTransition;
import com.arturjarosz.task.sharedkernel.status.StatusTransitionListener;

/**
 * Interface for listener of status transition of Project.
 */
public interface ProjectStatusTransitionListener extends StatusTransitionListener<ProjectStatusTransition> {

    /**
     * When there has been a change in status of Project, this method should be fired to apply changes resulting from
     * that status transition.
     */
    void onProjectStatusChange(Project project);
}
