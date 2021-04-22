package com.arturjarosz.task.project.status.task.validator.impl;

import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Task;
import com.arturjarosz.task.project.status.task.TaskStatusTransition;
import com.arturjarosz.task.project.status.task.validator.TaskStatusTransitionValidator;
import org.springframework.stereotype.Component;

@Component
public class TaskToDoToInProgressValidator implements TaskStatusTransitionValidator {
    private final TaskStatusTransition statusTransition = TaskStatusTransition.START_PROGRESS;

    @Override
    public void validate(Project project, Task object, Long stageId, TaskStatusTransition statusTransition) {

    }

    @Override
    public TaskStatusTransition getStatusTransition() {
        return this.statusTransition;
    }
}
