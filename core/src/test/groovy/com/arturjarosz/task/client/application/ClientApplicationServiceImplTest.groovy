package com.arturjarosz.task.client.application

import com.arturjarosz.task.client.application.dto.AddressDto

import com.arturjarosz.task.client.application.dto.ClientDto
import com.arturjarosz.task.client.application.dto.ContactDto
import com.arturjarosz.task.client.application.impl.ClientApplicationServiceImpl
import com.arturjarosz.task.client.infrastructure.repository.impl.ClientRepositoryImpl
import com.arturjarosz.task.client.model.Client
import com.arturjarosz.task.client.model.ClientType
import com.arturjarosz.task.project.query.impl.ProjectQueryServiceImpl
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException
import com.arturjarosz.task.sharedkernel.model.PersonName
import spock.lang.Specification

class ClientApplicationServiceImplTest extends Specification {

    private static final String FIRST_NAME = "firstName"
    private static final String NEW_FIRST_NAME = "newFirstName"
    private static final String LAST_NAME = "lastName"
    private static final String NEW_LAST_NAME = "newLastName"
    private static final String COMPANY_NAME = "companyName"
    private static final String NEW_EMAIL = "newEmail@test.pl"
    private static final String NEW_CITY = "newCity"
    private static final String NEW_STREET = "newStreet"
    private static final String NEW_POST_CODE = "11-111"
    private static final String NEW_HOUSE_NUMBER = "2"
    private static final String NEW_FLAT_NUMBER = "20"
    private static final String NEW_NOTE = "note2"
    private static final String NEW_TELEPHONE = "22334455"
    private static final Long EXISTING_PRIVATE_ID = 1L

    private Client privateClient = new Client(new PersonName(FIRST_NAME, LAST_NAME), COMPANY_NAME,
            ClientType.PRIVATE)

    def clientRepository = Mock(ClientRepositoryImpl) {
        load(EXISTING_PRIVATE_ID) >> {
            return privateClient
        }
        loadAll() >> {
            return Collections.singletonList(privateClient)
        }
    }

    def projectQueryService = Mock(ProjectQueryServiceImpl)

    ClientValidator clientValidator = Mock(ClientValidator) {
        validateClientBasicDto(null) >> { throw new IllegalArgumentException() }
    }
    def clientApplicationServiceImpl = new ClientApplicationServiceImpl(clientRepository, clientValidator
    )

    def "createClientShouldValidateClientBasicDto"() {
        given:
            ClientDto clientDto = this.prepareProperPrivateClint()
        when:
            clientApplicationServiceImpl.createClient(clientDto)
        then:
            1 * this.clientValidator.validateClientBasicDto(_)
    }

    def "whenClientDtoWithPrivateClientTypePassedPrivateClientShouldBeCreated"() {
        given:
            ClientDto clientDto = this.prepareProperPrivateClint()
        when:
            clientApplicationServiceImpl.createClient(clientDto)
        then:
            1 * this.clientRepository.save({
                Client client ->
                    client.isPrivate()
            })
    }

    def "whenClientWithCorporateClientTypePassedCorporateClientShouldBeCreated"() {
        given:
            ClientDto clientDto = this.prepareProperCorporateClient()
        when:
            clientApplicationServiceImpl.createClient(clientDto)
        then:
            1 * this.clientRepository.save({
                Client client ->
                    !client.isPrivate()
            })
    }

    def "createClientShouldCallRepositorySaveOnProperClientDto"() {
        given:
            ClientDto clientDto = this.prepareProperPrivateClint()
        when:
            clientApplicationServiceImpl.createClient(clientDto)
        then:
            1 * this.clientRepository.save(_)
    }

    def "removeClientShouldCallValidateClientExistence"() {
        given:
        when:
            clientApplicationServiceImpl.removeClient(EXISTING_PRIVATE_ID)
        then:
            1 * this.clientValidator.validateClientExistence(EXISTING_PRIVATE_ID)
    }

    def "removeClientShouldCallValidateClientHasNoProjects"() {
        given:
        when:
            clientApplicationServiceImpl.removeClient(EXISTING_PRIVATE_ID)
        then:
            1 * this.clientValidator.validateClientHasNoProjects(EXISTING_PRIVATE_ID)
    }

    def "removeClientShouldCallRepositoryRemove"() {
        given:
        when:
            clientApplicationServiceImpl.removeClient(EXISTING_PRIVATE_ID)
        then:
            1 * this.clientRepository.remove(EXISTING_PRIVATE_ID)
    }

    def "getClientShouldLoadClientFromRepository"() {
        given:
        when:
            ClientDto clientDto = this.clientApplicationServiceImpl.getClient(EXISTING_PRIVATE_ID)
        then:
            1 * this.clientRepository.load(EXISTING_PRIVATE_ID)
    }

    def "updateClientShouldCallValidateClientExistence"() {
        given:
            ClientDto clientDto = this.prepareClientDtoForUpdate()
        when:
            ClientDto updatedClientDto = this.clientApplicationServiceImpl.updateClient(EXISTING_PRIVATE_ID, clientDto)
        then:
            1 * this.clientValidator.validateClientExistence(_)
    }

    def "updateClientShouldCallValidateClientDtoPresence"() {
        given:
            ClientDto clientDto = this.prepareClientDtoForUpdate()
        when:
            ClientDto updatedClientDto = this.clientApplicationServiceImpl.updateClient(EXISTING_PRIVATE_ID, clientDto)
        then:
            1 * this.clientValidator.validateClientDtoPresence(_)
    }

    def "updateClientShouldCallSaveOnRepository"() {
        given:
            ClientDto clientDto = this.prepareClientDtoForUpdate()
        when:
            ClientDto updatedClientDto = this.clientApplicationServiceImpl.updateClient(EXISTING_PRIVATE_ID, clientDto)
        then:
            1 * this.clientRepository.save(_)
    }

    def "updateClientShouldReplaceClientsData"() {
        given:
            ClientDto clientDto = this.prepareClientDtoForUpdate()
        when:
            ClientDto updatedClientDto = this.clientApplicationServiceImpl.updateClient(EXISTING_PRIVATE_ID, clientDto)
        then:
            1 * this.clientRepository.save({
                Client client ->
                    client.personName.firstName == NEW_FIRST_NAME
                    client.personName.lastName == NEW_LAST_NAME
                    client.email.value == NEW_EMAIL
                    client.note == NEW_NOTE
                    client.telephone == NEW_TELEPHONE
                    client.address.city == NEW_CITY
                    client.address.flatNumber == NEW_FLAT_NUMBER
                    client.address.houseNumber == NEW_HOUSE_NUMBER
                    client.address.postCode == NEW_POST_CODE
                    client.address.street == NEW_STREET
            })
    }

    def "getBasicClientsShouldCallLoadAllONRepository"() {
        given:
        when:
            List<ClientDto> clientDtoList = this.clientApplicationServiceImpl.basicClients
        then:
            1 * this.clientRepository.loadAll() >> Collections.singletonList(privateClient)
    }

    def "getBasicClientsShouldReturnClientsList"() {
        given:
        when:
            List<ClientDto> clientDtoList = this.clientApplicationServiceImpl.basicClients
        then:
            clientDtoList.size() == 1
    }

    private ClientDto prepareProperPrivateClint() {
        ClientDto clientDto = new ClientDto(firstName: FIRST_NAME, lastName: LAST_NAME, clientType: ClientType.PRIVATE)
        return clientDto
    }

    private ClientDto prepareProperCorporateClient() {
        ClientDto clientDto = new ClientDto(companyName: COMPANY_NAME, clientType: ClientType.CORPORATE)
        return clientDto
    }

    private ClientDto prepareClientDtoForUpdate() {
        AddressDto addressDto = prepareAddressDto()
        ContactDto contactDto = prepareContactDto(addressDto)
        ClientDto clientDto = new ClientDto(clientType: ClientType.PRIVATE, firstName: NEW_FIRST_NAME,
                lastName: NEW_LAST_NAME, contact: contactDto, note: NEW_NOTE)
        return clientDto
    }

    private ContactDto prepareContactDto(AddressDto addressDto) {
        ContactDto contactDto = new ContactDto(address: addressDto, email: NEW_EMAIL, telephone: NEW_TELEPHONE)
        return contactDto
    }

    private AddressDto prepareAddressDto() {
        AddressDto addressDto = new AddressDto(city: NEW_CITY, houseNumber: NEW_HOUSE_NUMBER,
                flatNumber: NEW_FLAT_NUMBER, postCode: NEW_POST_CODE, street: NEW_STREET)
        return addressDto
    }
}
