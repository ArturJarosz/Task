package com.arturjarosz.task.project.status.stage.listener.impl;

import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.project.status.project.ProjectStatusTransitionService;
import com.arturjarosz.task.project.status.stage.StageStatus;
import com.arturjarosz.task.project.status.stage.StageStatusTransition;
import com.arturjarosz.task.project.status.stage.listener.StageStatusTransitionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class StageRejectFromProgressListener implements StageStatusTransitionListener {
    private final StageStatusTransition transition = StageStatusTransition.REJECT_FROM_IN_PROGRESS;
    private final ProjectStatusTransitionService projectStatusTransitionService;

    @Autowired
    public StageRejectFromProgressListener(ProjectStatusTransitionService projectStatusTransitionService) {
        this.projectStatusTransitionService = projectStatusTransitionService;
    }

    @Override
    public void onStageStatusChange(Project project) {
        if (this.hasStagesOnlyInRejected(project)) {
            this.projectStatusTransitionService.backToToDo(project);
        } else if (this.hasStagesOnlyInRejectedAndToDoStatus(project)) {
            this.projectStatusTransitionService.backToToDo(project);
        } else if (this.hasStagesOnlyInRejectedAndCompletedStatus(project)) {
            this.projectStatusTransitionService.completeWork(project);
        }
    }

    @Override
    public StageStatusTransition getStatusTransition() {
        return this.transition;
    }

    private boolean hasStagesOnlyInRejected(Project project) {
        List<Stage> allStages = new ArrayList<>(project.getStages());
        allStages.removeIf(stage -> stage.getStatus().equals(StageStatus.REJECTED));
        return allStages.isEmpty();
    }

    private boolean hasStagesOnlyInRejectedAndToDoStatus(Project project) {
        List<Stage> allStages = new ArrayList<>(project.getStages());
        //we are removing Stages in Rejected status, because they should not be taken into account
        allStages.removeIf(stage -> stage.getStatus().equals(StageStatus.REJECTED));
        allStages.removeIf(stage -> stage.getStatus().equals(StageStatus.TO_DO));
        return allStages.isEmpty();
    }

    private boolean hasStagesOnlyInRejectedAndCompletedStatus(Project project) {
        List<Stage> allStages = new ArrayList<>(project.getStages());
        //we are removing Stages in Rejected status, because they should not be taken into account
        allStages.removeIf(stage -> stage.getStatus().equals(StageStatus.REJECTED));
        allStages.removeIf(stage -> stage.getStatus().equals(StageStatus.COMPLETED));
        return allStages.isEmpty();
    }
}
