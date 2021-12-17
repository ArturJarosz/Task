package com.arturjarosz.task.architect.application

import com.arturjarosz.task.DatabaseMain
import com.arturjarosz.task.architect.application.dto.ArchitectBasicDto
import com.arturjarosz.task.architect.application.dto.ArchitectDto
import com.arturjarosz.task.architect.infrastructure.repository.ArchitectRepository
import com.arturjarosz.task.architect.rest.ArchitectRestController
import com.arturjarosz.task.configuration.BaseTestIT
import com.arturjarosz.task.sharedkernel.exceptions.ErrorMessage
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.env.Environment
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.spock.Testcontainers

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@SpringBootTest(classes = DatabaseMain.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Testcontainers
@AutoConfigureMockMvc
class ArchitectTestIT extends BaseTestIT {
    private static final String FIRST_NAME = "name";
    private static final String LAST_NAME = "last_name";
    private final static String HOST = "http://localhost"
    private final static String ARCHITECTS_URI = "/architects"
    private final static ObjectMapper MAPPER = new ObjectMapper();

    @Value('${server.port}')
    private String port;
    @Autowired
    private ArchitectRepository architectRepository;
    @Autowired
    private ArchitectRestController architectRestController;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    Environment environment;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    def "Creating Architect with proper DTO should return created architect and 201 code"() {
        given:
            ArchitectBasicDto architectBasicDto = this.prepareArchitectBasicDto(FIRST_NAME, LAST_NAME);
            String requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(architectBasicDto);
        when:
            def response = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + ARCHITECTS_URI))
                            .header("Content-Type", "application/json")
                            .content(requestBody)
            ).andReturn().response;
        then:
            response.status == HttpStatus.CREATED.value();
        and:
            ArchitectDto architectDto = MAPPER.readValue(response.contentAsString, ArchitectDto.class);
            architectDto.getFirstName() == FIRST_NAME;
            architectDto.getLastName() == LAST_NAME;
        and:
            response.getHeader("Location") == ARCHITECTS_URI + "/" + architectDto.getId();

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    def "Creating Architect with not proper DTO should return code 400 and not create new Architect"() {
        given:
            ArchitectBasicDto architectBasicDto = this.prepareArchitectBasicDto(null, LAST_NAME);
            String requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(architectBasicDto);
        when:
            def response = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + ARCHITECTS_URI))
                            .header("Content-Type", "application/json")
                            .content(requestBody)
            ).andReturn().response;
        then:
            response.status == HttpStatus.BAD_REQUEST.value();
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    def "Removing existing Architect will return code 200 and remove it from database"() {
        given:
            ArchitectBasicDto architectBasicDto = this.prepareArchitectBasicDto(FIRST_NAME, LAST_NAME);
            String requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(architectBasicDto);
            def createdArchitectString = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + ARCHITECTS_URI))
                            .header("Content-Type", "application/json")
                            .content(requestBody)
            ).andReturn().response.contentAsString
            ArchitectDto architectDto = MAPPER.readValue(createdArchitectString, ArchitectDto.class);
        when:
            def response = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .delete(URI.create(HOST + ":" + port + ARCHITECTS_URI + "/" + architectDto.getId()))
                            .header("Content-Type", "application/json")
            ).andReturn().response;
        then:
            response.status == HttpStatus.OK.value();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    def "Removing not existing Architect will give code 400 and error message about not existing Architect"() {
        given:
        when:
            def response = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .delete(URI.create(HOST + ":" + port + ARCHITECTS_URI + "/" + 2000))
                            .header("Content-Type", "application/json")
            ).andReturn().response;
        then:
            response.status == HttpStatus.BAD_REQUEST.value();
        and:
            def errorMessage = MAPPER.readValue(response.contentAsString, ErrorMessage.class);
            errorMessage.message == "Architect with id 2,000 does not exist.";
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    def "Calling get on /architects should return all architects basic dto"() {
        given:
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder(URI.create(HOST + ":" + port + ARCHITECTS_URI)).GET()
                    .build();
        when:
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        then:
            response.statusCode() == HttpStatus.OK.value();
            List<ArchitectBasicDto> architects = MAPPER.readValue(response.body(), List<ArchitectBasicDto>.class);
            architects.isEmpty();
    }


    private ArchitectBasicDto prepareArchitectBasicDto(String firstName, String lastName) {
        ArchitectBasicDto architectBasicDto = new ArchitectBasicDto();
        architectBasicDto.setFirstName(firstName);
        architectBasicDto.setLastName(lastName);
        return architectBasicDto;
    }
}
