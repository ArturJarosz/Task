package com.arturjarosz.task.architect.domain;

import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;

public final class ArchitectExceptionCodes {

    public static final String ARCHITECT = "architect";
    public static final String FIRST_NAME = "firstName";
    public static final String LAST_NAME = "lastName";
    public static final String PROJECTS = "projects";
    public static final String TIME_ENTRIES = "timeEntries";
    public static final String USER = "user";

    private ArchitectExceptionCodes() {
        throw new IllegalStateException(ExceptionCodes.NOT_FOR_INSTANTIATING);
    }
}
