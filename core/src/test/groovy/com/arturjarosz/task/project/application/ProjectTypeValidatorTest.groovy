package com.arturjarosz.task.project.application

import com.arturjarosz.task.dto.ProjectTypeDto
import com.arturjarosz.task.project.infrastructure.repositor.ProjectTypeRepository
import com.arturjarosz.task.project.model.ProjectType
import com.arturjarosz.task.project.query.ProjectCounts
import com.arturjarosz.task.project.query.ProjectQueryService
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException
import com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException
import spock.lang.Specification

class ProjectTypeValidatorTest extends Specification {
    static final Long PROJECT_TYPE_ID = 100L

    def projectTypeRepository = Mock(ProjectTypeRepository)
    def projectQueryService = Mock(ProjectQueryService)

    def projectTypeValidator = new ProjectTypeValidator(projectTypeRepository, projectQueryService)

    def "validateCreateProjectTypeDto should throw an exception for missing or blank name"() {
        when:
            this.projectTypeValidator.validateCreateProjectTypeDto(projectTypeDto)
        then:
            def exception = thrown(IllegalArgumentException)
            exception.message == message
        where:
            projectTypeDto                 | message
            null                           | "isNull.projectType"
            new ProjectTypeDto()           | "isNull.projectType.name"
            new ProjectTypeDto(name: "  ") | "isEmpty.projectType.name"
    }

    def "validateCreateProjectTypeDto should throw an exception for too long name"() {
        when:
            this.projectTypeValidator.validateCreateProjectTypeDto(
                    new ProjectTypeDto(name: "a" * (ProjectType.NAME_MAX_LENGTH + 1)))
        then:
            def exception = thrown(IllegalArgumentException)
            exception.message == "notValid.projectType.name.tooLong"
    }

    def "validateCreateProjectTypeDto should throw an exception when stripped name is already used"() {
        given:
            this.projectTypeRepository.existsByNameIgnoreCase("Concept") >> true
        when:
            this.projectTypeValidator.validateCreateProjectTypeDto(new ProjectTypeDto(name: " Concept "))
        then:
            def exception = thrown(IllegalArgumentException)
            exception.message == "notValid.projectType.name.unique"
    }

    def "validateUpdateProjectTypeDto should check name uniqueness against other project types only"() {
        when:
            this.projectTypeValidator.validateUpdateProjectTypeDto(new ProjectTypeDto(name: "Concept"), PROJECT_TYPE_ID)
        then:
            1 * this.projectTypeRepository.existsByNameIgnoreCaseAndIdNot("Concept", PROJECT_TYPE_ID) >> false
            noExceptionThrown()
    }

    def "validateProjectTypeExistence should throw an exception when project type does not exist"() {
        given:
            this.projectTypeRepository.findById(PROJECT_TYPE_ID) >> Optional.empty()
        when:
            this.projectTypeValidator.validateProjectTypeExistence(PROJECT_TYPE_ID)
        then:
            def exception = thrown(ResourceNotFoundException)
            exception.message == "notExist.projectType"
    }

    def "validateProjectTypeHasNoProjects should throw an exception when a project has the type"() {
        given:
            this.projectQueryService.getProjectCountsByProjectTypeIds([PROJECT_TYPE_ID]) >>
                    [(PROJECT_TYPE_ID): new ProjectCounts(2, 0)]
        when:
            this.projectTypeValidator.validateProjectTypeHasNoProjects(PROJECT_TYPE_ID)
        then:
            def exception = thrown(IllegalArgumentException)
            exception.message == "notValid.projectType.projects"
    }

    def "validateProjectTypeHasNoProjects should not throw an exception when no project has the type"() {
        given:
            this.projectQueryService.getProjectCountsByProjectTypeIds([PROJECT_TYPE_ID]) >>
                    [(PROJECT_TYPE_ID): ProjectCounts.NONE]
        when:
            this.projectTypeValidator.validateProjectTypeHasNoProjects(PROJECT_TYPE_ID)
        then:
            noExceptionThrown()
    }
}
