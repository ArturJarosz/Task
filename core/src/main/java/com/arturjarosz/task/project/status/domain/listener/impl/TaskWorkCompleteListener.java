package com.arturjarosz.task.project.status.domain.listener.impl;

import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.project.model.Task;
import com.arturjarosz.task.project.status.domain.StageStatus;
import com.arturjarosz.task.project.status.domain.StageWorkflowService;
import com.arturjarosz.task.project.status.domain.TaskStatus;
import com.arturjarosz.task.project.status.domain.TaskStatusTransition;
import com.arturjarosz.task.project.status.domain.listener.TaskStatusTransitionListener;

import java.util.ArrayList;
import java.util.List;

public class TaskWorkCompleteListener implements TaskStatusTransitionListener {
    private final TaskStatusTransition transition = TaskStatusTransition.COMPLETE_WORK;
    private final List<TaskStatus> finalStatuses;
    private final StageWorkflowService stageWorkflowService;

    public TaskWorkCompleteListener(StageWorkflowService stageWorkflowService) {
        this.stageWorkflowService = stageWorkflowService;
        this.finalStatuses = new ArrayList<>();
        this.finalStatuses.add(TaskStatus.REJECTED);
        this.finalStatuses.add(TaskStatus.DONE);
    }

    @Override
    public void onTaskStatusChange(Project project, Long stageId) {
        Stage stage = project.getStages().stream()
                .filter(stageOnProject -> stageOnProject.getId().equals(stageId))
                .findFirst().orElse(null);
        assert stage != null;
        if (stage.getStatus().equals(StageStatus.IN_PROGRESS) && this.hasNoTasksInNotFinalStatuses(stage)) {
            this.stageWorkflowService.changeStageStatusOnProject(project, stageId, StageStatus.DONE);
        }
    }

    @Override
    public TaskStatusTransition getStatusTransition() {
        return null;
    }

    private boolean hasNoTasksInNotFinalStatuses(Stage stage) {
        List<Task> allTasks = new ArrayList<>(stage.getTasks());
        allTasks.removeIf(task -> this.finalStatuses.contains(task.getStatus()));
        return allTasks.isEmpty();
    }
}
