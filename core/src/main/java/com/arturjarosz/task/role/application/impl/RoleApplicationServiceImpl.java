package com.arturjarosz.task.role.application.impl;

import com.arturjarosz.task.dto.ResourcePermissionsDto;
import com.arturjarosz.task.dto.RoleDto;
import com.arturjarosz.task.role.application.RoleApplicationService;
import com.arturjarosz.task.role.application.RoleValidator;
import com.arturjarosz.task.role.application.mapper.PermissionMapper;
import com.arturjarosz.task.role.application.mapper.RoleMapper;
import com.arturjarosz.task.role.infrastructure.repository.RoleRepository;
import com.arturjarosz.task.role.model.Role;
import com.arturjarosz.task.security.permission.Resource;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.namedentry.AbstractNamedEntryApplicationService;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

import static com.arturjarosz.task.role.application.RoleValidator.validateNotAdministrator;
import static com.arturjarosz.task.role.application.RoleValidator.validatePermissions;

@Slf4j
@ApplicationService
public class RoleApplicationServiceImpl extends AbstractNamedEntryApplicationService<Role, RoleDto>
        implements RoleApplicationService {

    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;

    public RoleApplicationServiceImpl(RoleRepository roleRepository, RoleValidator roleValidator,
            RoleMapper roleMapper, PermissionMapper permissionMapper) {
        super(roleRepository, roleValidator, "Role");
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
    }

    @Override
    public List<ResourcePermissionsDto> getAvailablePermissions() {
        LOG.info("Loading available permissions.");

        return Arrays.stream(Resource.values()).map(this.permissionMapper::mapToDto).toList();
    }

    @Override
    protected Role createEntry(String name) {
        return new Role(name, List.of());
    }

    @Override
    protected String nameOf(RoleDto roleDto) {
        return roleDto.getName();
    }

    @Override
    protected RoleDto toDto(Role role, Long numberOfUsers) {
        return this.roleMapper.mapToDto(role, numberOfUsers);
    }

    @Override
    protected void validateDto(RoleDto roleDto) {
        validatePermissions(roleDto);
    }

    @Override
    protected void validateChangeable(Role role) {
        validateNotAdministrator(role);
    }

    @Override
    protected void applyChanges(Role role, RoleDto roleDto) {
        role.changePermissions(this.permissionMapper.mapFromDtos(roleDto.getPermissions()));
    }
}
