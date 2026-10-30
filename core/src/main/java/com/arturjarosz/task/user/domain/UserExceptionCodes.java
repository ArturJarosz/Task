package com.arturjarosz.task.user.domain;

import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;

public final class UserExceptionCodes {
    public static final String CONFIGURATION = "configuration";
    public static final String DASHBOARD_LAYOUT = "dashboardLayout";
    public static final String NAME = "name";
    public static final String THEME_MODE = "themeMode";
    public static final String USER = "user";
    public static final String VALUE = "value";

    private UserExceptionCodes() {
        throw new IllegalStateException(ExceptionCodes.NOT_FOR_INSTANTIATING);
    }
}
