package com.arturjarosz.task.project

import com.arturjarosz.task.architect.application.dto.ArchitectBasicDto
import com.arturjarosz.task.architect.application.dto.ArchitectDto
import com.arturjarosz.task.client.application.dto.ClientDto
import com.arturjarosz.task.configuration.BaseTestIT
import com.arturjarosz.task.project.application.dto.ProjectCreateDto
import com.arturjarosz.task.project.application.dto.ProjectDto
import com.arturjarosz.task.project.application.dto.StageDto
import com.arturjarosz.task.project.status.stage.StageStatus
import com.arturjarosz.task.sharedkernel.exceptions.ErrorMessage
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.transaction.annotation.Transactional
import spock.lang.Shared

class StageTestIT extends BaseTestIT {
    private static final String ARCHITECT_FIRST_NAME = "First Name"
    private static final String ARCHITECT_LAST_NAME = "Last Name"
    private static final String ARCHITECTS_URI = "/architects"
    private static final String CLIENTS_URI = "/clients"
    private static final String PROJECTS_URI = "/projects"
    private static final String STAGES_URI = "/stages"
    private static final long NOT_EXISTING_PROJECT_ID = 10000l
    private static final long NOT_EXISTING_STAGE_ID = 10000l

    @Shared
    private final ObjectMapper mapper = new ObjectMapper()

    private final ClientDto privateClientDto =
            mapper.readValue(new File(getClass().classLoader.getResource('json/client/privateClient.json').file),
                    ClientDto.class)
    private final ProjectCreateDto projectDto =
            mapper.readValue(new File(getClass().classLoader.getResource('json/project/properProject.json').file),
                    ProjectCreateDto.class)
    private final StageDto properStageDto =
            mapper.readValue(new File(getClass().classLoader.getResource('json/stage/properStage.json').file),
                    StageDto.class)
    private final StageDto notProperStageDto =
            mapper.readValue(new File(getClass().classLoader.getResource('json/stage/notProperStage.json').file),
                    StageDto.class)
    private final StageDto properStageUpdateDto =
            mapper.readValue(new File(getClass().classLoader.getResource('json/stage/properStageUpdate.json').file),
                    StageDto.class)
    private final StageDto notProperStageUpdateDto =
            mapper.readValue(new File(getClass().classLoader.getResource('json/stage/notProperStageUpdate.json').file),
                    StageDto.class)

    @Autowired
    private MockMvc mockMvc

    @Override
    def setupSpec() {
        mapper.findAndRegisterModules()
    }

    @Transactional
    def "Creating stage for not existing project should return code 400 and error message about not existing project"() {
        given:
            String stageRequestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(properStageDto)
        when:
            def stageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.stageUrlBuilder(NOT_EXISTING_PROJECT_ID)))
                            .header("Content-Type", "application/json")
                            .content(stageRequestBody)
            ).andReturn().response
        then: "Returns code 400"
            stageResponse.status == HttpStatus.BAD_REQUEST.value()
        and:
            def message = mapper.readValue(stageResponse.contentAsString, ErrorMessage.class)
            message.getMessage() == "Project with id 10,000 does not exist."
    }

    @Transactional
    def "Creating stage with not proper data should return code 400 and error message about problem with data"() {
        given:
            ProjectDto createdProject = this.createProject()
            String stageRequestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(notProperStageDto)
        when:
            def stageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.stageUrlBuilder(createdProject.id)))
                            .header("Content-Type", "application/json")
                            .content(stageRequestBody)
            ).andReturn().response
        then: "Returns code 400"
            stageResponse.status == HttpStatus.BAD_REQUEST.value()
        and:
            def message = mapper.readValue(stageResponse.contentAsString, ErrorMessage.class)
            message.getMessage() == "Stage name cannot be empty."
    }

    @Transactional
    def "Creating stage for existing project, with proper data should return code 201, created stage dto and location header"() {
        given:
            ProjectDto createdProject = this.createProject()
            String stageRequestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(properStageDto)
        when:
            def stageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.stageUrlBuilder(createdProject.id)))
                            .header("Content-Type", "application/json")
                            .content(stageRequestBody)
            ).andReturn().response
        then:
            stageResponse.status == HttpStatus.CREATED.value()
        and:
            StageDto stageDto = mapper.readValue(stageResponse.contentAsString, StageDto.class)
            stageDto.name == properStageDto.name
            stageDto.id != null
            stageDto.status == StageStatus.TO_DO
    }

    @Transactional
    def "Removing not existing stage should return code 400 and error message"() {
        given:
            ProjectDto createdProject = this.createProject()
        when:
            def stageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .delete(URI.create(this.stageUrlBuilder(createdProject.id) + "/" +
                                    NOT_EXISTING_STAGE_ID))
            ).andReturn().response
        then: "Returns code 400"
            stageResponse.status == HttpStatus.BAD_REQUEST.value()
        and:
            def message = mapper.readValue(stageResponse.contentAsString, ErrorMessage.class)
            message.getMessage() == "Stage with id 10,000 does not exist."
    }

    @Transactional
    def "Removing existing stage should return code 200 and remove stage"() {
        given:
            ProjectDto createdProject = this.createProject()
            String stageRequestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(properStageDto)
            def createdStageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.stageUrlBuilder(createdProject.id)))
                            .header("Content-Type", "application/json")
                            .content(stageRequestBody)
            ).andReturn().response
            StageDto stageDto = mapper.readValue(createdStageResponse.contentAsString, StageDto.class)
        when:
            def stageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .delete(URI.create(this.stageUrlBuilder(createdProject.id) + "/" + stageDto.id))
            ).andReturn().response
        then: "Returns code 200"
            stageResponse.status == HttpStatus.OK.value()
        and: "Getting stage with removed stage id returns code 400 and error message."
            def removedStageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .get(URI.create(this.stageUrlBuilder(createdProject.id) + "/" + stageDto.id))
                            .header("Content-Type", "application/json")
                            .content(stageRequestBody)
            ).andReturn().response
            removedStageResponse.status == HttpStatus.BAD_REQUEST.value()
            def message = mapper.readValue(removedStageResponse.contentAsString, ErrorMessage.class)
            message.getMessage() == "Stage with id " + stageDto.id + " does not exist."
    }

    @Transactional
    def "Updating not existing stage should return code 400 and error message"() {
        given:
            ProjectDto createdProject = this.createProject()
            String stageRequestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(properStageUpdateDto)
        when:
            def stageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .put(URI.create(this.stageUrlBuilder(createdProject.id) + "/" +
                                    NOT_EXISTING_STAGE_ID))
                            .header("Content-Type", "application/json")
                            .content(stageRequestBody)
            ).andReturn().response
        then: "Returns code 400"
            stageResponse.status == HttpStatus.BAD_REQUEST.value()
        and:
            def message = mapper.readValue(stageResponse.contentAsString, ErrorMessage.class)
            message.getMessage() == "Stage with id 10,000 does not exist."
    }

    @Transactional
    def "Updating existing stage with not proper date should return 400 and error message"() {
        given:
            ProjectDto createdProject = this.createProject()
            String stageRequestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(properStageDto)
            def createdStageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.stageUrlBuilder(createdProject.id)))
                            .header("Content-Type", "application/json")
                            .content(stageRequestBody)
            ).andReturn().response
            StageDto stageDto = mapper.readValue(createdStageResponse.contentAsString, StageDto.class)
            String stageUpdateRequestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(notProperStageUpdateDto)
        when:
            def stageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .put(URI.create(this.stageUrlBuilder(createdProject.id) + "/" + stageDto.id))
                            .header("Content-Type", "application/json")
                            .content(stageUpdateRequestBody)
            ).andReturn().response
        then: "Returns code 400"
            stageResponse.status == HttpStatus.BAD_REQUEST.value()
        and:
            def message = mapper.readValue(stageResponse.contentAsString, ErrorMessage.class)
            message.getMessage() == "Stage name cannot be empty."
    }

    @Transactional
    def "Updating existing stage should return code 200 and dto of updated stage"() {
        given:
            ProjectDto createdProject = this.createProject()
            String stageRequestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(properStageDto)
            def createdStageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.stageUrlBuilder(createdProject.id)))
                            .header("Content-Type", "application/json")
                            .content(stageRequestBody)
            ).andReturn().response
            StageDto stageDto = mapper.readValue(createdStageResponse.contentAsString, StageDto.class)
            String stageUpdateRequestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(properStageUpdateDto)
        when:
            def stageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .put(URI.create(this.stageUrlBuilder(createdProject.id) + "/" + stageDto.id))
                            .header("Content-Type", "application/json")
                            .content(stageUpdateRequestBody)
            ).andReturn().response
        then: "Returns code 200"
            stageResponse.status == HttpStatus.OK.value()
        and: "Getting stage with removed stage id returns code 400 and error message."
            StageDto stageUpdateDto = mapper.readValue(stageResponse.contentAsString, StageDto.class)
            stageUpdateDto.name == properStageUpdateDto.name
            stageUpdateDto.note == properStageUpdateDto.note
            stageUpdateDto.stageType == properStageUpdateDto.stageType
            stageUpdateDto.deadline == properStageUpdateDto.deadline
    }

    @Transactional
    def "Getting not existing stage should code 400 and error message"() {
        given:
            ProjectDto createdProject = this.createProject()
        when:
            def stageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .get(URI.create(this.stageUrlBuilder(createdProject.id) + "/" +
                                    NOT_EXISTING_STAGE_ID))
            ).andReturn().response
        then: "Returns code 400"
            stageResponse.status == HttpStatus.BAD_REQUEST.value()
        and:
            def message = mapper.readValue(stageResponse.contentAsString, ErrorMessage.class)
            message.getMessage() == "Stage with id 10,000 does not exist."
    }

    @Transactional
    def "Getting stages should return list of all stages"() {
        given:
            ProjectDto createdProject = this.createProject()
            String stageRequestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(properStageDto)
            def createdStageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.stageUrlBuilder(createdProject.id)))
                            .header("Content-Type", "application/json")
                            .content(stageRequestBody)
            ).andReturn().response
            StageDto stageDto = mapper.readValue(createdStageResponse.contentAsString, StageDto.class)
        when:
            def stageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .get(URI.create(this.stageUrlBuilder(createdProject.id) + "/" + stageDto.id))
            ).andReturn().response
        then: "Returns code 200."
            stageResponse.status == HttpStatus.OK.value()
        and: "Returns data of proper stage."
            StageDto stageUpdateDto = mapper.readValue(stageResponse.contentAsString, StageDto.class)
            stageUpdateDto.name == properStageDto.name

    }

    private ProjectDto createProject() {
        ArchitectDto architectDto = this.createArchitect()
        this.projectDto.architectId = architectDto.id
        ClientDto clientDto = this.createClient()
        this.projectDto.clientId = clientDto.id
        String projectRequestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(projectDto)
        def projectResponse = this.mockMvc.perform(
                MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + PROJECTS_URI))
                        .header("Content-Type", "application/json")
                        .content(projectRequestBody)
        ).andReturn().response
        return mapper.readValue(projectResponse.contentAsString, ProjectDto.class)
    }

    private ArchitectDto createArchitect() {
        ArchitectBasicDto architectBasicDto = new ArchitectBasicDto(firstName: ARCHITECT_FIRST_NAME,
                lastName: ARCHITECT_LAST_NAME)
        String architectRequestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(architectBasicDto)
        def architectResponse = this.mockMvc.perform(
                MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + ARCHITECTS_URI))
                        .header("Content-Type", "application/json")
                        .content(architectRequestBody)
        ).andReturn().response.contentAsString
        return mapper.readValue(architectResponse, ArchitectDto.class)
    }

    private ClientDto createClient() {
        String clientRequestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(privateClientDto)
        def clientResponse = this.mockMvc.perform(
                MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + CLIENTS_URI))
                        .header("Content-Type", "application/json")
                        .content(clientRequestBody)
        ).andReturn().response.contentAsString
        return mapper.readValue(clientResponse, ClientDto.class)
    }

    private String stageUrlBuilder(long projectId) {
        return HOST + ":" + port + this.stagePartialUriBuilder(projectId)
    }

    private String stagePartialUriBuilder(long projectId) {
        return PROJECTS_URI + "/" + projectId + STAGES_URI
    }

}
