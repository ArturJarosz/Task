package com.arturjarosz.task.project

import com.arturjarosz.task.architect.application.dto.ArchitectBasicDto
import com.arturjarosz.task.architect.application.dto.ArchitectDto
import com.arturjarosz.task.client.application.dto.ClientDto
import com.arturjarosz.task.configuration.BaseTestIT
import com.arturjarosz.task.project.application.dto.*
import com.arturjarosz.task.project.status.project.ProjectStatus
import com.arturjarosz.task.project.status.stage.StageStatus
import com.arturjarosz.task.project.status.task.TaskStatus
import com.arturjarosz.task.sharedkernel.exceptions.ErrorMessage
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.transaction.annotation.Transactional
import spock.lang.Shared

class ProjectStatusWorkflowTestIT extends BaseTestIT {
    private static final String ARCHITECT_FIRST_NAME = "First Name"
    private static final String ARCHITECT_LAST_NAME = "Last Name"
    private static final String ARCHITECTS_URI = "/architects"
    private static final String CLIENTS_URI = "/clients"
    private static final String PROJECTS_URI = "/projects"
    private static final Double NEW_OFFER_VALUE = 10000.00D;

    private final ObjectMapper mapper = new ObjectMapper()

    private final ClientDto privateClientDto =
            mapper.readValue(new File(getClass().classLoader.getResource('json/client/privateClient.json').file),
                    ClientDto.class)
    private final ProjectCreateDto properProjectDto =
            mapper.readValue(new File(getClass().classLoader.getResource('json/project/properProject.json').file),
                    ProjectCreateDto.class)
    private final StageDto stageDto =
            mapper.readValue(new File(getClass().classLoader.getResource('json/stage/properStage.json').file),
                    StageDto.class)
    private final TaskDto taskDto =
            mapper.readValue(new File(getClass().classLoader.getResource('json/task/properTask.json').file),
                    TaskDto.class)

    @Shared
    private long projectId;

    @Autowired
    private MockMvc mockMvc

    @Transactional
    def "Creating project should return code 200 and put project in OFFER status"() {
        given: "Existing architect"
            ArchitectDto architectDto = this.createArchitect()
            properProjectDto.architectId = architectDto.id
        and: "Existing client"
            ClientDto clientDto = this.createClient()
            properProjectDto.clientId = clientDto.id
        when: "Creating project with proper data"
            String projectRequestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(properProjectDto)
            def projectResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + PROJECTS_URI))
                            .header("Content-Type", "application/json")
                            .content(projectRequestBody)
            ).andReturn().response
            ProjectDto projectDto = mapper.readValue(projectResponse.contentAsString, ProjectDto.class)
            projectId = projectDto.id
        then: "Returns code 201"
            projectResponse.status == HttpStatus.CREATED.value()
        and: "Project status is set to offer"
            projectDto.status == ProjectStatus.OFFER
    }

    @Transactional
    def "Rejecting new project should put it in REJECT status"() {
        given: "New project"
            ProjectDto createProjectDto = this.createProject()
        when: "Rejecting new project"
            def rejectResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.createProjectUri(createProjectDto.id) + "/reject"))
            ).andReturn().response
        then: "Response code should be 200"
            rejectResponse.status == HttpStatus.OK.value()
        and: "Project status is REJECTED"
            ProjectDto projectDto = mapper.readValue(rejectResponse.contentAsString, ProjectDto.class)
            projectDto.status == ProjectStatus.REJECTED
    }

    @Transactional
    def "Making new offer for rejected project, should put that project in OFFER status and change its value"() {
        given: "Create and project and reject it"
            ProjectDto createProjectDto = this.createProject()
            def rejectResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.createProjectUri(createProjectDto.id) + "/reject"))
            ).andReturn().response
            OfferDto offerDto = new OfferDto(offerValue: NEW_OFFER_VALUE)
            String offerRequestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(offerDto)
        when: "Making new offer for rejected project"
            def offerResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.createProjectUri(createProjectDto.id) + "/newOffer"))
                            .header("Content-Type", "application/json")
                            .content(offerRequestBody)
            ).andReturn().response
        then: "Return code is 200"
            offerResponse.status == HttpStatus.OK.value()
        and: "Project status is OFFER"
            ProjectDto projectDto = mapper.readValue(offerResponse.contentAsString, ProjectDto.class)
            projectDto.status == ProjectStatus.OFFER
        and: "Project value is same as new offer value"
            projectDto.projectValue == NEW_OFFER_VALUE
    }

    @Transactional
    def "Making new offer for new project, should change offer value to new value"() {
        given: "Create and project and reject it"
            ProjectDto createProjectDto = this.createProject()
            OfferDto offerDto = new OfferDto(offerValue: NEW_OFFER_VALUE)
            String offerRequestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(offerDto)
        when: "Making new offer for rejected project"
            def offerResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.createProjectUri(createProjectDto.id) + "/newOffer"))
                            .header("Content-Type", "application/json")
                            .content(offerRequestBody)
            ).andReturn().response
        then: "Return code is 200"
            offerResponse.status == HttpStatus.OK.value()
            ProjectDto projectDto = mapper.readValue(offerResponse.contentAsString, ProjectDto.class)
        and: "Project value is same as new offer value"
            projectDto.projectValue == NEW_OFFER_VALUE
    }

    @Transactional
    def "Accepting an offer for Project should change project status to TO DO"() {
        given: "Create and project and reject it"
            ProjectDto createProjectDto = this.createProject()
        when: "Making new offer for rejected project"
            def offerResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.createProjectUri(createProjectDto.id) + "/acceptOffer"))
            ).andReturn().response
        then: "Return code is 200"
            offerResponse.status == HttpStatus.OK.value()
            ProjectDto projectDto = mapper.readValue(offerResponse.contentAsString, ProjectDto.class)
        and: "Project value is same as new offer value"
            projectDto.status == ProjectStatus.TO_DO
    }

    @Transactional
    def "For project with not accepted offer, it should not be possible to start progress in work"() {
        given:
            ProjectDto createProjectDto = this.createProject()
            StageDto createStageDto = this.createStage(createProjectDto.id)
            TaskDto createTaskDto = this.createTask(createProjectDto.id, createStageDto.id)
            TaskDto updateStatusDto = new TaskDto(status: TaskStatus.IN_PROGRESS)
            String requestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(updateStatusDto)
        when:
            def changeTaskStatusResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .post(URI
                                    .create(this.createTaskUri(createProjectDto.id, createStageDto.id,
                                            createTaskDto.id) + "/updateStatus"))
                            .header("Content-Type", "application/json")
                            .content(requestBody)
            ).andReturn().response
        then:
            changeTaskStatusResponse.status == HttpStatus.BAD_REQUEST.value()
        and:
            def errorMessage = mapper.readValue(changeTaskStatusResponse.contentAsString, ErrorMessage.class)
            errorMessage.message == "You cannot start progress for Project, for which offer was not accepted."
    }

    @Transactional
    def "For project with accepted offer, it should be possible to start progress in work"() {
        given:
            ProjectDto createProjectDto = this.createProject()
            StageDto createStageDto = this.createStage(createProjectDto.id)
            TaskDto createTaskDto = this.createTask(createProjectDto.id, createStageDto.id)
            def acceptOfferResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .post(URI
                                    .create(this.createProjectUri(createProjectDto.id) + "/acceptOffer"))
            ).andReturn().response
            TaskDto updateStatusDto = new TaskDto(status: TaskStatus.IN_PROGRESS)
            String requestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(updateStatusDto)
        when:
            def changeTaskStatusResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .post(URI
                                    .create(this.createTaskUri(createProjectDto.id, createStageDto.id,
                                            createTaskDto.id) + "/updateStatus"))
                            .header("Content-Type", "application/json")
                            .content(requestBody)
            ).andReturn().response
        then:
            changeTaskStatusResponse.status == HttpStatus.OK.value()
        and:
            def updatedTaskDto = mapper.readValue(changeTaskStatusResponse.contentAsString, TaskDto.class)
            updatedTaskDto.status == TaskStatus.IN_PROGRESS
    }

    @Transactional
    def "Rejecting project with signed offer should put it in REJECTED status"() {
        given:
            ProjectDto createProjectDto = this.createProject()
            def acceptOfferResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .post(URI
                                    .create(this.createProjectUri(createProjectDto.id) + "/acceptOffer"))
            ).andReturn().response
        when:
            def rejectResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.createProjectUri(createProjectDto.id) + "/reject"))
            ).andReturn().response
        then: "Response code should be 200"
            rejectResponse.status == HttpStatus.OK.value()
        and: "Project status is REJECTED"
            ProjectDto projectDto = mapper.readValue(rejectResponse.contentAsString, ProjectDto.class)
            projectDto.status == ProjectStatus.REJECTED
    }

    @Transactional
    def "Starting working on one task should put task, stage and project that task belongs to in IN PROGRESS status"() {
        given: "Project with accepted offer and stages and tasks in TO DO"
            ProjectDto createProjectDto = this.createProject()
            StageDto createStageDto1 = this.createStage(createProjectDto.id)
            TaskDto createTaskDto11 = this.createTask(createProjectDto.id, createStageDto1.id)
            TaskDto createTaskDto12 = this.createTask(createProjectDto.id, createStageDto1.id)
            StageDto createStageDto2 = this.createStage(createProjectDto.id)
            TaskDto createTaskDto21 = this.createTask(createProjectDto.id, createStageDto1.id)
            TaskDto createTaskDto22 = this.createTask(createProjectDto.id, createStageDto1.id)
            def acceptOfferResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .post(URI
                                    .create(this.createProjectUri(createProjectDto.id) + "/acceptOffer"))
            ).andReturn().response
            TaskDto updateStatusDto = new TaskDto(status: TaskStatus.IN_PROGRESS)
            String requestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(updateStatusDto)
        when: "Change one task status to IN PROGRESS"
            def changeTaskStatusResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .post(URI.create(this.createTaskUri(createProjectDto.id, createStageDto1.id,
                                    createTaskDto11.id) + "/updateStatus"))
                            .header("Content-Type", "application/json")
                            .content(requestBody)
            ).andReturn().response
        then: "Response code is 200"
            changeTaskStatusResponse.status == HttpStatus.OK.value()
        and: "Task status is IN PROGRESS"
            def getTaskResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.get(URI.create(
                            this.createTaskUri(createProjectDto.id, createStageDto1.id, createTaskDto11.id))
                    )
            ).andReturn().response
            TaskDto getTaskDto = mapper.readValue(getTaskResponse.contentAsString, TaskDto.class)
            getTaskDto.status == TaskStatus.IN_PROGRESS
        and: "Stage status is IN PROGRESS"
            def getStageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.get(URI.create(this.createStageUri(createProjectDto.id, createStageDto1.id))
                    )
            ).andReturn().response
            StageDto getStageDto = mapper.readValue(getStageResponse.contentAsString, StageDto.class)
            getStageDto.status == StageStatus.IN_PROGRESS
        and: "Project status is IN PROGRESS"
            def getProjectResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.get(URI.create(this.createProjectUri(createProjectDto.id))
                    )
            ).andReturn().response
            ProjectDto getProjectDto = mapper.readValue(getProjectResponse.contentAsString, ProjectDto.class)
            getProjectDto.status == ProjectStatus.IN_PROGRESS
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

    private ProjectDto createProject() {
        ArchitectDto architectDto = this.createArchitect()
        properProjectDto.architectId = architectDto.id
        ClientDto clientDto = this.createClient()
        properProjectDto.clientId = clientDto.id
        String projectRequestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(properProjectDto)
        def projectResponse = this.mockMvc.perform(
                MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + PROJECTS_URI))
                        .header("Content-Type", "application/json")
                        .content(projectRequestBody)
        ).andReturn().response
        return mapper.readValue(projectResponse.contentAsString, ProjectDto.class)
    }

    private StageDto createStage(long projectId) {
        String stageRequestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(stageDto)
        def stageResponse = this.mockMvc.perform(
                MockMvcRequestBuilders.post(URI.create(this.createProjectUri(projectId) + "/stages"))
                        .header("Content-Type", "application/json")
                        .content(stageRequestBody)
        ).andReturn().response
        return mapper.readValue(stageResponse.contentAsString, StageDto.class)
    }

    private TaskDto createTask(long projectId, long stageId) {
        String taskRequestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(taskDto)
        def stageResponse = this.mockMvc.perform(
                MockMvcRequestBuilders.post(URI.create(this.createStageUri(projectId, stageId) + "/tasks"))
                        .header("Content-Type", "application/json")
                        .content(taskRequestBody)
        ).andReturn().response
        return mapper.readValue(stageResponse.contentAsString, TaskDto.class)
    }

    private String createProjectUri(long projectId) {
        HOST + ":" + port + PROJECTS_URI + "/" + projectId
    }

    private String createStageUri(long projectId, long stageId) {
        this.createProjectUri(projectId) + "/stages/" + stageId
    }

    private String createTaskUri(long projectId, long stageId, long taskId) {
        this.createStageUri(projectId, stageId) + "/tasks/" + taskId
    }
}
