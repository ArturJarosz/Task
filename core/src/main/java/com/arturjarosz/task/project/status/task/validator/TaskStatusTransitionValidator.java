package com.arturjarosz.task.project.status.task.validator;

import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Task;
import com.arturjarosz.task.project.status.task.TaskStatusTransition;
import com.arturjarosz.task.sharedkernel.status.StatusTransitionValidator;

public interface TaskStatusTransitionValidator extends StatusTransitionValidator<TaskStatusTransition> {

    void validate(Project project, Task task, Long stageId, TaskStatusTransition statusTransition);

}
