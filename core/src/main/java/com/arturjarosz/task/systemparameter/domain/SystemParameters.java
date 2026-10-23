package com.arturjarosz.task.systemparameter.domain;

import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;

public final class SystemParameters {
    public static final String VAT_TAX = "Vat tax";
    public static final String INCOME_TAX = "Income tax";
    public static final String THEME_COLOR = "Theme color";
    public static final String GOOGLE_CALENDAR_SYNC = "Google calendar sync";
    public static final String GOOGLE_CALENDAR_NAME = "Google calendar name";
    public static final String GOOGLE_CALENDAR_APPLICATION_NAME = "Google calendar application name";
    public static final String GOOGLE_CALENDAR_CREDENTIALS = "Google calendar credentials";

    private SystemParameters() {
        throw new IllegalStateException(ExceptionCodes.NOT_FOR_INSTANTIATING);
    }
}
