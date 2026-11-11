package com.arturjarosz.task.role.application.mapper;

import com.arturjarosz.task.dto.RoleDto;
import com.arturjarosz.task.role.model.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = PermissionMapper.class)
public interface RoleMapper {

    @Mapping(source = "role.id", target = "id")
    @Mapping(source = "role.name", target = "name")
    @Mapping(source = "role.administrator", target = "administrator")
    @Mapping(source = "role.permissions", target = "permissions")
    @Mapping(source = "role.createdDateTime", target = "createdDateTime")
    @Mapping(source = "role.lastModifiedDateTime", target = "lastModifiedDateTime")
    @Mapping(source = "numberOfUsers", target = "numberOfUsers")
    RoleDto mapToDto(Role role, Long numberOfUsers);
}
