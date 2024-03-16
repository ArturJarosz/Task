package com.arturjarosz.task.project.status.task.listener.impl;

import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.project.model.Task;
import com.arturjarosz.task.project.status.stage.StageStatus;
import com.arturjarosz.task.project.status.stage.StageStatusTransitionService;
import com.arturjarosz.task.project.status.task.TaskStatus;
import com.arturjarosz.task.project.status.task.TaskStatusTransition;
import com.arturjarosz.task.project.status.task.listener.TaskStatusTransitionListener;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Component
public class TaskBackToToDoListener implements TaskStatusTransitionListener {
    private final TaskStatusTransition transition = TaskStatusTransition.BACK_TO_TO_DO;
    private final StageStatusTransitionService stageStatusTransitionService;

    @Override
    public void onTaskStatusChange(Project project, Long stageId) {
        Stage stage = project.getStages()
                .stream()
                .filter(stageOnProject -> stageOnProject.getId().equals(stageId))
                .findFirst()
                .orElse(null);
        assert stage != null;
        if (stage.getStatus().equals(StageStatus.IN_PROGRESS) && this.hasOnlyTasksInToDoAndRejected(stage)) {
            this.stageStatusTransitionService.backToToDo(project, stageId);
        }
    }

    private boolean hasOnlyTasksInToDoAndRejected(Stage stage) {
        List<Task> allTasks = new ArrayList<>(stage.getTasks());
        allTasks.removeIf(task -> task.getStatus().equals(TaskStatus.REJECTED));
        allTasks.removeIf(task -> task.getStatus().equals(TaskStatus.TO_DO));
        return allTasks.isEmpty();
    }

    @Override
    public TaskStatusTransition getStatusTransition() {
        return this.transition;
    }
}
