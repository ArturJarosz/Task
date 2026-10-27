package com.arturjarosz.task.project.status.project.listener.impl;

import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.status.project.ProjectStatusTransition;
import com.arturjarosz.task.project.status.project.listener.ProjectStatusTransitionListener;
import org.springframework.stereotype.Component;

/**
 * Project moved back from DONE to IN_PROGRESS is not finished anymore, so end date has to be removed.
 */
@Component
public class ProjectReopenToProgressListener implements ProjectStatusTransitionListener {
    private final ProjectStatusTransition transition = ProjectStatusTransition.REOPEN_TO_PROGRESS;

    @Override
    public void onProjectStatusChange(Project project) {
        project.setEndDate(null);
    }

    @Override
    public ProjectStatusTransition getStatusTransition() {
        return this.transition;
    }
}
