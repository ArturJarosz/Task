package com.arturjarosz.task.role.model

import com.arturjarosz.task.security.permission.Action
import com.arturjarosz.task.security.permission.Permission
import com.arturjarosz.task.security.permission.Resource
import spock.lang.Specification

class RoleTest extends Specification {
    static final Permission CLIENT_READ = new Permission(Resource.CLIENT, Action.READ)
    static final Permission PROJECT_READ = new Permission(Resource.PROJECT, Action.READ)
    static final Permission PROJECT_EDIT = new Permission(Resource.PROJECT, Action.EDIT)
    static final Permission CALENDAR_READ = new Permission(Resource.CALENDAR, Action.READ)

    def "Role should return its permissions by resource, then action"() {
        given:
            def role = new Role("Architect", [CLIENT_READ, PROJECT_EDIT, PROJECT_READ])

        expect:
            role.permissions as List == [PROJECT_READ, PROJECT_EDIT, CLIENT_READ]
    }

    def "Changing permissions should replace the ones the role had"() {
        given:
            def role = new Role("Architect", [PROJECT_EDIT, CLIENT_READ])

        when:
            role.changePermissions([CLIENT_READ, CALENDAR_READ])

        then:
            role.permissions == [CLIENT_READ, CALENDAR_READ] as Set
    }

    def "Administrator role should have every permission without storing any"() {
        given:
            def role = new Role("Administrator", [])
            role.@administrator = true

        expect:
            role.permissions == Permission.all()
            role.@permissions.isEmpty()
    }

    def "Changing returned permissions should not change the role"() {
        given:
            def role = new Role("Architect", [PROJECT_READ])

        when:
            role.permissions.add(new Permission(Resource.ROLE, Action.DELETE))

        then:
            role.permissions == [PROJECT_READ] as Set
    }

    def "Administrator role should not be renamed or given permissions"() {
        given:
            def role = new Role("Administrator", [])
            role.@administrator = true

        when:
            change(role)

        then:
            thrown(IllegalStateException)
            role.name == "Administrator"
            role.@permissions.isEmpty()

        where:
            change << [{ Role administrator -> administrator.rename("Anyone") },
                       { Role administrator -> administrator.changePermissions([PROJECT_READ]) }]
    }
}
