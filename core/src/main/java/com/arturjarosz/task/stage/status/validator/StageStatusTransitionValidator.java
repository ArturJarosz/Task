package com.arturjarosz.task.stage.status.validator;

import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.stage.model.Stage;
import com.arturjarosz.task.stage.status.StageStatusTransition;
import com.arturjarosz.task.sharedkernel.status.StatusTransitionValidator;

public interface StageStatusTransitionValidator extends StatusTransitionValidator<StageStatusTransition> {

    /**
     * Validate if planned statusTransition for Stage on given Project can be executed.
     *
     * @param project
     * @param stage
     * @param statusTransition
     */
    void validate(Project project, Stage stage, StageStatusTransition statusTransition);
}
