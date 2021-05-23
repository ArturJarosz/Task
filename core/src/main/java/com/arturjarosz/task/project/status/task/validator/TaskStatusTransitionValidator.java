package com.arturjarosz.task.project.status.task.validator;

import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Task;
import com.arturjarosz.task.project.status.task.TaskStatusTransition;
import com.arturjarosz.task.sharedkernel.status.StatusTransitionValidator;

public interface TaskStatusTransitionValidator extends StatusTransitionValidator<TaskStatusTransition> {

    /**
     * Validate if planner statusTransition for Task for Stage with stageId on given Project can be executed.
     *
     * @param project
     * @param task
     * @param stageId
     * @param statusTransition
     */
    void validate(Project project, Task task, Long stageId, TaskStatusTransition statusTransition);

}
