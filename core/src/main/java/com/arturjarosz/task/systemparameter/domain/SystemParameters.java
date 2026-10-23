package com.arturjarosz.task.systemparameter.domain;

import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;

public final class SystemParameters {
    public static final String VAT_TAX = "Vat tax";
    public static final String INCOME_TAX = "Income tax";
    public static final String THEME_COLOR = "Theme color";

    private SystemParameters() {
        throw new IllegalStateException(ExceptionCodes.NOT_FOR_INSTANTIATING);
    }
}
