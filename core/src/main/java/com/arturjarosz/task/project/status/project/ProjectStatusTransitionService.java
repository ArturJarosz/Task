package com.arturjarosz.task.project.status.project;

import com.arturjarosz.task.project.model.Project;

/**
 * Interface for methods to change ProjectStatus.
 */
public interface ProjectStatusTransitionService {

    void create(Project project);

    void rejectNotStarted(Project project);

    void startProgress(Project project);

    void reopenRejected(Project project);

    void resumeRejected(Project project);

    void backToToDo(Project project);

    void rejectFromProgress(Project project);

    void finishWork(Project project);

    void backToProgress(Project project);

    void complete(Project project);

    void reject(Project project);

    void reopen(Project project);

}
