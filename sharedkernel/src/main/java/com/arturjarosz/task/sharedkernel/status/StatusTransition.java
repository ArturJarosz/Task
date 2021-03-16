package com.arturjarosz.task.sharedkernel.status;

public interface StatusTransition<TStatus extends Status> {

    TStatus getCurrentStatus();

    TStatus getNextStatus();
}
