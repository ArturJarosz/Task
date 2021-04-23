package com.arturjarosz.task.project.status.stage.listener.impl;

import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.status.project.ProjectStatus;
import com.arturjarosz.task.project.status.project.ProjectWorkflowService;
import com.arturjarosz.task.project.status.stage.StageStatusTransition;
import com.arturjarosz.task.project.status.stage.listener.StageStatusTransitionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StageReopenToInProgressListener implements StageStatusTransitionListener {
    private final StageStatusTransition transition = StageStatusTransition.REOPEN_TO_PROGRESS;
    private final ProjectWorkflowService projectWorkflowService;

    @Autowired
    public StageReopenToInProgressListener(ProjectWorkflowService projectWorkflowService) {
        this.projectWorkflowService = projectWorkflowService;
    }

    @Override
    public void onStageStatusChange(Project project) {
        if (project.getStatus().equals(ProjectStatus.COMPLETED)) {
            this.projectWorkflowService.changeProjectStatus(project, ProjectStatus.IN_PROGRESS);
        }
    }

    @Override
    public StageStatusTransition getStatusTransition() {
        return this.transition;
    }
}
