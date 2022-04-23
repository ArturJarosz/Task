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
import com.arturjarosz.task.utils.TestsHelper
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.transaction.annotation.Transactional

class StageTestIT extends BaseTestIT {
    private static final String ARCHITECTS_URI = "/architects"
    private static final String CLIENTS_URI = "/clients"
    private static final String PROJECTS_URI = "/projects"
    private static final String STAGES_URI = "/stages"
    private static final long NOT_EXISTING_PROJECT_ID = 10000l
    private static final long NOT_EXISTING_STAGE_ID = 10000l
    private static final ObjectMapper MAPPER = new ObjectMapper()

    private final ArchitectBasicDto architect =
            MAPPER.readValue(new File(getClass().classLoader.getResource('json/architect/architect.json').file),
                    ArchitectBasicDto.class)
    private final ClientDto privateClientDto =
            MAPPER.readValue(new File(getClass().classLoader.getResource('json/client/privateClient.json').file),
                    ClientDto.class)
    private final ProjectCreateDto projectDto =
            MAPPER.readValue(new File(getClass().classLoader.getResource('json/project/properProject.json').file),
                    ProjectCreateDto.class)
    private final StageDto properStageDto =
            MAPPER.readValue(new File(getClass().classLoader.getResource('json/stage/properStage.json').file),
                    StageDto.class)
    private final StageDto notProperStageDto =
            MAPPER.readValue(new File(getClass().classLoader.getResource('json/stage/notProperStage.json').file),
                    StageDto.class)
    private final StageDto properStageUpdateDto =
            MAPPER.readValue(new File(getClass().classLoader.getResource('json/stage/properStageUpdate.json').file),
                    StageDto.class)
    private final StageDto notProperStageUpdateDto =
            MAPPER.readValue(new File(getClass().classLoader.getResource('json/stage/notProperStageUpdate.json').file),
                    StageDto.class)

    @Autowired
    private MockMvc mockMvc

    @Override
    def setupSpec() {
        MAPPER.findAndRegisterModules()
    }

    @Transactional
    def "Creating stage for not existing project should return code 400 and error message about not existing project"() {
        given:
            String stageRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properStageDto)
        when:
            def stageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.stageUrlBuilder(NOT_EXISTING_PROJECT_ID)))
                            .header("Content-Type", "application/json")
                            .content(stageRequestBody)
            ).andReturn().response
        then: "Returns code 400"
            stageResponse.status == HttpStatus.BAD_REQUEST.value()
        and:
            def message = MAPPER.readValue(stageResponse.contentAsString, ErrorMessage.class)
            message.getMessage() == "Project with id 10,000 does not exist."
    }

    @Transactional
    def "Creating stage with not proper data should return code 400 and error message about problem with data"() {
        given:
            ProjectDto createdProject = this.createProject()
            String stageRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(notProperStageDto)
        when:
            def stageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.stageUrlBuilder(createdProject.id)))
                            .header("Content-Type", "application/json")
                            .content(stageRequestBody)
            ).andReturn().response
        then: "Returns code 400"
            stageResponse.status == HttpStatus.BAD_REQUEST.value()
        and:
            def message = MAPPER.readValue(stageResponse.contentAsString, ErrorMessage.class)
            message.getMessage() == "Stage name cannot be empty."
    }

    @Transactional
    def "Creating stage for existing project, with proper data should return code 201, created stage dto and location header"() {
        given:
            ProjectDto createdProject = this.createProject()
            String stageRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properStageDto)
        when:
            def stageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.stageUrlBuilder(createdProject.id)))
                            .header("Content-Type", "application/json")
                            .content(stageRequestBody)
            ).andReturn().response
        then:
            stageResponse.status == HttpStatus.CREATED.value()
        and:
            StageDto stageDto = MAPPER.readValue(stageResponse.contentAsString, StageDto.class)
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
            def message = MAPPER.readValue(stageResponse.contentAsString, ErrorMessage.class)
            message.getMessage() == "Stage with id 10,000 does not exist."
    }

    @Transactional
    def "Removing existing stage should return code 200 and remove stage"() {
        given:
            ProjectDto createdProject = this.createProject()
            String stageRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properStageDto)
            def createdStageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.stageUrlBuilder(createdProject.id)))
                            .header("Content-Type", "application/json")
                            .content(stageRequestBody)
            ).andReturn().response
            StageDto stageDto = MAPPER.readValue(createdStageResponse.contentAsString, StageDto.class)
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
            def message = MAPPER.readValue(removedStageResponse.contentAsString, ErrorMessage.class)
            message.getMessage() == "Stage with id " + stageDto.id + " does not exist."
    }

    @Transactional
    def "Updating not existing stage should return code 400 and error message"() {
        given:
            ProjectDto createdProject = this.createProject()
            String stageRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properStageUpdateDto)
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
            def message = MAPPER.readValue(stageResponse.contentAsString, ErrorMessage.class)
            message.getMessage() == "Stage with id 10,000 does not exist."
    }

    @Transactional
    def "Updating existing stage with not proper date should return 400 and error message"() {
        given:
            ProjectDto createdProject = this.createProject()
            String stageRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properStageDto)
            def createdStageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.stageUrlBuilder(createdProject.id)))
                            .header("Content-Type", "application/json")
                            .content(stageRequestBody)
            ).andReturn().response
            StageDto stageDto = MAPPER.readValue(createdStageResponse.contentAsString, StageDto.class)
            String stageUpdateRequestBody =
                    MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(notProperStageUpdateDto)
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
            def message = MAPPER.readValue(stageResponse.contentAsString, ErrorMessage.class)
            message.getMessage() == "Stage name cannot be empty."
    }

    @Transactional
    def "Updating existing stage should return code 200 and dto of updated stage"() {
        given:
            ProjectDto createdProject = this.createProject()
            String stageRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properStageDto)
            def createdStageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.stageUrlBuilder(createdProject.id)))
                            .header("Content-Type", "application/json")
                            .content(stageRequestBody)
            ).andReturn().response
            StageDto stageDto = MAPPER.readValue(createdStageResponse.contentAsString, StageDto.class)
            String stageUpdateRequestBody =
                    MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properStageUpdateDto)
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
            StageDto stageUpdateDto = MAPPER.readValue(stageResponse.contentAsString, StageDto.class)
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
            def message = MAPPER.readValue(stageResponse.contentAsString, ErrorMessage.class)
            message.getMessage() == "Stage with id 10,000 does not exist."
    }

    @Transactional
    def "Getting stages should return list of all stages"() {
        given:
            ProjectDto createdProject = this.createProject()
            String stageRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properStageDto)
            def createdStageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.stageUrlBuilder(createdProject.id)))
                            .header("Content-Type", "application/json")
                            .content(stageRequestBody)
            ).andReturn().response
            StageDto stageDto = MAPPER.readValue(createdStageResponse.contentAsString, StageDto.class)
        when:
            def stageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .get(URI.create(this.stageUrlBuilder(createdProject.id) + "/" + stageDto.id))
            ).andReturn().response
        then: "Returns code 200."
            stageResponse.status == HttpStatus.OK.value()
        and: "Returns data of proper stage."
            StageDto stageUpdateDto = MAPPER.readValue(stageResponse.contentAsString, StageDto.class)
            stageUpdateDto.name == properStageDto.name

    }

    private ProjectDto createProject() {
        ArchitectDto architectDto = TestsHelper.createArchitect(this.architect, this.createArchitectUrl(), this.mockMvc)
        this.projectDto.architectId = architectDto.id
        ClientDto clientDto = TestsHelper.createClient(this.privateClientDto, this.createClientUri(), this.mockMvc)
        this.projectDto.clientId = clientDto.id
        return TestsHelper.createProject(this.projectDto, this.createBasicProjectUri(), this.mockMvc)
    }

    private String createArchitectUrl() {
        return HOST + ":" + port + ARCHITECTS_URI
    }

    private String stageUrlBuilder(long projectId) {
        return HOST + ":" + port + this.stagePartialUriBuilder(projectId)
    }

    private String stagePartialUriBuilder(long projectId) {
        return PROJECTS_URI + "/" + projectId + STAGES_URI
    }

    private String createClientUri() {
        return HOST + ":" + port + CLIENTS_URI
    }

    private String createBasicProjectUri(){
        return HOST + ":" + port + PROJECTS_URI
    }

}
