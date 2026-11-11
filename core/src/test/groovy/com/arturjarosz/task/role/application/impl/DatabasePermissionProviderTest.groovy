package com.arturjarosz.task.role.application.impl

import com.arturjarosz.task.role.infrastructure.repository.RoleRepository
import com.arturjarosz.task.role.model.Role
import com.arturjarosz.task.security.permission.Action
import com.arturjarosz.task.security.permission.AuthenticatedPrincipal
import com.arturjarosz.task.security.permission.Permission
import com.arturjarosz.task.security.permission.Resource
import spock.lang.Specification

class DatabasePermissionProviderTest extends Specification {
    static final String AUTH_ID = "auth0|permissions"
    static final AuthenticatedPrincipal PRINCIPAL = new AuthenticatedPrincipal(AUTH_ID, [sub: AUTH_ID])
    static final Permission PROJECT_READ = new Permission(Resource.PROJECT, Action.READ)
    static final Permission PROJECT_EDIT = new Permission(Resource.PROJECT, Action.EDIT)
    static final Permission FINANCE_READ = new Permission(Resource.PROJECT_FINANCE, Action.READ)

    def roleRepository = Mock(RoleRepository)

    def subject = new DatabasePermissionProvider(roleRepository)

    def "getPermissions should return permissions of all roles of the user of the principal"() {
        given:
            this.roleRepository.findAllWithPermissionsByUserAuthId(AUTH_ID) >> [
                    new Role("Architect", [PROJECT_READ, PROJECT_EDIT]),
                    new Role("Accountant", [PROJECT_READ, FINANCE_READ])]

        expect:
            this.subject.getPermissions(PRINCIPAL) as List == [PROJECT_READ, PROJECT_EDIT, FINANCE_READ]
    }

    def "getPermissions should return no permissions when the user has no roles or there is no such user"() {
        given:
            this.roleRepository.findAllWithPermissionsByUserAuthId(AUTH_ID) >> []

        expect:
            this.subject.getPermissions(PRINCIPAL).isEmpty()
    }
}
