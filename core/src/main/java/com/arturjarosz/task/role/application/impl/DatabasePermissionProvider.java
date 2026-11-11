package com.arturjarosz.task.role.application.impl;

import com.arturjarosz.task.role.infrastructure.repository.RoleRepository;
import com.arturjarosz.task.role.model.Role;
import com.arturjarosz.task.security.permission.AuthenticatedPrincipal;
import com.arturjarosz.task.security.permission.Permission;
import com.arturjarosz.task.security.permission.PermissionProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Default {@link PermissionProvider}: permissions are those of the roles the local user of the principal has.
 */
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(name = PermissionProvider.PERMISSION_SOURCE_PROPERTY, havingValue = "database",
        matchIfMissing = true)
public class DatabasePermissionProvider implements PermissionProvider {

    private final RoleRepository roleRepository;

    @Transactional(readOnly = true)
    @Override
    public Set<Permission> getPermissions(AuthenticatedPrincipal principal) {
        var permissions = Permission.newSortedSet();
        this.roleRepository.findAllWithPermissionsByUserAuthId(principal.authId()).stream()
                .map(Role::getPermissions)
                .forEach(permissions::addAll);
        return permissions;
    }
}
