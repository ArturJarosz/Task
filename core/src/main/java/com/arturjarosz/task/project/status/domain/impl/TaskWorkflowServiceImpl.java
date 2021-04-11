package com.arturjarosz.task.project.status.domain.impl;

import com.arturjarosz.task.project.application.ProjectExceptionCodes;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.project.model.Task;
import com.arturjarosz.task.project.status.domain.TaskStatus;
import com.arturjarosz.task.project.status.domain.TaskStatusTransition;
import com.arturjarosz.task.project.status.domain.TaskWorkflowService;
import com.arturjarosz.task.project.status.domain.validator.TaskStatusTransitionValidator;
import com.arturjarosz.task.sharedkernel.annotations.DomainService;
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

@DomainService
public class TaskWorkflowServiceImpl implements TaskWorkflowService {

    private Map<String, List<TaskStatusTransitionValidator>> mapNameToStatusTransitionValidators;

    @Autowired
    public TaskWorkflowServiceImpl(List<TaskStatusTransitionValidator> transitionList) {
        this.mapNameToStatusTransitionValidators = new HashMap<>();
        this.mapNameToStatusTransitionValidators = transitionList.stream()
                .collect(Collectors.groupingBy(
                        taskStatusTransitionValidator -> taskStatusTransitionValidator.getStatusTransition().name()));
    }

    @Override
    public void changeStatus(Task task, TaskStatus status) {
        TaskStatusTransition taskStatusTransition = this.getTransitionForStatuses(task.getStatus(), status);
        BaseValidator.assertNotNull(taskStatusTransition, BaseValidator.createMessageCode(ExceptionCodes.NOT_VALID,
                ProjectExceptionCodes.TASK, ProjectExceptionCodes.STATUS, ProjectExceptionCodes.TRANSITION,
                task.getStatus().getStatusName(), status.getStatusName()));
        this.beforeStatusChange(task, taskStatusTransition);
        task.changeStatus(status);
        this.afterStatusChange(task, taskStatusTransition);

    }

    @Override
    public void changeTaskStatusOnProject(Project project, Long stageId, Long taskId, TaskStatus newStatus) {
        Predicate<Stage> stagePredicate = stage -> stage.getId().equals(stageId);
        Predicate<Task> taskPredicate = task -> task.getId().equals(taskId);
        Task task = project.getStages().stream()
                .filter(stagePredicate)
                .flatMap(stage -> stage.getTasks().stream())
                .filter(taskPredicate)
                .findFirst().orElse(null);
        this.changeStatus(task, newStatus);
    }

    @Override
    public void beforeStatusChange(Task task, TaskStatusTransition statusTransition) {
        List<TaskStatusTransitionValidator> validators = this.getStatusTransitionValidators(statusTransition);
        validators.forEach(validator -> validator.validate(task, statusTransition));
    }

    @Override
    public void afterStatusChange(Task task, TaskStatusTransition statusTransition) {
        // TODO: run loggers
        // TODO: run listeners for status change on task
    }

    private List<TaskStatusTransitionValidator> getStatusTransitionValidators(TaskStatusTransition statusTransition) {
        List<TaskStatusTransitionValidator> validators = this.mapNameToStatusTransitionValidators
                .get(statusTransition.name());
        if (validators == null) {
            return Collections.emptyList();
        }
        return validators;
    }

    private TaskStatusTransition getTransitionForStatuses(TaskStatus oldStatus, TaskStatus newStatus) {
        return Arrays.stream(TaskStatusTransition.values())
                .filter(transition -> transition.getCurrentStatus().equals(oldStatus) && transition.getNextStatus()
                        .equals(newStatus)).findFirst().orElse(null);
    }
}
