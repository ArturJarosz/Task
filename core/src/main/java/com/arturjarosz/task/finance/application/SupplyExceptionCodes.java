package com.arturjarosz.task.finance.application;

import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;

public class SupplyExceptionCodes {

    public static final String NAME = "name";
    public static final String SUPPLIER = "supplier";
    public static final String SUPPLY = "supply";
    public static final String VALUE = "value";


    private SupplyExceptionCodes() {
        throw new IllegalStateException(ExceptionCodes.NOT_FOR_INSTANTIATING);
    }
}
