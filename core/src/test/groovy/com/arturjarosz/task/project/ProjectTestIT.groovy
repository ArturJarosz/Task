package com.arturjarosz.task.project

import com.arturjarosz.task.architect.application.dto.ArchitectBasicDto
import com.arturjarosz.task.architect.application.dto.ArchitectDto
import com.arturjarosz.task.client.application.dto.ClientDto
import com.arturjarosz.task.configuration.BaseTestIT
import com.arturjarosz.task.project.application.dto.ProjectCreateDto
import com.arturjarosz.task.project.application.dto.ProjectDto
import com.arturjarosz.task.sharedkernel.exceptions.ErrorMessage
import com.arturjarosz.task.utils.TestsHelper
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.transaction.annotation.Transactional

class ProjectTestIT extends BaseTestIT {
    private static final String ARCHITECTS_URI = "/architects"
    private static final String CLIENTS_URI = "/clients"
    private static final String PROJECTS_URI = "/projects"
    private static final long NOT_EXISTING_CLIENT_ID = 10000l
    private static final long NOT_EXISTING_ARCHITECT_ID = 10000l
    private static final long NOT_EXISTING_PROJECT_ID = 10000l
    private static final ObjectMapper MAPPER = new ObjectMapper()

    private final ArchitectBasicDto architect =
            MAPPER.readValue(new File(getClass().classLoader.getResource('json/architect/architect.json').file),
                    ArchitectBasicDto.class)
    private final ClientDto privateClientDto =
            MAPPER.readValue(new File(getClass().classLoader.getResource('json/client/privateClient.json').file),
                    ClientDto.class)
    private final ProjectCreateDto properProjectDto =
            MAPPER.readValue(new File(getClass().classLoader.getResource('json/project/properProject.json').file),
                    ProjectCreateDto.class)
    private final ProjectCreateDto notProperProjectDto =
            MAPPER.readValue(new File(getClass().classLoader.getResource('json/project/notProperProject.json').file),
                    ProjectCreateDto.class)
    private final ProjectDto updateProjectDto =
            MAPPER.readValue(new File(getClass().classLoader.getResource('json/project/properUpdateProject.json').file),
                    ProjectDto.class)

    @Autowired
    private MockMvc mockMvc

    @Transactional
    def "Creating project with proper data should return code 201, dto of created project and project location header"() {
        given: "Existing architect"
            ArchitectDto architectDto = TestsHelper.createArchitect(architect, this.createArchitectUri(), this.mockMvc)
            properProjectDto.architectId = architectDto.id
        and: "Existing client"
            ClientDto clientDto = TestsHelper.createClient(this.privateClientDto, this.createClientUri(), this.mockMvc)
            properProjectDto.clientId = clientDto.id
        when: "Creating project with proper data"
            String projectRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properProjectDto)
            def projectResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + PROJECTS_URI))
                            .header("Content-Type", "application/json")
                            .content(projectRequestBody)
            ).andReturn().response
        then: "Returns code 201"
            projectResponse.status == HttpStatus.CREATED.value()
        and: "Returns project dto"
            ProjectDto projectDto = MAPPER.readValue(projectResponse.contentAsString, ProjectDto.class)
        and: "Return project location header"
            projectResponse.getHeader("Location") == PROJECTS_URI + "/" + projectDto.id
    }

    @Transactional
    def "Creating project with not proper data should return code 400 and error message"() {
        given: "Existing architect"
            ArchitectDto architectDto = TestsHelper.createArchitect(architect, this.createArchitectUri(), this.mockMvc)
            properProjectDto.architectId = architectDto.id
        and: "Existing client"
            ClientDto clientDto = TestsHelper.createClient(this.privateClientDto, this.createClientUri(), this.mockMvc)
            properProjectDto.clientId = clientDto.id
        when: "Creating project with not proper data"
            String projectRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(notProperProjectDto)
            def projectResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + PROJECTS_URI))
                            .header("Content-Type", "application/json")
                            .content(projectRequestBody)
            ).andReturn().response
        then: "Returns code 400"
            projectResponse.status == HttpStatus.BAD_REQUEST.value()
        and:
            def message = MAPPER.readValue(projectResponse.contentAsString, ErrorMessage.class)
            message.getMessage() == "Project name cannot be empty."
    }

    @Transactional
    def "Creating project with not existing client should give code 400 and error message"() {
        given: "Existing architect"
            ArchitectDto architectDto = TestsHelper.createArchitect(architect, this.createArchitectUri(), this.mockMvc)
            properProjectDto.architectId = architectDto.id
        and: "Not existing client"
            properProjectDto.clientId = NOT_EXISTING_CLIENT_ID
        when: "Creating project with not proper data"
            String projectRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properProjectDto)
            def projectResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + PROJECTS_URI))
                            .header("Content-Type", "application/json")
                            .content(projectRequestBody)
            ).andReturn().response
        then: "Returns code 400"
            projectResponse.status == HttpStatus.BAD_REQUEST.value()
        and:
            def message = MAPPER.readValue(projectResponse.contentAsString, ErrorMessage.class)
            message.getMessage() == "Client with id 10,000 does not exist."
    }

    @Transactional
    def "Creating project with not existing architect should give code 400 and error message"() {
        given: "Not existing architect"
            properProjectDto.architectId = NOT_EXISTING_ARCHITECT_ID
        and: "Existing client"
            ClientDto clientDto = TestsHelper.createClient(this.privateClientDto, this.createClientUri(), this.mockMvc)
            properProjectDto.clientId = clientDto.id
        when: "Creating project with not proper data"
            String projectRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properProjectDto)
            def projectResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + PROJECTS_URI))
                            .header("Content-Type", "application/json")
                            .content(projectRequestBody)
            ).andReturn().response
        then: "Returns code 400"
            projectResponse.status == HttpStatus.BAD_REQUEST.value()
        and:
            def message = MAPPER.readValue(projectResponse.contentAsString, ErrorMessage.class)
            message.getMessage() == "Architect with id 10,000 does not exist."
    }

    @Transactional
    def "Getting not existing project should give code 400 and error message"() {
        given:
        when:
            MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properProjectDto)
            def projectResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .get(URI.create(HOST + ":" + port + PROJECTS_URI + "/" + NOT_EXISTING_PROJECT_ID))
            ).andReturn().response
        then: "Returns code 400"
            projectResponse.status == HttpStatus.BAD_REQUEST.value()
        and:
            def message = MAPPER.readValue(projectResponse.contentAsString, ErrorMessage.class)
            message.getMessage() == "Project with id 10,000 does not exist."
    }

    @Transactional
    def "Getting existing project should return code 200 and dto of project"() {
        given: "Existing architect"
            ArchitectDto architectDto = TestsHelper.createArchitect(architect, this.createArchitectUri(), this.mockMvc)
            properProjectDto.architectId = architectDto.id
        and: "Existing client"
            ClientDto clientDto = TestsHelper.createClient(this.privateClientDto, this.createClientUri(), this.mockMvc)
            properProjectDto.clientId = clientDto.id
        and: "Creating project with not proper data"
            String projectRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properProjectDto)
            def creatingProjectResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + PROJECTS_URI))
                            .header("Content-Type", "application/json")
                            .content(projectRequestBody)
            ).andReturn().response
            ProjectDto createdProject = MAPPER.readValue(creatingProjectResponse.contentAsString, ProjectDto.class)
        when:
            def projectResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.get(URI.create(HOST + ":" + port + PROJECTS_URI + "/" + createdProject.id))
            ).andReturn().response
        then:
            projectResponse.status == HttpStatus.OK.value()
        and:
            ProjectDto projectDto = MAPPER.readValue(creatingProjectResponse.contentAsString, ProjectDto.class)
            createdProject.name == projectDto.name
            createdProject.id == projectDto.id
    }

    @Transactional
    def "Updating not existing project should give code 400 and error message"() {
        given:
        when:
            String projectRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properProjectDto)
            def projectResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .put(URI.create(HOST + ":" + port + PROJECTS_URI + "/" + NOT_EXISTING_PROJECT_ID))
                            .header("Content-Type", "application/json")
                            .content(projectRequestBody)
            ).andReturn().response
        then: "Returns code 400"
            projectResponse.status == HttpStatus.BAD_REQUEST.value()
        and:
            def message = MAPPER.readValue(projectResponse.contentAsString, ErrorMessage.class)
            message.getMessage() == "Project with id 10,000 does not exist."
    }

    @Transactional
    def "Updating existing project with proper data should give code 200 and dto of updated project"() {
        given: "Existing architect"
            ArchitectDto architectDto = TestsHelper.createArchitect(architect, this.createArchitectUri(), this.mockMvc)
            properProjectDto.architectId = architectDto.id
        and: "Existing client"
            ClientDto clientDto = TestsHelper.createClient(this.privateClientDto, this.createClientUri(), this.mockMvc)
            properProjectDto.clientId = clientDto.id
        and: "Creating project with not proper data"
            String creatingProjectRequestBody =
                    MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properProjectDto)
            def creatingProjectResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + PROJECTS_URI))
                            .header("Content-Type", "application/json")
                            .content(creatingProjectRequestBody)
            ).andReturn().response
            ProjectDto createdProject = MAPPER.readValue(creatingProjectResponse.contentAsString, ProjectDto.class)
        when:
            String projectRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(updateProjectDto)
            def projectResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .put(URI.create(HOST + ":" + port + PROJECTS_URI + "/" + createdProject.id))
                            .header("Content-Type", "application/json")
                            .content(projectRequestBody)
            ).andReturn().response
        then:
            projectResponse.status == HttpStatus.OK.value()
        and:
            ProjectDto projectDto = MAPPER.readValue(creatingProjectResponse.contentAsString, ProjectDto.class)
            createdProject.name == projectDto.name
            createdProject.note == projectDto.note
            createdProject.projectType == projectDto.projectType
    }

    @Transactional
    def "Removing not existing project should give code 400 and error message"() {
        given:
        when:
            MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properProjectDto)
            def projectResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .delete(URI.create(HOST + ":" + port + PROJECTS_URI + "/" + NOT_EXISTING_PROJECT_ID))
            ).andReturn().response
        then: "Returns code 400"
            projectResponse.status == HttpStatus.BAD_REQUEST.value()
        and:
            def message = MAPPER.readValue(projectResponse.contentAsString, ErrorMessage.class)
            message.getMessage() == "Project with id 10,000 does not exist."
    }

    @Transactional
    def "Removing existing project should give code 200 and remove project"() {
        given: "Existing architect"
            ArchitectDto architectDto = TestsHelper.createArchitect(architect, this.createArchitectUri(), this.mockMvc)
            properProjectDto.architectId = architectDto.id
        and: "Existing client"
            ClientDto clientDto = TestsHelper.createClient(this.privateClientDto, this.createClientUri(), this.mockMvc)
            properProjectDto.clientId = clientDto.id
        and: "Creating project with not proper data"
            String creatingProjectRequestBody =
                    MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properProjectDto)
            def creatingProjectResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + PROJECTS_URI))
                            .header("Content-Type", "application/json")
                            .content(creatingProjectRequestBody)
            ).andReturn().response
            ProjectDto createdProject = MAPPER.readValue(creatingProjectResponse.contentAsString, ProjectDto.class)
        when:
            def projectResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .delete(URI.create(HOST + ":" + port + PROJECTS_URI + "/" + createdProject.id))
            ).andReturn().response
        then:
            projectResponse.status == HttpStatus.OK.value()
        and:
            ProjectDto projectDto = MAPPER.readValue(creatingProjectResponse.contentAsString, ProjectDto.class)
            createdProject.name == projectDto.name
            createdProject.id == projectDto.id
    }

    @Transactional
    def "Get projects should return dto list of all existing projects"() {
        given: "Existing architect"
            ArchitectDto architectDto = TestsHelper.createArchitect(architect, this.createArchitectUri(), this.mockMvc)
            properProjectDto.architectId = architectDto.id
        and: "Existing client"
            ClientDto clientDto = TestsHelper.createClient(this.privateClientDto, this.createClientUri(), this.mockMvc)
            properProjectDto.clientId = clientDto.id
        and: "Creating project with not proper data"
            String creatingProjectRequestBody =
                    MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properProjectDto)
            this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + PROJECTS_URI))
                    .header("Content-Type", "application/json")
                    .content(creatingProjectRequestBody)
            ).andReturn().response
            this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + PROJECTS_URI))
                    .header("Content-Type", "application/json")
                    .content(creatingProjectRequestBody)
            ).andReturn().response
        when:
            def projectsListResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.get(URI.create(HOST + ":" + port + PROJECTS_URI))
            ).andReturn().response
        then:
            projectsListResponse.status == HttpStatus.OK.value()
        and:
            List<ProjectDto> projects =
                    MAPPER.readValue(projectsListResponse.contentAsString, List<ProjectDto>.class)
            projects.size() == 2
    }

    private String createArchitectUri() {
        return HOST + ":" + port + ARCHITECTS_URI
    }

    private String createClientUri() {
        return HOST + ":" + port + CLIENTS_URI
    }
}
