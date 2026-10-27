package com.arturjarosz.task.project.status.project.listener.impl;

import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.status.project.ProjectStatusTransition;
import com.arturjarosz.task.project.status.project.listener.ProjectStatusTransitionListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ProjectFinishWorkListener implements ProjectStatusTransitionListener {
    private final ProjectStatusTransition transition = ProjectStatusTransition.FINISH_WORK;

    @Override
    public void onProjectStatusChange(Project project) {
        // end date provided earlier by user should not be overridden
        if (project.getEndDate() == null) {
            project.setEndDate(LocalDate.now());
        }
    }

    @Override
    public ProjectStatusTransition getStatusTransition() {
        return this.transition;
    }
}
