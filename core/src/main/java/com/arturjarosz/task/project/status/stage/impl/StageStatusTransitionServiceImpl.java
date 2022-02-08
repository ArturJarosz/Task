package com.arturjarosz.task.project.status.stage.impl;

import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.project.model.Task;
import com.arturjarosz.task.project.query.ProjectQueryService;
import com.arturjarosz.task.project.status.stage.StageStatus;
import com.arturjarosz.task.project.status.stage.StageStatusTransitionService;
import com.arturjarosz.task.project.status.stage.StageWorkflowService;
import com.arturjarosz.task.project.status.task.TaskStatus;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@ApplicationService
public class StageStatusTransitionServiceImpl implements StageStatusTransitionService {
    private final ProjectQueryService projectQueryService;
    private final StageWorkflowService stageWorkflowService;

    @Autowired
    public StageStatusTransitionServiceImpl(ProjectQueryService projectQueryService,
                                            StageWorkflowService stageWorkflowService) {
        this.projectQueryService = projectQueryService;
        this.stageWorkflowService = stageWorkflowService;
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
            this.reopenToInProgress(project, stageId);
        }
    }

    private boolean stageHasOnlyTasksInToDoStatus(Long stageId) {
        Stage stage = this.projectQueryService.getStageById(stageId);
        List<Task> allTasks = new ArrayList<>(stage.getTasks());
        allTasks.removeIf(task -> task.getStatus().equals(TaskStatus.REJECTED));
        allTasks.removeIf(task -> task.getStatus().equals(TaskStatus.TO_DO));
        return allTasks.isEmpty();
    }
}
