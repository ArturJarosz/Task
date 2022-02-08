package com.arturjarosz.task.project.status.project;

import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.sharedkernel.status.WorkflowService;

public interface ProjectWorkflowService extends WorkflowService<ProjectStatus, Project> {

    /**
     * Executing status transition for given Project to new status passed in ProjectStatus. Checks if planned status
     * transition from current ProjectStatus to target one is possible.
     *
     * Method is also responsible for triggering action that should be run before status transition and after status
     * transition is made.
     */
    void changeProjectStatus(Project project, ProjectStatus newStatus);

    /**
     * Method contains logic that should be executed before status transition is executed, such as validators.
     */
    void beforeStatusChange(Project project, ProjectStatusTransition statusTransition);

    /**
     * Contains logic that should be executed after successful execution of status transition, like listeners
     * or loggers.
     */
    void afterStatusChange(Project project, ProjectStatusTransition statusTransition);
}
