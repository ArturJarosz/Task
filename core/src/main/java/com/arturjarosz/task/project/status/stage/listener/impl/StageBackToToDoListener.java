package com.arturjarosz.task.project.status.stage.listener.impl;

import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.project.status.project.ProjectStatus;
import com.arturjarosz.task.project.status.project.ProjectWorkflowService;
import com.arturjarosz.task.project.status.stage.StageStatus;
import com.arturjarosz.task.project.status.stage.StageStatusTransition;
import com.arturjarosz.task.project.status.stage.listener.StageStatusTransitionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class StageBackToToDoListener implements StageStatusTransitionListener {
    private final StageStatusTransition transition = StageStatusTransition.BACK_TO_TO_DO;
    private final ProjectWorkflowService projectWorkflowService;

    @Autowired
    public StageBackToToDoListener(ProjectWorkflowService projectWorkflowService) {
        this.projectWorkflowService = projectWorkflowService;
    }

    @Override
    public void onStageStatusChange(Project project) {
        if (this.hasStagesOnlyInRejectedAndToDoStatus(project)) {
            this.projectWorkflowService.changeProjectStatus(project, ProjectStatus.TO_DO);
        }
    }

    @Override
    public StageStatusTransition getStatusTransition() {
        return this.transition;
    }

    private boolean hasStagesOnlyInRejectedAndToDoStatus(Project project) {
        List<Stage> allStages = new ArrayList<>(project.getStages());
        //we are removing Stages in Rejected status, because they should not be taken into account
        allStages.removeIf(stage -> stage.getStatus().equals(StageStatus.REJECTED));
        allStages.removeIf(stage -> stage.getStatus().equals(StageStatus.TO_DO));
        return allStages.isEmpty();
    }
}
