package com.arturjarosz.task.role.domain;

import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;

public final class RoleExceptionCodes {
    public static final String ADMINISTRATOR = "administrator";
    public static final String PERMISSIONS = "permissions";
    public static final String REQUIRED = "required";
    public static final String ROLE = "role";
    public static final String USERS = "users";

    private RoleExceptionCodes() {
        throw new IllegalStateException(ExceptionCodes.NOT_FOR_INSTANTIATING);
    }
}
