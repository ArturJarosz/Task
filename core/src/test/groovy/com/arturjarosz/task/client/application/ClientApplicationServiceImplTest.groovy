package com.arturjarosz.task.client.application

import com.arturjarosz.task.client.application.impl.ClientApplicationServiceImpl
import com.arturjarosz.task.client.application.mapper.ClientMapperImpl
import com.arturjarosz.task.client.infrastructure.repository.ClientRepository
import com.arturjarosz.task.client.model.Client
import com.arturjarosz.task.client.model.ClientType
import com.arturjarosz.task.contract.query.ContractQueryService
import com.arturjarosz.task.dto.AddressDto
import com.arturjarosz.task.dto.ClientDto
import com.arturjarosz.task.dto.ClientTypeDto
import com.arturjarosz.task.dto.ContactDto
import com.arturjarosz.task.project.application.mapper.ProjectsToEntityProjectsSummaryDtoMapperImpl
import com.arturjarosz.task.project.model.Project
import com.arturjarosz.task.project.query.ProjectCounts
import com.arturjarosz.task.project.query.ProjectQueryService
import com.arturjarosz.task.project.status.project.ProjectStatus
import com.arturjarosz.task.project.status.project.ProjectWorkflow
import com.arturjarosz.task.properties.ProjectProperties
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException
import com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException
import com.arturjarosz.task.sharedkernel.model.PersonName
import com.arturjarosz.task.sharedkernel.testhelpers.TestUtils
import spock.lang.Specification

class ClientApplicationServiceImplTest extends Specification {

    static final FIRST_NAME = "firstName"
    static final NEW_FIRST_NAME = "newFirstName"
    static final LAST_NAME = "lastName"
    static final NEW_LAST_NAME = "newLastName"
    static final COMPANY_NAME = "companyName"
    static final NEW_EMAIL = "newEmail@test.pl"
    static final NEW_CITY = "newCity"
    static final NEW_STREET = "newStreet 12"
    static final NEW_POST_CODE = "11-111"
    static final NEW_NOTE = "note2"
    static final NEW_TELEPHONE = "22334455"
    static final EXISTING_PRIVATE_ID = 1L
    static final NOT_EXISTING_CLIENT_ID = 10L
    static final PROJECT_ID_1 = 11L
    static final PROJECT_ID_2 = 12L

    Client privateClient = new Client(new PersonName(FIRST_NAME, LAST_NAME), COMPANY_NAME, ClientType.PRIVATE)

    def clientRepository = Mock(ClientRepository) {
        findById(EXISTING_PRIVATE_ID) >> { return Optional.of(privateClient) }
        findAll() >> { return Collections.singletonList(privateClient) }
    }

    def clientValidator = Mock(ClientValidator) {
        validateClientBasicDto(null) >> { throw new IllegalArgumentException() }
        validateClientExistence(NOT_EXISTING_CLIENT_ID) >> { throw new ResourceNotFoundException() }
    }

    def clientMapper = new ClientMapperImpl()
    static final ACTIVE_STATUSES = [ProjectStatus.TO_DO, ProjectStatus.IN_PROGRESS] as Set

    def projectQueryService = Mock(ProjectQueryService) {
        getProjectCountsByClientIds(_, _) >> [:]
    }
    def contractQueryService = Mock(ContractQueryService) {
        getProjectsValueByClientIds(_) >> [:]
    }
    def projectsToClientProjectsSummaryDtoMapper = new ProjectsToEntityProjectsSummaryDtoMapperImpl()

    def projectProperties = new ProjectProperties(ACTIVE_STATUSES)

    def clientApplicationServiceImpl = new ClientApplicationServiceImpl(clientRepository, clientValidator, clientMapper,
            projectQueryService, contractQueryService, projectsToClientProjectsSummaryDtoMapper, projectProperties)

    def "createClient should validate clientBasicDto"() {
        given:
            def clientDto = this.prepareProperPrivateClint()
        when:
            clientApplicationServiceImpl.createClient(clientDto)
        then:
            1 * this.clientValidator.validateClientBasicDto(_)
    }

    def "when ClientDto with private client type passed client of private type should be created"() {
        given:
            def clientDto = this.prepareProperPrivateClint()
        when:
            clientApplicationServiceImpl.createClient(clientDto)
        then:
            1 * this.clientRepository.save({ Client client -> client.isPrivate()
            })
    }

    def "when client with corporate client type passed corporate client should be created"() {
        given:
            def clientDto = this.prepareProperCorporateClient()
        when:
            clientApplicationServiceImpl.createClient(clientDto)
        then:
            1 * this.clientRepository.save({ Client client -> !client.isPrivate()
            })
    }

    def "createClient should call repository cave on proper ClientDto"() {
        given:
            def clientDto = this.prepareProperPrivateClint()
        when:
            clientApplicationServiceImpl.createClient(clientDto)
        then:
            1 * this.clientRepository.save(_)
    }

    def "removeClient should call validateClientExistence"() {
        given:
        when:
            clientApplicationServiceImpl.removeClient(EXISTING_PRIVATE_ID)
        then:
            1 * this.clientValidator.validateClientExistence(EXISTING_PRIVATE_ID)
    }

    def "removeClient should call validateClientHasNoProjects"() {
        given:
        when:
            clientApplicationServiceImpl.removeClient(EXISTING_PRIVATE_ID)
        then:
            1 * this.clientValidator.validateClientHasNoProjects(EXISTING_PRIVATE_ID)
    }

    def "removeClient should call repository remove"() {
        given:
        when:
            clientApplicationServiceImpl.removeClient(EXISTING_PRIVATE_ID)
        then:
            1 * this.clientRepository.deleteById(EXISTING_PRIVATE_ID)
    }

    def "getClient should load client from repository"() {
        given:
        when:
            this.clientApplicationServiceImpl.getClient(EXISTING_PRIVATE_ID)
        then:
            1 * this.clientRepository.findById(EXISTING_PRIVATE_ID) >> Optional.of(privateClient)
    }

    def "updateClient should call validateClientExistence"() {
        given:
            def clientDto = this.prepareClientDtoForUpdate()
        when:
            this.clientApplicationServiceImpl.updateClient(EXISTING_PRIVATE_ID, clientDto)
        then:
            1 * this.clientValidator.validateClientExistence(_, EXISTING_PRIVATE_ID)
    }

    def "updateClient should call validateClientDtoPresence"() {
        given:
            def clientDto = this.prepareClientDtoForUpdate()
        when:
            this.clientApplicationServiceImpl.updateClient(EXISTING_PRIVATE_ID, clientDto)
        then:
            1 * this.clientValidator.validateClientDtoPresence(_)
    }

    def "updateClient should call save on repository"() {
        given:
            def clientDto = this.prepareClientDtoForUpdate()
        when:
            this.clientApplicationServiceImpl.updateClient(EXISTING_PRIVATE_ID, clientDto)
        then:
            1 * this.clientRepository.save(_)
    }

    def "updateClient should replace client data"() {
        given:
            def clientDto = this.prepareClientDtoForUpdate()
        when:
            def updatedClientDto = this.clientApplicationServiceImpl.updateClient(EXISTING_PRIVATE_ID, clientDto)
        then:
            with(updatedClientDto) {
                firstName == NEW_FIRST_NAME
                lastName == NEW_LAST_NAME
                contact.email == NEW_EMAIL
                note == NEW_NOTE
                contact.telephone == NEW_TELEPHONE
                contact.address.city == NEW_CITY
                contact.address.postCode == NEW_POST_CODE
                contact.address.street == NEW_STREET
            }
    }

    def "getBasicClients should call loadAll on repository"() {
        given:
        when:
            this.clientApplicationServiceImpl.getClients()
        then:
            1 * this.clientRepository.findAll() >> Collections.singletonList(privateClient)
    }

    def "getBasicClients should return list of clients"() {
        given:
        when:
            List<ClientDto> clientDtoList = this.clientApplicationServiceImpl.getClients()
        then:
            clientDtoList.size() == 1
    }

    def "getClients should return number of all and active projects counted for configured active statuses"() {
        given:
            def clientId = privateClient.id
        when:
            List<ClientDto> clientDtoList = this.clientApplicationServiceImpl.getClients()
        then:
            1 * this.projectQueryService.getProjectCountsByClientIds([clientId], ACTIVE_STATUSES) >>
                    [(clientId): new ProjectCounts(4, 1)]
            1 * this.contractQueryService.getProjectsValueByClientIds([clientId]) >> [(clientId): new BigDecimal("900.00")]
            clientDtoList[0].numberOfProjects == 4
            clientDtoList[0].numberOfActiveProjects == 1
            clientDtoList[0].projectsValue == 900d
    }

    def "getClient should return zero projects for client without projects"() {
        given:
        when:
            def clientDto = this.clientApplicationServiceImpl.getClient(EXISTING_PRIVATE_ID)
        then:
            1 * this.projectQueryService.getProjectCountsByClientIds(_, ACTIVE_STATUSES) >> [:]
            clientDto.numberOfProjects == 0
            clientDto.numberOfActiveProjects == 0
            clientDto.projectsValue == 0d
    }

    def "getClientsBasicData should load clients in one query and return them by id without projects data"() {
        given:
            def client = new Client(new PersonName(FIRST_NAME, LAST_NAME), COMPANY_NAME, ClientType.PRIVATE)
            TestUtils.setFieldForObject(client, "id", EXISTING_PRIVATE_ID)
        when:
            def clientsById = this.clientApplicationServiceImpl.getClientsBasicData([EXISTING_PRIVATE_ID, null])
        then:
            1 * this.clientRepository.findAllById([EXISTING_PRIVATE_ID]) >> [client]
            0 * this.projectQueryService._
            0 * this.contractQueryService._
            clientsById.keySet() == [EXISTING_PRIVATE_ID] as Set
            clientsById[EXISTING_PRIVATE_ID].firstName == FIRST_NAME
    }

    def "getClientsBasicData should not query repository for empty ids"() {
        when:
            def clientsById = this.clientApplicationServiceImpl.getClientsBasicData([])
        then:
            0 * this.clientRepository.findAllById(_)
            clientsById.isEmpty()
    }

    def "getClientProjectsSummary should not return object if client existence fails"() {
        given:
        when:
            def result = this.clientApplicationServiceImpl.getClientProjectsSummary(NOT_EXISTING_CLIENT_ID)
        then:
            thrown(ResourceNotFoundException)
            result == null
    }

    def "getClientProjectsSummary should should return projects summary for existing client"() {
        given:
            mockGetProjectsAndContractsForClient()
        when:
            def result = this.clientApplicationServiceImpl.getClientProjectsSummary(EXISTING_PRIVATE_ID)
        then:
            result.numberOfProjects == 2
            result.totalValue == 150.0D
    }

    ClientDto prepareProperPrivateClint() {
        def clientDto = new ClientDto(firstName: FIRST_NAME, lastName: LAST_NAME, clientType: ClientTypeDto.PRIVATE)
        return clientDto
    }

    ClientDto prepareProperCorporateClient() {
        def clientDto = new ClientDto(companyName: COMPANY_NAME, clientType: ClientTypeDto.CORPORATE)
        return clientDto
    }

    ClientDto prepareClientDtoForUpdate() {
        def addressDto = prepareAddressDto()
        def contactDto = prepareContactDto(addressDto)
        def clientDto = new ClientDto(clientType: ClientTypeDto.PRIVATE, firstName: NEW_FIRST_NAME,
                lastName: NEW_LAST_NAME, contact: contactDto, note: NEW_NOTE)
        return clientDto
    }

    ContactDto prepareContactDto(AddressDto addressDto) {
        return new ContactDto(address: addressDto, email: NEW_EMAIL, telephone: NEW_TELEPHONE)
    }

    AddressDto prepareAddressDto() {
        return new AddressDto(city: NEW_CITY, postCode: NEW_POST_CODE, street: NEW_STREET)
    }

    def mockGetProjectsAndContractsForClient() {
        def project1 = new Project("project name 1", null, null, 1L, new ProjectWorkflow(), 1L)
        TestUtils.setFieldForObject(project1, "id", PROJECT_ID_1)
        def project2 = new Project("project name 2", null, null, 1L, new ProjectWorkflow(), 2L)
        TestUtils.setFieldForObject(project2, "id", PROJECT_ID_2)
        this.projectQueryService.getProjectsForClientId(EXISTING_PRIVATE_ID) >> [project1, project2]
        this.contractQueryService.getContractValuesForProjectsByClientId(EXISTING_PRIVATE_ID) >> [(PROJECT_ID_1): BigDecimal.valueOf(50.0D), (PROJECT_ID_2): BigDecimal.valueOf(100.0D)]
    }

}
