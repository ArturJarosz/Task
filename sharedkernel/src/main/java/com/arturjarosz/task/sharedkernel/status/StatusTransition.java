package com.arturjarosz.task.sharedkernel.status;

public interface StatusTransition<T extends Status> {

    T getCurrentStatus();

    T getNextStatus();

    String getName();
}
