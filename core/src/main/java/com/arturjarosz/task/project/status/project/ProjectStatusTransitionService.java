package com.arturjarosz.task.project.status.project;

import com.arturjarosz.task.project.model.Project;

/**
 * Interface for methods to change ProjectStatus.
 */
public interface ProjectStatusTransitionService {

    void create(Project project);

    void rejectOffer(Project project);

    void acceptOffer(Project project);

    void makeNewOffer(Project project);

    void reopenRejected(Project project);

    void resumeRejected(Project project);

    void rejectFromSigned(Project project);

    void startProgress(Project project);

    void backToToDo(Project project);

    void rejectFromProgress(Project project);

    void completeWork(Project project);

    void backToProgress(Project project);

    void projectPaid(Project project);

    void reopenCompleted(Project project);

    void reject(Project project);

    void reopen(Project project);

    void makeOffer(Project project);

}
