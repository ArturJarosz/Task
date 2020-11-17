package com.arturjarosz.task.client.domain;

public class ClientExceptionCodes {

    public static final String CLIENT = "client";

    public static final String COMPANY_NAME = "companyName";
    public static final String PERSON_NAME = "personName";

    protected ClientExceptionCodes() {
        throw new IllegalStateException("This class should not be instantiated.");
    }
}
