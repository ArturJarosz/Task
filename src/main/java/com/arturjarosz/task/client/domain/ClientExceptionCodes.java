package com.arturjarosz.task.client.domain;

/**
 * Stores exception codes related to Client context.
 */
public class ClientExceptionCodes {

    public static final String CLIENT = "client";
    public static final String CLIENT_BASIC_DTO = "clientBasicDto";

    public static final String COMPANY_NAME = "companyName";
    public static final String FIRST_NAME = "firstName";
    public static final String LAST_NAME = "lastName";

    protected ClientExceptionCodes() {
        throw new IllegalStateException("This class should not be instantiated.");
    }
}
