package com.arturjarosz.task.role.infrastructure.repository;

import com.arturjarosz.task.role.model.Role;
import com.arturjarosz.task.sharedkernel.namedentry.NamedEntryRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoleRepository extends NamedEntryRepository<Role> {

    /**
     * Finds the roles of the user with given authId together with their permissions, in one query; none when there is
     * no such user.
     */
    @Query("select role from Role role left join fetch role.permissions where role.id in "
            + "(select roleId from User localUser join localUser.roleIds roleId where localUser.authId = :authId)")
    List<Role> findAllWithPermissionsByUserAuthId(@Param("authId") String authId);
}
