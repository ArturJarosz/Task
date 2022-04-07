package com.arturjarosz.task.project.domain.dto;

import com.arturjarosz.task.project.status.stage.StageStatus;

public class StageStatusData {
    private StageStatus stageStatus;
    private String workflowName;

    public StageStatusData(StageStatus projectStatus, String workflowName) {
        this.stageStatus = projectStatus;
        this.workflowName = workflowName;
    }

    public StageStatus getStageStatus() {
        return this.stageStatus;
    }

    public String getWorkflowName() {
        return this.workflowName;
    }
}
