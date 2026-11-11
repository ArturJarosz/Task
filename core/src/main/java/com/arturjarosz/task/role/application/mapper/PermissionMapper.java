package com.arturjarosz.task.role.application.mapper;

import com.arturjarosz.task.dto.PermissionActionDto;
import com.arturjarosz.task.dto.PermissionDto;
import com.arturjarosz.task.dto.PermissionResourceDto;
import com.arturjarosz.task.dto.ResourcePermissionsDto;
import com.arturjarosz.task.security.permission.Action;
import com.arturjarosz.task.security.permission.Permission;
import com.arturjarosz.task.security.permission.Resource;
import org.mapstruct.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * Maps permissions by the names of their resource and action, which are the same in the dtos.
 */
@Mapper
public interface PermissionMapper {

    default PermissionDto mapToDto(Permission permission) {
        return new PermissionDto()
                .resource(PermissionResourceDto.valueOf(permission.resource().name()))
                .action(PermissionActionDto.valueOf(permission.action().name()));
    }

    default List<PermissionDto> mapToDtos(Collection<Permission> permissions) {
        return permissions.stream().map(this::mapToDto).toList();
    }

    /**
     * Expects a dto checked by RoleValidator.validatePermissions.
     */
    default Permission mapFromDto(PermissionDto permissionDto) {
        return new Permission(Resource.valueOf(permissionDto.getResource().name()),
                Action.valueOf(permissionDto.getAction().name()));
    }

    default List<Permission> mapFromDtos(Collection<PermissionDto> permissionDtos) {
        return permissionDtos.stream().map(this::mapFromDto).toList();
    }

    default ResourcePermissionsDto mapToDto(Resource resource) {
        return new ResourcePermissionsDto()
                .resource(PermissionResourceDto.valueOf(resource.name()))
                .actions(resource.getActions().stream().map(action -> PermissionActionDto.valueOf(action.name()))
                        .toList())
                .requiredResource(resource.getRequiredResource()
                        .map(requiredResource -> PermissionResourceDto.valueOf(requiredResource.name()))
                        .orElse(null));
    }
}
