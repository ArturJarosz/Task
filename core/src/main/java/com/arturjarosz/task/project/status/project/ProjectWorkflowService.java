package com.arturjarosz.task.project.status.project;

import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.sharedkernel.status.WorkflowService;

public interface ProjectWorkflowService extends WorkflowService<ProjectStatus, Project> {

    /**
     * Executing status transition of Project with projectId.
     *
     * @param project
     * @param statusTransition
     */
    void changeProjectStatus(Project project, ProjectStatusTransition statusTransition);

    void beforeStatusChange(Project project, ProjectStatusTransition statusTransition);

    void afterStatusChange(Project project, ProjectStatusTransition statusTransition);
}
