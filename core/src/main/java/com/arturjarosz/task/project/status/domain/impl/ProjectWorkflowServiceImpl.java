package com.arturjarosz.task.project.status.domain.impl;

import com.arturjarosz.task.project.application.ProjectExceptionCodes;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.status.domain.ProjectStatus;
import com.arturjarosz.task.project.status.domain.ProjectStatusTransition;
import com.arturjarosz.task.project.status.domain.ProjectWorkflowService;
import com.arturjarosz.task.project.status.domain.validator.ProjectStatusTransitionValidator;
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
        this.changeStatus(project, newStatus);
    }

    @Override
    public void changeStatus(Project project, ProjectStatus newStatus) {
        ProjectStatusTransition projectStatusTransition = this.getTransitionForStatuses(project.getStatus(), newStatus);
        BaseValidator.assertNotNull(projectStatusTransition, BaseValidator.createMessageCode(ExceptionCodes.NOT_VALID,
                ProjectExceptionCodes.PROJECT, ProjectExceptionCodes.STATUS, ProjectExceptionCodes.TRANSITION,
                project.getStatus().getStatusName(), newStatus.getStatusName()));
        this.beforeStatusChange(project, projectStatusTransition);
        project.changeStatus(newStatus);
        //this.afterStatusChange(project, projectStatusTransition);
    }

    @Override
    public void beforeStatusChange(Project project, ProjectStatusTransition statusTransition) {
        List<ProjectStatusTransitionValidator> validators = this.getStatusTransitionValidators(statusTransition);
        validators.forEach(validator -> validator.validate(project, statusTransition));
    }

/*    @Override
    public void afterStatusChange(Project project, ProjectStatusTransition statusTransition) {
        //TODO: run loggers
        //TODO: run listeners for status change on project
    }*/

    private ProjectStatusTransition getTransitionForStatuses(ProjectStatus status, ProjectStatus newStatus) {
        return Arrays.stream(ProjectStatusTransition.values())
                .filter(transition -> transition.getCurrentStatus().equals(status) && transition.getNextStatus()
                        .equals(newStatus)).findFirst().orElse(null);
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
