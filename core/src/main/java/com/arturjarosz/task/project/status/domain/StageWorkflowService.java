package com.arturjarosz.task.project.status.domain;

import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.sharedkernel.status.WorkflowService;

public interface StageWorkflowService extends WorkflowService<StageStatus, StageStatusTransition, Stage> {

    /**
     * Changes status for Stage with stageId on Project with projectId to newStatus of type StageStatus.
     *
     * @param project
     * @param stageId
     * @param newStatus
     */
    void changeStageStatusOnProject(Project project, Long stageId, StageStatus newStatus);

    void afterStatusChange(Project project, StageStatusTransition statusTransition);
}
