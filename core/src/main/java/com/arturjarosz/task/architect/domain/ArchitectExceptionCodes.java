package com.arturjarosz.task.architect.domain;

import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;

public class ArchitectExceptionCodes {

    public static final String ARCHITECT = "architect";
    public static final String PERSON_NAME = "personName";
    public static final String FIRST_NAME = "firstName";
    public static final String LAST_NAME = "lastName";

    private ArchitectExceptionCodes() {
        throw new IllegalStateException(ExceptionCodes.NOT_FOR_INSTANTIATING);
    }
}
