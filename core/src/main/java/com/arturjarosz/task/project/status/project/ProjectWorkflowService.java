package com.arturjarosz.task.project.status.project;

import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.sharedkernel.status.WorkflowService;

public interface ProjectWorkflowService extends WorkflowService<ProjectStatus, Project> {

    /**
     * Executing status transition for given Project to new status passed in ProjectStatus.
     *
     * @param project
     * @param newStatus
     */
    void changeProjectStatus(Project project, ProjectStatus newStatus);

    /**
     * Method contains logic that should be executed before status transition is executed, such as validators.
     *
     * @param project
     * @param statusTransition
     */
    void beforeStatusChange(Project project, ProjectStatusTransition statusTransition);

    /**
     * Contains logic that should be executed after successful execution of status transition, like listeners
     * or loggers.
     *
     * @param project
     * @param statusTransition
     */
    void afterStatusChange(Project project, ProjectStatusTransition statusTransition);
}
