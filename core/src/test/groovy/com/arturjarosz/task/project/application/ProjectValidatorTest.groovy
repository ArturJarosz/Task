package com.arturjarosz.task.project.application

import com.arturjarosz.task.project.application.dto.ProjectContractDto
import com.arturjarosz.task.project.application.dto.ProjectCreateDto
import com.arturjarosz.task.project.infrastructure.repositor.impl.ProjectRepositoryImpl
import com.arturjarosz.task.project.model.Project
import com.arturjarosz.task.project.model.ProjectType
import com.arturjarosz.task.project.query.impl.ProjectQueryServiceImpl
import com.arturjarosz.task.project.utils.ProjectBuilder
import spock.lang.Shared
import spock.lang.Specification

import java.time.LocalDate

class ProjectValidatorTest extends Specification {
    private static final Long ARCHITECT_ID = 1L
    private static final Long CLIENT_ID = 2L
    private static final Long EXISTING_PROJECT_ID = 10L
    private static final Long NOT_EXISTING_PROJECT_ID = 19L

    private static final LocalDate SIGNING_DATE = LocalDate.now()
    private static final LocalDate START_DATE = LocalDate.now()
    private static final LocalDate DEADLINE = LocalDate.now()

    private static final String EMPTY_PROJECT_NAME = ""
    private static final String PROJECT_NAME = "name"
    private static final ProjectType PROJECT_TYPE_CONCEPT = ProjectType.CONCEPT

    @Shared
    Project project = new ProjectBuilder().withName("name").build()

    def projectRepository = Mock(ProjectRepositoryImpl) {
        load(NOT_EXISTING_PROJECT_ID) >> { null }
        load(EXISTING_PROJECT_ID) >> {
            return project
        }
    }

    def projectQueryService = Mock(ProjectQueryServiceImpl)

    ProjectValidator projectValidator = new ProjectValidator(projectRepository, projectQueryService)

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
            ProjectCreateDto projectCreateDto = new ProjectCreateDto()
            projectCreateDto.setName(null)
        when:
            this.projectValidator.validateProjectBasicDto(projectCreateDto)
        then:
            Exception ex = thrown()
            ex.message == "isNull.project.name"
    }

    def "passing projectCreateDto with empty name to a validateProjectBasicDto should thrown an exception"() {
        given:
            ProjectCreateDto projectCreateDto = new ProjectCreateDto()
            projectCreateDto.name = EMPTY_PROJECT_NAME
        when:
            this.projectValidator.validateProjectBasicDto(projectCreateDto)
        then:
            Exception ex = thrown()
            ex.message == "isEmpty.project.name"
    }

    def "passing projectCreateDto with null as a clientId to validateProjectBasicDto should thrown an exception"() {
        given:
            ProjectCreateDto projectCreateDto = new ProjectCreateDto()
            projectCreateDto.name = PROJECT_NAME
            projectCreateDto.architectId = ARCHITECT_ID
            projectCreateDto.clientId = null
            projectCreateDto.projectType = PROJECT_TYPE_CONCEPT
        when:
            this.projectValidator.validateProjectBasicDto(projectCreateDto)
        then:
            Exception ex = thrown()
            ex.message == "isNull.project.client"
    }

    def "passing projectCreateDto with null as a architectId to validateProjectBasicDto should thrown an exception"() {
        given:
            ProjectCreateDto projectCreateDto = new ProjectCreateDto()
            projectCreateDto.name = PROJECT_NAME
            projectCreateDto.architectId = null
            projectCreateDto.clientId = CLIENT_ID
            projectCreateDto.projectType = PROJECT_TYPE_CONCEPT
        when:
            this.projectValidator.validateProjectBasicDto(projectCreateDto)
        then:
            Exception ex = thrown()
            ex.message == "isNull.project.architect"
    }

    def "passing projectCreateDto with null as a projectType to validateProjectBasicDto should thrown an exception"() {
        given:
            ProjectCreateDto projectCreateDto = new ProjectCreateDto()
            projectCreateDto.name = PROJECT_NAME
            projectCreateDto.architectId = ARCHITECT_ID
            projectCreateDto.clientId = CLIENT_ID
            projectCreateDto.projectType = null
        when:
            this.projectValidator.validateProjectBasicDto(projectCreateDto)
        then:
            Exception ex = thrown()
            ex.message == "isNull.project.type"
    }

    def "passing proper projectCreateDto to validateProjectBasicDto should not throw any exception"() {
        given:
            ProjectCreateDto projectCreateDto = new ProjectCreateDto()
            projectCreateDto.name = PROJECT_NAME
            projectCreateDto.architectId = ARCHITECT_ID
            projectCreateDto.clientId = CLIENT_ID
            projectCreateDto.projectType = PROJECT_TYPE_CONCEPT
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

    def "when passing null as projectContractDto validateProjectContractDto should throw an exception"() {
        given:
            ProjectContractDto projectContractDto = null
        when:
            this.projectValidator.validateProjectContractDto(projectContractDto)
        then:
            Exception ex = thrown()
            ex.message == "isNull.contract"
    }

    def "when signing date not present in projectContractDto, validateProjectContractDto should throw an exception"() {
        given:
            ProjectContractDto projectContractDto = new ProjectContractDto()
            projectContractDto.deadline = DEADLINE
            projectContractDto.startDate = START_DATE
        when:
            this.projectValidator.validateProjectContractDto(projectContractDto)
        then:
            Exception ex = thrown()
            ex.message == "isNull.project.signingDate"
    }

    def "when end date not present in projectContractDto, validateProjectContractDto should throw an exception"() {
        given:
            ProjectContractDto projectContractDto = new ProjectContractDto()
            projectContractDto.deadline = DEADLINE
            projectContractDto.signingDate = SIGNING_DATE
            projectContractDto.startDate = null
        when:
            this.projectValidator.validateProjectContractDto(projectContractDto)
        then:
            Exception ex = thrown()
            ex.message == "isNull.project.startDate"
    }

    def "when deadline date not present in projectContractDto, validateProjectContractDto should throw an exception"() {
        given:
            ProjectContractDto projectContractDto = new ProjectContractDto()
            projectContractDto.deadline = null
            projectContractDto.signingDate = SIGNING_DATE
            projectContractDto.startDate = START_DATE
        when:
            this.projectValidator.validateProjectContractDto(projectContractDto)
        then:
            Exception ex = thrown()
            ex.message == "isNull.project.deadline"
    }
}
