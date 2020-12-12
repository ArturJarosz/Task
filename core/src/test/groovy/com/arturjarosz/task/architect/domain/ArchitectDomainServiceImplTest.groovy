package com.arturjarosz.task.architect.domain

import com.arturjarosz.task.architect.application.dto.ArchitectBasicDto
import com.arturjarosz.task.architect.application.dto.ArchitectDto
import com.arturjarosz.task.architect.infrastructure.repository.impl.ArchitectRepositoryImpl
import com.arturjarosz.task.architect.model.Architect
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException
import spock.lang.Shared
import spock.lang.Specification

class ArchitectDomainServiceImplTest extends Specification {

    private static final String FIRST_NAME = "firstName";
    private static final String NEW_FIRST_NAME = "newFirstName";
    private static final String LAST_NAME = "lastName";
    private static final String NEW_LAST_NAME = "newLastName";
    private static final Long EXISTING_ID = 1L;
    private static final Long EXISTING_ID2 = 12L;
    private static final Long NON_EXISTING_ID = 999L;

    @Shared
    private static final Architect ARCHITECT = new Architect(FIRST_NAME, LAST_NAME);
    private static final Architect ANOTHER_ARCHITECT = new Architect(NEW_FIRST_NAME, NEW_LAST_NAME);

    def architectRepository = Mock(ArchitectRepositoryImpl) {
        load(NON_EXISTING_ID) >> { null };
        load(EXISTING_ID) >> { ARCHITECT };
        load(EXISTING_ID2) >> { ANOTHER_ARCHITECT };
        loadAll() >> { [ARCHITECT, ANOTHER_ARCHITECT] }
        remove(EXISTING_ID) >> {};
        remove(NON_EXISTING_ID) >> { throw new IllegalArgumentException() }
    }

    def architectDomainService = new ArchitectDomainServiceImpl(architectRepository);

    def architectValidator = Stub(ArchitectValidator) {
        validateArchitectDto(null) >> { throw new IllegalArgumentException() }
        validateBasicArchitectDto(null) >> { throw new IllegalArgumentException() }
    }

    def "when passing null an exception should be thrown and architect should not be saved"() {
        given:
        ArchitectBasicDto architectBasicDto = null;
        when:
        architectDomainService.createArchitect(architectBasicDto);
        then:
        thrown(IllegalArgumentException);
        0 * architectRepository.save(_)
    }

    def "when passing architect with missing data exception should be thrown and architect should be not saved"() {
        given:
        ArchitectBasicDto architectBasicDto = new ArchitectBasicDto(null, FIRST_NAME, "");
        when:
        architectDomainService.createArchitect(architectBasicDto);
        then:
        thrown(IllegalArgumentException);
        0 * architectRepository.save(_)
    }

    def "when passing proper architect data no exception should be thrown and architect should be saved"() {
        given:
        ArchitectBasicDto architectBasicDto = new ArchitectBasicDto(null, FIRST_NAME, LAST_NAME);
        when:
        architectDomainService.createArchitect(architectBasicDto);
        then:
        noExceptionThrown();
        1 * architectRepository.save({
            Architect architect ->
                architect.getPersonName().getFirstName() == FIRST_NAME;
                architect.getPersonName().getLastName() == LAST_NAME;
        })
    }

    def "when passing non existing architect id removeArchitect should throw an exception"() {
        given:
        when:
        architectDomainService.removeArchitect(NON_EXISTING_ID);
        then:
        thrown(IllegalArgumentException);
    }

    def "when passing existing architect id removeArchitect no should throw an exception and architect should be deleted"() {
        given:
        when:
        architectDomainService.removeArchitect(EXISTING_ID);
        then:
        noExceptionThrown();
        1 * architectRepository.remove(EXISTING_ID);
    }

    def "when passing existing architect id getArchitect should return architect"() {
        given:
        when:
        ArchitectDto architectDto = architectDomainService.getArchitect(EXISTING_ID);
        then:
        architectDto.getFirstName() == FIRST_NAME;
        architectDto.getLastName() == LAST_NAME;
    }

    def "when passing non existing architect id getArchitect should return not architect and exception should be thrown"() {
        given:
        when:
        ArchitectDto architectDto = architectDomainService.getArchitect(NON_EXISTING_ID);
        then:
        thrown(IllegalArgumentException)
        architectDto == null;
    }

    def "getArchitects should get list of architects"() {
        given:
        when:
        ArchitectBasicDto[] architectBasicDtos = architectDomainService.getArchitects();
        then:
        architectBasicDtos.length == 2;
    }

    def "when updating non existing architect an exception should be thrown"() {
        given:
        ArchitectDto architectDto = new ArchitectDto(NEW_FIRST_NAME, NEW_LAST_NAME, null);
        when:
        architectDomainService.updateArchitect(NON_EXISTING_ID, architectDto);
        then:
        thrown(IllegalArgumentException)
    }

    def "when updating architect with dto with missing data architect should not be updated"() {
        given:
        ArchitectDto architectDto = new ArchitectDto(NEW_FIRST_NAME, null, null);
        when:
        architectDomainService.updateArchitect(EXISTING_ID, architectDto);
        then:
        thrown(IllegalArgumentException)
    }

    def "when updating architect with correct data no exception should be thrown and architect should be updated"() {
        given:
        ArchitectDto architectDto = new ArchitectDto(NEW_FIRST_NAME, NEW_LAST_NAME, null);
        when:
        architectDomainService.updateArchitect(EXISTING_ID, architectDto);
        then:
        noExceptionThrown();
        1 * architectRepository.save({
            Architect architect ->
                architect.getPersonName().getFirstName() == NEW_FIRST_NAME;
                architect.getPersonName().getLastName() == NEW_LAST_NAME
        })
    }


}
