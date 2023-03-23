package com.arturjarosz.task.project.application

import com.arturjarosz.task.project.application.dto.ProjectCreateDto
import com.arturjarosz.task.project.model.Project
import com.arturjarosz.task.project.model.ProjectType
import com.arturjarosz.task.project.query.ProjectQueryService
import com.arturjarosz.task.utils.ProjectBuilder
import spock.lang.Specification

class ProjectValidatorTest extends Specification {
    private static final Long ARCHITECT_ID = 1L
    private static final Long CLIENT_ID = 2L
    private static final Long EXISTING_PROJECT_ID = 10L
    private static final Long NOT_EXISTING_PROJECT_ID = 19L

    private static final String EMPTY_PROJECT_NAME = ""
    private static final String PROJECT_NAME = "name"
    private static final ProjectType PROJECT_TYPE_CONCEPT = ProjectType.CONCEPT

    Project project = new ProjectBuilder().withName("name").build()

    def projectQueryService = Mock(ProjectQueryService)

    ProjectValidator projectValidator = new ProjectValidator(projectQueryService)

    def setup() {
        projectQueryService.doesProjectExistByProjectId(NOT_EXISTING_PROJECT_ID) >> false
        projectQueryService.doesProjectExistByProjectId(EXISTING_PROJECT_ID) >> true
    }

    def "passing null to validateProjectBasicDto should thrown an exception"() {
        given:
            ProjectCreateDto projectCreateDto = null
        when:
            this.projectValidator.validateProjectBasicDto(projectCreateDto)
        then:
            Exception ex = thrown()
            ex.message == "isNull.project"
    }

    def "passing projectCreateDto with null as a name to validateProjectBasicDto should thrown an exception"() {
        given:
            ProjectCreateDto projectCreateDto = new ProjectCreateDto(name: null)
        when:
            this.projectValidator.validateProjectBasicDto(projectCreateDto)
        then:
            Exception ex = thrown()
            ex.message == "isNull.project.name"
    }

    def "passing projectCreateDto with empty name to a validateProjectBasicDto should thrown an exception"() {
        given:
            ProjectCreateDto projectCreateDto = new ProjectCreateDto(name: EMPTY_PROJECT_NAME)
        when:
            this.projectValidator.validateProjectBasicDto(projectCreateDto)
        then:
            Exception ex = thrown()
            ex.message == "isEmpty.project.name"
    }

    def "passing projectCreateDto with null as a clientId to validateProjectBasicDto should thrown an exception"() {
        given:
            ProjectCreateDto projectCreateDto = new ProjectCreateDto(name: PROJECT_NAME, architectId: ARCHITECT_ID,
                    clientId: null, projectType: PROJECT_TYPE_CONCEPT)
        when:
            this.projectValidator.validateProjectBasicDto(projectCreateDto)
        then:
            Exception ex = thrown()
            ex.message == "isNull.project.client"
    }

    def "passing projectCreateDto with null as a architectId to validateProjectBasicDto should thrown an exception"() {
        given:
            ProjectCreateDto projectCreateDto = new ProjectCreateDto(name: PROJECT_NAME, architectId: null,
                    clientId: CLIENT_ID, projectType: PROJECT_TYPE_CONCEPT)
        when:
            this.projectValidator.validateProjectBasicDto(projectCreateDto)
        then:
            Exception ex = thrown()
            ex.message == "isNull.project.architect"
    }

    def "passing projectCreateDto with null as a projectType to validateProjectBasicDto should thrown an exception"() {
        given:
            ProjectCreateDto projectCreateDto = new ProjectCreateDto(name: PROJECT_NAME, architectId: ARCHITECT_ID,
                    clientId: CLIENT_ID, projectType: null)
        when:
            this.projectValidator.validateProjectBasicDto(projectCreateDto)
        then:
            Exception ex = thrown()
            ex.message == "isNull.project.type"
    }

    def "passing proper projectCreateDto to validateProjectBasicDto should not throw any exception"() {
        given:
            ProjectCreateDto projectCreateDto = new ProjectCreateDto(name: PROJECT_NAME, architectId: ARCHITECT_ID,
                    clientId: CLIENT_ID, projectType: PROJECT_TYPE_CONCEPT)
        when:
            this.projectValidator.validateProjectBasicDto(projectCreateDto)
        then:
            noExceptionThrown()
    }

    def "when passing not existing project id validateProjectExistence should throw an exception"() {
        given:
        when:
            this.projectValidator.validateProjectExistence(NOT_EXISTING_PROJECT_ID)
        then:
            Exception ex = thrown()
            ex.message == "notExist.project"
    }

    def "when passing existing project id validateProjectExistence should not throw any exception"() {
        given:
        when:
            this.projectValidator.validateProjectExistence(EXISTING_PROJECT_ID)
        then:
            noExceptionThrown()
    }
}
