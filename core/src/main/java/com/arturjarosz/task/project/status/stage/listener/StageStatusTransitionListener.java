package com.arturjarosz.task.project.status.stage.listener;

import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.status.stage.StageStatusTransition;
import com.arturjarosz.task.sharedkernel.status.StatusChangeListener;

/**
 * Interface for listener  of status transition of Stage.
 */
public interface StageStatusTransitionListener extends StatusChangeListener<StageStatusTransition> {

    /**
     * When there has been a change in status of Stage, this method should be fired and checked whether some changes to
     * Project is required.
     *
     * @param project
     */
    void onStageStatusChange(Project project);
}
