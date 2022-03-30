package com.arturjarosz.task.contract.domain;

import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import com.arturjarosz.task.sharedkernel.exceptions.IllegalStateException;

public class ContractExceptionCodes {

    public static final String CONTRACT = "contract";
    public static final String DEADLINE = "deadline";
    public static final String END_DATE = "endDate";
    public static final String OFFER = "offer";
    public static final String PROJECT = "project";
    public static final String START_DATE = "startDate";
    public static final String SIGNING_DATE = "signingDate";
    public static final String STATUS = "status";
    public static final String VALUE = "value";

    public static final String CREATE = "create";
    public static final String CHANGE = "change";


    private ContractExceptionCodes() {
        throw new IllegalStateException(ExceptionCodes.NOT_FOR_INSTANTIATING);
    }
}
