package com.arturjarosz.task.systemparameter.domain;

import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;

public class SystemParameters {
    public static final String VAT_TAX = "Vat tax";

    private SystemParameters() {
        throw new IllegalStateException(ExceptionCodes.NOT_FOR_INSTANTIATING);
    }
}
