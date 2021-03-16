package com.arturjarosz.task.sharedkernel.status;

public class StatusChange<TStatus extends Status> {
    private final Status<TStatus> currentStatus;
    private final Status<TStatus> nextStatus;

    public StatusChange(Status<TStatus> currentStatus, Status<TStatus> nextStatus) {
        this.currentStatus = currentStatus;
        this.nextStatus = nextStatus;
    }

    public Status<TStatus> getCurrentStatus() {
        return this.currentStatus;
    }

    public Status<TStatus> getNextStatus() {
        return this.nextStatus;
    }
}
