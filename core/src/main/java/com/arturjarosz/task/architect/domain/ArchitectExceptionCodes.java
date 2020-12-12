package com.arturjarosz.task.architect.domain;

public class ArchitectExceptionCodes {

    public static final String ARCHITECT = "architect";
    public static final String PERSON_NAME = "personName";
    public static final String FIRST_NAME = "firstName";
    public static final String LAST_NAME = "lastName";

    protected ArchitectExceptionCodes() {
        throw new IllegalStateException("This class should not be instantiated.");
    }
}
