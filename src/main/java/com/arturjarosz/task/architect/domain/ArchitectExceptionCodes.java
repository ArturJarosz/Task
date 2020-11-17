package com.arturjarosz.task.architect.domain;

public class ArchitectExceptionCodes {

    public static final String ARCHITECT = "architect";
    public static final String PERSON_NAME = "personName";

    protected ArchitectExceptionCodes() {
        throw new IllegalStateException("This class should not be instantiated.");
    }
}
