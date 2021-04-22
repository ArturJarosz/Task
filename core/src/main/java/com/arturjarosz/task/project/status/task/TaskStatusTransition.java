package com.arturjarosz.task.project.status.task;

import com.arturjarosz.task.sharedkernel.status.StatusTransition;

public enum TaskStatusTransition implements StatusTransition<TaskStatus> {
    // creating Task has no transition of Status
    CREATE_TASK(TaskStatus.TO_DO, TaskStatus.TO_DO),
    // from TO_DO
    START_PROGRESS(TaskStatus.TO_DO, TaskStatus.IN_PROGRESS),
    REJECT_FROM_TO_DO(TaskStatus.TO_DO, TaskStatus.REJECTED),
    // from IN_PROGRESS
    COMPLETE_WORK(TaskStatus.IN_PROGRESS, TaskStatus.DONE),
    REJECT_FROM_IN_PROGRESS(TaskStatus.IN_PROGRESS, TaskStatus.REJECTED),
    BACK_TO_TO_DO(TaskStatus.IN_PROGRESS, TaskStatus.TO_DO),
    // from REJECTED
    REOPEN(TaskStatus.REJECTED, TaskStatus.TO_DO),
    // from COMPLETED
    BACK_TO_IN_PROGRESS(TaskStatus.DONE, TaskStatus.IN_PROGRESS);

    private final TaskStatus currentStatus;
    private final TaskStatus nextStatus;

    TaskStatusTransition(TaskStatus currentStatus, TaskStatus nextStatus) {
        this.currentStatus = currentStatus;
        this.nextStatus = nextStatus;
    }

    @Override
    public TaskStatus getCurrentStatus() {
        return this.currentStatus;
    }

    @Override
    public TaskStatus getNextStatus() {
        return this.nextStatus;
    }

    @Override
    public String getName() {
        return this.name();
    }
}
