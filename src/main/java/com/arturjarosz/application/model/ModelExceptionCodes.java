package com.arturjarosz.application.model;

public class ModelExceptionCodes {

    //Classes
    public static final String ADDRESS = "address";
    public static final String ARCHITECT = "architect";
    public static final String CLIENT = "client";
    public static final String EMAIL = "email";
    public static final String MONEY = "money";
    public static final String PERSON_NAME = "personName";

    //Fields
    public static final String FIRST_NAME = "firstName";
    public static final String LAST_NAME = "lastName";

    private ModelExceptionCodes() {
        throw new IllegalStateException("This class should not be instantiated.");
    }
}
