package com.arturjarosz.task.sharedkernel.status;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Defines workflow of statuses for Object.
 */
public class Workflow<T extends Status> {
    private final String name;
    private final Map<String, T> nameToStatus;
    private final T initialStatus;

    public Workflow(String name, T initialStatus, List<T> statusList) {
        this.name = name;
        this.initialStatus = initialStatus;
        this.nameToStatus = statusList.stream()
                .collect(Collectors.toMap(Status::getStatusName, Function.identity()));
    }

    public String getName() {
        return this.name;
    }

    public Map<String, T> getNameToStatus() {
        return this.nameToStatus;
    }

    public T getInitialStatus() {
        return this.initialStatus;
    }

    public boolean containsStatus(T status) {
        return this.nameToStatus.containsValue(status);
    }
}
