package com.arturjarosz.task.cooperator.domain;

import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import com.arturjarosz.task.sharedkernel.exceptions.IllegalStateException;

public class CooperatorExceptionCodes {
    public static final String CONTRACTOR = "contractor";
    public static final String SUPPLIER = "supplier";

    public static final String NAME = "name";

    private CooperatorExceptionCodes() {
        throw new IllegalStateException(ExceptionCodes.NOT_FOR_INSTANTIATING);
    }
}
