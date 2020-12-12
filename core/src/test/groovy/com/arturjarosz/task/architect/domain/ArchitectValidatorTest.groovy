package com.arturjarosz.task.architect.domain

import com.arturjarosz.task.architect.application.dto.ArchitectBasicDto
import com.arturjarosz.task.architect.application.dto.ArchitectDto
import spock.lang.Specification

class ArchitectValidatorTest extends Specification {

    private static final String FIRST_NAME = "first";
    private static final String LAST_NAME = "last";

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
}
