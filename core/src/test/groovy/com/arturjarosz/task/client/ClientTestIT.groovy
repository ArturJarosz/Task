package com.arturjarosz.task.client

import com.arturjarosz.task.client.application.dto.ClientDto
import com.arturjarosz.task.configuration.BaseTestIT
import com.arturjarosz.task.sharedkernel.exceptions.ErrorMessage
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.transaction.annotation.Transactional

class ClientTestIT extends BaseTestIT {

    private static final String CLIENTS_URI = "/clients"
    private static final long NOT_EXISTING_CLIENT_ID = 2000
    private static final ObjectMapper MAPPER = new ObjectMapper()

    private final ClientDto privateClient =
            MAPPER.readValue(new File(getClass().classLoader.getResource('json/client/privateClient.json').file),
                    ClientDto.class)
    private final ClientDto corporateClient =
            MAPPER.readValue(new File(getClass().classLoader.getResource('json/client/corporateClient.json').file),
                    ClientDto.class)
    private final ClientDto privateClientNotProper =
            MAPPER.readValue(new File(
                    getClass().classLoader.getResource('json/client/privateClientNotProper.json').file),
                    ClientDto.class)
    private final ClientDto corporateClientNotProper =
            MAPPER.readValue(new File(
                    getClass().classLoader.getResource('json/client/corporateClientNotProper.json').file),
                    ClientDto.class)
    private final ClientDto updateClient =
            MAPPER.readValue(new File(getClass().classLoader.getResource('json/client/updateClient.json').file),
                    ClientDto.class)
    private final ClientDto updateClientNotProper =
            MAPPER.readValue(new File(
                    getClass().classLoader.getResource('json/client/updateClientNotProper.json').file), ClientDto.class)

    @Autowired
    private MockMvc mockMvc

    @Transactional
    def "Creating private client with proper dto should return code 201, created client dto and client location header"() {
        given:
            String requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(privateClient)
        when:
            def response = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + CLIENTS_URI))
                            .header("Content-Type", "application/json")
                            .content(requestBody)
            ).andReturn().response
        then:
            response.status == HttpStatus.CREATED.value()
        and:
            ClientDto createdClient = MAPPER.readValue(response.contentAsString, ClientDto.class)
        and:
            response.getHeader("Location") == CLIENTS_URI + "/" + createdClient.id
    }

    @Transactional
    def "Creating corporate client with proper dto should return code 201, created client dto and client location header"() {
        given:
            String requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(corporateClient)
        when:
            def response = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + CLIENTS_URI))
                            .header("Content-Type", "application/json")
                            .content(requestBody)
            ).andReturn().response
        then:
            response.status == HttpStatus.CREATED.value()
        and:
            ClientDto createdClient = MAPPER.readValue(response.contentAsString, ClientDto.class)
        and:
            response.getHeader("Location") == CLIENTS_URI + "/" + createdClient.id
    }

    @Transactional
    def "Creating private client with not proper dto should give code 400, not create client"() {
        given:
            String requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(privateClientNotProper)
        when:
            def response = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + CLIENTS_URI))
                            .header("Content-Type", "application/json")
                            .content(requestBody)
            ).andReturn().response
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            ErrorMessage errorMessage = MAPPER.readValue(response.contentAsString, ErrorMessage.class)
            errorMessage.message == "Client first name cannot be empty."
    }

    @Transactional
    def "Creating corporate client with not proper dto should give code 400, not create client"() {
        given:
            String requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(corporateClientNotProper)
        when:
            def response = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + CLIENTS_URI))
                            .header("Content-Type", "application/json")
                            .content(requestBody)
            ).andReturn().response
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            ErrorMessage errorMessage = MAPPER.readValue(response.contentAsString, ErrorMessage.class)
            errorMessage.message == "Client company name cannot be empty."
    }

    @Transactional
    def "Removing existing client should give code 200, and remove client"() {
        given:
            String requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(privateClient)
            def createdClientResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + CLIENTS_URI))
                            .header("Content-Type", "application/json")
                            .content(requestBody)
            ).andReturn().response.contentAsString
            ClientDto createdClient = MAPPER.readValue(createdClientResponse, ClientDto.class)
        when:
            def removedClientResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .delete(URI.create(HOST + ":" + port + CLIENTS_URI + "/" + createdClient.id))
            ).andReturn().response
        then:
            removedClientResponse.status == HttpStatus.OK.value()
    }

    @Transactional
    def "Removing not existing client should give code 400 and error message"() {
        given:
        when:
            def removedClientResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .delete(URI.create(HOST + ":" + port + CLIENTS_URI + "/" + NOT_EXISTING_CLIENT_ID))
            ).andReturn().response
        then:
            removedClientResponse.status == HttpStatus.BAD_REQUEST.value()
            ErrorMessage errorMessage = MAPPER.readValue(removedClientResponse.contentAsString, ErrorMessage.class)
            errorMessage.message == "Client with id 2,000 does not exist."
    }

    @Transactional
    def "Updating existing client should give coe 200 and return updated client dto"() {
        given:
            String requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(privateClient)
            def createdClientResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + CLIENTS_URI))
                            .header("Content-Type", "application/json")
                            .content(requestBody)
            ).andReturn().response.contentAsString
            ClientDto createdClient = MAPPER.readValue(createdClientResponse, ClientDto.class)
            String updateRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(updateClient)
        when:
            def updatedClientResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .put(URI.create(HOST + ":" + port + CLIENTS_URI + "/" + createdClient.id))
                            .content(updateRequestBody)
                            .header("Content-Type", "application/json")
            ).andReturn().response
        then:
            updatedClientResponse.status == HttpStatus.OK.value()
        and:
            ClientDto updatedClient = MAPPER.readValue(updatedClientResponse.contentAsString, ClientDto.class)
            updatedClient.firstName == updatedClient.firstName
            updatedClient.lastName == updateClient.lastName
            updatedClient.contact.address.city == updateClient.contact.address.city
            updatedClient.contact.address.postCode == updateClient.contact.address.postCode
            updatedClient.contact.address.street == updateClient.contact.address.street
            updatedClient.contact.address.houseNumber == updatedClient.contact.address.houseNumber
            updatedClient.contact.address.flatNumber == updateClient.contact.address.flatNumber
            updatedClient.contact.email == updateClient.contact.email
            updatedClient.contact.telephone == updatedClient.contact.telephone
            updatedClient.note == updateClient.note

    }

    @Transactional
    def "Updating not existing client should give code 400"() {
        given:
            String updateRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(updateClient)
        when:
            def updatedClientResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .put(URI.create(HOST + ":" + port + CLIENTS_URI + "/" + NOT_EXISTING_CLIENT_ID))
                            .content(updateRequestBody)
                            .header("Content-Type", "application/json")
            ).andReturn().response
        then:
            updatedClientResponse.status == HttpStatus.BAD_REQUEST.value()
            ErrorMessage errorMessage = MAPPER.readValue(updatedClientResponse.contentAsString, ErrorMessage.class)
            errorMessage.message == "Client with id 2,000 does not exist."
    }

    @Transactional
    def "Updating client with not proper client dto should give code 400 and not update client data"() {
        given:
            String requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(privateClient)
            def createdClientResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + CLIENTS_URI))
                            .header("Content-Type", "application/json")
                            .content(requestBody)
            ).andReturn().response.contentAsString
            ClientDto createdClient = MAPPER.readValue(createdClientResponse, ClientDto.class)
            String updateRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(updateClientNotProper)
        when:
            def updatedClientResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .put(URI.create(HOST + ":" + port + CLIENTS_URI + "/" + createdClient.id))
                            .content(updateRequestBody)
                            .header("Content-Type", "application/json")
            ).andReturn().response
        then:
            updatedClientResponse.status == HttpStatus.BAD_REQUEST.value()
            ErrorMessage errorMessage = MAPPER.readValue(updatedClientResponse.contentAsString, ErrorMessage.class)
            errorMessage.message == "Client last name cannot be empty."
    }

    @Transactional
    def "Getting existing client should return code 200 and dto of existing client"() {
        given:
            String requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(privateClient)
            def createdClientResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + CLIENTS_URI))
                            .header("Content-Type", "application/json")
                            .content(requestBody)
            ).andReturn().response.contentAsString
            ClientDto createdClient = MAPPER.readValue(createdClientResponse, ClientDto.class)
        when:
            def clientResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .get(URI.create(HOST + ":" + port + CLIENTS_URI + "/" + createdClient.id))
                            .header("Content-Type", "application/json")
            ).andReturn().response
        then:
            clientResponse.status == HttpStatus.OK.value()
        and:
            ClientDto client = MAPPER.readValue(clientResponse.contentAsString, ClientDto.class)
            client.firstName == privateClient.firstName
            client.lastName == privateClient.lastName
    }

    @Transactional
    def "Getting not existing client should return code 400 and error message"() {
        given:
        when:
            def clientResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .get(URI.create(HOST + ":" + port + CLIENTS_URI + "/" + NOT_EXISTING_CLIENT_ID))
            ).andReturn().response
        then:
            clientResponse.status == HttpStatus.BAD_REQUEST.value()
            ErrorMessage errorMessage = MAPPER.readValue(clientResponse.contentAsString, ErrorMessage.class)
            errorMessage.message == "Client with id 2,000 does not exist."
    }

    @Transactional
    def "Getting all clients should return list of basic client dto of all existing clients"() {
        given:
            String requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(privateClient)
            this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + CLIENTS_URI))
                            .header("Content-Type", "application/json")
                            .content(requestBody)
            ).andReturn().response.contentAsString
            String secondRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(corporateClient)
            this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + CLIENTS_URI))
                            .header("Content-Type", "application/json")
                            .content(secondRequestBody)
            ).andReturn().response
        when:
            def clientsResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .get(URI.create(HOST + ":" + port + CLIENTS_URI))
            ).andReturn().response
        then:
            clientsResponse.status == HttpStatus.OK.value()
            List<ClientDto> createdClients = MAPPER.readValue(clientsResponse.contentAsString, List<ClientDto>.class)
            createdClients.size() == 2
    }
}
