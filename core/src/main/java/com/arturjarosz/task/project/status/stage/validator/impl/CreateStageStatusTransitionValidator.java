package com.arturjarosz.task.project.status.stage.validator.impl;

import com.arturjarosz.task.project.application.ProjectExceptionCodes;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.project.status.project.ProjectStatus;
import com.arturjarosz.task.project.status.stage.StageStatusTransition;
import com.arturjarosz.task.project.status.stage.validator.StageStatusTransitionValidator;
import com.arturjarosz.task.sharedkernel.exceptions.BaseValidator;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;

@Component
public class CreateStageStatusTransitionValidator implements StageStatusTransitionValidator {
    private final StageStatusTransition transition = StageStatusTransition.CREATE_STAGE;
    private final Set<ProjectStatus> forbiddenStatusesSet = EnumSet.of(ProjectStatus.REJECTED,
            ProjectStatus.DONE);

    @Override
    public void validate(Project project, Stage stage, StageStatusTransition statusTransition) {
        this.projectNotInForbiddenStatus(project.getStatus());
    }

    @Override
    public StageStatusTransition getStatusTransition() {
        return this.transition;
    }

    private void projectNotInForbiddenStatus(ProjectStatus projectStatus) {
        BaseValidator.assertIsTrue(!this.forbiddenStatusesSet.contains(projectStatus), BaseValidator.createMessageCode(
                ExceptionCodes.NOT_VALID, ProjectExceptionCodes.PROJECT, ProjectExceptionCodes.STATUS,
                ProjectExceptionCodes.STAGE, ProjectExceptionCodes.CREATE), projectStatus);
    }

}
