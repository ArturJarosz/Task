package com.arturjarosz.task.role.rest;

import com.arturjarosz.task.dto.ResourcePermissionsDto;
import com.arturjarosz.task.dto.RoleDto;
import com.arturjarosz.task.rest.RoleApi;
import com.arturjarosz.task.role.application.RoleApplicationService;
import com.arturjarosz.task.sharedkernel.testhelpers.HttpHeadersBuilder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class RoleRestController implements RoleApi {

    @NonNull
    private final RoleApplicationService roleApplicationService;

    @Override
    public ResponseEntity<RoleDto> createRole(RoleDto roleDto) {
        var role = this.roleApplicationService.create(roleDto);
        var headers = new HttpHeadersBuilder().withLocation("/roles/{roleId}", role.getId()).build();
        return new ResponseEntity<>(role, headers, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<RoleDto> updateRole(RoleDto roleDto, Long roleId) {
        return new ResponseEntity<>(this.roleApplicationService.update(roleId, roleDto), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> deleteRole(Long roleId) {
        this.roleApplicationService.delete(roleId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<RoleDto> getRole(Long roleId) {
        return new ResponseEntity<>(this.roleApplicationService.get(roleId), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<RoleDto>> getRoles() {
        return new ResponseEntity<>(this.roleApplicationService.getAll(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<ResourcePermissionsDto>> getAvailablePermissions() {
        return new ResponseEntity<>(this.roleApplicationService.getAvailablePermissions(), HttpStatus.OK);
    }
}
