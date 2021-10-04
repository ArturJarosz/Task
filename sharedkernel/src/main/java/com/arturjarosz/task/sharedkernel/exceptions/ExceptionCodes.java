package com.arturjarosz.task.sharedkernel.exceptions;

public class ExceptionCodes {

    public static final String NOT_FOR_INSTANTIATING = "This class should not be instantiated.";

    public static final String ALREADY_SET = "alreadySet";
    public static final String EMPTY = "isEmpty";
    public static final String NEGATIVE = "negative";
    public static final String NULL = "isNull";
    public static final String NOT_EXISTS = "notExists";
    public static final String NOT_NULL = "notNull";
    public static final String NOT_PRESENT = "notPresent";
    public static final String NOT_VALID = "notValid";

    public static final String AGGREGATE = "aggregate";

    private ExceptionCodes() {
        throw new IllegalStateException(NOT_FOR_INSTANTIATING);
    }
}
