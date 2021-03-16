package com.arturjarosz.task.status.domain.validator;

import com.arturjarosz.task.project.model.Task;
import com.arturjarosz.task.sharedkernel.status.StatusTransitionValidator;
import com.arturjarosz.task.status.domain.TaskStatusTransition;

public interface TaskStatusTransitionValidator extends StatusTransitionValidator<TaskStatusTransition, Task> {

}
