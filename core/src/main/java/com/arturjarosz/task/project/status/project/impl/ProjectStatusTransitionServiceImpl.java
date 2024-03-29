package com.arturjarosz.task.project.status.project.impl;

import com.arturjarosz.task.project.application.ProjectExceptionCodes;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.project.status.project.ProjectStatus;
import com.arturjarosz.task.project.status.project.ProjectStatusTransitionService;
import com.arturjarosz.task.project.status.project.ProjectWorkflowService;
import com.arturjarosz.task.project.status.stage.StageStatus;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.exceptions.BaseValidator;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

@ApplicationService
public class ProjectStatusTransitionServiceImpl implements ProjectStatusTransitionService {
    private final ProjectWorkflowService projectWorkflowService;

    @Autowired
    public ProjectStatusTransitionServiceImpl(ProjectWorkflowService projectWorkflowService) {
        this.projectWorkflowService = projectWorkflowService;
    }

    @Override
    public void create(Project project) {
        this.projectWorkflowService.changeProjectStatus(project, ProjectStatus.TO_DO);
    }

    @Override
    public void rejectNotStarted(Project project) {
        this.projectWorkflowService.changeProjectStatus(project, ProjectStatus.REJECTED);
    }

    @Override
    public void startProgress(Project project) {
        this.projectWorkflowService.changeProjectStatus(project, ProjectStatus.IN_PROGRESS);
    }

    @Override
    public void reopenRejected(Project project) {
        this.projectWorkflowService.changeProjectStatus(project, ProjectStatus.TO_DO);
    }

    @Override
    public void resumeRejected(Project project) {
        this.projectWorkflowService.changeProjectStatus(project, ProjectStatus.IN_PROGRESS);
    }

    @Override
    public void backToToDo(Project project) {
        this.projectWorkflowService.changeProjectStatus(project, ProjectStatus.TO_DO);
    }

    @Override
    public void rejectFromProgress(Project project) {
        this.projectWorkflowService.changeProjectStatus(project, ProjectStatus.REJECTED);
    }

    @Override
    public void finishWork(Project project) {
        this.projectWorkflowService.changeProjectStatus(project, ProjectStatus.DONE);
    }

    @Override
    public void backToProgress(Project project) {
        this.projectWorkflowService.changeProjectStatus(project, ProjectStatus.IN_PROGRESS);
    }

    @Override
    public void complete(Project project) {
        this.projectWorkflowService.changeProjectStatus(project, ProjectStatus.COMPLETED);
    }

    @Override
    public void reject(Project project) {
        ProjectStatus status = project.getStatus();
        switch (status) {
            case TO_DO -> this.rejectNotStarted(project);
            case IN_PROGRESS -> this.rejectFromProgress(project);
            default -> throw new IllegalArgumentException(createMessageCode(ExceptionCodes.NOT_VALID,
                    ProjectExceptionCodes.PROJECT, ProjectExceptionCodes.STATUS, ProjectExceptionCodes.TRANSITION),
                    status, ProjectStatus.REJECTED);
        }
    }

    @Override
    public void reopen(Project project) {
        this.assertProjectInRejected(project);
        if (this.hasStagesOnlyInRejectedAndToDoStatusOrHasNoStages(project)) {
            this.reopenRejected(project);
        } else {
            this.resumeRejected(project);
        }
    }

    private void assertProjectInRejected(Project project) {
        BaseValidator.assertIsTrue(project.getStatus().equals(ProjectStatus.REJECTED),
                createMessageCode(ExceptionCodes.NOT_VALID,
                        ProjectExceptionCodes.PROJECT, ProjectExceptionCodes.STATUS,
                        ProjectExceptionCodes.REOPEN), project.getStatus());
    }

    private boolean hasStagesOnlyInRejectedAndToDoStatusOrHasNoStages(Project project) {
        List<Stage> allStages = new ArrayList<>(project.getStages());
        //we are removing Stages in Rejected status, because they should not be taken into account
        allStages.removeIf(stage -> stage.getStatus().equals(StageStatus.REJECTED));
        allStages.removeIf(stage -> stage.getStatus().equals(StageStatus.TO_DO));
        return allStages.isEmpty();
    }
}
