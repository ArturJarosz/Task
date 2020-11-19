package com.arturjarosz.task.sharedkernel.exceptions;

public class ExceptionCodes {

    public static final String EMPTY = "empty";
    public static final String IS_NULL = "isNull";
    public static final String NOT_NULL = "notNull";
    public static final String NOT_VALID = "notValid";

    private ExceptionCodes() {
        throw new IllegalStateException("This class should not be instantiated.");
    }
}