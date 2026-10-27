package com.arturjarosz.task.project.status.project.listener.impl;

import com.arturjarosz.task.project.status.project.ProjectStatusTransition;
import org.springframework.stereotype.Component;

/**
 * Resuming rejected project moves it straight to IN_PROGRESS, so start date has to be set the same way as on starting
 * progress.
 */
@Component
public class ProjectResumeWorkListener extends ProjectStartProgressListener {
    private final ProjectStatusTransition transition = ProjectStatusTransition.RESUME_WORK;

    @Override
    public ProjectStatusTransition getStatusTransition() {
        return this.transition;
    }
}
