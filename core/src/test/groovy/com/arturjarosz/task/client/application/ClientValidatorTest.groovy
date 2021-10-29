package com.arturjarosz.task.client.application

import com.arturjarosz.task.client.application.dto.ClientDto
import com.arturjarosz.task.client.infrastructure.repository.impl.ClientRepositoryImpl
import com.arturjarosz.task.client.model.Client
import com.arturjarosz.task.client.model.ClientType
import com.arturjarosz.task.project.model.Project
import com.arturjarosz.task.project.query.impl.ProjectQueryServiceImpl
import com.arturjarosz.task.project.utils.ProjectBuilder
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException
import com.arturjarosz.task.sharedkernel.model.PersonName
import spock.lang.Specification

class ClientValidatorTest extends Specification {

    private static final Long PRIVATE_CLIENT_ID = 1L;
    private static final Long CORPORATE_CLIENT_ID = 2L;
    private static final Long NON_EXISTING_ID = 999L;
    private static final Long CLIENT_ID_WITHOUT_PROJECTS = 899L;
    private static final Long CLIENT_ID_WITH_PROJECTS = 10L;
    private static final String FIRST_NAME = "first";
    private static final String LAST_NAME = "last";
    private static final String COMPANY_NAME = "company";

    private static Project emptyProject = new ProjectBuilder().build();

    private static Client client = new Client(new PersonName(FIRST_NAME, LAST_NAME), null, ClientType.PRIVATE);

    def clientRepository = Mock(ClientRepositoryImpl) {
        load(NON_EXISTING_ID) >> { null };
        load(PRIVATE_CLIENT_ID) >> { client };
    }

    def projectQueryService = Mock(ProjectQueryServiceImpl) {
        getProjectsForClientId(CLIENT_ID_WITHOUT_PROJECTS) >> { Collections.emptyList() }
        getProjectsForClientId(CLIENT_ID_WITH_PROJECTS) >> { Collections.singletonList(emptyProject) }
    }

    def clientValidator = new ClientValidator(clientRepository, projectQueryService);


    def "Should not throw any exception when private client data provided"() {
        given:
            ClientDto clientDto = new ClientDto();
            clientDto.setFirstName(FIRST_NAME);
            clientDto.setId(PRIVATE_CLIENT_ID);
            clientDto.setLastName(LAST_NAME);
            clientDto.setClientType(ClientType.PRIVATE);
        when:
            this.clientValidator.validateClientBasicDto(clientDto);
        then:
            noExceptionThrown();
    }

    def "Should not throw any exception when corporate client data provided"() {
        given:
            ClientDto clientDto = new ClientDto();
            clientDto.setClientType(ClientType.CORPORATE);
            clientDto.setCompanyName(COMPANY_NAME);
            clientDto.setId(CORPORATE_CLIENT_ID);
        when:
            clientValidator.validateClientBasicDto(clientDto);
        then:
            noExceptionThrown();
    }

    def "Not providing client dto should throw an error with specific message"() {
        given:
            ClientDto clientDto = null;
        when:
            clientValidator.validateClientBasicDto(clientDto);
        then:
            Exception ex = thrown();
            ex.message == "isNull.client";
    }

    def "Should throw an error with specific message when client type not provided"() {
        given:
            ClientDto clientDto = new ClientDto();
            clientDto.setId(CORPORATE_CLIENT_ID);
            clientDto.setCompanyName(COMPANY_NAME);
        when:
            clientValidator.validateClientBasicDto(clientDto);
        then:
            Exception ex = thrown();
            ex.message == "isNull.client.clientType";
    }

    def "Should throw an error with specific message when first name is not provided for private client"() {
        given:
            ClientDto clientDto = new ClientDto();
            clientDto.setClientType(ClientType.PRIVATE);
            clientDto.setId(PRIVATE_CLIENT_ID);
            clientDto.setLastName(LAST_NAME);
        when:
            clientValidator.validateClientBasicDto(clientDto);
        then:
            Exception ex = thrown();
            ex.message == "isNull.client.firstName";
    }

    def "Should throw an error with specific message when last name is not provided for private client"() {
        given:
            ClientDto clientDto = new ClientDto();
            clientDto.setClientType(ClientType.PRIVATE);
            clientDto.setFirstName(FIRST_NAME);
            clientDto.setId(PRIVATE_CLIENT_ID);
        when:
            clientValidator.validateClientBasicDto(clientDto);
        then:
            Exception ex = thrown();
            ex.message == "isNull.client.lastName";
    }

    def "Should throw an error with specific message when company name is not provided for corporate client"() {
        given:
            ClientDto clientDto = new ClientDto();
            clientDto.setClientType(ClientType.CORPORATE);
            clientDto.setId(CORPORATE_CLIENT_ID);
        when:
            clientValidator.validateClientBasicDto(clientDto);
        then:
            Exception ex = thrown();
            ex.message == "isNull.client.companyName";
    }

    def "Should throw an error with specific message when company name is empty for corporate client"() {
        given:
            ClientDto clientDto = new ClientDto();
            clientDto.setClientType(ClientType.CORPORATE);
            clientDto.setCompanyName("");
            clientDto.setId(CORPORATE_CLIENT_ID);
        when:
            clientValidator.validateClientBasicDto(clientDto);
        then:
            Exception ex = thrown();
            ex.message == "isEmpty.client.companyName";
    }

    def "Should throw an error with specific message when first name is empty for private client"() {
        given:
            ClientDto clientDto = new ClientDto();
            clientDto.setClientType(ClientType.PRIVATE);
            clientDto.setId(PRIVATE_CLIENT_ID);
            clientDto.setFirstName("");
            clientDto.setLastName(LAST_NAME);

        when:
            clientValidator.validateClientBasicDto(clientDto);
        then:
            Exception ex = thrown();
            ex.message == "isEmpty.client.firstName";
    }

    def "Should throw an error with specific message when last name is empty for private client"() {
        given:
            ClientDto clientDto = new ClientDto();
            clientDto.setClientType(ClientType.PRIVATE);
            clientDto.setId(PRIVATE_CLIENT_ID);
            clientDto.setFirstName(FIRST_NAME);
            clientDto.setLastName("");

        when:
            clientValidator.validateClientBasicDto(clientDto);
        then:
            Exception ex = thrown();
            ex.message == "isEmpty.client.lastName";
    }

    def "Should throw an exception when client is null"() {
        given:
            def client = null;
        when:
            clientValidator.validateClientExistence(client, PRIVATE_CLIENT_ID);
        then:
            thrown(IllegalArgumentException);
    }

    def "Should not throw an exception when client in not null"() {
        given:
            def client = new Client(new PersonName(FIRST_NAME, LAST_NAME), "", ClientType.PRIVATE);
        when:
            clientValidator.validateClientExistence(client, PRIVATE_CLIENT_ID);
        then:
            noExceptionThrown();
    }

    def "when passing non existing client id validateArchitectExistence should throw an exception with specific error message"() {
        given:
        when:
            this.clientValidator.validateClientExistence(NON_EXISTING_ID);
        then:
            Exception exception = thrown();
            exception.message == "notExist.client";
    }

    def "when passing existing client id validateArchitectExistence should not throw any exception"() {
        given:
        when:
            this.clientValidator.validateClientExistence(PRIVATE_CLIENT_ID);
        then:
            noExceptionThrown();
    }

    def "when passing client id that has project, exception should be thrown"() {
        given:
        when:
            this.clientValidator.validateClientHasNoProjects(CLIENT_ID_WITHOUT_PROJECTS);
        then:
            noExceptionThrown();
    }

    def "when passing client id with no projects, exception should not be thrown"() {
        given:
        when:
            this.clientValidator.validateClientHasNoProjects(CLIENT_ID_WITH_PROJECTS);
        then:
            Exception ex = thrown();
            ex.message == "notValid.client.projects";
    }

}
