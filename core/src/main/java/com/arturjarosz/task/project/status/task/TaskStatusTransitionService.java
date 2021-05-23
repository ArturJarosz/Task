package com.arturjarosz.task.project.status.task;

import com.arturjarosz.task.project.model.Project;

public interface TaskStatusTransitionService {

    void createTask(Project project, Long stageId, Long taskId);

    void startProgress(Project project, Long stageId, Long taskId);

    void rejectFromToDo(Project project, Long stageId, Long taskId);

    void completeWork(Project project, Long stageId, Long taskId);

    void rejectFromInProgress(Project project, Long stageId, Long taskId);

    void backToToDo(Project project, Long stageId, Long taskId);

    void reopenTask(Project project, Long stageId, Long taskId);

    void backToInProgress(Project project, Long stageId, Long taskId);

    void reopen(Project project, Long stageId, Long taskId);

    void reject(Project project, Long stageId, Long taskId);

    void changeTaskStatus(Project project, Long stageId, Long taskId, TaskStatus newStatus);

}
