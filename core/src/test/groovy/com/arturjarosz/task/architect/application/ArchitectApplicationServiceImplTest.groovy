package com.arturjarosz.task.architect.application

import com.arturjarosz.task.project.query.TimeEntryQueryService
import com.arturjarosz.task.architect.application.impl.ArchitectApplicationServiceImpl
import com.arturjarosz.task.architect.application.mapper.ArchitectMapperImpl
import com.arturjarosz.task.architect.infrastructure.repository.ArchitectRepository
import com.arturjarosz.task.architect.model.Architect
import com.arturjarosz.task.contract.query.ContractQueryService
import com.arturjarosz.task.dto.ArchitectDto
import com.arturjarosz.task.dto.ArchitectUserLinkDto
import com.arturjarosz.task.dto.UserSummaryDto
import com.arturjarosz.task.project.application.mapper.ProjectsToEntityProjectsSummaryDtoMapperImpl
import com.arturjarosz.task.project.model.Project
import com.arturjarosz.task.project.query.ProjectCounts
import com.arturjarosz.task.project.query.impl.ProjectQueryServiceImpl
import com.arturjarosz.task.project.status.project.ProjectStatus
import com.arturjarosz.task.project.status.project.ProjectWorkflow
import com.arturjarosz.task.properties.ProjectProperties
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException
import com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException
import com.arturjarosz.task.sharedkernel.testhelpers.TestUtils
import com.arturjarosz.task.user.application.UserApplicationService
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Sort
import spock.lang.Specification

import java.lang.reflect.Field

class ArchitectApplicationServiceImplTest extends Specification {

    static final FIRST_NAME = "firstName"
    static final NEW_FIRST_NAME = "newFirstName"
    static final LAST_NAME = "lastName"
    static final NEW_LAST_NAME = "newLastName"
    static final EXISTING_ID = 1L
    static final EXISTING_ID2 = 12L
    static final NOT_EXISTING_ID = 999L
    static final PROJECT_ID = 200L
    static final LINKABLE_ID = 3L
    static final LINKED_ID = 4L
    static final USER_ID = 300L
    static final OTHER_USER_ID = 301L
    static final FREE_USER_ID = 302L
    static final NOT_EXISTING_USER_ID = 999L
    static final USER_SUMMARY = new UserSummaryDto(id: USER_ID, username: "user", email: "user@example.com")

    static final ARCHITECT_ONE = new Architect(FIRST_NAME, LAST_NAME)
    static final ANOTHER_ARCHITECT = new Architect(NEW_FIRST_NAME, NEW_LAST_NAME)

    def architectRepository = Mock(ArchitectRepository) {
        findById(NOT_EXISTING_ID) >> { Optional.ofNullable(null) }
        findById(EXISTING_ID) >> { Optional.ofNullable(ARCHITECT_ONE) }
        findById(EXISTING_ID2) >> { Optional.ofNullable(ANOTHER_ARCHITECT) }
        findById(LINKABLE_ID) >> { Optional.of(new Architect(FIRST_NAME, LAST_NAME)) }
        findById(LINKED_ID) >> {
            def architect = new Architect(FIRST_NAME, LAST_NAME)
            architect.linkUser(USER_ID)
            return Optional.of(architect)
        }
        existsByUserIdAndIdNot(OTHER_USER_ID, LINKABLE_ID) >> true
        saveAndFlush(_ as Architect) >> { Architect architect -> architect }
        findAll(Sort.by("id")) >> { [ARCHITECT_ONE, ANOTHER_ARCHITECT] }
        deleteById(EXISTING_ID) >> {}
        deleteById(NOT_EXISTING_ID) >> { throw new IllegalArgumentException() }
        save(_ as Architect) >> {
            ARCHITECT_ONE
            Field field = Architect.superclass.superclass.getDeclaredField("id")
            field.setAccessible(true)
            field.set(ARCHITECT_ONE, EXISTING_ID)
            return ARCHITECT_ONE
        }
    }

    static final ACTIVE_STATUSES = [ProjectStatus.TO_DO, ProjectStatus.IN_PROGRESS] as Set

    def projectQueryService = Mock(ProjectQueryServiceImpl) {
        getProjectCountsByArchitectIds(_, _) >> [:]
    }
    def architectValidator = new ArchitectValidator(architectRepository, projectQueryService, Mock(TimeEntryQueryService))
    def architectMapper = new ArchitectMapperImpl()
    def contractQueryService = Mock(ContractQueryService) {
        getProjectsValueByArchitectIds(_) >> [:]
    }
    def projectsToEntityProjectsSummaryDtoMapper = new ProjectsToEntityProjectsSummaryDtoMapperImpl()

    def projectProperties = new ProjectProperties(ACTIVE_STATUSES)

    def userApplicationService = Mock(UserApplicationService) {
        getUserSummaries([USER_ID]) >> [(USER_ID): USER_SUMMARY]
        getUserSummaries([OTHER_USER_ID]) >> [(OTHER_USER_ID): new UserSummaryDto(id: OTHER_USER_ID)]
        getUserSummaries([FREE_USER_ID]) >> [(FREE_USER_ID): new UserSummaryDto(id: FREE_USER_ID)]
        getUserSummaries(_) >> [:]
    }

    def subject = new ArchitectApplicationServiceImpl(architectRepository, architectValidator,
            architectMapper, projectQueryService, contractQueryService, projectsToEntityProjectsSummaryDtoMapper,
            projectProperties, userApplicationService)

    def "when passing null an exception should be thrown and architect should not be saved"() {
        given:
            ArchitectDto architectDto = null
        when:
            subject.createArchitect(architectDto)
        then:
            thrown(IllegalArgumentException)
            0 * architectRepository.save(_)
    }

    def "when passing architect with missing data exception should be thrown and architect should be not saved"() {
        given:
            ArchitectDto architectDto = new ArchitectDto(firstName: FIRST_NAME, lastName: "")
        when:
            subject.createArchitect(architectDto)
        then:
            thrown(IllegalArgumentException)
            0 * architectRepository.save(_)
    }

    def "when passing proper architect data no exception should be thrown and architect should be saved"() {
        given:
            ArchitectDto architectDto = new ArchitectDto(firstName: FIRST_NAME, lastName: LAST_NAME)
        when:
            subject.createArchitect(architectDto)
        then:
            noExceptionThrown()
            1 * architectRepository.save(_)

    }

    def "when passing non existing architect id removeArchitect should throw an exception"() {
        given:
        when:
            subject.removeArchitect(NOT_EXISTING_ID)
        then:
            thrown(ResourceNotFoundException)
    }

    def "when passing existing architect id removeArchitect no should throw an exception and architect should be removed"() {
        given:
            projectQueryService.getProjectsForArchitect(_ as Long) >> []
        when:
            subject.removeArchitect(EXISTING_ID)
        then:
            noExceptionThrown()
            1 * architectRepository.deleteById(EXISTING_ID)
    }

    def "when passing existing architect id getArchitect should return architect"() {
        given:
        when:
            def architectDto = subject.getArchitect(EXISTING_ID)
        then:
            architectDto.firstName == FIRST_NAME
            architectDto.lastName == LAST_NAME
    }

    def "when passing non existing architect id getArchitect should return not architect and exception should be thrown"() {
        given:
        when:
            def architectDto = subject.getArchitect(NOT_EXISTING_ID)
        then:
            thrown(ResourceNotFoundException)
            architectDto == null
    }

    def "getArchitects should get list of architects"() {
        given:
        when:
            List<ArchitectDto> architectDtos = subject.getArchitects()
        then:
            architectDtos.size() == 2
    }

    def "getArchitect should return number of all and active projects counted for configured active statuses"() {
        given:
            def architectId = ARCHITECT_ONE.id
        when:
            def architectDto = subject.getArchitect(EXISTING_ID)
        then:
            1 * projectQueryService.getProjectCountsByArchitectIds([architectId], ACTIVE_STATUSES) >>
                    [(architectId): new ProjectCounts(3, 2)]
            1 * contractQueryService.getProjectsValueByArchitectIds([architectId]) >>
                    [(architectId): new BigDecimal("1250.50")]
            architectDto.numberOfProjects == 3
            architectDto.numberOfActiveProjects == 2
            architectDto.projectsValue == 1250.50d
    }

    def "getArchitect should not load or return the linked user"() {
        when:
            def result = subject.getArchitect(LINKED_ID)
        then:
            result.linkedUser == null
            0 * userApplicationService.getUserSummaries(_)
    }

    def "getArchitectWithLinkedUser should return the linked user"() {
        when:
            def result = subject.getArchitectWithLinkedUser(LINKED_ID)
        then:
            result.linkedUser == USER_SUMMARY
    }

    def "getArchitects should return zero projects for architects without projects"() {
        given:
        when:
            List<ArchitectDto> architectDtos = subject.getArchitects()
        then:
            1 * projectQueryService.getProjectCountsByArchitectIds(_, ACTIVE_STATUSES) >> [:]
            architectDtos.every { it.numberOfProjects == 0 && it.numberOfActiveProjects == 0 && it.projectsValue == 0d }
    }

    def "getArchitectsBasicData should load architects in one query and return them by id without projects data"() {
        given:
            def architect = new Architect(FIRST_NAME, LAST_NAME)
            TestUtils.setFieldForObject(architect, "id", EXISTING_ID)
        when:
            def architectsById = subject.getArchitectsBasicData([EXISTING_ID, null])
        then:
            1 * architectRepository.findAllById([EXISTING_ID]) >> [architect]
            0 * projectQueryService._
            0 * contractQueryService._
            architectsById.keySet() == [EXISTING_ID] as Set
            architectsById[EXISTING_ID].firstName == FIRST_NAME
    }

    def "getArchitectsBasicData should not query repository for empty ids"() {
        when:
            def architectsById = subject.getArchitectsBasicData([])
        then:
            0 * architectRepository.findAllById(_)
            architectsById.isEmpty()
    }

    def "when updating non existing architect an exception should be thrown"() {
        given:
            def architectDto = new ArchitectDto(firstName: NEW_FIRST_NAME, lastName: NEW_LAST_NAME)
        when:
            subject.updateArchitect(NOT_EXISTING_ID, architectDto)
        then:
            thrown(ResourceNotFoundException)
    }

    def "when updating architect with dto with missing data architect should not be updated"() {
        given:
            def architectDto = new ArchitectDto(firstName: NEW_FIRST_NAME)
        when:
            subject.updateArchitect(EXISTING_ID, architectDto)
        then:
            thrown(IllegalArgumentException)
    }

    def "when updating architect with correct data no exception should be thrown and architect should be updated"() {
        given:
            def architectDto = new ArchitectDto(firstName: NEW_FIRST_NAME, lastName: NEW_LAST_NAME)
        when:
            subject.updateArchitect(EXISTING_ID, architectDto)
        then:
            noExceptionThrown()
            1 * architectRepository.save({ Architect architect ->
                architect.personName.firstName == NEW_FIRST_NAME
                architect.personName.lastName == NEW_LAST_NAME
            }) >> { Architect architect -> architect }
    }

    def "when getting project summary for architect exception should be thrown if architect does not exist"() {
        given:
        when:
            def result = subject.getProjectsSummary(NOT_EXISTING_ID)
        then:
            thrown(ResourceNotFoundException)
            result == null
    }

    def "when getting project summary for existing architect it should be returned"() {
        given:
            mockGetContractValuesForProjects()
        when:
            def result = subject.getProjectsSummary(EXISTING_ID)
        then:
            noExceptionThrown()
            result != null
    }

    def "linkUser should link the user with the architect and return the architect with the user"() {
        when:
            def result = subject.linkUser(LINKABLE_ID, new ArchitectUserLinkDto(userId: USER_ID))
        then:
            1 * architectRepository.saveAndFlush({ Architect architect -> architect.userId == USER_ID }) >> {
                Architect architect -> architect
            }
            result.linkedUser == USER_SUMMARY
    }

    def "linkUser should replace the user linked so far"() {
        when:
            subject.linkUser(LINKED_ID, new ArchitectUserLinkDto(userId: FREE_USER_ID))
        then:
            1 * architectRepository.saveAndFlush({ Architect architect -> architect.userId == FREE_USER_ID }) >> {
                Architect architect -> architect
            }
    }

    def "linkUser should throw an exception and not save when user is not provided"() {
        when:
            subject.linkUser(LINKABLE_ID, new ArchitectUserLinkDto())
        then:
            Exception exception = thrown(IllegalArgumentException)
            exception.message == "isNull.architect.user"
            0 * architectRepository.saveAndFlush(_)
    }

    def "linkUser should throw an exception and not save when the architect does not exist"() {
        when:
            subject.linkUser(NOT_EXISTING_ID, new ArchitectUserLinkDto(userId: USER_ID))
        then:
            thrown(ResourceNotFoundException)
            0 * architectRepository.saveAndFlush(_)
    }

    def "linkUser should throw an exception and not save when the user does not exist"() {
        when:
            subject.linkUser(LINKABLE_ID, new ArchitectUserLinkDto(userId: NOT_EXISTING_USER_ID))
        then:
            Exception exception = thrown(ResourceNotFoundException)
            exception.message == "notExist.architect.user"
            0 * architectRepository.saveAndFlush(_)
    }

    def "linkUser should throw an exception and not save when the user is linked with another architect"() {
        when:
            subject.linkUser(LINKABLE_ID, new ArchitectUserLinkDto(userId: OTHER_USER_ID))
        then:
            Exception exception = thrown(IllegalArgumentException)
            exception.message == "alreadySet.architect.user"
            0 * architectRepository.saveAndFlush(_)
    }

    def "linkUser should throw the same exception when the unique constraint rejects the user"() {
        when:
            subject.linkUser(LINKABLE_ID, new ArchitectUserLinkDto(userId: USER_ID))
        then:
            1 * architectRepository.saveAndFlush(_ as Architect) >> {
                throw new DataIntegrityViolationException("duplicate")
            }
            Exception exception = thrown(IllegalArgumentException)
            exception.message == "alreadySet.architect.user"
    }

    def "unlinkUser should remove the link and return the architect without a user"() {
        when:
            def result = subject.unlinkUser(LINKED_ID)
        then:
            1 * architectRepository.save({ Architect architect -> architect.userId == null }) >> {
                Architect architect -> architect
            }
            result.linkedUser == null
    }

    def "unlinkUser should throw an exception when the architect does not exist"() {
        when:
            subject.unlinkUser(NOT_EXISTING_ID)
        then:
            thrown(ResourceNotFoundException)
    }

    void mockGetContractValuesForProjects() {
        var project = new Project("name", EXISTING_ID, 101L, 1L, new ProjectWorkflow(), 102L)
        TestUtils.setFieldForObject(project, "id", PROJECT_ID)
        projectQueryService.getProjectsForArchitect(_ as Long) >> [project]
        this.contractQueryService.getContractValuesForProjects(_ as List) >> [(PROJECT_ID): BigDecimal.valueOf(1.0D)]
    }
}
