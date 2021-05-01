package com.arturjarosz.task.client.application

import com.arturjarosz.task.client.application.dto.AddressDto
import com.arturjarosz.task.client.application.dto.ClientAdditionalDataDto
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

    private static final String FIRST_NAME = "firstName";
    private static final String NEW_FIRST_NAME = "newFirstName";
    private static final String LAST_NAME = "lastName";
    private static final String NEW_LAST_NAME = "newLastName";
    private static final String COMPANY_NAME = "companyName";
    private static final String NEW_EMAIL = "newEmail@test.pl";
    private static final String NEW_CITY = "newCity";
    private static final String NEW_STREET = "newStreet";
    private static final String NEW_POST_CODE = "11-111";
    private static final String NEW_HOUSE_NUMBER = "2";
    private static final String NEW_FLAT_NUMBER = "20";
    private static final String NEW_NOTE = "note2";
    private static final String NEW_TELEPHONE = "22334455";
    private static final Long EXISTING_PRIVATE_ID = 1L;

    private Client privateClient = new Client(new PersonName(FIRST_NAME, LAST_NAME), COMPANY_NAME,
            ClientType.PRIVATE);

    def clientRepository = Mock(ClientRepositoryImpl) {
        load(EXISTING_PRIVATE_ID) >> {
            return privateClient;
        };
        loadAll() >> {
            return Collections.singletonList(privateClient);
        };
    }

    def projectQueryService = Mock(ProjectQueryServiceImpl);

    ClientValidator clientValidator = Mock(ClientValidator) {
        validateClientBasicDto(null) >> { throw new IllegalArgumentException() }
    }
    def clientApplicationServiceImpl = new ClientApplicationServiceImpl(clientRepository, clientValidator,
            projectQueryService);

    def "createClientShouldValidateClientBasicDto"() {
        given:
            ClientDto clientDto = this.prepareProperPrivateClint();
        when:
            clientApplicationServiceImpl.createClient(clientDto);
        then:
            1 * this.clientValidator.validateClientBasicDto(_);
    }

    def "whenClientDtoWithPrivateClientTypePassedPrivateClientShouldBeCreated"() {
        given:
            ClientDto clientDto = this.prepareProperPrivateClint();
        when:
            clientApplicationServiceImpl.createClient(clientDto);
        then:
            1 * this.clientRepository.save({
                Client client ->
                    client.isPrivate();
            })
    }

    def "whenClientWithCorporateClientTypePassedCorporateClientShouldBeCreated"() {
        given:
            ClientDto clientDto = this.prepareProperCorporateClient();
        when:
            clientApplicationServiceImpl.createClient(clientDto);
        then:
            1 * this.clientRepository.save({
                Client client ->
                    !client.isPrivate();
            })
    }

    def "createClientShouldCallRepositorySaveOnProperClientDto"() {
        given:
            ClientDto clientDto = this.prepareProperPrivateClint();
        when:
            clientApplicationServiceImpl.createClient(clientDto);
        then:
            1 * this.clientRepository.save(_);
    }

    def "removeClientShouldCallValidateClientExistence"() {
        given:
        when:
            clientApplicationServiceImpl.removeClient(EXISTING_PRIVATE_ID);
        then:
            1 * this.clientValidator.validateClientExistence(EXISTING_PRIVATE_ID);
    }

    def "removeClientShouldCallValidateClientHasNoProjects"() {
        given:
        when:
            clientApplicationServiceImpl.removeClient(EXISTING_PRIVATE_ID);
        then:
            1 * this.clientValidator.validateClientHasNoProjects(EXISTING_PRIVATE_ID);
    }

    def "removeClientShouldCallRepositoryRemove"() {
        given:
        when:
            clientApplicationServiceImpl.removeClient(EXISTING_PRIVATE_ID);
        then:
            1 * this.clientRepository.remove(EXISTING_PRIVATE_ID);
    }

    def "getClientShouldLoadClientFromRepository"() {
        given:
        when:
            ClientDto clientDto = this.clientApplicationServiceImpl.getClient(EXISTING_PRIVATE_ID);
        then:
            1 * this.clientRepository.load(EXISTING_PRIVATE_ID);
    }

    def "updateClientShouldCallValidateClientExistence"() {
        given:
            ClientDto clientDto = this.prepareClientDtoForUpdate();
        when:
            ClientDto updatedClientDto = this.clientApplicationServiceImpl.updateClient(EXISTING_PRIVATE_ID, clientDto);
        then:
            1 * this.clientValidator.validateClientExistence(_);
    }

    def "updateClientShouldCallValidateClientDtoPresence"() {
        given:
            ClientDto clientDto = this.prepareClientDtoForUpdate();
        when:
            ClientDto updatedClientDto = this.clientApplicationServiceImpl.updateClient(EXISTING_PRIVATE_ID, clientDto);
        then:
            1 * this.clientValidator.validateClientDtoPresence(_);
    }

    def "updateClientShouldCallSaveOnRepository"() {
        given:
            ClientDto clientDto = this.prepareClientDtoForUpdate();
        when:
            ClientDto updatedClientDto = this.clientApplicationServiceImpl.updateClient(EXISTING_PRIVATE_ID, clientDto);
        then:
            1 * this.clientRepository.save(_);
    }

    def "updateClientShouldReplaceClientsData"() {
        given:
            ClientDto clientDto = this.prepareClientDtoForUpdate();
        when:
            ClientDto updatedClientDto = this.clientApplicationServiceImpl.updateClient(EXISTING_PRIVATE_ID, clientDto);
        then:
            1 * this.clientRepository.save({
                Client client ->
                    client.getPersonName().getFirstName() == NEW_FIRST_NAME;
                    client.getPersonName().getLastName() == NEW_LAST_NAME;
                    (client.getEmail().getValue() == NEW_EMAIL);
                    client.getNote() == NEW_NOTE;
                    client.getTelephone() == NEW_TELEPHONE;
                    client.getAddress().getCity() == NEW_CITY;
                    client.getAddress().getFlatNumber() == NEW_FLAT_NUMBER;
                    client.getAddress().getHouseNumber() == NEW_HOUSE_NUMBER;
                    client.getAddress().getPostCode() == NEW_POST_CODE;
                    client.getAddress().getStreet() == NEW_STREET;
            });
    }

    def "getBasicClientsShouldCallLoadAllONRepository"() {
        given:
        when:
            List<ClientDto> clientDtoList = this.clientApplicationServiceImpl.getBasicClients();
        then:
            1 * this.clientRepository.loadAll() >> Collections.singletonList(privateClient);
    }

    def "getBasicClientsShouldReturnClientsList"() {
        given:
        when:
            List<ClientDto> clientDtoList = this.clientApplicationServiceImpl.getBasicClients();
        then:
            clientDtoList.size() == 1;
    }

    private ClientDto prepareProperPrivateClint() {
        ClientDto clientDto = new ClientDto();
        clientDto.setLastName(FIRST_NAME);
        clientDto.setFirstName(LAST_NAME);
        clientDto.setClientType(ClientType.PRIVATE);
        return clientDto;
    }

    private ClientDto prepareProperCorporateClient() {
        ClientDto clientDto = new ClientDto();
        clientDto.setCompanyName(COMPANY_NAME);
        clientDto.setClientType(ClientType.CORPORATE);
        return clientDto;
    }

    private ClientDto prepareClientDtoForUpdate() {
        ClientDto clientDto = new ClientDto();
        clientDto.setClientType(ClientType.PRIVATE);
        clientDto.setFirstName(NEW_FIRST_NAME);
        clientDto.setLastName(NEW_LAST_NAME);
        AddressDto addressDto = prepareAddressDto()
        ContactDto contactDto = prepareContactDto(addressDto)
        clientDto.setContact(contactDto);
        ClientAdditionalDataDto additionalDataDto = prepareClientAddictionalDataDto()
        clientDto.setAdditionalData(additionalDataDto);
        return clientDto;
    }

    private ClientAdditionalDataDto prepareClientAddictionalDataDto() {
        ClientAdditionalDataDto additionalDataDto = new ClientAdditionalDataDto();
        additionalDataDto.setNote(NEW_NOTE);
        return additionalDataDto;
    }

    private ContactDto prepareContactDto(AddressDto addressDto) {
        ContactDto contactDto = new ContactDto();
        contactDto.setAddress(addressDto);
        contactDto.setEmail(NEW_EMAIL);
        contactDto.setTelephone(NEW_TELEPHONE);
        return contactDto;
    }

    private AddressDto prepareAddressDto() {
        AddressDto addressDto = new AddressDto();
        addressDto.setCity(NEW_CITY);
        addressDto.setHouseNumber(NEW_HOUSE_NUMBER);
        addressDto.setFlatNumber(NEW_FLAT_NUMBER);
        addressDto.setPostCode(NEW_POST_CODE);
        addressDto.setStreet(NEW_STREET);
        return addressDto;
    }
}
