package com.arturjarosz.task.sharedkernel.status;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Defines workflow of statuses for Object.
 */
public class Workflow<TStatus extends Status> {
    private final String name;
    private final Map<String, TStatus> nameToStatus;
    private final TStatus initialStatus;

    public Workflow(String name, TStatus initialStatus, List<TStatus> statusList) {
        this.name = name;
        this.initialStatus = initialStatus;
        this.nameToStatus = statusList.stream()
                .collect(Collectors.toMap(Status::getStatusName, Function.identity()));
    }

    public String getName() {
        return this.name;
    }

    public Map<String, TStatus> getNameToStatus() {
        return this.nameToStatus;
    }

    public TStatus getInitialStatus() {
        return this.initialStatus;
    }

    public boolean containsStatus(TStatus status) {
        return this.nameToStatus.containsValue(status);
    }
}
