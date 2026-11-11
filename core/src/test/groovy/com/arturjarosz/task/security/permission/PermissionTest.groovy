package com.arturjarosz.task.security.permission

import spock.lang.Specification

class PermissionTest extends Specification {

    def "Permission should take only an action available for its resource"() {
        when:
            new Permission(resource, action)
        then:
            thrown(IllegalArgumentException)
        where:
            resource          | action
            Resource.CONTRACT | Action.CREATE
            Resource.CONTRACT | Action.DELETE
            Resource.USER     | Action.DELETE
            null              | Action.READ
            Resource.CLIENT   | null
    }

    def "All permissions should cover every action of every resource, in order"() {
        when:
            def permissions = Permission.all() as List
        then:
            permissions.size() == Resource.values().sum { it.actions.size() }
            permissions.take(5) == [new Permission(Resource.PROJECT, Action.READ),
                                    new Permission(Resource.PROJECT, Action.CREATE),
                                    new Permission(Resource.PROJECT, Action.EDIT),
                                    new Permission(Resource.PROJECT, Action.DELETE),
                                    new Permission(Resource.CONTRACT, Action.READ)]
            !permissions.contains(null)
    }

    def "Permission should require reading its resource and the resource it is part of"() {
        expect:
            new Permission(resource, action).requiredPermissions as List == required
        where:
            resource                  | action        || required
            Resource.CLIENT           | Action.READ   || []
            Resource.CLIENT           | Action.EDIT   || [new Permission(Resource.CLIENT, Action.READ)]
            Resource.FINANCIAL_REPORT | Action.READ   || []
            Resource.PROJECT_FINANCE  | Action.READ   || [new Permission(Resource.PROJECT, Action.READ)]
            Resource.PROJECT_FINANCE  | Action.DELETE || [new Permission(Resource.PROJECT, Action.READ),
                                                          new Permission(Resource.PROJECT_FINANCE, Action.READ)]
    }
}
