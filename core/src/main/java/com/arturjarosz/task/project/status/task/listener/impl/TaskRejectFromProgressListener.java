package com.arturjarosz.task.project.status.task.listener.impl;

import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.project.model.Task;
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
public class TaskRejectFromProgressListener implements TaskStatusTransitionListener {
    private final TaskStatusTransition transition = TaskStatusTransition.REJECT_FROM_IN_PROGRESS;
    private final StageStatusTransitionService stageStatusTransitionService;

    @Override
    public void onTaskStatusChange(Project project, Long stageId) {
        Stage stage = project.getStages()
                .stream()
                .filter(stageOnProject -> stageOnProject.getId().equals(stageId))
                .findFirst()
                .orElse(null);
        assert stage != null;
        /*
        First we are checking for REJECTED and TO_DO, as Stage with only Tasks in only REJECTED statuses should
        go back to TO_DO.
         */
        if (this.hasStatusesOnlyInRejected(stage) || this.hasStatusesOnlyInRejectedAndToDo(stage)) {
            this.stageStatusTransitionService.rejectFromInProgress(project, stageId);
        } else if (this.hasStatusesOnlyInRejectedAndDone(stage)) {
            this.stageStatusTransitionService.completeWork(project, stageId);
        }
    }

    private boolean hasStatusesOnlyInRejected(Stage stage) {
        List<Task> allTasks = new ArrayList<>(stage.getTasks());
        allTasks.removeIf(task -> task.getStatus().equals(TaskStatus.REJECTED));
        return allTasks.isEmpty();
    }

    private boolean hasStatusesOnlyInRejectedAndToDo(Stage stage) {
        List<Task> allTasks = new ArrayList<>(stage.getTasks());
        //we are removing Task in REJECTED status, because they should not be taken into account
        allTasks.removeIf(task -> task.getStatus().equals(TaskStatus.REJECTED));
        allTasks.removeIf(task -> task.getStatus().equals(TaskStatus.TO_DO));
        return allTasks.isEmpty();
    }

    private boolean hasStatusesOnlyInRejectedAndDone(Stage stage) {
        List<Task> allTasks = new ArrayList<>(stage.getTasks());
        //we are removing Task in REJECTED status, because they should not be taken into account
        allTasks.removeIf(task -> task.getStatus().equals(TaskStatus.REJECTED));
        allTasks.removeIf(task -> task.getStatus().equals(TaskStatus.DONE));
        return allTasks.isEmpty();
    }

    @Override
    public TaskStatusTransition getStatusTransition() {
        return this.transition;
    }
}
