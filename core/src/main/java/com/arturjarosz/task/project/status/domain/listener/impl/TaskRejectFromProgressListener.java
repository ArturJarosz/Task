package com.arturjarosz.task.project.status.domain.listener.impl;

import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.project.model.Task;
import com.arturjarosz.task.project.status.domain.StageStatus;
import com.arturjarosz.task.project.status.domain.StageWorkflowService;
import com.arturjarosz.task.project.status.domain.TaskStatus;
import com.arturjarosz.task.project.status.domain.TaskStatusTransition;
import com.arturjarosz.task.project.status.domain.listener.TaskStatusTransitionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@Component
public class TaskRejectFromProgressListener implements TaskStatusTransitionListener {
    private final TaskStatusTransition transition = TaskStatusTransition.REJECT_FROM_IN_PROGRESS;
    private final EnumSet<TaskStatus> finalStatuses;
    private final StageWorkflowService stageWorkflowService;

    @Autowired
    public TaskRejectFromProgressListener(StageWorkflowService stageWorkflowService) {
        this.stageWorkflowService = stageWorkflowService;
        this.finalStatuses = EnumSet.of(TaskStatus.DONE, TaskStatus.REJECTED);
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

    private boolean hasNoTasksInNotFinalStatuses(Stage stage) {
        List<Task> allTasks = new ArrayList<>(stage.getTasks());
        allTasks.removeIf(task -> this.finalStatuses.contains(task.getStatus()));
        return allTasks.isEmpty();
    }

    @Override
    public TaskStatusTransition getStatusTransition() {
        return this.transition;
    }
}
