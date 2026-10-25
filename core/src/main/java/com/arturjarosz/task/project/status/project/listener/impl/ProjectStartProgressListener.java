package com.arturjarosz.task.project.status.project.listener.impl;

import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.status.project.ProjectStatusTransition;
import com.arturjarosz.task.project.status.project.listener.ProjectStatusTransitionListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ProjectStartProgressListener implements ProjectStatusTransitionListener {
    private final ProjectStatusTransition transition = ProjectStatusTransition.START_PROGRESS;

    @Override
    public void onProjectStatusChange(Project project) {
        // start date provided earlier by user (or kept from previous progress) should not be overridden
        if (project.getStartDate() == null) {
            project.setStartDate(LocalDate.now());
        }
    }

    @Override
    public ProjectStatusTransition getStatusTransition() {
        return this.transition;
    }
}
