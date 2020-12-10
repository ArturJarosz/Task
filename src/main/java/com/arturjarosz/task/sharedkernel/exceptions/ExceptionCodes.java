package com.arturjarosz.task.sharedkernel.exceptions;

public class ExceptionCodes {

    public static final String EMPTY = "isEmpty";
    public static final String IS_NULL = "isNull";
    public static final String NOT_EXISTS = "notExists";
    public static final String NOT_NULL = "notNull";
    public static final String NOT_VALID = "notValid";

    public static final String AGGREGATE = "aggregate";

    private ExceptionCodes() {
        throw new IllegalStateException("This class should not be instantiated.");
    }
}
