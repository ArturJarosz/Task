package com.arturjarosz.task.client

import com.arturjarosz.task.configuration.BaseTestIT
import com.arturjarosz.task.dto.ClientDto
import com.arturjarosz.task.sharedkernel.exceptions.ErrorMessage
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.transaction.annotation.Transactional

class ClientTestIT extends BaseTestIT {

    static final String CLIENTS_URI = "/clients"
    static final long NOT_EXISTING_CLIENT_ID = 2000
    static final ObjectMapper MAPPER = new ObjectMapper()

    final ClientDto privateClient = MAPPER.readValue(new File(getClass().classLoader.getResource('json/client/privateClient.json').file),
            ClientDto)
    final ClientDto corporateClient = MAPPER.readValue(new File(getClass().classLoader.getResource('json/client/corporateClient.json').file),
            ClientDto)
    final ClientDto privateClientNotProper = MAPPER.readValue(new File(getClass().classLoader.getResource('json/client/privateClientNotProper.json').file),
            ClientDto)
    final ClientDto corporateClientNotProper = MAPPER.readValue(new File(getClass().classLoader.getResource('json/client/corporateClientNotProper.json').file),
            ClientDto)
    final ClientDto updateClient = MAPPER.readValue(new File(getClass().classLoader.getResource('json/client/updateClient.json').file),
            ClientDto)
    final ClientDto updateClientNotProper = MAPPER.readValue(new File(getClass().classLoader.getResource('json/client/updateClientNotProper.json').file), ClientDto.class)

    @Autowired
    private MockMvc mockMvc

    @Transactional
    def "Creating private client with proper dto should return code 201, created client dto and client location header"() {
        given:
            def requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(privateClient)
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + CLIENTS_URI))
                    .header("Content-Type", "application/json")
                    .content(requestBody)).andReturn().response
        then:
            response.status == HttpStatus.CREATED.value()
        and:
            def createdClient = MAPPER.readValue(response.contentAsString, ClientDto)
        and:
            response.getHeader("Location") == CLIENTS_URI + "/" + createdClient.id
            createdClient.createdDateTime != null
    }

    @Transactional
    def "Creating corporate client with proper dto should return code 201, created client dto and client location header"() {
        given:
            def requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(corporateClient)
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + CLIENTS_URI))
                    .header("Content-Type", "application/json")
                    .content(requestBody)).andReturn().response
        then:
            response.status == HttpStatus.CREATED.value()
        and:
            def createdClient = MAPPER.readValue(response.contentAsString, ClientDto)
        and:
            response.getHeader("Location") == CLIENTS_URI + "/" + createdClient.id
    }

    @Transactional
    def "Creating private client with not proper dto should give code 400, not create client"() {
        given:
            def requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(privateClientNotProper)
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + CLIENTS_URI))
                    .header("Content-Type", "application/json")
                    .content(requestBody)).andReturn().response
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            def errorMessage = MAPPER.readValue(response.contentAsString, ErrorMessage)
            errorMessage.message == "Client first name cannot be empty."
    }

    @Transactional
    def "Creating corporate client with not proper dto should give code 400, not create client"() {
        given:
            def requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(corporateClientNotProper)
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + CLIENTS_URI))
                    .header("Content-Type", "application/json")
                    .content(requestBody)).andReturn().response
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            def errorMessage = MAPPER.readValue(response.contentAsString, ErrorMessage)
            errorMessage.message == "Client company name cannot be empty."
    }

    @Transactional
    def "Removing existing client should give code 200, and remove client"() {
        given:
            def requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(privateClient)
            def createdClientResponse = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + CLIENTS_URI))
                    .header("Content-Type", "application/json")
                    .content(requestBody)).andReturn().response.contentAsString
            def createdClient = MAPPER.readValue(createdClientResponse, ClientDto)
        when:
            def removedClientResponse = this.mockMvc.perform(MockMvcRequestBuilders
                    .delete(URI.create(HOST + ":" + port + CLIENTS_URI + "/" + createdClient.id))).andReturn().response
        then:
            removedClientResponse.status == HttpStatus.OK.value()
    }

    @Transactional
    def "Removing not existing client should give code 404 and error message"() {
        given:
        when:
            def removedClientResponse = this.mockMvc.perform(MockMvcRequestBuilders
                    .delete(URI.create(HOST + ":" + port + CLIENTS_URI + "/" + NOT_EXISTING_CLIENT_ID))).andReturn().response
        then:
            removedClientResponse.status == HttpStatus.NOT_FOUND.value()
            def errorMessage = MAPPER.readValue(removedClientResponse.contentAsString, ErrorMessage)
            errorMessage.message == "Client with id 2,000 does not exist."
    }

    @Transactional
    def "Updating existing client should give coe 200 and return updated client dto"() {
        given:
            def requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(privateClient)
            def createdClientResponse = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + CLIENTS_URI))
                    .header("Content-Type", "application/json")
                    .content(requestBody)).andReturn().response.contentAsString
            def createdClient = MAPPER.readValue(createdClientResponse, ClientDto)
            String updateRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(updateClient)
        when:
            def updatedClientResponse = this.mockMvc.perform(MockMvcRequestBuilders
                    .put(URI.create(HOST + ":" + port + CLIENTS_URI + "/" + createdClient.id))
                    .content(updateRequestBody)
                    .header("Content-Type", "application/json")).andReturn().response
        then:
            updatedClientResponse.status == HttpStatus.OK.value()
        and:
            ClientDto updatedClient = MAPPER.readValue(updatedClientResponse.contentAsString, ClientDto)
            with(updatedClient) {
                firstName == updatedClient.firstName
                lastName == updateClient.lastName
                note == updateClient.note
                with(contact) {
                    address.city == updateClient.contact.address.city
                    address.postCode == updateClient.contact.address.postCode
                    address.street == updateClient.contact.address.street
                    email == updateClient.contact.email
                    telephone == updatedClient.contact.telephone
                    lastModifiedDateTime != null
                }
            }
    }

    @Transactional
    def "Updating not existing client should give code 404 and error message"() {
        given:
            def updateRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(updateClient)
        when:
            def updatedClientResponse = this.mockMvc.perform(MockMvcRequestBuilders
                    .put(URI.create(HOST + ":" + port + CLIENTS_URI + "/" + NOT_EXISTING_CLIENT_ID))
                    .content(updateRequestBody)
                    .header("Content-Type", "application/json")).andReturn().response
        then:
            updatedClientResponse.status == HttpStatus.NOT_FOUND.value()
            def errorMessage = MAPPER.readValue(updatedClientResponse.contentAsString, ErrorMessage)
            errorMessage.message == "Client with id 2,000 does not exist."
    }

    @Transactional
    def "Updating client with not proper client dto should give code 400 and not update client data"() {
        given:
            def requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(privateClient)
            def createdClientResponse = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + CLIENTS_URI))
                    .header("Content-Type", "application/json")
                    .content(requestBody)).andReturn().response.contentAsString
            def createdClient = MAPPER.readValue(createdClientResponse, ClientDto)
            String updateRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(updateClientNotProper)
        when:
            def updatedClientResponse = this.mockMvc.perform(MockMvcRequestBuilders
                    .put(URI.create(HOST + ":" + port + CLIENTS_URI + "/" + createdClient.id))
                    .content(updateRequestBody)
                    .header("Content-Type", "application/json")).andReturn().response
        then:
            updatedClientResponse.status == HttpStatus.BAD_REQUEST.value()
            ErrorMessage errorMessage = MAPPER.readValue(updatedClientResponse.contentAsString, ErrorMessage)
            errorMessage.message == "Client last name cannot be empty."
    }

    @Transactional
    def "Getting existing client should return code 200 and dto of existing client"() {
        given:
            def requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(privateClient)
            def createdClientResponse = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + CLIENTS_URI))
                    .header("Content-Type", "application/json")
                    .content(requestBody)).andReturn().response.contentAsString
            def createdClient = MAPPER.readValue(createdClientResponse, ClientDto)
        when:
            def clientResponse = this.mockMvc.perform(MockMvcRequestBuilders
                    .get(URI.create(HOST + ":" + port + CLIENTS_URI + "/" + createdClient.id))
                    .header("Content-Type", "application/json")).andReturn().response
        then:
            clientResponse.status == HttpStatus.OK.value()
        and:
            def client = MAPPER.readValue(clientResponse.contentAsString, ClientDto)
            client.firstName == privateClient.firstName
            client.lastName == privateClient.lastName
    }

    @Transactional
    def "Getting not existing client should return code 404 and error message"() {
        given:
        when:
            def clientResponse = this.mockMvc.perform(MockMvcRequestBuilders
                    .get(URI.create(HOST + ":" + port + CLIENTS_URI + "/" + NOT_EXISTING_CLIENT_ID))).andReturn().response
        then:
            clientResponse.status == HttpStatus.NOT_FOUND.value()
            def errorMessage = MAPPER.readValue(clientResponse.contentAsString, ErrorMessage)
            errorMessage.message == "Client with id 2,000 does not exist."
    }

    @Transactional
    def "Getting all clients should return list of basic client dto of all existing clients"() {
        given:
            String requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(privateClient)
            this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + CLIENTS_URI))
                    .header("Content-Type", "application/json")
                    .content(requestBody)).andReturn().response.contentAsString
            String secondRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(corporateClient)
            this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + CLIENTS_URI))
                    .header("Content-Type", "application/json")
                    .content(secondRequestBody)).andReturn().response
        when:
            def clientsResponse = this.mockMvc.perform(MockMvcRequestBuilders
                    .get(URI.create(HOST + ":" + port + CLIENTS_URI))).andReturn().response
        then:
            clientsResponse.status == HttpStatus.OK.value()
            List<ClientDto> createdClients = MAPPER.readValue(clientsResponse.contentAsString, List<ClientDto>)
            createdClients.size() == 2
    }
}
