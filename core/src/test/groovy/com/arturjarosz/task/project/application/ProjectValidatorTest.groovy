package com.arturjarosz.task.project.application

import com.arturjarosz.task.dto.ProjectCreateDto
import com.arturjarosz.task.dto.ProjectDto
import com.arturjarosz.task.dto.ProjectTypeDto
import com.arturjarosz.task.project.query.ProjectQueryService
import com.arturjarosz.task.utils.ProjectBuilder
import spock.lang.Specification

import java.time.LocalDate

class ProjectValidatorTest extends Specification {
    static final Long ARCHITECT_ID = 1L
    static final Long CLIENT_ID = 2L
    static final Long EXISTING_PROJECT_ID = 10L
    static final Long NOT_EXISTING_PROJECT_ID = 19L

    static final String EMPTY_PROJECT_NAME = ""
    static final String PROJECT_NAME = "name"
    static final ProjectTypeDto PROJECT_TYPE_CONCEPT = ProjectTypeDto.CONCEPT

    def project = new ProjectBuilder().withName("name").build()

    def projectQueryService = Mock(ProjectQueryService)

    ProjectValidator projectValidator = new ProjectValidator(projectQueryService)

    def setup() {
        projectQueryService.doesProjectExistByProjectId(NOT_EXISTING_PROJECT_ID) >> false
        projectQueryService.doesProjectExistByProjectId(EXISTING_PROJECT_ID) >> true
    }

    def "passing null to validateProjectBasicDto should thrown an exception"() {
        given:
            def projectCreateDto = null
        when:
            this.projectValidator.validateProjectBasicDto(projectCreateDto)
        then:
            Exception ex = thrown()
            ex.message == "isNull.project"
    }

    def "passing projectCreateDto with null as a name to validateProjectBasicDto should thrown an exception"() {
        given:
            def projectCreateDto = new ProjectCreateDto(name: null)
        when:
            this.projectValidator.validateProjectBasicDto(projectCreateDto)
        then:
            Exception ex = thrown()
            ex.message == "isNull.project.name"
    }

    def "passing projectCreateDto with empty name to a validateProjectBasicDto should thrown an exception"() {
        given:
            def projectCreateDto = new ProjectCreateDto(name: EMPTY_PROJECT_NAME)
        when:
            this.projectValidator.validateProjectBasicDto(projectCreateDto)
        then:
            Exception ex = thrown()
            ex.message == "isEmpty.project.name"
    }

    def "passing projectCreateDto with null as a clientId to validateProjectBasicDto should thrown an exception"() {
        given:
            def projectCreateDto = new ProjectCreateDto(name: PROJECT_NAME, architectId: ARCHITECT_ID,
                    clientId: null, type: PROJECT_TYPE_CONCEPT)
        when:
            this.projectValidator.validateProjectBasicDto(projectCreateDto)
        then:
            Exception ex = thrown()
            ex.message == "isNull.project.client"
    }

    def "passing projectCreateDto with null as a architectId to validateProjectBasicDto should thrown an exception"() {
        given:
            def projectCreateDto = new ProjectCreateDto(name: PROJECT_NAME, architectId: null,
                    clientId: CLIENT_ID, type: PROJECT_TYPE_CONCEPT)
        when:
            this.projectValidator.validateProjectBasicDto(projectCreateDto)
        then:
            Exception ex = thrown()
            ex.message == "isNull.project.architect"
    }

    def "passing projectCreateDto with null as a projectType to validateProjectBasicDto should thrown an exception"() {
        given:
            def projectCreateDto = new ProjectCreateDto(name: PROJECT_NAME, architectId: ARCHITECT_ID,
                    clientId: CLIENT_ID, type: null)
        when:
            this.projectValidator.validateProjectBasicDto(projectCreateDto)
        then:
            Exception ex = thrown()
            ex.message == "isNull.project.type"
    }

    def "passing proper projectCreateDto to validateProjectBasicDto should not throw any exception"() {
        given:
            def projectCreateDto = new ProjectCreateDto(name: PROJECT_NAME, architectId: ARCHITECT_ID,
                    clientId: CLIENT_ID, type: PROJECT_TYPE_CONCEPT)
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

    def "passing null as startDate to validateStartDate should not throw any exception"() {
        given:
            def startDate = null
        when:
            this.projectValidator.validateStartDate(startDate)
        then:
            noExceptionThrown()
    }

    def "passing past date as startDate to validateStartDate should not throw any exception"() {
        given:
            def startDate = LocalDate.now().minusDays(1)
        when:
            this.projectValidator.validateStartDate(startDate)
        then:
            noExceptionThrown()
    }

    def "passing current date as startDate to validateStartDate should not throw any exception"() {
        given:
            def startDate = LocalDate.now()
        when:
            this.projectValidator.validateStartDate(startDate)
        then:
            noExceptionThrown()
    }

    def "passing future date as startDate to validateStartDate should throw an exception"() {
        given:
            def startDate = LocalDate.now().plusDays(1)
        when:
            this.projectValidator.validateStartDate(startDate)
        then:
            Exception ex = thrown()
            ex.message == "notValid.project.startDate"
    }

    def "passing null as startDate and endDate to validateStartAndEndDate should not throw an exception"() {
        given:
            def startDate = null
            def endDate = null
        when:
            this.projectValidator.validateStartAndEndDate(startDate, endDate)
        then:
            noExceptionThrown()
    }

    def "passing startDate after endDate to validateStartAndEndDate should throw an exception"() {
        given:
            def startDate = LocalDate.now().plusDays(1)
            def endDate = LocalDate.now()
        when:
            this.projectValidator.validateStartAndEndDate(startDate, endDate)
        then:
            Exception ex = thrown()
            ex.message == "notValid.project.startDate.endDate"
    }

    def "passing startDate same as endDate to validateStartAndEndDate should not throw an exception"() {
        given:
            def startDate = LocalDate.now()
            def endDate = LocalDate.now()
        when:
            this.projectValidator.validateStartAndEndDate(startDate, endDate)
        then:
            noExceptionThrown()
    }

    def "passing startDate before endDate to validateStartAndEndDate should not throw any exception"() {
        given:
            def startDate = LocalDate.now().minusDays(1)
            def endDate = LocalDate.now()
        when:
            this.projectValidator.validateStartAndEndDate(startDate, endDate)
        then:
            noExceptionThrown()
    }

    def "validateUpdateProjectDto should throw an exception when start date is after stored end date"() {
        given:
            def project = new ProjectBuilder().withEndDate(LocalDate.now().minusDays(5)).build()
            def projectDto = new ProjectDto(name: PROJECT_NAME, startDate: LocalDate.now().minusDays(1))
        when:
            this.projectValidator.validateUpdateProjectDto(projectDto, project)
        then:
            Exception ex = thrown()
            ex.message == "notValid.project.startDate.endDate"
    }

    def "validateUpdateProjectDto should throw an exception when start date is removed from finished project"() {
        given:
            def project = new ProjectBuilder().withEndDate(LocalDate.now().minusDays(5)).build()
            def projectDto = new ProjectDto(name: PROJECT_NAME)
        when:
            this.projectValidator.validateUpdateProjectDto(projectDto, project)
        then:
            Exception ex = thrown()
            ex.message == "isNull.project.startDate"
    }

    def "validateUpdateProjectDto should ignore end date passed in dto"() {
        given:
            def project = new ProjectBuilder().withEndDate(LocalDate.now().minusDays(5)).build()
            def projectDto = new ProjectDto(name: PROJECT_NAME, startDate: LocalDate.now().minusDays(10),
                    endDate: LocalDate.now().minusDays(20))
        when:
            this.projectValidator.validateUpdateProjectDto(projectDto, project)
        then:
            noExceptionThrown()
    }

    def "validateUpdateProjectDto should throw an exception when start date is removed from started project"() {
        given:
            def project = new ProjectBuilder().withStartDate(LocalDate.now().minusDays(5)).build()
            def projectDto = new ProjectDto(name: PROJECT_NAME)
        when:
            this.projectValidator.validateUpdateProjectDto(projectDto, project)
        then:
            Exception ex = thrown()
            ex.message == "isNull.project.startDate"
    }

    def "validateUpdateProjectDto should not require start date for not started project"() {
        given:
            def project = new ProjectBuilder().build()
            def projectDto = new ProjectDto(name: PROJECT_NAME)
        when:
            this.projectValidator.validateUpdateProjectDto(projectDto, project)
        then:
            noExceptionThrown()
    }
}
