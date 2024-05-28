package com.arturjarosz.task.architect


import com.arturjarosz.task.configuration.BaseTestIT
import com.arturjarosz.task.dto.ArchitectDto
import com.arturjarosz.task.sharedkernel.exceptions.ErrorMessage
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.transaction.annotation.Transactional

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class ArchitectTestIT extends BaseTestIT {
    static final String FIRST_NAME = "First Name"
    static final String LAST_NAME = "Last Name"
    static final String NEW_FIRST_NAME = "New Name"
    static final String NEW_LAST_NAME = "New Last Name"
    static final String ARCHITECTS_URI = "/architects"
    static final ObjectMapper MAPPER = new ObjectMapper()

    @Autowired
    MockMvc mockMvc

    @Transactional
    def "Creating Architect with proper DTO should return created architect and 201 code"() {
        given:
            def requestArchitectDto = this.prepareArchitectDto(FIRST_NAME, LAST_NAME)
            def requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(requestArchitectDto)
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + ARCHITECTS_URI))
                    .header("Content-Type", "application/json")
                    .content(requestBody)).andReturn().response
        then:
            response.status == HttpStatus.CREATED.value()
        and:
            ArchitectDto architectDto = MAPPER.readValue(response.contentAsString, ArchitectDto)
            architectDto.firstName == FIRST_NAME
            architectDto.lastName == LAST_NAME
            architectDto.createdDateTime != null
        and:
            response.getHeader("Location") == ARCHITECTS_URI + "/" + architectDto.id

    }

    @Transactional
    def "Creating Architect with not proper DTO should return code 400 and not create new Architect"() {
        given:
            def architectDto = this.prepareArchitectDto(null, LAST_NAME)
            def requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(architectDto)
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + ARCHITECTS_URI))
                    .header("Content-Type", "application/json")
                    .content(requestBody)).andReturn().response
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
    }


    @Transactional
    def "Removing existing Architect will return code 200 and remove it from database"() {
        given:
            def requestArchitectDto = this.prepareArchitectDto(FIRST_NAME, LAST_NAME)
            def requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(requestArchitectDto)
            def createdArchitectString = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + ARCHITECTS_URI))
                    .header("Content-Type", "application/json")
                    .content(requestBody)).andReturn().response.contentAsString
            def architectDto = MAPPER.readValue(createdArchitectString, ArchitectDto)
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders
                    .delete(URI.create(HOST + ":" + port + ARCHITECTS_URI + "/" + architectDto.id))
                    .header("Content-Type", "application/json")).andReturn().response
        then:
            response.status == HttpStatus.OK.value()
    }

    @Transactional
    def "Removing not existing Architect will give code 404 and error message about not existing Architect"() {
        given:
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders
                    .delete(URI.create(HOST + ":" + port + ARCHITECTS_URI + "/" + 2000))
                    .header("Content-Type", "application/json")).andReturn().response
        then:
            response.status == HttpStatus.NOT_FOUND.value()
        and:
            def errorMessage = MAPPER.readValue(response.contentAsString, ErrorMessage)
            errorMessage.message == "Architect with id 2,000 does not exist."
    }

    @Transactional
    def "Calling /get with existing Architect id should return ArchitectDto with proper data"() {
        given:
            def architectDto = this.prepareArchitectDto(FIRST_NAME, LAST_NAME)
            def requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(architectDto)
            def createdArchitectString = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + ARCHITECTS_URI))
                    .header("Content-Type", "application/json")
                    .content(requestBody)).andReturn().response.contentAsString
            def createdArchitectDto = MAPPER.readValue(createdArchitectString, ArchitectDto)
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders
                    .get(URI.create(HOST + ":" + port + ARCHITECTS_URI + "/" + createdArchitectDto.id))
                    .header("Content-Type", "application/json")).andReturn().response
        then:
            response.status == HttpStatus.OK.value()
        and:
            def architect = MAPPER.readValue(response.contentAsString, ArchitectDto)
            architect.firstName == FIRST_NAME
            architect.lastName == LAST_NAME
    }

    @Transactional
    def "Calling /get with not existing Architect id should return error message about not existing Architect"() {
        given:
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders
                    .get(URI.create(HOST + ":" + port + ARCHITECTS_URI + "/" + 2000))
                    .header("Content-Type", "application/json")).andReturn().response
        then:
            response.status == HttpStatus.NOT_FOUND.value()
        and:
            def errorMessage = MAPPER.readValue(response.contentAsString, ErrorMessage)
            errorMessage.message == "Architect with id 2,000 does not exist."
    }

    @Transactional
    def "Updating existing Architect with proper data should update Architect and return updated ArchitectDto"() {
        given:
            def architectDto = this.prepareArchitectDto(FIRST_NAME, LAST_NAME)
            String requestBodyCreate =
                    MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(architectDto)
            def createdArchitectString = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + ARCHITECTS_URI))
                    .header("Content-Type", "application/json")
                    .content(requestBodyCreate)).andReturn().response.contentAsString
            def createdArchitectDto = MAPPER.readValue(createdArchitectString, ArchitectDto)
            def updateArchitectDto = this.prepareArchitectDto(NEW_FIRST_NAME, NEW_LAST_NAME)
            String requestBodyUpdate =
                    MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(updateArchitectDto)
        when:
            def updatedArchitectString = this.mockMvc.perform(MockMvcRequestBuilders
                    .put(URI.create(HOST + ":" + port + ARCHITECTS_URI + "/" + createdArchitectDto.id))
                    .header("Content-Type", "application/json")
                    .content(requestBodyUpdate)).andReturn().response
        then:
            updatedArchitectString.status == HttpStatus.OK.value()
        and:
            def architect = MAPPER.readValue(updatedArchitectString.contentAsString, ArchitectDto)
            architect.firstName == NEW_FIRST_NAME
            architect.lastName == NEW_LAST_NAME
            architect.lastModifiedDateTime != null
    }

    @Transactional
    def "Updating existing Architect with not proper data should not update Architect and return error message"() {
        given:
            def architectDto = this.prepareArchitectDto(FIRST_NAME, LAST_NAME)
            String requestBodyCreate =
                    MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(architectDto)
            def createdArchitectString = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + ARCHITECTS_URI))
                    .header("Content-Type", "application/json")
                    .content(requestBodyCreate)).andReturn().response.contentAsString
            def createdArchitectDto = MAPPER.readValue(createdArchitectString, ArchitectDto)
            def updateArchitectDto = this.prepareArchitectDto("", NEW_LAST_NAME)
            String requestBodyUpdate =
                    MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(updateArchitectDto)
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders
                    .put(URI.create(HOST + ":" + port + ARCHITECTS_URI + "/" + createdArchitectDto.id))
                    .header("Content-Type", "application/json")
                    .content(requestBodyUpdate)).andReturn().response
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            def errorMessage = MAPPER.readValue(response.contentAsString, ErrorMessage)
            errorMessage.message == "Architect first name cannot be empty."
    }

    @Transactional
    def "Updating not existing Architect should return error message"() {
        given:
            def updateArchitectDto = this.prepareArchitectDto("", NEW_LAST_NAME)
            String requestBodyUpdate =
                    MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(updateArchitectDto)
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders
                    .put(URI.create(HOST + ":" + port + ARCHITECTS_URI + "/" + 2000))
                    .header("Content-Type", "application/json")
                    .content(requestBodyUpdate)).andReturn().response
        then:
            response.status == HttpStatus.NOT_FOUND.value()
        and:
            def errorMessage = MAPPER.readValue(response.contentAsString, ErrorMessage)
            errorMessage.message == "Architect with id 2,000 does not exist."
    }


    @Transactional
    def "Calling get on /architects should return all architects basic dto"() {
        given:
            HttpClient httpClient = HttpClient.newHttpClient()
            HttpRequest request = HttpRequest.newBuilder(URI.create(HOST + ":" + port + ARCHITECTS_URI)).GET()
                    .build()
        when:
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        then:
            response.statusCode() == HttpStatus.OK.value()
            List<ArchitectDto> architects = MAPPER.readValue(response.body(), List<ArchitectDto>)
            architects.isEmpty()
    }


    private ArchitectDto prepareArchitectDto(String firstName, String lastName) {
        return new ArchitectDto(firstName: firstName, lastName: lastName)
    }
}
