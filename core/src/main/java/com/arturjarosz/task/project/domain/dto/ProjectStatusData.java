package com.arturjarosz.task.project.domain.dto;

import com.arturjarosz.task.project.status.project.ProjectStatus;

public record ProjectStatusData(ProjectStatus projectStatus, String workflowName) {
}
