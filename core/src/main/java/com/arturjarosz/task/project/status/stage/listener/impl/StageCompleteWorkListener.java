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
public class StageCompleteWorkListener implements StageStatusTransitionListener {
    private final StageStatusTransition transition = StageStatusTransition.COMPLETE_WORK;
    private final ProjectStatusTransitionService projectStatusTransitionService;

    @Autowired
    public StageCompleteWorkListener(ProjectStatusTransitionService projectStatusTransitionService) {
        this.projectStatusTransitionService = projectStatusTransitionService;
    }

    @Override
    public void onStageStatusChange(Project project) {
        if (this.hasStagesOnlyInRejectedAndDoneStatus(project)) {
            this.projectStatusTransitionService.finishWork(project);
        }
    }

    @Override
    public StageStatusTransition getStatusTransition() {
        return this.transition;
    }

    private boolean hasStagesOnlyInRejectedAndDoneStatus(Project project) {
        List<Stage> allStages = new ArrayList<>(project.getStages());
        //we are removing Stages in Rejected status, because they should not be taken into account
        allStages.removeIf(stage -> stage.getStatus().equals(StageStatus.REJECTED));
        allStages.removeIf(stage -> stage.getStatus().equals(StageStatus.DONE));
        return allStages.isEmpty();
    }
}
