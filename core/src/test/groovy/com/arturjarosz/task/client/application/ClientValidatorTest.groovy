package com.arturjarosz.task.client.application

import com.arturjarosz.task.client.infrastructure.repository.ClientRepository
import com.arturjarosz.task.client.model.Client
import com.arturjarosz.task.client.model.ClientType
import com.arturjarosz.task.dto.ClientDto
import com.arturjarosz.task.dto.ClientTypeDto
import com.arturjarosz.task.project.model.Project
import com.arturjarosz.task.project.query.impl.ProjectQueryServiceImpl
import com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException
import com.arturjarosz.task.sharedkernel.model.PersonName
import com.arturjarosz.task.utils.ProjectBuilder
import spock.lang.Specification

class ClientValidatorTest extends Specification {

    static final Long PRIVATE_CLIENT_ID = 1L
    static final Long CORPORATE_CLIENT_ID = 2L
    static final Long NON_EXISTING_ID = 999L
    static final Long CLIENT_ID_WITHOUT_PROJECTS = 899L
    static final Long CLIENT_ID_WITH_PROJECTS = 10L
    static final String FIRST_NAME = "first"
    static final String LAST_NAME = "last"
    static final String COMPANY_NAME = "company"

    static Project emptyProject = new ProjectBuilder().build()

    static Client client = new Client(new PersonName(FIRST_NAME, LAST_NAME), null, ClientType.PRIVATE)

    def clientRepository = Mock(ClientRepository) {
        findById(NON_EXISTING_ID) >> { Optional.ofNullable(null) }
        findById(PRIVATE_CLIENT_ID) >> { Optional.of(client) }
    }

    def projectQueryService = Mock(ProjectQueryServiceImpl) {
        getProjectsForClientId(CLIENT_ID_WITHOUT_PROJECTS) >> { Collections.emptyList() }
        getProjectsForClientId(CLIENT_ID_WITH_PROJECTS) >> { Collections.singletonList(emptyProject) }
    }

    def clientValidator = new ClientValidator(clientRepository, projectQueryService)


    def "Should not throw any exception when private client data provided"() {
        given:
            def clientDto = new ClientDto(firstName: FIRST_NAME, id: PRIVATE_CLIENT_ID, lastName: LAST_NAME,
                    clientType: ClientTypeDto.PRIVATE)
        when:
            this.clientValidator.validateClientBasicDto(clientDto)
        then:
            noExceptionThrown()
    }

    def "Should not throw any exception when corporate client data provided"() {
        given:
            def clientDto = new ClientDto(clientType: ClientTypeDto.CORPORATE, companyName: COMPANY_NAME,
                    id: CORPORATE_CLIENT_ID)
        when:
            clientValidator.validateClientBasicDto(clientDto)
        then:
            noExceptionThrown()
    }

    def "Not providing client dto should throw an error with specific message"() {
        given:
            def clientDto = null
        when:
            clientValidator.validateClientBasicDto(clientDto)
        then:
            Exception ex = thrown()
            ex.message == "isNull.client"
    }

    def "Should throw an error with specific message when client type not provided"() {
        given:
            def clientDto = new ClientDto(id: CORPORATE_CLIENT_ID, companyName: COMPANY_NAME)
        when:
            clientValidator.validateClientBasicDto(clientDto)
        then:
            Exception ex = thrown()
            ex.message == "isNull.client.clientType"
    }

    def "Should throw an error with specific message when first name is not provided for private client"() {
        given:
            def clientDto = new ClientDto(clientType: ClientTypeDto.PRIVATE, id: PRIVATE_CLIENT_ID,
                    lastName: LAST_NAME)
        when:
            clientValidator.validateClientBasicDto(clientDto)
        then:
            Exception ex = thrown()
            ex.message == "isNull.client.firstName"
    }

    def "Should throw an error with specific message when last name is not provided for private client"() {
        given:
            def clientDto = new ClientDto(clientType: ClientTypeDto.PRIVATE, firstName: FIRST_NAME,
                    id: PRIVATE_CLIENT_ID)
        when:
            clientValidator.validateClientBasicDto(clientDto)
        then:
            Exception ex = thrown()
            ex.message == "isNull.client.lastName"
    }

    def "Should throw an error with specific message when company name is not provided for corporate client"() {
        given:
            def clientDto = new ClientDto(clientType: ClientTypeDto.CORPORATE, id: CORPORATE_CLIENT_ID)
        when:
            clientValidator.validateClientBasicDto(clientDto)
        then:
            Exception ex = thrown()
            ex.message == "isNull.client.companyName"
    }

    def "Should throw an error with specific message when company name is empty for corporate client"() {
        given:
            def clientDto = new ClientDto(clientType: ClientTypeDto.CORPORATE, companyName: "",
                    id: CORPORATE_CLIENT_ID)
        when:
            clientValidator.validateClientBasicDto(clientDto)
        then:
            Exception ex = thrown()
            ex.message == "isEmpty.client.companyName"
    }

    def "Should throw an error with specific message when first name is empty for private client"() {
        given:
            def clientDto = new ClientDto(clientType: ClientTypeDto.PRIVATE, id: PRIVATE_CLIENT_ID, firstName: "",
                    lastName: LAST_NAME)
        when:
            clientValidator.validateClientBasicDto(clientDto)
        then:
            Exception ex = thrown()
            ex.message == "isEmpty.client.firstName"
    }

    def "Should throw an error with specific message when last name is empty for private client"() {
        given:
            def clientDto = new ClientDto(clientType: ClientTypeDto.PRIVATE, id: PRIVATE_CLIENT_ID,
                    firstName: FIRST_NAME, lastName: "")
        when:
            clientValidator.validateClientBasicDto(clientDto)
        then:
            Exception ex = thrown()
            ex.message == "isEmpty.client.lastName"
    }

    def "Should throw an exception when client is null"() {
        given:
            Optional<Client> maybeClient = Optional.ofNullable(null)
        when:
            clientValidator.validateClientExistence(maybeClient, PRIVATE_CLIENT_ID)
        then:
            thrown(ResourceNotFoundException)
    }

    def "Should not throw an exception when client in not null"() {
        given:
            def maybeClient = Optional.of(new Client(new PersonName(FIRST_NAME, LAST_NAME), "", ClientType.PRIVATE))
        when:
            clientValidator.validateClientExistence(maybeClient, PRIVATE_CLIENT_ID)
        then:
            noExceptionThrown()
    }

    def "when passing non existing client id validateArchitectExistence should throw an exception with specific error message"() {
        given:
        when:
            this.clientValidator.validateClientExistence(NON_EXISTING_ID)
        then:
            Exception exception = thrown()
            exception.message == "notExist.client"
    }

    def "when passing existing client id validateArchitectExistence should not throw any exception"() {
        given:
        when:
            this.clientValidator.validateClientExistence(PRIVATE_CLIENT_ID)
        then:
            noExceptionThrown()
    }

    def "when passing client id that has project, exception should be thrown"() {
        given:
        when:
            this.clientValidator.validateClientHasNoProjects(CLIENT_ID_WITHOUT_PROJECTS)
        then:
            noExceptionThrown()
    }

    def "when passing client id with no projects, exception should not be thrown"() {
        given:
        when:
            this.clientValidator.validateClientHasNoProjects(CLIENT_ID_WITH_PROJECTS)
        then:
            Exception ex = thrown()
            ex.message == "notValid.client.projects"
    }
}
