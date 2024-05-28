package com.arturjarosz.task.project

import com.arturjarosz.task.configuration.BaseTestIT
import com.arturjarosz.task.dto.*
import com.arturjarosz.task.sharedkernel.exceptions.ErrorMessage
import com.arturjarosz.task.utils.TestsHelper
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.transaction.annotation.Transactional

class StageTestIT extends BaseTestIT {
    static final String ARCHITECTS_URI = "/architects"
    static final String CLIENTS_URI = "/clients"
    static final String PROJECTS_URI = "/projects"
    static final String STAGES_URI = "/stages"
    static final long NOT_EXISTING_PROJECT_ID = 10000l
    static final long NOT_EXISTING_STAGE_ID = 10000l
    static final ObjectMapper MAPPER = new ObjectMapper()

    final def architect = MAPPER.readValue(new File(getClass().classLoader.getResource('json/architect/architect.json').file),
            ArchitectDto)
    final def privateClientDto = MAPPER.readValue(new File(getClass().classLoader.getResource('json/client/privateClient.json').file),
            ClientDto)
    final def projectDto =
            MAPPER.readValue(new File(getClass().classLoader.getResource('json/project/properProject.json').file),
                    ProjectCreateDto)
    final def properStageDto = MAPPER.readValue(new File(getClass().classLoader.getResource('json/stage/properStage.json').file),
            StageDto)
    final def notProperStageDto = MAPPER.readValue(new File(getClass().classLoader.getResource('json/stage/notProperStage.json').file),
            StageDto)
    final def properStageUpdateDto = MAPPER.readValue(new File(getClass().classLoader.getResource('json/stage/properStageUpdate.json').file),
            StageDto)
    final def notProperStageUpdateDto = MAPPER.readValue(new File(getClass().classLoader.getResource('json/stage/notProperStageUpdate.json').file),
            StageDto)

    @Autowired
    private MockMvc mockMvc

    @Override
    def setupSpec() {
        MAPPER.findAndRegisterModules()
    }

    @Transactional
    def "Creating stage for not existing project should return code 404 and error message about not existing project"() {
        given:
            def stageRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properStageDto)
        when:
            def stageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.stageUrlBuilder(NOT_EXISTING_PROJECT_ID)))
                            .header("Content-Type", "application/json")
                            .content(stageRequestBody)
            ).andReturn().response
        then: "Returns code 400"
            stageResponse.status == HttpStatus.NOT_FOUND.value()
        and:
            def message = MAPPER.readValue(stageResponse.contentAsString, ErrorMessage)
            message.getMessage() == "Project with id 10,000 does not exist."
    }

    @Transactional
    def "Creating stage with not proper data should return code 400 and error message about problem with data"() {
        given:
            def createdProject = this.createProject()
            def stageRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(notProperStageDto)
        when:
            def stageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.stageUrlBuilder(createdProject.id)))
                            .header("Content-Type", "application/json")
                            .content(stageRequestBody)
            ).andReturn().response
        then: "Returns code 400"
            stageResponse.status == HttpStatus.BAD_REQUEST.value()
        and:
            def message = MAPPER.readValue(stageResponse.contentAsString, ErrorMessage)
            message.getMessage() == "Stage name cannot be empty."
    }

    @Transactional
    def "Creating stage for existing project, with proper data should return code 201, created stage dto and location header"() {
        given:
            def createdProject = this.createProject()
            def stageRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properStageDto)
        when:
            def stageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.stageUrlBuilder(createdProject.id)))
                            .header("Content-Type", "application/json")
                            .content(stageRequestBody)
            ).andReturn().response
        then:
            stageResponse.status == HttpStatus.CREATED.value()
        and:
            def stageDto = MAPPER.readValue(stageResponse.contentAsString, StageDto)
            stageDto.name == properStageDto.name
            stageDto.id != null
            stageDto.status == StageStatusDto.TO_DO
            stageDto.createdDateTime != null
        and:
            !stageDto.nextStatuses.empty
    }

    @Transactional
    def "Removing not existing stage should return code 404 and error message"() {
        given:
            def createdProject = this.createProject()
        when:
            def stageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .delete(URI.create(this.stageUrlBuilder(createdProject.id) + "/" +
                                    NOT_EXISTING_STAGE_ID))
            ).andReturn().response
        then: "Returns code 400"
            stageResponse.status == HttpStatus.NOT_FOUND.value()
        and:
            def message = MAPPER.readValue(stageResponse.contentAsString, ErrorMessage)
            message.getMessage() == "Stage with id 10,000 does not exist."
    }


    @Transactional
    def "Removing existing stage should return code 200 and remove stage"() {
        given:
            def createdProject = this.createProject()
            def stageRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properStageDto)
            def createdStageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.stageUrlBuilder(createdProject.id)))
                            .header("Content-Type", "application/json")
                            .content(stageRequestBody)
            ).andReturn().response
            def stageDto = MAPPER.readValue(createdStageResponse.contentAsString, StageDto)
        when:
            def stageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .delete(URI.create(this.stageUrlBuilder(createdProject.id) + "/" + stageDto.id))
            ).andReturn().response
        then: "Returns code 200"
            stageResponse.status == HttpStatus.OK.value()
        and: "Getting stage with removed stage id returns code 404 and error message."
            def removedStageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .get(URI.create(this.stageUrlBuilder(createdProject.id) + "/" + stageDto.id))
                            .header("Content-Type", "application/json")
                            .content(stageRequestBody)
            ).andReturn().response
            removedStageResponse.status == HttpStatus.NOT_FOUND.value()
            def message = MAPPER.readValue(removedStageResponse.contentAsString, ErrorMessage)
            message.getMessage() == "Stage with id " + stageDto.id + " does not exist."
    }

    @Transactional
    def "Updating not existing stage should return code 404 and error message"() {
        given:
            def createdProject = this.createProject()
            def stageRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properStageUpdateDto)
        when:
            def stageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .put(URI.create(this.stageUrlBuilder(createdProject.id) + "/" +
                                    NOT_EXISTING_STAGE_ID))
                            .header("Content-Type", "application/json")
                            .content(stageRequestBody)
            ).andReturn().response
        then: "Returns code 400"
            stageResponse.status == HttpStatus.NOT_FOUND.value()
        and:
            def message = MAPPER.readValue(stageResponse.contentAsString, ErrorMessage)
            message.getMessage() == "Stage with id 10,000 does not exist."
    }

    @Transactional
    def "Updating existing stage with not proper date should return 400 and error message"() {
        given:
            def createdProject = this.createProject()
            def stageRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properStageDto)
            def createdStageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.stageUrlBuilder(createdProject.id)))
                            .header("Content-Type", "application/json")
                            .content(stageRequestBody)
            ).andReturn().response
            def stageDto = MAPPER.readValue(createdStageResponse.contentAsString, StageDto)
            def stageUpdateRequestBody =
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
            def message = MAPPER.readValue(stageResponse.contentAsString, ErrorMessage)
            message.getMessage() == "Stage name cannot be empty."
    }

    @Transactional
    def "Updating existing stage should return code 200 and dto of updated stage"() {
        given:
            def createdProject = this.createProject()
            def stageRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properStageDto)
            def createdStageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.stageUrlBuilder(createdProject.id)))
                            .header("Content-Type", "application/json")
                            .content(stageRequestBody)
            ).andReturn().response
            def stageDto = MAPPER.readValue(createdStageResponse.contentAsString, StageDto)
            def stageUpdateRequestBody =
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
            def stageUpdateDto = MAPPER.readValue(stageResponse.contentAsString, StageDto)
            stageUpdateDto.name == properStageUpdateDto.name
            stageUpdateDto.note == properStageUpdateDto.note
            stageUpdateDto.type == properStageUpdateDto.type
            stageUpdateDto.deadline == properStageUpdateDto.deadline
            stageUpdateDto.lastModifiedDateTime != null
        and:
            !stageDto.nextStatuses.empty
    }

    @Transactional
    def "Getting not existing stage should code 404 and error message"() {
        given:
            def createdProject = this.createProject()
        when:
            def stageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .get(URI.create(this.stageUrlBuilder(createdProject.id) + "/" +
                                    NOT_EXISTING_STAGE_ID))
            ).andReturn().response
        then: "Returns code 400"
            stageResponse.status == HttpStatus.NOT_FOUND.value()
        and:
            def message = MAPPER.readValue(stageResponse.contentAsString, ErrorMessage)
            message.getMessage() == "Stage with id 10,000 does not exist."
    }

    @Transactional
    def "Getting stages should return list of all stages"() {
        given:
            def createdProject = this.createProject()
            def stageRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properStageDto)
            def createdStageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.stageUrlBuilder(createdProject.id)))
                            .header("Content-Type", "application/json")
                            .content(stageRequestBody)
            ).andReturn().response
            def stageDto = MAPPER.readValue(createdStageResponse.contentAsString, StageDto)
        when:
            def stageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .get(URI.create(this.stageUrlBuilder(createdProject.id) + "/" + stageDto.id))
            ).andReturn().response
        then: "Returns code 200."
            stageResponse.status == HttpStatus.OK.value()
        and: "Returns data of proper stage."
            def stageUpdateDto = MAPPER.readValue(stageResponse.contentAsString, StageDto)
            stageUpdateDto.name == properStageDto.name

    }

    private ProjectDto createProject() {
        def architectDto = TestsHelper.createArchitect(this.architect, this.createArchitectUrl(), this.mockMvc)
        this.projectDto.architectId = architectDto.id
        def clientDto = TestsHelper.createClient(this.privateClientDto, this.createClientUri(), this.mockMvc)
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

    private String createBasicProjectUri() {
        return HOST + ":" + port + PROJECTS_URI
    }

}
