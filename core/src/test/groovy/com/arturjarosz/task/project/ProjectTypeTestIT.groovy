package com.arturjarosz.task.project

import com.arturjarosz.task.configuration.BaseTestIT
import com.arturjarosz.task.dto.ApplicationConfigurationDto
import com.arturjarosz.task.dto.ArchitectDto
import com.arturjarosz.task.dto.ClientDto
import com.arturjarosz.task.dto.ProjectCreateDto
import com.arturjarosz.task.dto.ProjectTypeDto
import com.arturjarosz.task.sharedkernel.exceptions.ErrorMessage
import com.arturjarosz.task.utils.TestsHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper

class ProjectTypeTestIT extends BaseTestIT {
    static final String PROJECT_TYPES_URI = "/project-types"
    static final String ARCHITECTS_URI = "/architects"
    static final String CLIENTS_URI = "/clients"
    static final String PROJECTS_URI = "/projects"
    static final String CONTENT_TYPE = "Content-Type"
    static final String APPLICATION_JSON = "application/json"
    static final long NOT_EXISTING_PROJECT_TYPE_ID = 10000L
    // inserted by the test changelog and used by the project fixtures
    static final String SEEDED_PROJECT_TYPE_NAME = "Interior house"
    static final ObjectMapper MAPPER = new ObjectMapper()

    final def architect = readJson('json/architect/architect.json', ArchitectDto)
    final def privateClientDto = readJson('json/client/privateClient.json', ClientDto)
    final def properProjectDto = readJson('json/project/properProject.json', ProjectCreateDto)

    @Autowired
    private MockMvc mockMvc

    @Transactional
    def "Creating project type should return code 201, created project type and its location"() {
        when:
            def response = this.postProjectType(new ProjectTypeDto(name: "  Retail interior  "))
        then:
            response.status == HttpStatus.CREATED.value()
        and:
            def createdProjectType = MAPPER.readValue(response.contentAsString, ProjectTypeDto)
            createdProjectType.id != null
            createdProjectType.name == "Retail interior"
            createdProjectType.numberOfProjects == 0
            response.getHeader("Location") == "$PROJECT_TYPES_URI/${createdProjectType.id}"
    }

    @Transactional
    def "Creating project type with empty name should return code 400 and error message"() {
        when:
            def response = this.postProjectType(new ProjectTypeDto(name: " "))
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            MAPPER.readValue(response.contentAsString, ErrorMessage).message == "Project type name cannot be empty."
    }

    @Transactional
    def "Creating project type with name of existing type should return code 400 and error message"() {
        when:
            def response = this.postProjectType(new ProjectTypeDto(name: SEEDED_PROJECT_TYPE_NAME.toUpperCase()))
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            MAPPER.readValue(response.contentAsString, ErrorMessage).message ==
                    "Project type with name INTERIOR HOUSE already exists."
    }

    @Transactional
    def "Renaming project type should return code 200 and renamed project type"() {
        given:
            def projectType = this.createProjectType("Retail interior")
        when:
            def response = this.putProjectType(projectType.id, new ProjectTypeDto(name: "Shop interior"))
        then:
            response.status == HttpStatus.OK.value()
        and:
            MAPPER.readValue(response.contentAsString, ProjectTypeDto).name == "Shop interior"
        and:
            def loaded = MAPPER.readValue(this.mockMvc.perform(
                    MockMvcRequestBuilders.get("$PROJECT_TYPES_URI/${projectType.id}")).andReturn().response
                    .contentAsString, ProjectTypeDto)
            loaded.name == "Shop interior"
    }

    @Transactional
    def "Renaming project type to its own name with different letter case should be allowed"() {
        given:
            def projectType = this.createProjectType("Retail interior")
        when:
            def response = this.putProjectType(projectType.id, new ProjectTypeDto(name: "Retail Interior"))
        then:
            response.status == HttpStatus.OK.value()
    }

    @Transactional
    def "Renaming project type to name of another type should return code 400"() {
        given:
            def projectType = this.createProjectType("Retail interior")
        when:
            def response = this.putProjectType(projectType.id, new ProjectTypeDto(name: SEEDED_PROJECT_TYPE_NAME))
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
    }

    @Transactional
    def "Updating not existing project type should return code 404 and error message"() {
        when:
            def response = this.putProjectType(NOT_EXISTING_PROJECT_TYPE_ID, new ProjectTypeDto(name: "Any"))
        then:
            response.status == HttpStatus.NOT_FOUND.value()
        and:
            MAPPER.readValue(response.contentAsString, ErrorMessage).message ==
                    "Project type with id 10,000 does not exist."
    }

    @Transactional
    def "Deleting project type without projects should return code 200 and remove it"() {
        given:
            def projectType = this.createProjectType("Retail interior")
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.delete("$PROJECT_TYPES_URI/${projectType.id}"))
                    .andReturn().response
        then:
            response.status == HttpStatus.OK.value()
        and:
            this.mockMvc.perform(MockMvcRequestBuilders.get("$PROJECT_TYPES_URI/${projectType.id}"))
                    .andReturn().response.status == HttpStatus.NOT_FOUND.value()
    }

    @Transactional
    def "Deleting project type used by a project should return code 400 and keep it"() {
        given:
            def projectType = this.createProjectType("Retail interior")
            this.createProject(projectType.id)
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.delete("$PROJECT_TYPES_URI/${projectType.id}"))
                    .andReturn().response
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            MAPPER.readValue(response.contentAsString, ErrorMessage).message ==
                    "Project type used by projects cannot be removed."
        and:
            this.mockMvc.perform(MockMvcRequestBuilders.get("$PROJECT_TYPES_URI/${projectType.id}"))
                    .andReturn().response.status == HttpStatus.OK.value()
    }

    @Transactional
    def "Getting project types should return them sorted by name with the number of their projects"() {
        given:
            def retail = this.createProjectType("Retail interior")
            def apartment = this.createProjectType("Apartment interior")
            this.createProject(retail.id)
            this.createProject(retail.id)
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.get(PROJECT_TYPES_URI)).andReturn().response
        then:
            response.status == HttpStatus.OK.value()
        and:
            List<ProjectTypeDto> projectTypes = MAPPER.readValue(response.contentAsString, ProjectTypeDto[]).toList()
            projectTypes*.name == ["Apartment interior", SEEDED_PROJECT_TYPE_NAME, "Retail interior"]
            projectTypes.find { it.id == retail.id }.numberOfProjects == 2
            projectTypes.find { it.id == apartment.id }.numberOfProjects == 0
    }

    @Transactional
    def "Application configuration should list project types with their id and name"() {
        given:
            def projectType = this.createProjectType("Retail interior")
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.get("/configuration")).andReturn().response
        then:
            def configuration = MAPPER.readValue(response.contentAsString, ApplicationConfigurationDto)
            configuration.projectTypes.find { it.id == String.valueOf(projectType.id) }.label == "Retail interior"
    }

    private def postProjectType(ProjectTypeDto projectTypeDto) {
        return this.mockMvc.perform(MockMvcRequestBuilders.post(PROJECT_TYPES_URI)
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(projectTypeDto))).andReturn().response
    }

    private def putProjectType(Long projectTypeId, ProjectTypeDto projectTypeDto) {
        return this.mockMvc.perform(MockMvcRequestBuilders.put("$PROJECT_TYPES_URI/$projectTypeId")
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(projectTypeDto))).andReturn().response
    }

    private ProjectTypeDto createProjectType(String name) {
        return MAPPER.readValue(this.postProjectType(new ProjectTypeDto(name: name)).contentAsString, ProjectTypeDto)
    }

    private void createProject(Long projectTypeId) {
        properProjectDto.architectId = TestsHelper.createArchitect(architect, "$HOST:$port$ARCHITECTS_URI",
                this.mockMvc).id
        properProjectDto.clientId = TestsHelper.createClient(privateClientDto, "$HOST:$port$CLIENTS_URI",
                this.mockMvc).id
        properProjectDto.typeId = projectTypeId
        TestsHelper.createProject(properProjectDto, "$HOST:$port$PROJECTS_URI", this.mockMvc)
    }

    private <T> T readJson(String path, Class<T> clazz) {
        return MAPPER.readValue(new File(getClass().classLoader.getResource(path).file), clazz)
    }
}
