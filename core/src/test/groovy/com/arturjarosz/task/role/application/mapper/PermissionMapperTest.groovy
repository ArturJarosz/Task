package com.arturjarosz.task.role.application.mapper

import com.arturjarosz.task.dto.PermissionActionDto
import com.arturjarosz.task.dto.PermissionResourceDto
import com.arturjarosz.task.security.permission.Action
import com.arturjarosz.task.security.permission.Resource
import spock.lang.Specification

class PermissionMapperTest extends Specification {

    def subject = new PermissionMapper() {}

    // Mapped by name, so a resource or action missing from the API would fail every response listing it
    def "Resources and actions should match the ones of the API"() {
        expect:
            Resource.values()*.name() == PermissionResourceDto.values()*.name()
            Action.values()*.name() == PermissionActionDto.values()*.name()
    }

    def "Resource should be mapped with its actions and the resource it is part of"() {
        when:
            def dto = this.subject.mapToDto(Resource.PROJECT_FINANCE)
        then:
            dto.resource == PermissionResourceDto.PROJECT_FINANCE
            dto.actions == PermissionActionDto.values() as List
            dto.requiredResource == PermissionResourceDto.PROJECT
    }

    def "Resource that is not part of another should be mapped without a required resource"() {
        expect:
            this.subject.mapToDto(Resource.CLIENT).requiredResource == null
    }
}
