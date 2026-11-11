package com.arturjarosz.task.security.permission;

/**
 * What a permission lets a user do with a {@link Resource}. Names are stored in the database, so an action must not be
 * renamed without a migration.
 */
public enum Action {
    READ,
    CREATE,
    EDIT,
    DELETE
}
