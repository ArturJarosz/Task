package com.arturjarosz.task.project.domain.dto;

import com.arturjarosz.task.project.status.project.ProjectStatus;

public class ProjectStatusData {
    private ProjectStatus projectStatus;
    private String workflowName;

    public ProjectStatusData(ProjectStatus projectStatus, String workflowName) {
        this.projectStatus = projectStatus;
        this.workflowName = workflowName;
    }

    public ProjectStatus getProjectStatus() {
        return this.projectStatus;
    }

    public String getWorkflowName() {
        return this.workflowName;
    }
}
