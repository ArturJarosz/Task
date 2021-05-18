package com.arturjarosz.task.project.status.stage;

import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.sharedkernel.status.WorkflowService;

public interface StageWorkflowService extends WorkflowService<StageStatus, Stage> {

    /**
     * Changes status for Stage with stageId on Project with projectId to newStatus of type StageStatus.
     *
     * @param project
     * @param stageId
     * @param statusTransition
     */
    void changeStageStatusOnProject(Project project, Long stageId, StageStatusTransition statusTransition);

    void beforeStatusChange(Project project, Stage stage, StageStatusTransition statusTransition);

    void afterStatusChange(Project project, StageStatusTransition statusTransition);
}
