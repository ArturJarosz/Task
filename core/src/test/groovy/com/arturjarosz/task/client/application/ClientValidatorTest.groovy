package com.arturjarosz.task.client.application

import com.arturjarosz.task.client.application.dto.ClientBasicDto
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

    private static final Long EXISTING_ID = 1L;
    private static final Long NON_EXISTING_ID = 999L;
    private static final Long CLIENT_ID_WITHOUT_PROJECTS = 899L;
    private static final Long CLIENT_ID_WITH_PROJECTS = 10L;
    private static final ClientType PRIVATE_CLIENT_TYPE = ClientType.PRIVATE;
    private static final ClientType CORPORATE_CLIENT_TYPE = ClientType.CORPORATE;
    private static final String FIRST_NAME = "first";
    private static final String LAST_NAME = "last";
    private static final String COMPANY_NAME = "company";

    private static Project emptyProject = new ProjectBuilder().build();

    private static Client client = new Client(new PersonName(FIRST_NAME, LAST_NAME), null, PRIVATE_CLIENT_TYPE);

    def clientRepository = Mock(ClientRepositoryImpl) {
        load(NON_EXISTING_ID) >> { null };
        load(EXISTING_ID) >> { client };
    }

    def projectQueryService = Mock(ProjectQueryServiceImpl) {
        getProjectForClientId(CLIENT_ID_WITHOUT_PROJECTS) >> { Collections.emptyList() }
        getProjectForClientId(CLIENT_ID_WITH_PROJECTS) >> { Collections.singletonList(emptyProject) }
    }

    def clientValidator = new ClientValidator(clientRepository, projectQueryService);


    def "Should not throw any exception when private client data provided"() {
        given:
            ClientBasicDto clientBasicDto = new ClientBasicDto(EXISTING_ID, PRIVATE_CLIENT_TYPE,
                    FIRST_NAME, LAST_NAME, null);
        when:
            ClientValidator.validateClientBasicDto(clientBasicDto);
        then:
            noExceptionThrown();
    }

    def "Should not throw any exception when corporate client data provided"() {
        given:
            ClientBasicDto clientBasicDto = new ClientBasicDto(EXISTING_ID, CORPORATE_CLIENT_TYPE,
                    null, null, COMPANY_NAME);
        when:
            ClientValidator.validateClientBasicDto(clientBasicDto);
        then:
            noExceptionThrown();
    }

    def "Not providing client dto should throw an error with specific message"() {
        given:
            ClientBasicDto clientBasicDto = null;
        when:
            ClientValidator.validateClientBasicDto(clientBasicDto);
        then:
            Exception ex = thrown();
            ex.message == "isNull.client";
    }

    def "Should throw an error with specific message when client type not provided"() {
        given:
            ClientBasicDto clientBasicDto = new ClientBasicDto(EXISTING_ID, null,
                    null, null, COMPANY_NAME);
        when:
            ClientValidator.validateClientBasicDto(clientBasicDto);
        then:
            Exception ex = thrown();
            ex.message == "isNull.client.clientType";
    }

    def "Should throw an error with specific message when first name is not provided for private client"() {
        given:
            ClientBasicDto clientBasicDto = new ClientBasicDto(EXISTING_ID, PRIVATE_CLIENT_TYPE,
                    null, LAST_NAME, COMPANY_NAME);
        when:
            ClientValidator.validateClientBasicDto(clientBasicDto);
        then:
            Exception ex = thrown();
            ex.message == "isNull.client.firstName";
    }

    def "Should throw an error with specific message when last name is not provided for private client"() {
        given:
            ClientBasicDto clientBasicDto = new ClientBasicDto(EXISTING_ID, PRIVATE_CLIENT_TYPE,
                    FIRST_NAME, null, null);
        when:
            ClientValidator.validateClientBasicDto(clientBasicDto);
        then:
            Exception ex = thrown();
            ex.message == "isNull.client.lastName";
    }

    def "Should throw an error with specific message when company name is not provided for corporate client"() {
        given:
            ClientBasicDto clientBasicDto = new ClientBasicDto(EXISTING_ID, CORPORATE_CLIENT_TYPE,
                    null, null, null);
        when:
            ClientValidator.validateClientBasicDto(clientBasicDto);
        then:
            Exception ex = thrown();
            ex.message == "isNull.client.companyName";
    }

    def "Should throw an error with specific message when company name is empty for corporate client"() {
        given:
            ClientBasicDto clientBasicDto = new ClientBasicDto(EXISTING_ID, CORPORATE_CLIENT_TYPE,
                    null, null, "");
        when:
            ClientValidator.validateClientBasicDto(clientBasicDto);
        then:
            Exception ex = thrown();
            ex.message == "isEmpty.client.companyName";
    }

    def "Should throw an error with specific message when first name is empty for private client"() {
        given:
            ClientBasicDto clientBasicDto = new ClientBasicDto(EXISTING_ID, PRIVATE_CLIENT_TYPE,
                    "", LAST_NAME, null);
        when:
            ClientValidator.validateClientBasicDto(clientBasicDto);
        then:
            Exception ex = thrown();
            ex.message == "isEmpty.client.firstName";
    }

    def "Should throw an error with specific message when last name is empty for private client"() {
        given:
            ClientBasicDto clientBasicDto = new ClientBasicDto(EXISTING_ID, PRIVATE_CLIENT_TYPE,
                    FIRST_NAME, "", null);
        when:
            ClientValidator.validateClientBasicDto(clientBasicDto);
        then:
            Exception ex = thrown();
            ex.message == "isEmpty.client.lastName";
    }

    def "Should throw an exception when client is null"() {
        given:
            def client = null;
        when:
            ClientValidator.validateClientExistence(client, EXISTING_ID);
        then:
            thrown(IllegalArgumentException);
    }

    def "Should not throw an exception when client in not null"() {
        given:
            def client = new Client(new PersonName(FIRST_NAME, LAST_NAME), COMPANY_NAME, PRIVATE_CLIENT_TYPE);
        when:
            ClientValidator.validateClientExistence(client, EXISTING_ID);
        then:
            noExceptionThrown();
    }

    def "when passing non existing client id validateArchitectExistence should throw an exception with specific error message"() {
        given:
        when:
            this.clientValidator.validateClientExistence(NON_EXISTING_ID);
        then:
            Exception exception = thrown();
            exception.message == "notExists.client";
    }

    def "when passing existing client id validateArchitectExistence should not throw any exception"() {
        given:
        when:
            this.clientValidator.validateClientExistence(EXISTING_ID);
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
