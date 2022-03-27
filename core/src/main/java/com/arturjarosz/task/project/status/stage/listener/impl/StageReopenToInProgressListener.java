package com.arturjarosz.task.project.status.stage.listener.impl;

import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.status.project.ProjectStatus;
import com.arturjarosz.task.project.status.project.ProjectStatusTransitionService;
import com.arturjarosz.task.project.status.stage.StageStatusTransition;
import com.arturjarosz.task.project.status.stage.listener.StageStatusTransitionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StageReopenToInProgressListener implements StageStatusTransitionListener {
    private final StageStatusTransition transition = StageStatusTransition.REOPEN_TO_PROGRESS;
    private final ProjectStatusTransitionService projectStatusTransitionService;

    @Autowired
    public StageReopenToInProgressListener(ProjectStatusTransitionService projectStatusTransitionService) {
        this.projectStatusTransitionService = projectStatusTransitionService;
    }

    @Override
    public void onStageStatusChange(Project project) {
        if (project.getStatus().equals(ProjectStatus.DONE)) {
            this.projectStatusTransitionService.backToProgress(project);
        }
        if (project.getStatus().equals(ProjectStatus.TO_DO)) {
            this.projectStatusTransitionService.startProgress(project);
        }
    }

    @Override
    public StageStatusTransition getStatusTransition() {
        return this.transition;
    }

}
