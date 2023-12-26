package com.arturjarosz.task.client.application

import com.arturjarosz.task.client.application.impl.ClientApplicationServiceImpl
import com.arturjarosz.task.client.infrastructure.repository.ClientRepository
import com.arturjarosz.task.client.model.Client
import com.arturjarosz.task.client.model.ClientType
import com.arturjarosz.task.dto.AddressDto
import com.arturjarosz.task.dto.ClientDto
import com.arturjarosz.task.dto.ClientTypeDto
import com.arturjarosz.task.dto.ContactDto
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException
import com.arturjarosz.task.sharedkernel.model.PersonName
import spock.lang.Specification

class ClientApplicationServiceImplTest extends Specification {

    static final String FIRST_NAME = "firstName"
    static final String NEW_FIRST_NAME = "newFirstName"
    static final String LAST_NAME = "lastName"
    static final String NEW_LAST_NAME = "newLastName"
    static final String COMPANY_NAME = "companyName"
    static final String NEW_EMAIL = "newEmail@test.pl"
    static final String NEW_CITY = "newCity"
    static final String NEW_STREET = "newStreet"
    static final String NEW_POST_CODE = "11-111"
    static final String NEW_HOUSE_NUMBER = "2"
    static final String NEW_FLAT_NUMBER = "20"
    static final String NEW_NOTE = "note2"
    static final String NEW_TELEPHONE = "22334455"
    static final Long EXISTING_PRIVATE_ID = 1L

    Client privateClient = new Client(new PersonName(FIRST_NAME, LAST_NAME), COMPANY_NAME, ClientType.PRIVATE)

    def clientRepository = Mock(ClientRepository) {
        findById(EXISTING_PRIVATE_ID) >> { return Optional.of(privateClient) }
        findAll() >> { return Collections.singletonList(privateClient) }

    }

    ClientValidator clientValidator = Mock(ClientValidator) {
        validateClientBasicDto(null) >> { throw new IllegalArgumentException() }
    }
    def clientApplicationServiceImpl = new ClientApplicationServiceImpl(clientRepository, clientValidator
    )

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
            1 * this.clientRepository.save({
                Client client -> client.isPrivate()
            })
    }

    def "when client with corporate client type passed corporate client should be created"() {
        given:
            def clientDto = this.prepareProperCorporateClient()
        when:
            clientApplicationServiceImpl.createClient(clientDto)
        then:
            1 * this.clientRepository.save({
                Client client -> !client.isPrivate()
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
                contact.address.flatNumber == NEW_FLAT_NUMBER
                contact.address.houseNumber == NEW_HOUSE_NUMBER
                contact.address.postCode == NEW_POST_CODE
                contact.address.street == NEW_STREET
            }

    }

    def "getBasicClients should call loadAll on repository"() {
        given:
        when:
            this.clientApplicationServiceImpl.clients
        then:
            1 * this.clientRepository.findAll() >> Collections.singletonList(privateClient)
    }

    def "getBasicClients should return list of clients"() {
        given:
        when:
            List<ClientDto> clientDtoList = this.clientApplicationServiceImpl.clients
        then:
            clientDtoList.size() == 1
    }

    private ClientDto prepareProperPrivateClint() {
        def clientDto = new ClientDto(firstName: FIRST_NAME, lastName: LAST_NAME, clientType: ClientTypeDto.PRIVATE)
        return clientDto
    }

    private ClientDto prepareProperCorporateClient() {
        def clientDto = new ClientDto(companyName: COMPANY_NAME, clientType: ClientTypeDto.CORPORATE)
        return clientDto
    }

    private ClientDto prepareClientDtoForUpdate() {
        def addressDto = prepareAddressDto()
        def contactDto = prepareContactDto(addressDto)
        def clientDto = new ClientDto(clientType: ClientTypeDto.PRIVATE, firstName: NEW_FIRST_NAME,
                lastName: NEW_LAST_NAME, contact: contactDto, note: NEW_NOTE)
        return clientDto
    }

    private ContactDto prepareContactDto(AddressDto addressDto) {
        return new ContactDto(address: addressDto, email: NEW_EMAIL, telephone: NEW_TELEPHONE)
    }

    private AddressDto prepareAddressDto() {
        return new AddressDto(city: NEW_CITY, houseNumber: NEW_HOUSE_NUMBER,
                flatNumber: NEW_FLAT_NUMBER, postCode: NEW_POST_CODE, street: NEW_STREET)
    }

}
