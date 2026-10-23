package com.arturjarosz.task.user.model;

/**
 * Settings a user can define. Each one is stored as a row in the USER_CONFIGURATION table, and its allowed values are
 * checked by {@link com.arturjarosz.task.user.domain.UserConfigurationValidator}.
 */
public enum UserConfigurationName {
    THEME_MODE
}
