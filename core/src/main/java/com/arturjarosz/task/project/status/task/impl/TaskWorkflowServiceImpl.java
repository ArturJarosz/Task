package com.arturjarosz.task.project.status.task.impl;

import com.arturjarosz.task.project.application.ProjectExceptionCodes;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.project.model.Task;
import com.arturjarosz.task.project.status.project.validator.ProjectWorkflowValidator;
import com.arturjarosz.task.project.status.stage.validator.StageWorkflowValidator;
import com.arturjarosz.task.project.status.task.TaskStatus;
import com.arturjarosz.task.project.status.task.TaskStatusTransition;
import com.arturjarosz.task.project.status.task.TaskWorkflowService;
import com.arturjarosz.task.project.status.task.listener.TaskStatusTransitionListener;
import com.arturjarosz.task.project.status.task.validator.TaskStatusTransitionValidator;
import com.arturjarosz.task.sharedkernel.annotations.DomainService;
import com.arturjarosz.task.sharedkernel.exceptions.BaseValidator;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@DomainService
public class TaskWorkflowServiceImpl implements TaskWorkflowService {
    private final ProjectWorkflowValidator projectWorkflowValidator;
    private final StageWorkflowValidator stageWorkflowValidator;

    private Map<String, List<TaskStatusTransitionValidator>> mapNameToStatusTransitionValidators;
    private Map<String, List<TaskStatusTransitionListener>> mapNameToStatusTransitionListeners;

    @Autowired
    public TaskWorkflowServiceImpl(ProjectWorkflowValidator projectWorkflowValidator,
            StageWorkflowValidator stageWorkflowValidator,
            List<TaskStatusTransitionListener> taskStatusTransitionListenerList,
            List<TaskStatusTransitionValidator> transitionValidatorList) {
        this.projectWorkflowValidator = projectWorkflowValidator;
        this.stageWorkflowValidator = stageWorkflowValidator;
        this.mapNameToStatusTransitionValidators = new HashMap<>();
        this.mapNameToStatusTransitionValidators = transitionValidatorList.stream()
                .collect(Collectors.groupingBy(
                        taskStatusTransitionValidator -> taskStatusTransitionValidator.getStatusTransition()
                                .getName()));
        this.mapNameToStatusTransitionListeners = new HashMap<>();
        this.mapNameToStatusTransitionListeners = taskStatusTransitionListenerList.stream()
                .collect(Collectors.groupingBy(
                        taskStatusTransitionListener -> taskStatusTransitionListener.getStatusTransition().getName()));
    }

    @Override
    public void changeStatus(Task task, TaskStatus newStatus) {
        task.changeStatus(newStatus);
    }

    @Override
    public void changeTaskStatusOnProject(Project project, Long stageId, Long taskId, TaskStatus newStatus) {
        this.projectWorkflowValidator.validateProjectAllowsForWorking(project);
        this.stageWorkflowValidator.stageStatusAllowsForWorking(Objects.requireNonNull(
                project.getStages().stream().filter(stage -> stage.getId().equals(stageId)).findFirst().orElse(null)));
        Task task = this.getTask(project, stageId, taskId);
        TaskStatus oldStatus = task.getStatus();
        TaskStatusTransition taskStatusTransition = this.getTransitionForStatuses(oldStatus, newStatus);
        BaseValidator.assertNotNull(taskStatusTransition,
                BaseValidator.createMessageCode(ExceptionCodes.NOT_VALID, ProjectExceptionCodes.TASK,
                        ProjectExceptionCodes.STATUS, ProjectExceptionCodes.TRANSITION),
                oldStatus != null ? oldStatus.getStatusName() : "null", newStatus.getStatusName());
        this.beforeStatusChange(project, task, stageId, taskStatusTransition);
        this.changeStatus(task, newStatus);
        this.afterStatusChange(project, stageId, taskStatusTransition);
    }

    @Override
    public void beforeStatusChange(Project project, Task task, Long stageId, TaskStatusTransition statusTransition) {
        List<TaskStatusTransitionValidator> validators = this.getStatusTransitionValidators(statusTransition);
        validators.forEach(validator -> validator.validate(project, task, stageId, statusTransition));
    }

    @Override
    public void afterStatusChange(Project project, Long stageId, TaskStatusTransition statusTransition) {
        List<TaskStatusTransitionListener> listenerList = this.getStatusTransitionListeners(statusTransition);
        listenerList.forEach(listener -> listener.onTaskStatusChange(project, stageId));
        // TODO: run loggers
    }

    private List<TaskStatusTransitionListener> getStatusTransitionListeners(TaskStatusTransition statusTransition) {
        List<TaskStatusTransitionListener> listeners = this.mapNameToStatusTransitionListeners.get(
                statusTransition.getName());
        if (listeners == null) {
            return Collections.emptyList();
        }
        return listeners;
    }

    private List<TaskStatusTransitionValidator> getStatusTransitionValidators(TaskStatusTransition statusTransition) {
        List<TaskStatusTransitionValidator> validators = this.mapNameToStatusTransitionValidators.get(
                statusTransition.getName());
        if (validators == null) {
            return Collections.emptyList();
        }
        return validators;
    }

    private TaskStatusTransition getTransitionForStatuses(TaskStatus oldStatus, TaskStatus newStatus) {
        return Arrays.stream(TaskStatusTransition.values())
                .filter(transition -> transition.getCurrentStatus() == oldStatus && transition.getNextStatus() == newStatus)
                .findFirst()
                .orElse(null);
    }

    private Task getTask(Project project, Long stageId, Long taskId) {
        Predicate<Stage> stagePredicate = stage -> stage.getId().equals(stageId);
        //Newly created Task do not have assigned id yet.
        Predicate<Task> taskPredicate = taskId != null ? task -> task.getId()
                .equals(taskId) : task -> task.getId() == null;
        return project.getStages()
                .stream()
                .filter(stagePredicate)
                .flatMap(stage -> stage.getTasks().stream())
                .filter(taskPredicate)
                .findFirst()
                .orElseThrow(ResourceNotFoundException::new);
    }
}
