package com.arturjarosz.task.stage.status.listener.impl;

import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.status.project.ProjectStatus;
import com.arturjarosz.task.project.status.project.ProjectStatusTransitionService;
import com.arturjarosz.task.stage.status.StageStatusTransition;
import com.arturjarosz.task.stage.status.listener.StageStatusTransitionListener;
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
        if (project.getStatus().equals(ProjectStatus.COMPLETED)) {
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
