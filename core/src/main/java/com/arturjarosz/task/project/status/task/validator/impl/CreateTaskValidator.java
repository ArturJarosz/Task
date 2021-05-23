package com.arturjarosz.task.project.status.task.validator.impl;

import com.arturjarosz.task.project.application.ProjectExceptionCodes;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.project.model.Task;
import com.arturjarosz.task.project.status.project.ProjectStatus;
import com.arturjarosz.task.project.status.stage.StageStatus;
import com.arturjarosz.task.project.status.task.TaskStatusTransition;
import com.arturjarosz.task.project.status.task.validator.TaskStatusTransitionValidator;
import com.arturjarosz.task.sharedkernel.exceptions.BaseValidator;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;

@Component
public class CreateTaskValidator implements TaskStatusTransitionValidator {
    private final TaskStatusTransition transition = TaskStatusTransition.CREATE_TASK;

    @Autowired
    public CreateTaskValidator() {
        // needed by Hibernate
    }

    @Override
    public void validate(Project project, Task object, Long stageId, TaskStatusTransition statusTransition) {
        Stage stage = this.getStageFromProject(project, stageId);
        Predicate<Stage> stagePredicate = stageOnProject -> !stageOnProject.getStatus().equals(StageStatus.REJECTED);
        Predicate<Project> projectPredicate = projectToCheck ->
                !projectToCheck.getStatus().equals(ProjectStatus.REJECTED)
                        && !projectToCheck.getStatus().equals(ProjectStatus.DONE);
        assertIsTrue(stagePredicate.test(stage), BaseValidator.createMessageCode(ExceptionCodes.NOT_VALID,
                ProjectExceptionCodes.STAGE, ProjectExceptionCodes.STATUS, ProjectExceptionCodes.TASK,
                ProjectExceptionCodes.CREATE), stage.getStatus().getStatusName());
        assertIsTrue(projectPredicate.test(project), BaseValidator.createMessageCode(ExceptionCodes.NOT_VALID,
                ProjectExceptionCodes.PROJECT, ProjectExceptionCodes.STATUS, ProjectExceptionCodes.TASK,
                ProjectExceptionCodes.CREATE), project.getStatus());
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
