package com.arturjarosz.task.project

import com.arturjarosz.task.configuration.BaseTestIT
import com.arturjarosz.task.dto.ArchitectDto
import com.arturjarosz.task.dto.ClientDto
import com.arturjarosz.task.dto.ProjectCreateDto
import com.arturjarosz.task.dto.ProjectDto
import com.arturjarosz.task.sharedkernel.exceptions.ErrorMessage
import com.arturjarosz.task.utils.TestsHelper
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.transaction.annotation.Transactional

class ProjectTestIT extends BaseTestIT {
    static final String ARCHITECTS_URI = "/architects"
    static final String CLIENTS_URI = "/clients"
    static final String PROJECTS_URI = "/projects"
    static final long NOT_EXISTING_CLIENT_ID = 10000l
    static final long NOT_EXISTING_ARCHITECT_ID = 10000l
    static final long NOT_EXISTING_PROJECT_ID = 10000l
    static final ObjectMapper MAPPER = new ObjectMapper()

    final def architect = MAPPER.readValue(new File(getClass().classLoader.getResource('json/architect/architect.json').file),
            ArchitectDto)
    final def privateClientDto = MAPPER.readValue(new File(getClass().classLoader.getResource('json/client/privateClient.json').file),
            ClientDto)
    final def properProjectDto = MAPPER.readValue(new File(getClass().classLoader.getResource('json/project/properProject.json').file),
            ProjectCreateDto)
    final def notProperProjectDto = MAPPER.readValue(new File(getClass().classLoader.getResource('json/project/notProperProject.json').file),
            ProjectCreateDto)
    final def updateProjectDto = MAPPER.readValue(new File(getClass().classLoader.getResource('json/project/properUpdateProject.json').file),
            ProjectDto)

    @Autowired
    private MockMvc mockMvc

    @Transactional
    def "Creating project with proper data should return code 201, dto of created project and project location header"() {
        given: "Existing architect"
            def architectDto = TestsHelper.createArchitect(architect, this.createArchitectUri(), this.mockMvc)
            properProjectDto.architectId = architectDto.id
        and: "Existing client"
            def clientDto = TestsHelper.createClient(this.privateClientDto, this.createClientUri(), this.mockMvc)
            properProjectDto.clientId = clientDto.id
        when: "Creating project with proper data"
            def projectRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properProjectDto)
            def projectResponse = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + PROJECTS_URI))
                    .header("Content-Type", "application/json")
                    .content(projectRequestBody)).andReturn().response
        then: "Returns code 201"
            projectResponse.status == HttpStatus.CREATED.value()
        and: "Returns project dto"
            ProjectDto projectDto = MAPPER.readValue(projectResponse.contentAsString, ProjectDto)
        and: "Return project location header"
            projectResponse.getHeader("Location") == PROJECTS_URI + "/" + projectDto.id
        and:
            !projectDto.nextStatuses.empty
            !projectDto.contract.nextStatuses.empty
        and:
            projectDto.contract.id != null
            projectDto.contract.offerValue != null
            projectDto.contract.status != null
            projectDto.contract.nextStatuses != null
    }

    @Transactional
    def "Creating project with not proper data should return code 400 and error message"() {
        given: "Existing architect"
            def architectDto = TestsHelper.createArchitect(architect, this.createArchitectUri(), this.mockMvc)
            properProjectDto.architectId = architectDto.id
        and: "Existing client"
            def clientDto = TestsHelper.createClient(this.privateClientDto, this.createClientUri(), this.mockMvc)
            properProjectDto.clientId = clientDto.id
        when: "Creating project with not proper data"
            def projectRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(notProperProjectDto)
            def projectResponse = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + PROJECTS_URI))
                    .header("Content-Type", "application/json")
                    .content(projectRequestBody)).andReturn().response
        then: "Returns code 400"
            projectResponse.status == HttpStatus.BAD_REQUEST.value()
        and:
            def message = MAPPER.readValue(projectResponse.contentAsString, ErrorMessage)
            message.getMessage() == "Project name cannot be empty."
    }

    @Transactional
    def "Creating project with not existing client should give code 400 and error message"() {
        given: "Existing architect"
            def architectDto = TestsHelper.createArchitect(architect, this.createArchitectUri(), this.mockMvc)
            properProjectDto.architectId = architectDto.id
        and: "Not existing client"
            properProjectDto.clientId = NOT_EXISTING_CLIENT_ID
        when: "Creating project with not proper data"
            def projectRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properProjectDto)
            def projectResponse = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + PROJECTS_URI))
                    .header("Content-Type", "application/json")
                    .content(projectRequestBody)).andReturn().response
        then: "Returns code 400"
            projectResponse.status == HttpStatus.BAD_REQUEST.value()
        and:
            def message = MAPPER.readValue(projectResponse.contentAsString, ErrorMessage)
            message.getMessage() == "Client with id 10,000 does not exist."
    }

    @Transactional
    def "Creating project with not existing architect should give code 400 and error message"() {
        given: "Not existing architect"
            properProjectDto.architectId = NOT_EXISTING_ARCHITECT_ID
        and: "Existing client"
            def clientDto = TestsHelper.createClient(this.privateClientDto, this.createClientUri(), this.mockMvc)
            properProjectDto.clientId = clientDto.id
        when: "Creating project with not proper data"
            def projectRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properProjectDto)
            def projectResponse = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + PROJECTS_URI))
                    .header("Content-Type", "application/json")
                    .content(projectRequestBody)).andReturn().response
        then: "Returns code 400"
            projectResponse.status == HttpStatus.BAD_REQUEST.value()
        and:
            def message = MAPPER.readValue(projectResponse.contentAsString, ErrorMessage)
            message.getMessage() == "Architect with id 10,000 does not exist."
    }

    @Transactional
    def "Getting not existing project should give code 400 and error message"() {
        given:
        when:
            def projectResponse = this.mockMvc.perform(MockMvcRequestBuilders
                    .get(URI.create(HOST + ":" + port + PROJECTS_URI + "/" + NOT_EXISTING_PROJECT_ID))).andReturn().response
        then: "Returns code 400"
            projectResponse.status == HttpStatus.BAD_REQUEST.value()
        and:
            def message = MAPPER.readValue(projectResponse.contentAsString, ErrorMessage)
            message.getMessage() == "Project with id 10,000 does not exist."
    }

    @Transactional
    def "Getting existing project should return code 200 and dto of project"() {
        given: "Existing architect"
            def architectDto = TestsHelper.createArchitect(architect, this.createArchitectUri(), this.mockMvc)
            properProjectDto.architectId = architectDto.id
        and: "Existing client"
            def clientDto = TestsHelper.createClient(this.privateClientDto, this.createClientUri(), this.mockMvc)
            properProjectDto.clientId = clientDto.id
        and: "Creating project with not proper data"
            def projectRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properProjectDto)
            def creatingProjectResponse = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + PROJECTS_URI))
                    .header("Content-Type", "application/json")
                    .content(projectRequestBody)).andReturn().response
            def createdProject = MAPPER.readValue(creatingProjectResponse.contentAsString, ProjectDto)
        when:
            def projectResponse = this.mockMvc.perform(MockMvcRequestBuilders.get(URI.create(HOST + ":" + port + PROJECTS_URI + "/" + createdProject.id))).andReturn().response
        then:
            projectResponse.status == HttpStatus.OK.value()
        and:
            def projectDto = MAPPER.readValue(creatingProjectResponse.contentAsString, ProjectDto)
            createdProject.name == projectDto.name
            createdProject.id == projectDto.id
        and:
            !projectDto.nextStatuses.empty
            !projectDto.contract.nextStatuses.empty
        and:
            projectDto.contract.id != null
            projectDto.contract.offerValue != null
            projectDto.contract.status != null
            projectDto.contract.nextStatuses != null
    }

    @Transactional
    def "Updating not existing project should give code 400 and error message"() {
        given:
        when:
            def projectRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properProjectDto)
            def projectResponse = this.mockMvc.perform(MockMvcRequestBuilders
                    .put(URI.create(HOST + ":" + port + PROJECTS_URI + "/" + NOT_EXISTING_PROJECT_ID))
                    .header("Content-Type", "application/json")
                    .content(projectRequestBody)).andReturn().response
        then: "Returns code 400"
            projectResponse.status == HttpStatus.BAD_REQUEST.value()
        and:
            def message = MAPPER.readValue(projectResponse.contentAsString, ErrorMessage)
            message.getMessage() == "Project with id 10,000 does not exist."
    }

    @Transactional
    def "Updating existing project with proper data should give code 200 and dto of updated project"() {
        given: "Existing architect"
            def architectDto = TestsHelper.createArchitect(architect, this.createArchitectUri(), this.mockMvc)
            properProjectDto.architectId = architectDto.id
        and: "Existing client"
            def clientDto = TestsHelper.createClient(this.privateClientDto, this.createClientUri(), this.mockMvc)
            properProjectDto.clientId = clientDto.id
        and: "Creating project with not proper data"
            def creatingProjectRequestBody =
                    MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properProjectDto)
            def creatingProjectResponse = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + PROJECTS_URI))
                    .header("Content-Type", "application/json")
                    .content(creatingProjectRequestBody)).andReturn().response
            def createdProject = MAPPER.readValue(creatingProjectResponse.contentAsString, ProjectDto)
        when:
            def projectRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(updateProjectDto)
            def projectResponse = this.mockMvc.perform(MockMvcRequestBuilders
                    .put(URI.create(HOST + ":" + port + PROJECTS_URI + "/" + createdProject.id))
                    .header("Content-Type", "application/json")
                    .content(projectRequestBody)).andReturn().response
        then:
            projectResponse.status == HttpStatus.OK.value()
        and:
            def projectDto = MAPPER.readValue(creatingProjectResponse.contentAsString, ProjectDto)
            createdProject.name == projectDto.name
            createdProject.note == projectDto.note
            createdProject.type == projectDto.type
        and:
            !projectDto.nextStatuses.empty
            !projectDto.contract.nextStatuses.empty
    }

    @Transactional
    def "Removing not existing project should give code 400 and error message"() {
        given:
        when:
            MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properProjectDto)
            def projectResponse = this.mockMvc.perform(MockMvcRequestBuilders
                    .delete(URI.create(HOST + ":" + port + PROJECTS_URI + "/" + NOT_EXISTING_PROJECT_ID))).andReturn().response
        then: "Returns code 400"
            projectResponse.status == HttpStatus.BAD_REQUEST.value()
        and:
            def message = MAPPER.readValue(projectResponse.contentAsString, ErrorMessage)
            message.getMessage() == "Project with id 10,000 does not exist."
    }

    @Transactional
    def "Removing existing project should give code 200 and remove project"() {
        given: "Existing architect"
            def architectDto = TestsHelper.createArchitect(architect, this.createArchitectUri(), this.mockMvc)
            properProjectDto.architectId = architectDto.id
        and: "Existing client"
            def clientDto = TestsHelper.createClient(this.privateClientDto, this.createClientUri(), this.mockMvc)
            properProjectDto.clientId = clientDto.id
        and: "Creating project with not proper data"
            def creatingProjectRequestBody =
                    MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properProjectDto)
            def creatingProjectResponse = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + PROJECTS_URI))
                    .header("Content-Type", "application/json")
                    .content(creatingProjectRequestBody)).andReturn().response
            def createdProject = MAPPER.readValue(creatingProjectResponse.contentAsString, ProjectDto)
        when:
            def projectResponse = this.mockMvc.perform(MockMvcRequestBuilders
                    .delete(URI.create(HOST + ":" + port + PROJECTS_URI + "/" + createdProject.id))).andReturn().response
        then:
            projectResponse.status == HttpStatus.OK.value()
        and:
            def projectDto = MAPPER.readValue(creatingProjectResponse.contentAsString, ProjectDto)
            createdProject.name == projectDto.name
            createdProject.id == projectDto.id
    }

    @Transactional
    def "Get projects should return dto list of all existing projects"() {
        given: "Existing architect"
            def architectDto = TestsHelper.createArchitect(architect, this.createArchitectUri(), this.mockMvc)
            properProjectDto.architectId = architectDto.id
        and: "Existing client"
            def clientDto = TestsHelper.createClient(this.privateClientDto, this.createClientUri(), this.mockMvc)
            properProjectDto.clientId = clientDto.id
        and: "Creating project with not proper data"
            def creatingProjectRequestBody =
                    MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properProjectDto)
            this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + PROJECTS_URI))
                    .header("Content-Type", "application/json")
                    .content(creatingProjectRequestBody)).andReturn().response
            this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + PROJECTS_URI))
                    .header("Content-Type", "application/json")
                    .content(creatingProjectRequestBody)).andReturn().response
        when:
            def projectsListResponse = this.mockMvc.perform(MockMvcRequestBuilders.get(URI.create(HOST + ":" + port + PROJECTS_URI))).andReturn().response
        then:
            projectsListResponse.status == HttpStatus.OK.value()
        and:
            List<ProjectDto> projects =
                    MAPPER.readValue(projectsListResponse.contentAsString, List<ProjectDto>)
            projects.size() == 2
    }

    private String createArchitectUri() {
        return "${HOST}:${port}${ARCHITECTS_URI}"
    }

    private String createClientUri() {
        return "${HOST}:${port}${CLIENTS_URI}"
    }
}
