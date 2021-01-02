package com.arturjarosz.task.architect.application

import com.arturjarosz.task.architect.application.ArchitectValidator
import com.arturjarosz.task.architect.application.dto.ArchitectBasicDto
import com.arturjarosz.task.architect.application.dto.ArchitectDto
import com.arturjarosz.task.architect.infrastructure.repository.impl.ArchitectRepositoryImpl
import com.arturjarosz.task.architect.model.Architect
import spock.lang.Shared
import spock.lang.Specification

class ArchitectValidatorTest extends Specification {

    private static final String FIRST_NAME = "first";
    private static final String LAST_NAME = "last";
    private static final Long EXISTING_ID = 1L;
    private static final Long NON_EXISTING_ID = 999L;
    @Shared
    private static final Architect ARCHITECT = new Architect(FIRST_NAME, LAST_NAME);

    def architectRepository = Mock(ArchitectRepositoryImpl) {
        load(NON_EXISTING_ID) >> { null };
        load(EXISTING_ID) >> { ARCHITECT };
    }

    def architectValidator = new ArchitectValidator(architectRepository);

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
            ArchitectBasicDto architectBasicDto = new ArchitectBasicDto(null, null, LAST_NAME);
        when:
            ArchitectValidator.validateBasicArchitectDto(architectBasicDto);
        then:
            Exception exception = thrown();
            exception.message == "isNull.architect.firstName";
    }

    def "when passing architect without lastName field architectBasicDtoValidator should throw an exception with specific error message"() {
        given:
            ArchitectBasicDto architectBasicDto = new ArchitectBasicDto(null, FIRST_NAME, null);
        when:
            ArchitectValidator.validateBasicArchitectDto(architectBasicDto);
        then:
            Exception exception = thrown();
            exception.message == "isNull.architect.lastName";
    }

    def "when passing architect with empty firstName field architectBasicDtoValidator should throw an exception with specific error message"() {
        given:
            ArchitectBasicDto architectBasicDto = new ArchitectBasicDto(null, "", LAST_NAME);
        when:
            ArchitectValidator.validateBasicArchitectDto(architectBasicDto);
        then:
            Exception exception = thrown();
            exception.message == "isEmpty.architect.firstName";
    }

    def "when passing architect with empty lastName field architectBasicDtoValidator should throw an exception with specific error message"() {
        given:
            ArchitectBasicDto architectBasicDto = new ArchitectBasicDto(null, FIRST_NAME, "");
        when:
            ArchitectValidator.validateBasicArchitectDto(architectBasicDto);
        then:
            Exception exception = thrown();
            exception.message == "isEmpty.architect.lastName";
    }

    def "when passing proper data architectBasicDtoValidator should not throw any exception"() {
        given:
            ArchitectBasicDto architectBasicDto = new ArchitectBasicDto(null, FIRST_NAME, LAST_NAME);
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
            ArchitectDto architectDto = new ArchitectDto(null, LAST_NAME, null);
        when:
            ArchitectValidator.validateArchitectDto(architectDto);
        then:
            Exception exception = thrown();
            exception.message == "isNull.architect.firstName";
    }

    def "when passing architect without lastName field architectDtoValidator should throw an exception with specific error message"() {
        given:
            ArchitectDto architectDto = new ArchitectDto(FIRST_NAME, null, null);
        when:
            ArchitectValidator.validateArchitectDto(architectDto);
        then:
            Exception exception = thrown();
            exception.message == "isNull.architect.lastName";
    }

    def "when passing architect with empty firstName field architectDtoValidator should throw an exception with specific error message"() {
        given:
            ArchitectDto architectDto = new ArchitectDto("", LAST_NAME, null);
        when:
            ArchitectValidator.validateArchitectDto(architectDto);
        then:
            Exception exception = thrown();
            exception.message == "isEmpty.architect.firstName";
    }

    def "when passing architect with empty lastName field architectDtoValidator should throw an exception with specific error message"() {
        given:
            ArchitectDto architectDto = new ArchitectDto(FIRST_NAME, "", null);
        when:
            ArchitectValidator.validateArchitectDto(architectDto);
        then:
            Exception exception = thrown();
            exception.message == "isEmpty.architect.lastName";
    }

    def "when passing proper data architectDtoValidator should not throw any exception"() {
        given:
            ArchitectDto architectDto = new ArchitectDto(FIRST_NAME, LAST_NAME, null);
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
}
