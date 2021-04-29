package com.arturjarosz.task.architect.application

import com.arturjarosz.task.architect.application.dto.ArchitectBasicDto
import com.arturjarosz.task.architect.application.dto.ArchitectDto
import com.arturjarosz.task.architect.infrastructure.repository.impl.ArchitectRepositoryImpl
import com.arturjarosz.task.architect.model.Architect
import com.arturjarosz.task.project.model.Project
import com.arturjarosz.task.project.query.impl.ProjectQueryServiceImpl
import com.arturjarosz.task.project.utils.ProjectBuilder
import spock.lang.Specification

class ArchitectValidatorTest extends Specification {

    private static final String FIRST_NAME = "first";
    private static final String LAST_NAME = "last";
    private static final Long EXISTING_ID = 1L;
    private static final Long NON_EXISTING_ID = 999L;
    private static final Long ARCHITECT_WITH_PROJECTS = 10L;
    private static final Long ARCHITECT_WITHOUT_PROJECTS = 888L;

    private static Architect architect = new Architect(FIRST_NAME, LAST_NAME);

    private static Project project = new ProjectBuilder().build();

    def architectRepository = Mock(ArchitectRepositoryImpl) {
        load(NON_EXISTING_ID) >> { null };
        load(EXISTING_ID) >> { architect };
    }

    def projectQueryService = Mock(ProjectQueryServiceImpl) {
        getProjectsForArchitect(ARCHITECT_WITH_PROJECTS) >> { Arrays.asList(project) };
        getProjectsForArchitect(ARCHITECT_WITHOUT_PROJECTS) >> { Collections.emptyList() }
    }

    def architectValidator = new ArchitectValidator(architectRepository, projectQueryService);

    def "when passing null architectBasicDtoValidator should throw an exception with specific error message"() {
        given:
            ArchitectBasicDto architectBasicDto = null;
        when:
            ArchitectValidator.validateBasicArchitectDto(architectBasicDto);
        then:
            Exception exception = thrown();
            exception.message == "isNull.architect";
    }

    def "when passing architect without firstName field architectBasicDtoValidator should throw an exception with specific error message"() {
        given:
            ArchitectBasicDto architectBasicDto = new ArchitectBasicDto();
            architectBasicDto.setLastName(LAST_NAME);
        when:
            ArchitectValidator.validateBasicArchitectDto(architectBasicDto);
        then:
            Exception exception = thrown();
            exception.message == "isNull.architect.firstName";
    }

    def "when passing architect without lastName field architectBasicDtoValidator should throw an exception with specific error message"() {
        given:
            ArchitectBasicDto architectBasicDto = new ArchitectBasicDto();
            architectBasicDto.setFirstName(FIRST_NAME);
        when:
            ArchitectValidator.validateBasicArchitectDto(architectBasicDto);
        then:
            Exception exception = thrown();
            exception.message == "isNull.architect.lastName";
    }

    def "when passing architect with empty firstName field architectBasicDtoValidator should throw an exception with specific error message"() {
        given:
            ArchitectBasicDto architectBasicDto = new ArchitectBasicDto();
            architectBasicDto.setFirstName("");
            architectBasicDto.setLastName(LAST_NAME);
        when:
            ArchitectValidator.validateBasicArchitectDto(architectBasicDto);
        then:
            Exception exception = thrown();
            exception.message == "isEmpty.architect.firstName";
    }

    def "when passing architect with empty lastName field architectBasicDtoValidator should throw an exception with specific error message"() {
        given:
            ArchitectBasicDto architectBasicDto = new ArchitectBasicDto();
            architectBasicDto.setFirstName(FIRST_NAME);
            architectBasicDto.setLastName("");
        when:
            ArchitectValidator.validateBasicArchitectDto(architectBasicDto);
        then:
            Exception exception = thrown();
            exception.message == "isEmpty.architect.lastName";
    }

    def "when passing proper data architectBasicDtoValidator should not throw any exception"() {
        given:
            ArchitectBasicDto architectBasicDto = new ArchitectBasicDto();
            architectBasicDto.setFirstName(FIRST_NAME);
            architectBasicDto.setLastName(LAST_NAME);
        when:
            ArchitectValidator.validateBasicArchitectDto(architectBasicDto);
        then:
            noExceptionThrown();
    }

    def "when passing null architectDtoValidator should throw an exception with specific error message"() {
        given:
            ArchitectDto architectDto = null;
        when:
            ArchitectValidator.validateArchitectDto(architectDto);
        then:
            Exception exception = thrown();
            exception.message == "isNull.architect";
    }

    def "when passing architect without firstName field architectDtoValidator should throw an exception with specific error message"() {
        given:
            ArchitectDto architectDto = new ArchitectDto();
            architectDto.setLastName(LAST_NAME);
        when:
            ArchitectValidator.validateArchitectDto(architectDto);
        then:
            Exception exception = thrown();
            exception.message == "isNull.architect.firstName";
    }

    def "when passing architect without lastName field architectDtoValidator should throw an exception with specific error message"() {
        given:
            ArchitectDto architectDto = new ArchitectDto();
            architectDto.setFirstName(FIRST_NAME);
        when:
            ArchitectValidator.validateArchitectDto(architectDto);
        then:
            Exception exception = thrown();
            exception.message == "isNull.architect.lastName";
    }

    def "when passing architect with empty firstName field architectDtoValidator should throw an exception with specific error message"() {
        given:
            ArchitectDto architectDto = new ArchitectDto();
            architectDto.setFirstName("");
            architectDto.setLastName(LAST_NAME);
        when:
            ArchitectValidator.validateArchitectDto(architectDto);
        then:
            Exception exception = thrown();
            exception.message == "isEmpty.architect.firstName";
    }

    def "when passing architect with empty lastName field architectDtoValidator should throw an exception with specific error message"() {
        given:
            ArchitectDto architectDto = new ArchitectDto();
            architectDto.setFirstName(FIRST_NAME);
            architectDto.setLastName("");
        when:
            ArchitectValidator.validateArchitectDto(architectDto);
        then:
            Exception exception = thrown();
            exception.message == "isEmpty.architect.lastName";
    }

    def "when passing proper data architectDtoValidator should not throw any exception"() {
        given:
            ArchitectDto architectDto = new ArchitectDto();
            architectDto.setFirstName(FIRST_NAME);
            architectDto.setLastName(LAST_NAME);
        when:
            ArchitectValidator.validateArchitectDto(architectDto);
        then:
            noExceptionThrown();
    }

    def "when passing non existing architect id validateArchitectExistence should throw an exception with specific error message"() {
        given:
        when:
            this.architectValidator.validateArchitectExistence(NON_EXISTING_ID);
        then:
            Exception exception = thrown();
            exception.message == "notExists.architect";
    }

    def "when passing existing architect id validateArchitectExistence should not throw any exception"() {
        given:
        when:
            this.architectValidator.validateArchitectExistence(EXISTING_ID);
        then:
            noExceptionThrown();
    }

    def "when passing architect id with no Projects validate should not throw an exception"() {
        given:
        when:
            this.architectValidator.validateArchitectHasNoProjects(ARCHITECT_WITHOUT_PROJECTS);
        then:
            noExceptionThrown();
    }

    def "when passing architect id with Projects validate should throw an exception"() {
        given:
        when:
            this.architectValidator.validateArchitectHasNoProjects(ARCHITECT_WITH_PROJECTS);
        then:
            Exception ex = thrown();
            ex.message == "notValid.architect.projects";
    }
}
