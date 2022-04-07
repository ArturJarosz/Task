package com.arturjarosz.task.project.status.project.validator;

import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.status.project.ProjectStatusTransition;
import com.arturjarosz.task.sharedkernel.status.StatusTransitionValidator;

public interface ProjectStatusTransitionValidator extends StatusTransitionValidator<ProjectStatusTransition> {

    /**
     * Validate if planned statusTransition for Project can be executed.
     */
    void validate(Project project, ProjectStatusTransition statusTransition);
}
