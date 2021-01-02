package com.arturjarosz.task.client.application

import com.arturjarosz.task.client.application.ClientApplicationServiceImpl
import com.arturjarosz.task.client.application.ClientValidator
import com.arturjarosz.task.client.application.dto.*
import com.arturjarosz.task.client.infrastructure.repository.impl.ClientRepositoryImpl
import com.arturjarosz.task.client.model.Client
import com.arturjarosz.task.client.model.ClientType
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException
import com.arturjarosz.task.sharedkernel.model.Address
import com.arturjarosz.task.sharedkernel.model.PersonName
import spock.lang.Shared
import spock.lang.Specification

class ClientApplicationServiceImplTest extends Specification {

    private static final String FIRST_NAME = "firstName";
    private static final String NEW_FIRST_NAME = "newFirstName";
    private static final String LAST_NAME = "lastName";
    private static final String NEW_LAST_NAME = "newLastName";
    private static final String COMPANY_NAME = "companyName";
    private static final String NEW_COMPANY_NAME = "newCompanyName";
    private static final String EMAIL = "email@test.pl";
    private static final String NEW_EMAIL = "newEmail@test.pl";
    private static final String CITY = "city";
    private static final String NEW_CITY = "newCity";
    private static final String STREET = "street";
    private static final String NEW_STREET = "newStreet";
    private static final String POST_CODE = "00-000";
    private static final String NEW_POST_CODE = "11-111";
    private static final String HOUSE = "1";
    private static final String NEW_HOUSE = "2";
    private static final String FLAT = "10";
    private static final String NEW_FLAT = "20";
    private static final String NOTE = "note";
    private static final String NEW_NOTE = "note2";
    private static final String TELEPHONE = "00112233"
    private static final String NEW_TELEPHONE = "22334455";
    private static final Long NON_EXISTING_ID = 999L;
    private static final Long EXISTING_PRIVATE_ID = 1L;
    private static final Long EXISTING_CORPORATE_ID = 2L;

    @Shared
    private static final Client CLIENT_PRIVATE = new Client(new PersonName(FIRST_NAME, LAST_NAME), COMPANY_NAME, ClientType.PRIVATE)
    @Shared
    private static final Client CLIENT_CORPORATE = new Client(new PersonName(FIRST_NAME, LAST_NAME), COMPANY_NAME, ClientType.CORPORATE)
    private static final ClientBasicDto CLIENT_WITH_NO_TYPE = new ClientBasicDto(null, null, FIRST_NAME, LAST_NAME, COMPANY_NAME);
    private static final ClientBasicDto PRIVATE_CLIENT_DTO = new ClientBasicDto(null, ClientType.PRIVATE, FIRST_NAME, LAST_NAME, null);
    private static final ClientBasicDto CORPORATE_CLIENT_DTO = new ClientBasicDto(null, ClientType.CORPORATE, null, null, COMPANY_NAME);

    def setupSpec() {
        def address = new Address(POST_CODE, CITY, STREET, HOUSE, FLAT);
        CLIENT_PRIVATE.updateAddress(address)
        CLIENT_PRIVATE.updateEmail(EMAIL)
        CLIENT_PRIVATE.updateNote(NOTE)
        CLIENT_PRIVATE.updateTelephone(TELEPHONE)
    }


    def clientRepository = Mock(ClientRepositoryImpl) {
        load(NON_EXISTING_ID) >> { null }
        load(EXISTING_PRIVATE_ID) >> { CLIENT_PRIVATE }
        load(EXISTING_CORPORATE_ID) >> { CLIENT_CORPORATE }
    }

    ClientValidator clientValidator = Stub {
        validateClientBasicDto(null) >> { throw new IllegalArgumentException() }
    }
    def ClientApplicationServiceImpl = new ClientApplicationServiceImpl(clientRepository, clientValidator);


    def "client should not be saved when ClientBasicDto is null"() {
        given:
            ClientBasicDto clientBasicDto = null;
        when:
            ClientApplicationServiceImpl.createClient(clientBasicDto);
        then:
            thrown(IllegalArgumentException);
            0 * clientRepository.save(_);
    }

    def "client should not be saved when ClientBasicDto has no type"() {
        given:
        when:
            ClientApplicationServiceImpl.createClient(CLIENT_WITH_NO_TYPE);
        then:
            thrown(IllegalArgumentException);
            0 * clientRepository.save(_);
    }

    def "client should be saved when dto with proper private client data passed"() {
        given:
        when:
            ClientApplicationServiceImpl.createClient(PRIVATE_CLIENT_DTO);
        then:
            noExceptionThrown();
            1 * clientRepository.save(_);
    }

    def "client should be saved when dto with proper corporate client data passed"() {
        given:
        when:
            ClientApplicationServiceImpl.createClient(CORPORATE_CLIENT_DTO);
        then:
            noExceptionThrown();
            1 * clientRepository.save(_);
    }

    def "getClient should not return clientDto and exception should be thrown when passing non existing client id"() {
        given:
        when:
            ClientDto clientDto = ClientApplicationServiceImpl.getClient(NON_EXISTING_ID);
        then:
            thrown(IllegalArgumentException);
            clientDto == null;
    }

    def "getClient should return clientDto and no exception should be thrown when passing existing client id"() {
        given:
        when:
            ClientDto clientDto = ClientApplicationServiceImpl.getClient(EXISTING_PRIVATE_ID);
        then:
            noExceptionThrown();
            clientDto.getFirstName() == FIRST_NAME;
            clientDto.getLastName() == LAST_NAME;
    }

    def "update should throw an exception when non existing client id is passes and client should not be updated"() {
        given:
            def clientDto = new ClientDto(null, null, null, null, null, null);
        when:
            ClientApplicationServiceImpl.updateClient(NON_EXISTING_ID, clientDto)
        then:
            thrown(IllegalArgumentException)
            0 * clientRepository.save(_)
    }

    def "update should throw an exception when passing null in dto and client should not be updated"() {
        given:
            def clientDto = null;
        when:
            ClientApplicationServiceImpl.updateClient(EXISTING_PRIVATE_ID, clientDto)
        then:
            thrown(IllegalArgumentException)
            0 * clientRepository.save(_)
    }

    def "update should throw an exception when passing private client with missing first name last name and client should not be updated"() {
        given:
            def address = new AddressDto(CITY, POST_CODE, STREET, HOUSE, FLAT);
            def contact = new ContactDto(address, EMAIL, TELEPHONE);
            def clientDto = new ClientDto("", "", null, contact, null, ClientType.PRIVATE);
        when:
            ClientApplicationServiceImpl.updateClient(EXISTING_PRIVATE_ID, clientDto);
        then:
            Exception ex = thrown()
            ex.message == "isEmpty.client.firstName"
            0 * clientRepository.save(_)
    }

    def "update should change client first name and last name when proper private client dto is passed"() {
        given:
            def address = new AddressDto(CITY, POST_CODE, STREET, HOUSE, FLAT);
            def contact = new ContactDto(address, EMAIL, TELEPHONE);
            def clientDto = new ClientDto(NEW_FIRST_NAME, NEW_LAST_NAME, null, contact, null, ClientType.PRIVATE);
        when:
            ClientApplicationServiceImpl.updateClient(EXISTING_PRIVATE_ID, clientDto)
        then:
            noExceptionThrown();
            1 * clientRepository.save({
                Client clientResult ->
                    clientResult.getPersonName().getFirstName() == NEW_FIRST_NAME;
                    clientResult.getPersonName().getLastName() == NEW_LAST_NAME;
            })
    }

    def "update should throw an exception when passing corporate client with missing corporate name and client should not be updated"() {
        given:
            def address = new AddressDto(CITY, POST_CODE, STREET, HOUSE, FLAT);
            def contact = new ContactDto(address, EMAIL, TELEPHONE);
            def clientDto = new ClientDto(null, null, "", contact, null, ClientType.CORPORATE);
        when:
            ClientApplicationServiceImpl.updateClient(EXISTING_CORPORATE_ID, clientDto);
        then:
            Exception ex = thrown()
            ex.message == "isEmpty.client.companyName"
            0 * clientRepository.save(_)
    }

    def "update should change company name when proper corporate client dto is passed"() {
        given:
            def address = new AddressDto(CITY, POST_CODE, STREET, HOUSE, FLAT);
            def contact = new ContactDto(address, EMAIL, TELEPHONE);
            def clientDto = new ClientDto(null, null, NEW_COMPANY_NAME, contact, null, ClientType.CORPORATE);
        when:
            ClientApplicationServiceImpl.updateClient(EXISTING_CORPORATE_ID, clientDto)
        then:
            noExceptionThrown();
            1 * clientRepository.save({
                Client clientResult ->
                    clientResult.getCompanyName() == NEW_COMPANY_NAME;
            })
    }

    def "update should not change client contact data when contact in dto is null"() {
        given:
            def clientDto = new ClientDto(FIRST_NAME, LAST_NAME, "", null, null, ClientType.PRIVATE);
        when:
            ClientApplicationServiceImpl.updateClient(EXISTING_PRIVATE_ID, clientDto);
        then:
            noExceptionThrown()
            1 * clientRepository.save({
                Client clientResult ->
                    def resultAddress = clientResult.getAddress();
                    resultAddress.getCity() == CITY;
                    resultAddress.getPostCode() == POST_CODE;
                    resultAddress.getStreet() == STREET;
                    resultAddress.getHouseNumber() == HOUSE;
                    resultAddress.getFlatNumber() == FLAT;
            })
    };

    def "update should change client address when contact in dto is null"() {
        given:
            def address = new AddressDto(NEW_CITY, NEW_POST_CODE, NEW_STREET, NEW_HOUSE, NEW_FLAT);
            def contact = new ContactDto(address, EMAIL, TELEPHONE);
            def clientDto = new ClientDto(NEW_FIRST_NAME, NEW_LAST_NAME, null, contact, null, ClientType.PRIVATE);
        when:
            ClientApplicationServiceImpl.updateClient(EXISTING_PRIVATE_ID, clientDto)
        then:
            noExceptionThrown();
            1 * clientRepository.save({
                Client clientResult ->
                    def resultAddress = clientResult.getAddress();
                    resultAddress.getCity() == NEW_CITY;
                    resultAddress.getPostCode() == NEW_POST_CODE;
                    resultAddress.getStreet() == NEW_STREET;
                    resultAddress.getHouseNumber() == NEW_HOUSE;
                    resultAddress.getFlatNumber() == NEW_FLAT;
            })
    };

    def "update should not throw an exception when passing client with no email and email address should not be updated"() {
        given:
            def address = new AddressDto(CITY, POST_CODE, STREET, HOUSE, FLAT);
            def contact = new ContactDto(address, null, TELEPHONE);
            def clientDto = new ClientDto(FIRST_NAME, LAST_NAME, null, contact, null, ClientType.PRIVATE);
        when:
            ClientApplicationServiceImpl.updateClient(EXISTING_PRIVATE_ID, clientDto);
        then:
            noExceptionThrown();
            1 * clientRepository.save({
                Client clientResult ->
                    clientResult.getEmail().getValue() == EMAIL;
            })
    };

    def "update should update email value when proper client dto with email passed"() {
        given:
            def address = new AddressDto(CITY, POST_CODE, STREET, HOUSE, FLAT);
            def contact = new ContactDto(address, NEW_EMAIL, TELEPHONE);
            def clientDto = new ClientDto(FIRST_NAME, LAST_NAME, null, contact, null, ClientType.PRIVATE);
        when:
            ClientApplicationServiceImpl.updateClient(EXISTING_PRIVATE_ID, clientDto);
        then:
            noExceptionThrown();
            1 * clientRepository.save({
                Client clientResult ->
                    clientResult.getEmail().getValue() == NEW_EMAIL;
            })
    };

    def "update should update telephone value when proper client dto with telephone passed"() {
        given:
            def address = new AddressDto(CITY, POST_CODE, STREET, HOUSE, FLAT);
            def contact = new ContactDto(address, EMAIL, NEW_TELEPHONE);
            def clientDto = new ClientDto(FIRST_NAME, LAST_NAME, null, contact, null, ClientType.PRIVATE);
        when:
            ClientApplicationServiceImpl.updateClient(EXISTING_PRIVATE_ID, clientDto);
        then:
            noExceptionThrown();
            1 * clientRepository.save({
                Client clientResult ->
                    clientResult.getTelephone() == NEW_TELEPHONE;
            })
    };

    def "update should update set null as a new telephone when proper client dto with null as a telephone passed"() {
        given:
            def address = new AddressDto(CITY, POST_CODE, STREET, HOUSE, FLAT);
            def contact = new ContactDto(address, EMAIL, null);
            def clientDto = new ClientDto(FIRST_NAME, LAST_NAME, null, contact, null, ClientType.PRIVATE);
        when:
            ClientApplicationServiceImpl.updateClient(EXISTING_PRIVATE_ID, clientDto);
        then:
            noExceptionThrown();
            1 * clientRepository.save({
                Client clientResult ->
                    clientResult.getTelephone() == null;
            })
    };

    def "update should change note value when proper client dto with note passed"() {
        given:
            def address = new AddressDto(CITY, POST_CODE, STREET, HOUSE, FLAT);
            def contact = new ContactDto(address, EMAIL, TELEPHONE);
            def clientDto = new ClientDto(FIRST_NAME, LAST_NAME, null, contact, new ClientAdditionalDataDto(NEW_NOTE, null), ClientType.PRIVATE);
        when:
            ClientApplicationServiceImpl.updateClient(EXISTING_PRIVATE_ID, clientDto);
        then:
            noExceptionThrown();
            1 * clientRepository.save({
                Client clientResult ->
                    clientResult.getNote() == NEW_NOTE;
            })
    };

    def "update should change note to null when proper client dto with null as a note passed"() {
        given:
            def address = new AddressDto(CITY, POST_CODE, STREET, HOUSE, FLAT);
            def contact = new ContactDto(address, EMAIL, TELEPHONE);
            def clientDto = new ClientDto(FIRST_NAME, LAST_NAME, null, contact, new ClientAdditionalDataDto(null, null), ClientType.PRIVATE);
        when:
            ClientApplicationServiceImpl.updateClient(EXISTING_PRIVATE_ID, clientDto);
        then:
            noExceptionThrown();
            1 * clientRepository.save({
                Client clientResult ->
                    clientResult.getNote() == null;
            })
    };

}
