package com.arturjarosz.task.project.status.task.listener.impl;

import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.stage.model.Stage;
import com.arturjarosz.task.project.model.Task;
import com.arturjarosz.task.stage.status.StageStatus;
import com.arturjarosz.task.stage.status.StageWorkflowService;
import com.arturjarosz.task.project.status.task.TaskStatus;
import com.arturjarosz.task.project.status.task.TaskStatusTransition;
import com.arturjarosz.task.project.status.task.listener.TaskStatusTransitionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TaskRejectFromToDoTaskListener implements TaskStatusTransitionListener {
    private final TaskStatusTransition transition = TaskStatusTransition.REJECT_FROM_TO_DO;
    private final StageWorkflowService stageWorkflowService;

    @Autowired
    public TaskRejectFromToDoTaskListener(StageWorkflowService stageWorkflowService) {
        this.stageWorkflowService = stageWorkflowService;
    }

    @Override
    public void onTaskStatusChange(Project project, Long stageId) {
        Stage stage = project.getStages().stream()
                .filter(stageOnProject -> stageOnProject.getId().equals(stageId))
                .findFirst().orElse(null);
        assert stage != null;
        if (stage.getStatus().equals(StageStatus.IN_PROGRESS) && (this.allTasksInCompletedOrRejected(stage))) {
            this.stageWorkflowService.changeStageStatusOnProject(project, stageId, StageStatus.COMPLETED);
        }
    }

    @Override
    public TaskStatusTransition getStatusTransition() {
        return this.transition;
    }

    private boolean allTasksInCompletedOrRejected(Stage stage) {
        List<Task> allTasks = new ArrayList<>(stage.getTasks());
        //we are removing Task in Rejected status, because they should not be taken into account
        allTasks.removeIf(task -> task.getStatus().equals(TaskStatus.REJECTED));
        allTasks.removeIf(task -> task.getStatus().equals(TaskStatus.COMPLETED));
        return allTasks.isEmpty();
    }

}
