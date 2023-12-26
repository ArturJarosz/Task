package com.arturjarosz.task.architect.application

import com.arturjarosz.task.architect.infrastructure.repository.ArchitectRepository
import com.arturjarosz.task.architect.model.Architect
import com.arturjarosz.task.dto.ArchitectDto
import com.arturjarosz.task.project.model.Project
import com.arturjarosz.task.project.query.impl.ProjectQueryServiceImpl
import com.arturjarosz.task.utils.ProjectBuilder
import spock.lang.Specification

class ArchitectValidatorTest extends Specification {

    static final String FIRST_NAME = "first"
    static final String LAST_NAME = "last"
    static final Long EXISTING_ID = 1L
    static final Long NON_EXISTING_ID = 999L
    static final Long ARCHITECT_WITH_PROJECTS = 10L
    static final Long ARCHITECT_WITHOUT_PROJECTS = 888L

    static Architect architect = new Architect(FIRST_NAME, LAST_NAME)

    static Project project = new ProjectBuilder().build()

    def architectRepository = Mock(ArchitectRepository) {
        findById(NON_EXISTING_ID) >> { Optional.ofNullable(null) }
        findById(EXISTING_ID) >> { Optional.of(architect) }
    }

    def projectQueryService = Mock(ProjectQueryServiceImpl) {
        getProjectsForArchitect(ARCHITECT_WITH_PROJECTS) >> { Arrays.asList(project) }
        getProjectsForArchitect(ARCHITECT_WITHOUT_PROJECTS) >> { Collections.emptyList() }
    }

    def architectValidator = new ArchitectValidator(architectRepository, projectQueryService)

    def "when passing null architectDtoValidator should throw an exception with specific error message"() {
        given:
            ArchitectDto architectDto = null
        when:
            ArchitectValidator.validateArchitectDto(architectDto)
        then:
            Exception exception = thrown()
            exception.message == "isNull.architect"
    }

    def "when passing architect without firstName field architectDtoValidator should throw an exception with specific error message"() {
        given:
            def architectDto = new ArchitectDto(lastName: LAST_NAME)
        when:
            ArchitectValidator.validateArchitectDto(architectDto)
        then:
            Exception exception = thrown()
            exception.message == "isNull.architect.firstName"
    }

    def "when passing architect without lastName field architectDtoValidator should throw an exception with specific error message"() {
        given:
            def architectDto = new ArchitectDto(firstName: FIRST_NAME)
        when:
            ArchitectValidator.validateArchitectDto(architectDto)
        then:
            Exception exception = thrown()
            exception.message == "isNull.architect.lastName"
    }

    def "when passing architect with empty firstName field architectDtoValidator should throw an exception with specific error message"() {
        given:
            def architectDto = new ArchitectDto(firstName: "", lastName: LAST_NAME)
        when:
            ArchitectValidator.validateArchitectDto(architectDto)
        then:
            Exception exception = thrown()
            exception.message == "isEmpty.architect.firstName"
    }

    def "when passing architect with empty lastName field architectDtoValidator should throw an exception with specific error message"() {
        given:
            def architectDto = new ArchitectDto(firstName: FIRST_NAME, lastName: "")
        when:
            ArchitectValidator.validateArchitectDto(architectDto)
        then:
            Exception exception = thrown()
            exception.message == "isEmpty.architect.lastName"
    }

    def "when passing proper data architectDtoValidator should not throw any exception"() {
        given:
            def architectDto = new ArchitectDto(firstName: FIRST_NAME, lastName: LAST_NAME)
        when:
            ArchitectValidator.validateArchitectDto(architectDto)
        then:
            noExceptionThrown()
    }

    def "when passing non existing architect id validateArchitectExistence should throw an exception with specific error message"() {
        given:
        when:
            this.architectValidator.validateArchitectExistence(NON_EXISTING_ID)
        then:
            Exception exception = thrown()
            exception.message == "notExist.architect"
    }

    def "when passing existing architect id validateArchitectExistence should not throw any exception"() {
        given:
        when:
            this.architectValidator.validateArchitectExistence(EXISTING_ID)
        then:
            noExceptionThrown()
    }

    def "when passing architect id with no Projects validate should not throw an exception"() {
        given:
        when:
            this.architectValidator.validateArchitectHasNoProjects(ARCHITECT_WITHOUT_PROJECTS)
        then:
            noExceptionThrown()
    }

    def "when passing architect id with Projects validate should throw an exception"() {
        given:
        when:
            this.architectValidator.validateArchitectHasNoProjects(ARCHITECT_WITH_PROJECTS)
        then:
            Exception ex = thrown()
            ex.message == "notValid.architect.projects"
    }
}
