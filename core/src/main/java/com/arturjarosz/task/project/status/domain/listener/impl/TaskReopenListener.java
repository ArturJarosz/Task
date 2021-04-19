package com.arturjarosz.task.project.status.domain.listener.impl;

import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.project.status.domain.StageStatus;
import com.arturjarosz.task.project.status.domain.StageWorkflowService;
import com.arturjarosz.task.project.status.domain.TaskStatusTransition;
import com.arturjarosz.task.project.status.domain.listener.TaskStatusTransitionListener;

public class TaskReopenListener implements TaskStatusTransitionListener {
    private final TaskStatusTransition statusTransition = TaskStatusTransition.REOPEN;
    private final StageWorkflowService stageWorkflowService;

    public TaskReopenListener(StageWorkflowService stageWorkflowService) {
        this.stageWorkflowService = stageWorkflowService;
    }

    @Override
    public void onTaskStatusChange(Project project, Long stageId) {
        Stage stage = project.getStages().stream()
                .filter(stageOnProject -> stageOnProject.getId().equals(stageId))
                .findFirst().orElse(null);
        assert stage != null;
        if (stage.getStatus().equals(StageStatus.DONE)) {
            this.stageWorkflowService.changeStageStatusOnProject(project, stageId, StageStatus.IN_PROGRESS);
        }
    }

    @Override
    public TaskStatusTransition getStatusTransition() {
        return this.statusTransition;
    }
}
