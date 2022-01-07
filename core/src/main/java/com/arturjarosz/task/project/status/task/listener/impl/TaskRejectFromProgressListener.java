package com.arturjarosz.task.project.status.task.listener.impl;

import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.project.model.Task;
import com.arturjarosz.task.project.status.stage.StageStatus;
import com.arturjarosz.task.project.status.stage.StageWorkflowService;
import com.arturjarosz.task.project.status.task.TaskStatus;
import com.arturjarosz.task.project.status.task.TaskStatusTransition;
import com.arturjarosz.task.project.status.task.listener.TaskStatusTransitionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TaskRejectFromProgressListener implements TaskStatusTransitionListener {
    private final TaskStatusTransition transition = TaskStatusTransition.REJECT_FROM_IN_PROGRESS;
    private final StageWorkflowService stageWorkflowService;

    @Autowired
    public TaskRejectFromProgressListener(StageWorkflowService stageWorkflowService) {
        this.stageWorkflowService = stageWorkflowService;
    }

    @Override
    public void onTaskStatusChange(Project project, Long stageId) {
        Stage stage = project.getStages().stream().filter(stageOnProject -> stageOnProject.getId().equals(stageId))
                .findFirst().orElse(null);
        assert stage != null;
        /*
        First we are checking for REJECTED and TO_DO, as Stage with only Tasks in only REJECTED statuses should
        go back to TO_DO.
         */
        if (this.hasStatusesOnlyInRejected(stage)) {
            this.stageWorkflowService.changeStageStatusOnProject(project, stageId, StageStatus.TO_DO);
        } else if (this.hasStatusesOnlyInRejectedAndToDo(stage)) {
            this.stageWorkflowService.changeStageStatusOnProject(project, stageId, StageStatus.TO_DO);
        } else if (this.hasStatusesOnlyInRejectedAndCompleted(stage)) {
            this.stageWorkflowService.changeStageStatusOnProject(project, stageId, StageStatus.COMPLETED);
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

    private boolean hasStatusesOnlyInRejectedAndCompleted(Stage stage) {
        List<Task> allTasks = new ArrayList<>(stage.getTasks());
        //we are removing Task in REJECTED status, because they should not be taken into account
        allTasks.removeIf(task -> task.getStatus().equals(TaskStatus.REJECTED));
        allTasks.removeIf(task -> task.getStatus().equals(TaskStatus.COMPLETED));
        return allTasks.isEmpty();
    }

    @Override
    public TaskStatusTransition getStatusTransition() {
        return this.transition;
    }
}
