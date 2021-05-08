package com.arturjarosz.task.project.status.stage.impl;

import com.arturjarosz.task.project.application.ProjectExceptionCodes;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Stage;
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
        Stage stage = this.getStage(project, stageId);
        /*
        In case of newly created Stage, there is no status transition. For avoiding nullPointerException
        old status is set to TO_DO as well, as there is no status before.
         */
        StageStatus oldStatus = stage.getStatus() != null ? stage.getStatus() : StageStatus.TO_DO;
        StageStatusTransition stageStatusTransition = this.getTransitionForStatuses(oldStatus, newStatus);
        BaseValidator.assertNotNull(stageStatusTransition, BaseValidator.createMessageCode(ExceptionCodes.NOT_VALID,
                ProjectExceptionCodes.STAGE, ProjectExceptionCodes.STATUS, ProjectExceptionCodes.TRANSITION,
                oldStatus.getStatusName(), newStatus.getStatusName()));
        this.beforeStatusChange(project, stage, stageStatusTransition);
        this.changeStatus(stage, newStatus);
        this.afterStatusChange(project, stageStatusTransition);
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
                .filter(transition -> transition.getCurrentStatus().equals(oldStatus) && transition.getNextStatus()
                        .equals(newStatus)).findFirst().orElse(null);
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
}
