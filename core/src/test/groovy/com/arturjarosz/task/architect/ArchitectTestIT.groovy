package com.arturjarosz.task.architect


import com.arturjarosz.task.configuration.BaseTestIT
import com.arturjarosz.task.dto.ArchitectDto
import com.arturjarosz.task.dto.ArchitectUserLinkDto
import com.arturjarosz.task.dto.ClientDto
import com.arturjarosz.task.dto.ProjectCreateDto
import com.arturjarosz.task.sharedkernel.exceptions.ErrorMessage
import com.arturjarosz.task.user.infrastructure.repository.UserRepository
import com.arturjarosz.task.user.model.User
import com.arturjarosz.task.utils.TestsHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class ArchitectTestIT extends BaseTestIT {
    static final String FIRST_NAME = "First Name"
    static final String LAST_NAME = "Last Name"
    static final String NEW_FIRST_NAME = "New Name"
    static final String NEW_LAST_NAME = "New Last Name"
    static final String ARCHITECTS_URI = "/architects"
    static final String CLIENTS_URI = "/clients"
    static final String PROJECTS_URI = "/projects"
    static final Long NOT_EXISTING_ID = 99999999L
    static final ObjectMapper MAPPER = new ObjectMapper()

    @Autowired
    MockMvc mockMvc

    @Autowired
    UserRepository userRepository

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

    @Transactional
    def "Getting architect and architects should return number of all and active projects and their value"() {
        given: "Architect with two projects, one of them rejected"
            def architectDto = TestsHelper.createArchitect(this.prepareArchitectDto(FIRST_NAME, LAST_NAME),
                    HOST + ":" + port + ARCHITECTS_URI, this.mockMvc)
            def clientDto = TestsHelper.createClient(this.readJson('json/client/privateClient.json', ClientDto),
                    HOST + ":" + port + CLIENTS_URI, this.mockMvc)
            def projectCreateDto = this.readJson('json/project/properProject.json', ProjectCreateDto)
            projectCreateDto.architectId = architectDto.id
            projectCreateDto.clientId = clientDto.id
            projectCreateDto.typeId = projectTypeId
            TestsHelper.createProject(projectCreateDto, HOST + ":" + port + PROJECTS_URI, this.mockMvc)
            def rejectedProject = TestsHelper.createProject(projectCreateDto, HOST + ":" + port + PROJECTS_URI, this.mockMvc)
            this.mockMvc.perform(MockMvcRequestBuilders.post(
                    URI.create(HOST + ":" + port + PROJECTS_URI + "/" + rejectedProject.id + "/reject")))
        when:
            def architectResponse = this.mockMvc.perform(MockMvcRequestBuilders
                    .get(URI.create(HOST + ":" + port + ARCHITECTS_URI + "/" + architectDto.id))).andReturn().response
            def architectsResponse = this.mockMvc.perform(MockMvcRequestBuilders
                    .get(URI.create(HOST + ":" + port + ARCHITECTS_URI))).andReturn().response
        then:
            def architect = MAPPER.readValue(architectResponse.contentAsString, ArchitectDto)
            architect.numberOfProjects == 2
            architect.numberOfActiveProjects == 1
            architect.projectsValue == 2 * projectCreateDto.offerValue
        and:
            List<ArchitectDto> architects = MAPPER.readValue(architectsResponse.contentAsString,
                    MAPPER.typeFactory.constructCollectionType(List, ArchitectDto))
            def listedArchitect = architects.find { it.id == architectDto.id }
            listedArchitect.numberOfProjects == 2
            listedArchitect.numberOfActiveProjects == 1
            listedArchitect.projectsValue == 2 * projectCreateDto.offerValue
    }

    @Transactional
    def "Linking a user should return 200 and show the user on the architect and the list of architects"() {
        given:
            def architectDto = this.createArchitect()
            def user = this.createUser("link-1")
        when:
            def response = this.putUser(architectDto.id, new ArchitectUserLinkDto(userId: user.id))
        then:
            response.status == HttpStatus.OK.value()
            def linkedArchitect = MAPPER.readValue(response.contentAsString, ArchitectDto)
            linkedArchitect.linkedUser.id == user.id
            linkedArchitect.linkedUser.username == user.username
            linkedArchitect.linkedUser.email == user.email
            linkedArchitect.numberOfProjects == 0
        and:
            this.getArchitect(architectDto.id).linkedUser.id == user.id
        and:
            def architectsResponse = this.mockMvc.perform(MockMvcRequestBuilders
                    .get(URI.create(HOST + ":" + port + ARCHITECTS_URI))).andReturn().response
            List<ArchitectDto> architects = MAPPER.readValue(architectsResponse.contentAsString,
                    MAPPER.typeFactory.constructCollectionType(List, ArchitectDto))
            architects.find { it.id == architectDto.id }.linkedUser.id == user.id
    }

    @Transactional
    def "Getting architects should return them sorted by id, also after one of them changes"() {
        given:
            def first = this.createArchitect()
            def second = this.createArchitect()
            this.putUser(first.id, new ArchitectUserLinkDto(userId: this.createUser("link-1").id))
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders
                    .get(URI.create(HOST + ":" + port + ARCHITECTS_URI))).andReturn().response
        then:
            List<ArchitectDto> architects = MAPPER.readValue(response.contentAsString,
                    MAPPER.typeFactory.constructCollectionType(List, ArchitectDto))
            architects*.id == architects*.id.sort(false)
            architects*.id.containsAll([first.id, second.id])
    }

    @Transactional
    def "Getting a project should not show the user linked with its architect"() {
        given:
            def architectDto = this.createArchitect()
            this.putUser(architectDto.id, new ArchitectUserLinkDto(userId: this.createUser("link-1").id))
            def clientDto = TestsHelper.createClient(this.readJson('json/client/privateClient.json', ClientDto),
                    HOST + ":" + port + CLIENTS_URI, this.mockMvc)
            def projectCreateDto = this.readJson('json/project/properProject.json', ProjectCreateDto)
            projectCreateDto.architectId = architectDto.id
            projectCreateDto.clientId = clientDto.id
            projectCreateDto.typeId = projectTypeId
            def project = TestsHelper.createProject(projectCreateDto, HOST + ":" + port + PROJECTS_URI, this.mockMvc)
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders
                    .get(URI.create(HOST + ":" + port + PROJECTS_URI + "/" + project.id))).andReturn().response
        then:
            response.status == HttpStatus.OK.value()
            !response.contentAsString.contains("link-1@example.com")
    }

    @Transactional
    def "Linking another user should replace the user linked so far and free it for another architect"() {
        given:
            def architectDto = this.createArchitect()
            def firstUser = this.createUser("link-1")
            def secondUser = this.createUser("link-2")
            this.putUser(architectDto.id, new ArchitectUserLinkDto(userId: firstUser.id))
        when:
            def response = this.putUser(architectDto.id, new ArchitectUserLinkDto(userId: secondUser.id))
        then:
            response.status == HttpStatus.OK.value()
            this.getArchitect(architectDto.id).linkedUser.id == secondUser.id
        and:
            this.putUser(this.createArchitect().id, new ArchitectUserLinkDto(userId: firstUser.id)).status ==
                    HttpStatus.OK.value()
    }

    @Transactional
    def "Linking a user already linked with another architect should return 400 and keep both architects as they were"() {
        given:
            def firstArchitect = this.createArchitect()
            def secondArchitect = this.createArchitect()
            def user = this.createUser("link-1")
            this.putUser(firstArchitect.id, new ArchitectUserLinkDto(userId: user.id))
        when:
            def response = this.putUser(secondArchitect.id, new ArchitectUserLinkDto(userId: user.id))
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
            this.getArchitect(firstArchitect.id).linkedUser.id == user.id
            this.getArchitect(secondArchitect.id).linkedUser == null
    }

    @Transactional
    def "Linking the same user with the same architect again should return 200"() {
        given:
            def architectDto = this.createArchitect()
            def user = this.createUser("link-1")
            this.putUser(architectDto.id, new ArchitectUserLinkDto(userId: user.id))
        when:
            def response = this.putUser(architectDto.id, new ArchitectUserLinkDto(userId: user.id))
        then:
            response.status == HttpStatus.OK.value()
    }

    @Transactional
    def "Linking without a user should return 400"() {
        when:
            def response = this.putUser(this.createArchitect().id, new ArchitectUserLinkDto())
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
    }

    @Transactional
    def "Linking a not existing user should return 404"() {
        when:
            def response = this.putUser(this.createArchitect().id, new ArchitectUserLinkDto(userId: NOT_EXISTING_ID))
        then:
            response.status == HttpStatus.NOT_FOUND.value()
    }

    @Transactional
    def "Linking a user with a not existing architect should return 404"() {
        given:
            def user = this.createUser("link-1")
        when:
            def response = this.putUser(NOT_EXISTING_ID, new ArchitectUserLinkDto(userId: user.id))
        then:
            response.status == HttpStatus.NOT_FOUND.value()
    }

    @Transactional
    def "Unlinking should return 200, keep the architect and let the user be linked again"() {
        given:
            def architectDto = this.createArchitect()
            def user = this.createUser("link-1")
            this.putUser(architectDto.id, new ArchitectUserLinkDto(userId: user.id))
        when:
            def response = this.deleteUser(architectDto.id)
        then:
            response.status == HttpStatus.OK.value()
            MAPPER.readValue(response.contentAsString, ArchitectDto).linkedUser == null
            this.getArchitect(architectDto.id).firstName == FIRST_NAME
            this.getArchitect(architectDto.id).linkedUser == null
        and:
            this.putUser(this.createArchitect().id, new ArchitectUserLinkDto(userId: user.id)).status ==
                    HttpStatus.OK.value()
    }

    @Transactional
    def "Unlinking from a not existing architect should return 404"() {
        when:
            def response = this.deleteUser(NOT_EXISTING_ID)
        then:
            response.status == HttpStatus.NOT_FOUND.value()
    }

    @Transactional
    def "Updating a linked architect should keep and return the linked user"() {
        given:
            def architectDto = this.createArchitect()
            def user = this.createUser("link-1")
            this.putUser(architectDto.id, new ArchitectUserLinkDto(userId: user.id))
            def requestBody = MAPPER.writeValueAsString(this.prepareArchitectDto(NEW_FIRST_NAME, NEW_LAST_NAME))
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders
                    .put(URI.create(HOST + ":" + port + ARCHITECTS_URI + "/" + architectDto.id))
                    .header("Content-Type", "application/json")
                    .content(requestBody)).andReturn().response
        then:
            response.status == HttpStatus.OK.value()
            MAPPER.readValue(response.contentAsString, ArchitectDto).linkedUser.id == user.id
    }

    private ArchitectDto createArchitect() {
        def requestBody = MAPPER.writeValueAsString(this.prepareArchitectDto(FIRST_NAME, LAST_NAME))
        def response = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + ARCHITECTS_URI))
                .header("Content-Type", "application/json")
                .content(requestBody)).andReturn().response
        return MAPPER.readValue(response.contentAsString, ArchitectDto)
    }

    private User createUser(String name) {
        return this.userRepository.save(new User(name, name + "@example.com", "auth0|" + name))
    }

    private ArchitectDto getArchitect(Long architectId) {
        def response = this.mockMvc.perform(MockMvcRequestBuilders
                .get(URI.create(HOST + ":" + port + ARCHITECTS_URI + "/" + architectId))).andReturn().response
        return MAPPER.readValue(response.contentAsString, ArchitectDto)
    }

    private def putUser(Long architectId, ArchitectUserLinkDto architectUserLinkDto) {
        return this.mockMvc.perform(MockMvcRequestBuilders
                .put(URI.create(HOST + ":" + port + ARCHITECTS_URI + "/" + architectId + "/user"))
                .header("Content-Type", "application/json")
                .content(MAPPER.writeValueAsString(architectUserLinkDto))).andReturn().response
    }

    private def deleteUser(Long architectId) {
        return this.mockMvc.perform(MockMvcRequestBuilders
                .delete(URI.create(HOST + ":" + port + ARCHITECTS_URI + "/" + architectId + "/user")))
                .andReturn().response
    }

    private <T> T readJson(String path, Class<T> type) {
        return MAPPER.readValue(new File(getClass().classLoader.getResource(path).file), type)
    }

    private ArchitectDto prepareArchitectDto(String firstName, String lastName) {
        return new ArchitectDto(firstName: firstName, lastName: lastName)
    }
}
