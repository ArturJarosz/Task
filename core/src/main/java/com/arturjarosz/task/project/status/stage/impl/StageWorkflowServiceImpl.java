package com.arturjarosz.task.project.status.stage.impl;

import com.arturjarosz.task.project.application.ProjectExceptionCodes;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.project.status.project.ProjectStatus;
import com.arturjarosz.task.project.status.stage.StageStatus;
import com.arturjarosz.task.project.status.stage.StageStatusTransition;
import com.arturjarosz.task.project.status.stage.StageWorkflowService;
import com.arturjarosz.task.project.status.stage.listener.StageStatusTransitionListener;
import com.arturjarosz.task.project.status.stage.validator.StageStatusTransitionValidator;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.exceptions.BaseValidator;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;

@ApplicationService
public class StageWorkflowServiceImpl implements StageWorkflowService {

    private Map<String, List<StageStatusTransitionValidator>> mapNameToStatusTransitionValidators;
    private Map<String, List<StageStatusTransitionListener>> mapNameToStatusTransitionListeners;

    @Autowired
    public StageWorkflowServiceImpl(List<StageStatusTransitionListener> stageStatusTransitionListenerList,
                                    List<StageStatusTransitionValidator> transitionValidatorList) {
        this.mapNameToStatusTransitionListeners = new HashMap<>();
        this.mapNameToStatusTransitionListeners = stageStatusTransitionListenerList.stream()
                .collect(Collectors.groupingBy(
                        stageStatusTransitionListener -> stageStatusTransitionListener.getStatusTransition().getName()
                ));
        this.mapNameToStatusTransitionValidators = new HashMap<>();
        this.mapNameToStatusTransitionValidators = transitionValidatorList.stream()
                .collect(Collectors.groupingBy(
                        stageStatusTransitionValidator -> stageStatusTransitionValidator.getStatusTransition().getName()
                ));
    }

    @Override
    public void changeStatus(Stage stage, StageStatus status) {
        stage.changeStatus(status);
    }

    @Override
    public void changeStageStatusOnProject(Project project, Long stageId, StageStatus newStatus) {
        this.assertProjectNotInRejected(project);
        Stage stage = this.getStage(project, stageId);
        StageStatus oldStatus = stage.getStatus();
        StageStatusTransition statusTransition = this.getTransitionForStatuses(oldStatus, newStatus);
        BaseValidator.assertNotNull(statusTransition, BaseValidator.createMessageCode(ExceptionCodes.NOT_VALID,
                ProjectExceptionCodes.STAGE, ProjectExceptionCodes.STATUS, ProjectExceptionCodes.TRANSITION,
                oldStatus != null ? oldStatus.getStatusName() : "null", newStatus.getStatusName()));
        this.beforeStatusChange(project, stage, statusTransition);
        this.changeStatus(stage, newStatus);
        this.afterStatusChange(project, statusTransition);
    }

    @Override
    public void beforeStatusChange(Project project, Stage stage, StageStatusTransition statusTransition) {
        List<StageStatusTransitionValidator> validators = this.getStatusTransitionValidators(statusTransition);
        validators.forEach(validator -> validator.validate(project, stage, statusTransition));
    }

    @Override
    public void afterStatusChange(Project project, StageStatusTransition statusTransition) {
        List<StageStatusTransitionListener> listeners = this.getStatusTransitionListeners(statusTransition);
        listeners.forEach(listener -> listener.onStageStatusChange(project));
    }

    private StageStatusTransition getTransitionForStatuses(StageStatus oldStatus, StageStatus newStatus) {
        return Arrays.stream(StageStatusTransition.values())
                .filter(transition -> transition.getCurrentStatus() == oldStatus
                        && transition.getNextStatus() == newStatus).findFirst().orElse(null);
    }

    private List<StageStatusTransitionListener> getStatusTransitionListeners(StageStatusTransition statusTransition) {
        List<StageStatusTransitionListener> listeners = this.mapNameToStatusTransitionListeners
                .get(statusTransition.getName());
        if (listeners == null) {
            return Collections.emptyList();
        }
        return listeners;
    }

    private List<StageStatusTransitionValidator> getStatusTransitionValidators(StageStatusTransition statusTransition) {
        List<StageStatusTransitionValidator> validators = this.mapNameToStatusTransitionValidators
                .get(statusTransition.getName());
        if (validators == null) {
            return Collections.emptyList();
        }
        return validators;
    }

    private Stage getStage(Project project, Long stageId) {
        Predicate<Stage> stagePredicate = stageId != null ? stage -> stage.getId().equals(stageId) : stage -> stage
                .getId() == null;
        return project.getStages().stream()
                .filter(stagePredicate)
                .findFirst().orElse(null);
    }

    private void assertProjectNotInRejected(Project project) {
        Predicate<Project> projectPredicate = projectToCheck ->
                !projectToCheck.getStatus().equals(ProjectStatus.REJECTED)
                        && !projectToCheck.getStatus().equals(ProjectStatus.DONE);
        assertIsTrue(projectPredicate.test(project), BaseValidator.createMessageCode(ExceptionCodes.NOT_VALID,
                ProjectExceptionCodes.PROJECT, ProjectExceptionCodes.STATUS, ProjectExceptionCodes.TASK,
                ProjectExceptionCodes.CREATE), project.getStatus());
    }
}
