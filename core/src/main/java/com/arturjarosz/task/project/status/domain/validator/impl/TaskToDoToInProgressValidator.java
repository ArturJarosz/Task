package com.arturjarosz.task.project.status.domain.validator.impl;

import com.arturjarosz.task.project.model.Task;
import com.arturjarosz.task.project.status.domain.TaskStatusTransition;
import com.arturjarosz.task.project.status.domain.validator.TaskStatusTransitionValidator;
import org.springframework.stereotype.Component;

@Component
public class TaskToDoToInProgressValidator implements TaskStatusTransitionValidator {
    private final TaskStatusTransition statusTransition = TaskStatusTransition.START_PROGRESS;

    @Override
    public void validate(Task object, TaskStatusTransition statusTransition) {

    }

    @Override
    public TaskStatusTransition getStatusTransition() {
        return this.statusTransition;
    }
}
