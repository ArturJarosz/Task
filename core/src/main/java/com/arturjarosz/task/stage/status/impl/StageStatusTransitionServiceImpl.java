package com.arturjarosz.task.stage.status.impl;

import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Task;
import com.arturjarosz.task.stage.status.StageStatus;
import com.arturjarosz.task.stage.status.StageStatusTransitionService;
import com.arturjarosz.task.stage.status.StageWorkflowService;
import com.arturjarosz.task.project.status.task.TaskStatus;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.stage.model.Stage;
import com.arturjarosz.task.stage.query.StageQueryService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@ApplicationService
public class StageStatusTransitionServiceImpl implements StageStatusTransitionService {
    private final StageWorkflowService stageWorkflowService;
    private final StageQueryService stageQueryService;

    @Autowired
    public StageStatusTransitionServiceImpl(StageWorkflowService stageWorkflowService,
                                            StageQueryService stageQueryService) {
        this.stageWorkflowService = stageWorkflowService;
        this.stageQueryService = stageQueryService;
    }

    @Override
    public void createStage(Project project, Long stageId) {
        this.stageWorkflowService.changeStageStatusOnProject(project, stageId, StageStatus.TO_DO);
    }

    @Override
    public void startProgress(Project project, Long stageId) {

    }

    @Override
    public void rejectFromToDo(Project project, Long stageId) {

    }

    @Override
    public void completeWork(Project project, Long stageId) {

    }

    @Override
    public void rejectFromInProgress(Project project, Long stageId) {

    }

    @Override
    public void backToToDo(Project project, Long stageId) {

    }

    @Override
    public void reopenToToDo(Project project, Long stageId) {
        this.stageWorkflowService.changeStageStatusOnProject(project, stageId, StageStatus.TO_DO);
    }

    @Override
    public void reopenToInProgress(Project project, Long stageId) {
        this.stageWorkflowService
                .changeStageStatusOnProject(project, stageId, StageStatus.IN_PROGRESS);
    }

    @Override
    public void backToInProgress(Project project, Long stageId) {

    }

    @Override
    public void reject(Project project, Long stageId) {
        this.stageWorkflowService
                .changeStageStatusOnProject(project, stageId, StageStatus.REJECTED);
    }

    @Override
    public void reopen(Project project, Long stageId) {
        if (this.stageHasOnlyTasksInToDoStatus(stageId)) {
            this.reopenToToDo(project, stageId);
        } else {
            this.rejectFromInProgress(project, stageId);
        }
    }

    private boolean stageHasOnlyTasksInToDoStatus(Long stageId) {
        Stage stage = this.stageQueryService.getStageById(stageId);
        List<Task> allTasks = new ArrayList<>(stage.getTasks());
        allTasks.removeIf(task -> task.getStatus().equals(TaskStatus.REJECTED));
        allTasks.removeIf(task -> task.getStatus().equals(TaskStatus.TO_DO));
        return allTasks.isEmpty();
    }
}
