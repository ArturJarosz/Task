package com.arturjarosz.task.role.application;

import com.arturjarosz.task.dto.ResourcePermissionsDto;
import com.arturjarosz.task.dto.RoleDto;
import com.arturjarosz.task.sharedkernel.namedentry.NamedEntryApplicationService;

import java.util.List;

/**
 * Roles configured by users, each with its permissions and the number of users having it. The administrator role
 * cannot be changed or removed.
 */
public interface RoleApplicationService extends NamedEntryApplicationService<RoleDto> {

    /**
     * Returns every resource with the actions a role can allow on it, in the order of the resources.
     */
    List<ResourcePermissionsDto> getAvailablePermissions();
}
