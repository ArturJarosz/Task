package com.arturjarosz.task.stage.status;

import com.arturjarosz.task.project.model.Project;

public interface StageStatusTransitionService {

    void createStage(Project project, Long stageId);

    void startProgress(Project project, Long stageId);

    void rejectFromToDo(Project project, Long stageId);

    void completeWork(Project project, Long stageId);

    void rejectFromInProgress(Project project, Long stageId);

    void backToToDo(Project project, Long stageId);

    void reopenToToDo(Project project, Long stageId);

    void reopenToInProgress(Project project, Long stageId);

    void backToInProgress(Project project, Long stageId);

    void reject(Project project, Long stageId);

    void reopen(Project project, Long stageId);
}
