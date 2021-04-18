package com.arturjarosz.task.project.status.domain.validator;

import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.project.status.domain.StageStatusTransition;
import com.arturjarosz.task.sharedkernel.status.StatusTransitionValidator;

public interface StageStatusTransitionValidator extends StatusTransitionValidator<StageStatusTransition> {

    void validate(Stage stage, StageStatusTransition statusTransition);
}
