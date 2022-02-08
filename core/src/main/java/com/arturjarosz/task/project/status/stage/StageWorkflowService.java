package com.arturjarosz.task.project.status.stage;

import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.sharedkernel.status.WorkflowService;

public interface StageWorkflowService extends WorkflowService<StageStatus, Stage> {

    /**
     * Changes status for Stage with stageId on Project with projectId to newStatus of type StageStatus.
     * Run validation codes, that checks if Project status allows for status transition of Stage. Also, code for checking
     * if planned status transition from current StageStatus to target one is possible. If not, then new Exception
     * will be thrown.
     *
     * Method is also responsible for triggering actions that should be run before and after status transition is made.
     */
    void changeStageStatusOnProject(Project project, Long stageId, StageStatus newStatus);

    /**
     * Method contains logic that should be executed before status transition is executed, such as validators.
     */
    void beforeStatusChange(Project project, Stage stage, StageStatusTransition statusTransition);

    /**
     * Contains logic that should be executed after successful execution of status transition, like listeners
     * or loggers.
     */
    void afterStatusChange(Project project, StageStatusTransition statusTransition);
}
