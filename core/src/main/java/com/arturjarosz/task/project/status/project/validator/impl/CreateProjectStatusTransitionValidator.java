package com.arturjarosz.task.project.status.project.validator.impl;

import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.status.project.ProjectStatusTransition;
import com.arturjarosz.task.project.status.project.validator.ProjectStatusTransitionValidator;
import org.springframework.stereotype.Component;

@Component
public class CreateProjectStatusTransitionValidator implements ProjectStatusTransitionValidator {
    private final ProjectStatusTransition transition = ProjectStatusTransition.CREATE_PROJECT;

    @Override
    public void validate(Project project, ProjectStatusTransition statusTransition) {

    }

    @Override
    public ProjectStatusTransition getStatusTransition() {
        return this.transition;
    }

}
