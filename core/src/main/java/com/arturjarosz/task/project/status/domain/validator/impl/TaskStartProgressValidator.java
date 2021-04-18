package com.arturjarosz.task.project.status.domain.validator.impl;

import com.arturjarosz.task.project.application.ProjectExceptionCodes;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.project.model.Task;
import com.arturjarosz.task.project.status.domain.StageStatus;
import com.arturjarosz.task.project.status.domain.TaskStatusTransition;
import com.arturjarosz.task.project.status.domain.validator.TaskStatusTransitionValidator;
import com.arturjarosz.task.sharedkernel.exceptions.BaseValidator;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import org.springframework.stereotype.Component;

@Component
public class TaskStartProgressValidator implements TaskStatusTransitionValidator {
    private final TaskStatusTransition transition = TaskStatusTransition.START_PROGRESS;

    @Override
    public void validate(Project project, Task task, Long stageId, TaskStatusTransition statusTransition) {
        Stage stage = this.getStageFromProject(project, stageId);
        BaseValidator.assertIsTrue(this.stageNotRejectedOrDone(stage),
                BaseValidator.createMessageCode(ExceptionCodes.NOT_VALID,
                        ProjectExceptionCodes.STAGE, ProjectExceptionCodes.STATUS, ProjectExceptionCodes.TASK,
                        ProjectExceptionCodes.START_PROGRESS), stage.getStatus().getStatusName());
    }

    @Override
    public TaskStatusTransition getStatusTransition() {
        return transition;
    }

    private Stage getStageFromProject(Project project, Long stageId) {
        return project.getStages().stream()
                .filter(stage -> stage.getId().equals(stageId))
                .findFirst().orElse(null);
    }

    private boolean stageNotRejectedOrDone(Stage stage) {
        return (!stage.getStatus().equals(StageStatus.REJECTED)) && (stage.getStatus().equals(StageStatus.DONE));
    }
}
