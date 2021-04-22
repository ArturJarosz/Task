package com.arturjarosz.task.project.status.project;

import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.sharedkernel.status.WorkflowService;

public interface ProjectWorkflowService extends WorkflowService<ProjectStatus, Project> {

    /**
     * Changes status of Project with projectId to newStatus of type ProjectStatus.
     *
     * @param project
     * @param newStatus
     */
    void changeProjectStatus(Project project, ProjectStatus newStatus);

    void beforeStatusChange(Project project, ProjectStatusTransition statusTransition);
}
