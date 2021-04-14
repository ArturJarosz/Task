package com.arturjarosz.task.project.status.domain;

import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.sharedkernel.status.WorkflowService;

public interface ProjectWorkflowService extends WorkflowService<ProjectStatus, ProjectStatusTransition, Project> {

    /**
     * Changes status of Project with projectId to newStatus of type ProjectStatus.
     *
     * @param project
     * @param newStatus
     */
    void changeProjectStatus(Project project, ProjectStatus newStatus);
}
