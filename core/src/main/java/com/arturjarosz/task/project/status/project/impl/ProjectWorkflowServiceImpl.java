package com.arturjarosz.task.project.status.project.impl;

import com.arturjarosz.task.project.application.ProjectExceptionCodes;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.status.project.ProjectStatus;
import com.arturjarosz.task.project.status.project.ProjectStatusTransition;
import com.arturjarosz.task.project.status.project.ProjectWorkflowService;
import com.arturjarosz.task.project.status.project.validator.ProjectStatusTransitionValidator;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.exceptions.BaseValidator;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationService
public class ProjectWorkflowServiceImpl implements ProjectWorkflowService {

    private Map<String, List<ProjectStatusTransitionValidator>> mapNameToStatusTransitionValidators;

    @Autowired
    public ProjectWorkflowServiceImpl(List<ProjectStatusTransitionValidator> transitionValidatorList) {
        this.mapNameToStatusTransitionValidators = new HashMap<>();
        this.mapNameToStatusTransitionValidators = transitionValidatorList.stream()
                .collect(Collectors.groupingBy(
                        projectStatusTransitionValidator -> projectStatusTransitionValidator.getStatusTransition()
                                .getName()));
    }

    @Override
    public void changeProjectStatus(Project project, ProjectStatus newStatus) {
        ProjectStatus oldStatus = project.getStatus();
        ProjectStatusTransition statusTransition = this.getTransitionForStatuses(oldStatus, newStatus);
        BaseValidator.assertNotNull(statusTransition, BaseValidator.createMessageCode(ExceptionCodes.NOT_VALID,
                ProjectExceptionCodes.PROJECT, ProjectExceptionCodes.STATUS, ProjectExceptionCodes.TRANSITION),
                oldStatus != null ? oldStatus.getStatusName() : "null", newStatus.getStatusName());
        this.beforeStatusChange(project, statusTransition);
        this.changeStatus(project, newStatus);
        this.afterStatusChange(project, statusTransition);
    }

    @Override
    public void changeStatus(Project project, ProjectStatus projectStatus) {
        project.changeStatus(projectStatus);
    }

    @Override
    public void beforeStatusChange(Project project, ProjectStatusTransition statusTransition) {
        List<ProjectStatusTransitionValidator> validators = this.getStatusTransitionValidators(statusTransition);
        validators.forEach(validator -> validator.validate(project, statusTransition));
    }

    @Override
    public void afterStatusChange(Project project, ProjectStatusTransition statusTransition) {
        //TODO: run Project listeners
    }

    private ProjectStatusTransition getTransitionForStatuses(ProjectStatus oldStatus, ProjectStatus newStatus) {
        return Arrays.stream(ProjectStatusTransition.values())
                .filter(transition -> transition.getCurrentStatus() == oldStatus &&
                        transition.getNextStatus() == newStatus)
                .findFirst().orElse(null);
    }

    private List<ProjectStatusTransitionValidator> getStatusTransitionValidators(
            ProjectStatusTransition statusTransition) {
        List<ProjectStatusTransitionValidator> validators = this.mapNameToStatusTransitionValidators
                .get(statusTransition.getName());
        if (validators == null) {
            return Collections.emptyList();
        }
        return validators;
    }
}
