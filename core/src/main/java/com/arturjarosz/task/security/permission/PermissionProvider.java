package com.arturjarosz.task.security.permission;

import java.util.Set;

/**
 * Source of the permissions of a user. The rest of the application asks for permissions only through this interface,
 * so where they come from (the roles kept in the database by default) can change without changing it. Select the
 * implementation with the task.security.permission-source property.
 */
public interface PermissionProvider {

    String PERMISSION_SOURCE_PROPERTY = "task.security.permission-source";

    /**
     * Returns permissions of given principal in {@link Permission#ORDER}; empty when it has none, also when there is no
     * local user for it.
     */
    Set<Permission> getPermissions(AuthenticatedPrincipal principal);
}
