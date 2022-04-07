package com.arturjarosz.task.architect.application

import com.arturjarosz.task.architect.application.dto.ArchitectBasicDto
import com.arturjarosz.task.architect.application.dto.ArchitectDto
import com.arturjarosz.task.architect.application.impl.ArchitectApplicationServiceImpl
import com.arturjarosz.task.architect.infrastructure.repository.impl.ArchitectRepositoryImpl
import com.arturjarosz.task.architect.model.Architect
import com.arturjarosz.task.project.query.impl.ProjectQueryServiceImpl
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException
import spock.lang.Specification

import java.lang.reflect.Field

class ArchitectApplicationServiceImplTest extends Specification {

    private static final String FIRST_NAME = "firstName"
    private static final String NEW_FIRST_NAME = "newFirstName"
    private static final String LAST_NAME = "lastName"
    private static final String NEW_LAST_NAME = "newLastName"
    private static final Long EXISTING_ID = 1L
    private static final Long EXISTING_ID2 = 12L
    private static final Long NOT_EXISTING_ID = 999L

    private static final Architect ARCHITECT_ONE = new Architect(FIRST_NAME, LAST_NAME)
    private static final Architect ANOTHER_ARCHITECT = new Architect(NEW_FIRST_NAME, NEW_LAST_NAME)

    def architectRepository = Mock(ArchitectRepositoryImpl) {
        load(NOT_EXISTING_ID) >> { null }
        load(EXISTING_ID) >> { ARCHITECT_ONE }
        load(EXISTING_ID2) >> { ANOTHER_ARCHITECT }
        loadAll() >> { [ARCHITECT_ONE, ANOTHER_ARCHITECT] }
        remove(EXISTING_ID) >> {}
        remove(NOT_EXISTING_ID) >> { throw new IllegalArgumentException() }
        save(_ as Architect) >> {
            ARCHITECT_ONE
            Field field = Architect.superclass.superclass.getDeclaredField("id")
            field.setAccessible(true)
            field.set(ARCHITECT_ONE, EXISTING_ID)
            return ARCHITECT_ONE
        }
    }

    def projectQueryService = Mock(ProjectQueryServiceImpl) {
        getProjectsForArchitect(_ as Long) >> { Collections.emptyList() }
    }

    def architectValidator = new ArchitectValidator(architectRepository, projectQueryService)

    def architectApplicationService = new ArchitectApplicationServiceImpl(architectRepository, architectValidator)

    def "when passing null an exception should be thrown and architect should not be saved"() {
        given:
            ArchitectBasicDto architectBasicDto = null
        when:
            architectApplicationService.createArchitect(architectBasicDto)
        then:
            thrown(IllegalArgumentException)
            0 * architectRepository.save(_)
    }

    def "when passing architect with missing data exception should be thrown and architect should be not saved"() {
        given:
            ArchitectBasicDto architectBasicDto = new ArchitectBasicDto(firstName: FIRST_NAME, lastName: "")
        when:
            architectApplicationService.createArchitect(architectBasicDto)
        then:
            thrown(IllegalArgumentException)
            0 * architectRepository.save(_)
    }

    def "when passing proper architect data no exception should be thrown and architect should be saved"() {
        given:
            ArchitectBasicDto architectBasicDto = new ArchitectBasicDto(firstName: FIRST_NAME, lastName: LAST_NAME)
        when:
            architectApplicationService.createArchitect(architectBasicDto)
        then:
            noExceptionThrown()
            1 * architectRepository.save(_)

    }

    def "when passing non existing architect id removeArchitect should throw an exception"() {
        given:
        when:
            architectApplicationService.removeArchitect(NOT_EXISTING_ID)
        then:
            thrown(IllegalArgumentException)
    }

    def "when passing existing architect id removeArchitect no should throw an exception and architect should be removed"() {
        given:
        when:
            architectApplicationService.removeArchitect(EXISTING_ID)
        then:
            noExceptionThrown()
            1 * architectRepository.remove(EXISTING_ID)
    }

    def "when passing existing architect id getArchitect should return architect"() {
        given:
        when:
            ArchitectDto architectDto = architectApplicationService.getArchitect(EXISTING_ID)
        then:
            architectDto.firstName == FIRST_NAME
            architectDto.lastName == LAST_NAME
    }

    def "when passing non existing architect id getArchitect should return not architect and exception should be thrown"() {
        given:
        when:
            ArchitectDto architectDto = architectApplicationService.getArchitect(NOT_EXISTING_ID)
        then:
            thrown(IllegalArgumentException)
            architectDto == null
    }

    def "getArchitects should get list of architects"() {
        given:
        when:
            List<ArchitectBasicDto> architectBasicDtos = architectApplicationService.getBasicArchitects()
        then:
            architectBasicDtos.size() == 2
    }

    def "when updating non existing architect an exception should be thrown"() {
        given:
            ArchitectDto architectDto = new ArchitectDto(firstName: NEW_FIRST_NAME, lastName: NEW_LAST_NAME)
        when:
            architectApplicationService.updateArchitect(NOT_EXISTING_ID, architectDto)
        then:
            thrown(IllegalArgumentException)
    }

    def "when updating architect with dto with missing data architect should not be updated"() {
        given:
            ArchitectDto architectDto = new ArchitectDto(firstName: NEW_FIRST_NAME)
        when:
            architectApplicationService.updateArchitect(EXISTING_ID, architectDto)
        then:
            thrown(IllegalArgumentException)
    }

    def "when updating architect with correct data no exception should be thrown and architect should be updated"() {
        given:
            ArchitectDto architectDto = new ArchitectDto(firstName: NEW_FIRST_NAME, lastName: NEW_LAST_NAME)
        when:
            architectApplicationService.updateArchitect(EXISTING_ID, architectDto)
        then:
            noExceptionThrown()
            1 * architectRepository.save({
                Architect architect ->
                    architect.personName.firstName == NEW_FIRST_NAME
                    architect.personName.lastName == NEW_LAST_NAME
            })
    }
}
