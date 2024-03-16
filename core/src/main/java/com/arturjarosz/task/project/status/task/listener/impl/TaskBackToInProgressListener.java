package com.arturjarosz.task.project.status.task.listener.impl;

import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.project.status.stage.StageStatus;
import com.arturjarosz.task.project.status.stage.StageStatusTransitionService;
import com.arturjarosz.task.project.status.task.TaskStatusTransition;
import com.arturjarosz.task.project.status.task.listener.TaskStatusTransitionListener;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class TaskBackToInProgressListener implements TaskStatusTransitionListener {
    private final TaskStatusTransition transition = TaskStatusTransition.BACK_TO_IN_PROGRESS;
    private final StageStatusTransitionService stageStatusTransitionService;

    @Override
    public void onTaskStatusChange(Project project, Long stageId) {
        Stage stage = project.getStages()
                .stream()
                .filter(stageOnProject -> stageOnProject.getId().equals(stageId))
                .findFirst()
                .orElse(null);
        assert stage != null;
        if (stage.getStatus().equals(StageStatus.DONE)) {
            this.stageStatusTransitionService.backToInProgress(project, stageId);
        }
    }

    @Override
    public TaskStatusTransition getStatusTransition() {
        return this.transition;
    }
}
