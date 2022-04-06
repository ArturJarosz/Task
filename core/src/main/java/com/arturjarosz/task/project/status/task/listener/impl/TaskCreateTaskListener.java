package com.arturjarosz.task.project.status.task.listener.impl;

import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.project.status.stage.StageStatus;
import com.arturjarosz.task.project.status.stage.StageWorkflowService;
import com.arturjarosz.task.project.status.task.TaskStatusTransition;
import com.arturjarosz.task.project.status.task.listener.TaskStatusTransitionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TaskCreateTaskListener implements TaskStatusTransitionListener {
    private final TaskStatusTransition transition = TaskStatusTransition.CREATE_TASK;

    private final StageWorkflowService stageWorkflowService;

    @Autowired
    public TaskCreateTaskListener(StageWorkflowService stageWorkflowService) {
        this.stageWorkflowService = stageWorkflowService;
    }

    @Override
    public void onTaskStatusChange(Project project, Long stageId) {
        Stage stage = project.getStages().stream()
                .filter(stageOnProject -> stageOnProject.getId().equals(stageId))
                .findFirst().orElse(null);
        assert stage != null;
        if (stage.getStatus() == StageStatus.DONE) {
            this.stageWorkflowService
                    .changeStageStatusOnProject(project, stageId, StageStatus.IN_PROGRESS);
        }
    }

    @Override
    public TaskStatusTransition getStatusTransition() {
        return this.transition;
    }
}
