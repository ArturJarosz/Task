package com.arturjarosz.task.systemparameter.domain;

import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;

public class SystemParameterExceptionCodes {
    public static final String DECIMAL_NUMBER = "decimalNumber";
    public static final String NAME = "name";
    public static final String SYSTEM_PARAMETER = "systemParameter";
    public static final String VALUE = "value";
    public static final String VALIDATOR = "validator";

    // system parameters names
    public static final String VAT_TAX = "vatTax";

    private SystemParameterExceptionCodes() {
        throw new IllegalStateException(ExceptionCodes.NOT_FOR_INSTANTIATING);
    }
}
