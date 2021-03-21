package com.arturjarosz.task.project.status.domain.validator;

import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.status.domain.ProjectStatusTransition;
import com.arturjarosz.task.sharedkernel.status.StatusTransitionValidator;

public interface ProjectStatusTransitionValidator extends StatusTransitionValidator<ProjectStatusTransition, Project> {
}
