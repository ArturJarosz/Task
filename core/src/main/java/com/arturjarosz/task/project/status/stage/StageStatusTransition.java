package com.arturjarosz.task.project.status.stage;

import com.arturjarosz.task.sharedkernel.status.StatusTransition;

public enum StageStatusTransition implements StatusTransition<StageStatus> {
    CREATE_STAGE(null, StageStatus.TO_DO),
    // from TO_DO
    START_PROGRESS(StageStatus.TO_DO, StageStatus.IN_PROGRESS),
    REJECT_FROM_TO_DO(StageStatus.TO_DO, StageStatus.REJECTED),
    // from IN_PROGRESS
    COMPLETE_WORK(StageStatus.IN_PROGRESS, StageStatus.DONE),
    REJECT_FROM_IN_PROGRESS(StageStatus.IN_PROGRESS, StageStatus.REJECTED),
    BACK_TO_TO_DO(StageStatus.IN_PROGRESS, StageStatus.TO_DO),
    // from REJECTED
    REOPEN(StageStatus.REJECTED, StageStatus.TO_DO),
    REOPEN_TO_PROGRESS(StageStatus.REJECTED, StageStatus.IN_PROGRESS),
    // from DONE
    BACK_TO_IN_PROGRESS(StageStatus.DONE, StageStatus.IN_PROGRESS);

    private final StageStatus currentStatus;
    private final StageStatus nextStatus;

    StageStatusTransition(StageStatus currentStatus, StageStatus nextStatus) {
        this.currentStatus = currentStatus;
        this.nextStatus = nextStatus;
    }

    @Override
    public StageStatus getCurrentStatus() {
        return this.currentStatus;
    }

    @Override
    public StageStatus getNextStatus() {
        return this.nextStatus;
    }

    @Override
    public String getName() {
        return this.name();
    }

}
