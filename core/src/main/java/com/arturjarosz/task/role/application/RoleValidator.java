package com.arturjarosz.task.role.application;

import com.arturjarosz.task.dto.PermissionDto;
import com.arturjarosz.task.dto.RoleDto;
import com.arturjarosz.task.role.domain.RoleExceptionCodes;
import com.arturjarosz.task.role.infrastructure.repository.RoleRepository;
import com.arturjarosz.task.role.model.Role;
import com.arturjarosz.task.security.permission.Action;
import com.arturjarosz.task.security.permission.Permission;
import com.arturjarosz.task.security.permission.Resource;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import com.arturjarosz.task.sharedkernel.namedentry.NamedEntryValidator;
import com.arturjarosz.task.user.query.UserQueryService;
import org.springframework.stereotype.Component;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsFalse;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotNull;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

/**
 * Validates role dtos and Role entities. Names are validated like those of other named entries; a role is used by the
 * users having it.
 */
@Component
public class RoleValidator extends NamedEntryValidator<Role> {

    public RoleValidator(RoleRepository roleRepository, UserQueryService userQueryService) {
        super(roleRepository, RoleExceptionCodes.ROLE, RoleExceptionCodes.USERS,
                userQueryService::getNumberOfUsersByRoleIds);
    }

    /**
     * Validates that permissions are given, each with a resource and an action available for that resource, together
     * with the permissions each of them requires (see {@link Permission#getRequiredPermissions()}).
     */
    public static void validatePermissions(RoleDto roleDto) {
        var nullMessageCode = createMessageCode(ExceptionCodes.NULL, RoleExceptionCodes.ROLE,
                RoleExceptionCodes.PERMISSIONS);
        assertNotNull(roleDto.getPermissions(), nullMessageCode);
        var permissions = Permission.newSortedSet();
        for (PermissionDto permissionDto : roleDto.getPermissions()) {
            assertIsTrue(permissionDto != null && permissionDto.getResource() != null
                    && permissionDto.getAction() != null, nullMessageCode);
            var resource = Resource.valueOf(permissionDto.getResource().name());
            var action = Action.valueOf(permissionDto.getAction().name());
            assertIsTrue(resource.allows(action), createMessageCode(ExceptionCodes.NOT_VALID, RoleExceptionCodes.ROLE,
                    RoleExceptionCodes.PERMISSIONS), action, resource);
            permissions.add(new Permission(resource, action));
        }
        permissions.forEach(permission -> permission.getRequiredPermissions().forEach(required ->
                assertIsTrue(permissions.contains(required), createMessageCode(ExceptionCodes.NOT_VALID,
                                RoleExceptionCodes.ROLE, RoleExceptionCodes.PERMISSIONS, RoleExceptionCodes.REQUIRED),
                        permission.action(), permission.resource(), required.action(), required.resource())));
    }

    public static void validateNotAdministrator(Role role) {
        assertIsFalse(role.isAdministrator(),
                createMessageCode(ExceptionCodes.NOT_VALID, RoleExceptionCodes.ROLE, RoleExceptionCodes.ADMINISTRATOR));
    }
}
