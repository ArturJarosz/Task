package com.arturjarosz.task.project.status.project.validator.impl;

import com.arturjarosz.task.project.application.ProjectExceptionCodes;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.status.project.ProjectStatus;
import com.arturjarosz.task.project.status.project.ProjectStatusTransition;
import com.arturjarosz.task.project.status.project.validator.ProjectStatusTransitionValidator;
import com.arturjarosz.task.sharedkernel.exceptions.BaseValidator;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import org.springframework.stereotype.Component;

@Component
public class OfferAcceptedProjectStatusTransitionValidator implements ProjectStatusTransitionValidator {
    private final ProjectStatusTransition transition = ProjectStatusTransition.OFFER_ACCEPTED;

    @Override
    public void validate(Project project, ProjectStatusTransition statusTransition) {
        BaseValidator.assertIsTrue(project.getStatus().equals(ProjectStatus.OFFER), BaseValidator.createMessageCode(
                ExceptionCodes.NOT_VALID,
                ProjectExceptionCodes.PROJECT, ProjectExceptionCodes.STATUS, ProjectExceptionCodes.SIGN),
                project.getStatus());
    }

    @Override
    public ProjectStatusTransition getStatusTransition() {
        return this.transition;
    }
}
