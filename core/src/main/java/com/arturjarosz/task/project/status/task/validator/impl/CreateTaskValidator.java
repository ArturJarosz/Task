package com.arturjarosz.task.project.status.task.validator.impl;

import com.arturjarosz.task.project.application.ProjectExceptionCodes;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.project.model.Task;
import com.arturjarosz.task.project.query.ProjectQueryService;
import com.arturjarosz.task.project.status.stage.StageStatus;
import com.arturjarosz.task.project.status.task.TaskStatusTransition;
import com.arturjarosz.task.project.status.task.validator.TaskStatusTransitionValidator;
import com.arturjarosz.task.sharedkernel.exceptions.BaseValidator;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;

@Component
public class CreateTaskValidator implements TaskStatusTransitionValidator {
    private final TaskStatusTransition transition = TaskStatusTransition.CREATE_TASK;
    private final ProjectQueryService projectQueryService;

    @Autowired
    public CreateTaskValidator(ProjectQueryService projectQueryService) {
        this.projectQueryService = projectQueryService;
    }

    @Override
    public void validate(Project project, Task object, Long stageId, TaskStatusTransition statusTransition) {
        Stage stage = this.getStageFromProject(project, stageId);
        Predicate<Stage> predicate = stageOnProject -> !stageOnProject.getStatus().equals(StageStatus.REJECTED);
        BaseValidator.assertIsTrue(predicate.test(stage), BaseValidator.createMessageCode(ExceptionCodes.NOT_VALID,
                ProjectExceptionCodes.STAGE, ProjectExceptionCodes.STATUS, ProjectExceptionCodes.TASK,
                ProjectExceptionCodes.CREATE), stage.getStatus().getStatusName());
    }

    @Override
    public TaskStatusTransition getStatusTransition() {
        return this.transition;
    }

    private Stage getStageFromProject(Project project, Long stageId) {
        return project.getStages().stream()
                .filter(stage -> stage.getId().equals(stageId))
                .findFirst().orElse(null);
    }

}
