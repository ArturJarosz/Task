package com.arturjarosz.task.client.domain;

import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;

/**
 * Stores exception codes related to Client context.
 */
public class ClientExceptionCodes {

    public static final String CLIENT = "client";

    public static final String COMPANY_NAME = "companyName";
    public static final String FIRST_NAME = "firstName";
    public static final String LAST_NAME = "lastName";
    public static final String CLIENT_TYPE = "clientType";
    public static final String PROJECTS = "projects";

    protected ClientExceptionCodes() {
        throw new IllegalStateException(ExceptionCodes.NOT_FOR_INSTANTIATING);
    }
}
