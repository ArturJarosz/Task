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
public class TaskReopenListener implements TaskStatusTransitionListener {
    private final TaskStatusTransition statusTransition = TaskStatusTransition.REOPEN;
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
            this.stageStatusTransitionService.reopen(project, stageId);
        }
    }

    @Override
    public TaskStatusTransition getStatusTransition() {
        return this.statusTransition;
    }
}
