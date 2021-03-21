package com.arturjarosz.task.project.status.domain;

import com.arturjarosz.task.sharedkernel.status.StatusTransition;

public enum ProjectStatusTransition implements StatusTransition<ProjectStatus> {
    // from OFFER
    OFFER_REJECTED(ProjectStatus.OFFER, ProjectStatus.REJECTED),
    OFFER_ACCEPTED(ProjectStatus.OFFER, ProjectStatus.TO_DO),
    // from REJECTED
    NEW_OFFER(ProjectStatus.REJECTED, ProjectStatus.OFFER),
    // from TO_DO
    REJECTED_FROM_SIGNED(ProjectStatus.TO_DO, ProjectStatus.REJECTED),
    START_PROGRESS(ProjectStatus.TO_DO, ProjectStatus.IN_PROGRESS),
    // from IN_PROGRESS
    BACK_TO_TO_DO(ProjectStatus.IN_PROGRESS, ProjectStatus.TO_DO),
    REJECT_FROM_PROGRESS(ProjectStatus.IN_PROGRESS, ProjectStatus.REJECTED),
    COMPLETE_WORK(ProjectStatus.IN_PROGRESS, ProjectStatus.COMPLETED),
    // from COMPLETED
    BACK_TO_PROGRESS(ProjectStatus.COMPLETED, ProjectStatus.IN_PROGRESS),
    PROJECT_PAID(ProjectStatus.COMPLETED, ProjectStatus.DONE),
    // from DONE
    REOPEN_TO_PROGRESS(ProjectStatus.DONE, ProjectStatus.IN_PROGRESS);

    private final ProjectStatus currentStatus;
    private final ProjectStatus nextStatus;

    ProjectStatusTransition(ProjectStatus currentStatus,
                            ProjectStatus nextStatus) {
        this.currentStatus = currentStatus;
        this.nextStatus = nextStatus;
    }

    @Override
    public ProjectStatus getCurrentStatus() {
        return null;
    }

    @Override
    public ProjectStatus getNextStatus() {
        return null;
    }
}
