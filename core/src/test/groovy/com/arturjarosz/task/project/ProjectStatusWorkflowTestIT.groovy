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
import org.springframework.mock.web.MockHttpServletResponse
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
            def acceptOfferResponse = this.acceptProjectOffer(createProjectDto.id)
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
    def "For project with accepted offer, it should not be possible to make new offer"() {
        given:
            ProjectDto createProjectDto = this.createProject()
            StageDto createStageDto = this.createStage(createProjectDto.id)
            TaskDto createTaskDto = this.createTask(createProjectDto.id, createStageDto.id)
            def acceptOfferResponse = this.acceptProjectOffer(createProjectDto.id)
            TaskDto updateStatusDto = new TaskDto(status: TaskStatus.IN_PROGRESS)
            OfferDto offerDto = new OfferDto(offerValue: NEW_OFFER_VALUE)
            String offerRequestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(offerDto)
        when:
            def offerResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.createProjectUri(createProjectDto.id) + "/newOffer"))
                            .header("Content-Type", "application/json")
                            .content(offerRequestBody)
            ).andReturn().response
        then:
            offerResponse.status == HttpStatus.BAD_REQUEST.value()
        and:
            def errorMessage = mapper.readValue(offerResponse.contentAsString, ErrorMessage.class)
            errorMessage.message == "It is not possible to make project status transition from TO_DO to OFFER."
    }

    @Transactional
    def "Rejecting project with accepted offer should put it in REJECTED status"() {
        given:
            ProjectDto createProjectDto = this.createProject()
            def acceptOfferResponse = this.acceptProjectOffer(createProjectDto.id)
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
    def "Reopening rejected project with accepted offer and stages only in TO_DO statuses should change project status to TO_DO"() {
        given:
            ProjectDto createProjectDto = this.createProject()
            def acceptOfferResponse = this.acceptProjectOffer(createProjectDto.id)
            StageDto createStageDto = this.createStage(createProjectDto.id)
            TaskDto createTaskDto = this.createTask(createProjectDto.id, createStageDto.id)
            def rejectResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.createProjectUri(createProjectDto.id) + "/reject"))
            ).andReturn().response
        when:
            def reopenResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.createProjectUri(createProjectDto.id) + "/reopen"))
            ).andReturn().response
        then: "Response code should be 200"
            reopenResponse.status == HttpStatus.OK.value()
        and: "Project status is REJECTED"
            ProjectDto projectDto = mapper.readValue(reopenResponse.contentAsString, ProjectDto.class)
            projectDto.status == ProjectStatus.TO_DO
    }

    @Transactional
    def "Reopening rejected project, that was in IN_PROGRESS status, should be back to IN_PROGRES"() {
        given:
            ProjectDto createProjectDto = this.createProject()
            def acceptOfferResponse = this.acceptProjectOffer(createProjectDto.id)
            StageDto createStageDto = this.createStage(createProjectDto.id)
            TaskDto createTaskDto = this.createTask(createProjectDto.id, createStageDto.id)
            def changeTaskStatusResponse =
                    this.updateTaskStatus(createProjectDto.id, createStageDto.id, createTaskDto.id,
                            TaskStatus.IN_PROGRESS)
            def rejectResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.createProjectUri(createProjectDto.id) + "/reject"))
            ).andReturn().response
        when:
            def reopenResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.createProjectUri(createProjectDto.id) + "/reopen"))
            ).andReturn().response
        then: "Response code should be 200"
            reopenResponse.status == HttpStatus.OK.value()
        and: "Project status is IN_PROGRESS"
            ProjectDto projectDto = mapper.readValue(reopenResponse.contentAsString, ProjectDto.class)
            projectDto.status == ProjectStatus.IN_PROGRESS
    }

    @Transactional
    def "Starting working on one task should put task, stage and project that task belongs to in IN PROGRESS status"() {
        given: "Project with accepted offer and stages and tasks in TO DO"
            ProjectDto createProjectDto = this.createProject()
            StageDto createStageDto = this.createStage(createProjectDto.id)
            TaskDto createTaskDto11 = this.createTask(createProjectDto.id, createStageDto.id)
            TaskDto createTaskDto12 = this.createTask(createProjectDto.id, createStageDto.id)
            StageDto createSta0geDto2 = this.createStage(createProjectDto.id)
            TaskDto createTaskDto21 = this.createTask(createProjectDto.id, createStageDto.id)
            TaskDto createTaskDto22 = this.createTask(createProjectDto.id, createStageDto.id)
            def acceptOfferResponse = this.acceptProjectOffer(createProjectDto.id)
        when: "Change one task status to IN PROGRESS"
            def changeTaskStatusResponse =
                    this.updateTaskStatus(createProjectDto.id, createStageDto.id, createTaskDto11.id,
                            TaskStatus.IN_PROGRESS)
        then: "Response code is 200"
            changeTaskStatusResponse.status == HttpStatus.OK.value()
        and: "Task status is IN PROGRESS"
            def getTaskResponse = getTaskResponse(createProjectDto.id, createStageDto.id, createTaskDto11.id)
            TaskDto getTaskDto = mapper.readValue(getTaskResponse.contentAsString, TaskDto.class)
            getTaskDto.status == TaskStatus.IN_PROGRESS
        and: "Stage status is IN PROGRESS"
            def getStageResponse = this.getStageResponse(createProjectDto.id, createStageDto.id)
            StageDto getStageDto = mapper.readValue(getStageResponse.contentAsString, StageDto.class)
            getStageDto.status == StageStatus.IN_PROGRESS
        and: "Project status is IN PROGRESS"
            def getProjectResponse = this.getProjectResponse(createProjectDto.id)
            ProjectDto getProjectDto = mapper.readValue(getProjectResponse.contentAsString, ProjectDto.class)
            getProjectDto.status == ProjectStatus.IN_PROGRESS
    }

    @Transactional
    def "Reject only task in stage, should put that stage in TO_DO status and put PROJECT in TO_DO"() {
        given: "Project with accepted offer and stages and tasks in TO DO"
            ProjectDto createProjectDto = this.createProject()
            StageDto createStageDto = this.createStage(createProjectDto.id)
            TaskDto createTaskDto11 = this.createTask(createProjectDto.id, createStageDto.id)
            def acceptOfferResponse = this.acceptProjectOffer(createProjectDto.id)
            def changeTaskStatusResponse =
                    this.updateTaskStatus(createProjectDto.id, createStageDto.id, createTaskDto11.id,
                            TaskStatus.IN_PROGRESS)
        when: "Change one task status to IN PROGRESS"
            changeTaskStatusResponse =
                    this.updateTaskStatus(createProjectDto.id, createStageDto.id, createTaskDto11.id,
                            TaskStatus.REJECTED)
        then: "Response code is 200"
            changeTaskStatusResponse.status == HttpStatus.OK.value()
        and: "Task status is REJECTED"
            def getTaskResponse = getTaskResponse(createProjectDto.id, createStageDto.id, createTaskDto11.id)
            TaskDto getTaskDto = mapper.readValue(getTaskResponse.contentAsString, TaskDto.class)
            getTaskDto.status == TaskStatus.REJECTED
        and: "Stage status is TO_DO"
            def getStageResponse = this.getStageResponse(createProjectDto.id, createStageDto.id)
            StageDto getStageDto = mapper.readValue(getStageResponse.contentAsString, StageDto.class)
            getStageDto.status == StageStatus.TO_DO
        and: "Project status is TO_DO"
            def getProjectResponse = this.getProjectResponse(createProjectDto.id)
            ProjectDto getProjectDto = mapper.readValue(getProjectResponse.contentAsString, ProjectDto.class)
            getProjectDto.status == ProjectStatus.TO_DO
    }

    @Transactional
    def "Finishing work on only task in stage, should put that stage in TO_DO status and put PROJECT in TO_DO"() {
        given: "Project with accepted offer and stages and tasks in TO DO"
            ProjectDto createProjectDto = this.createProject()
            StageDto createStageDto = this.createStage(createProjectDto.id)
            TaskDto createTaskDto11 = this.createTask(createProjectDto.id, createStageDto.id)
            def acceptOfferResponse = this.acceptProjectOffer(createProjectDto.id)
            def changeTaskStatusResponse =
                    this.updateTaskStatus(createProjectDto.id, createStageDto.id, createTaskDto11.id,
                            TaskStatus.IN_PROGRESS)
        when: "Change one task status to IN PROGRESS"
            changeTaskStatusResponse =
                    this.updateTaskStatus(createProjectDto.id, createStageDto.id, createTaskDto11.id,
                            TaskStatus.COMPLETED)
        then: "Response code is 200"
            changeTaskStatusResponse.status == HttpStatus.OK.value()
        and: "Task status is COMPLETED"
            def getTaskResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.get(URI.create(
                            this.createTaskUri(createProjectDto.id, createStageDto.id, createTaskDto11.id))
                    )
            ).andReturn().response
            TaskDto getTaskDto = mapper.readValue(getTaskResponse.contentAsString, TaskDto.class)
            getTaskDto.status == TaskStatus.COMPLETED
        and: "Stage status is COMPLETED"
            def getStageResponse = this.getStageResponse(createProjectDto.id, createStageDto.id)
            StageDto getStageDto = mapper.readValue(getStageResponse.contentAsString, StageDto.class)
            getStageDto.status == StageStatus.COMPLETED
        and: "Project status is COMPLETED"
            def getProjectResponse = this.getProjectResponse(createProjectDto.id)
            ProjectDto getProjectDto = mapper.readValue(getProjectResponse.contentAsString, ProjectDto.class)
            getProjectDto.status == ProjectStatus.COMPLETED
    }

    @Transactional
    def "Rejecting stage should put stage in REJECTED status and not change project status"() {
        given: "Project with accepted offer and stages and tasks in TO DO"
            ProjectDto createProjectDto = this.createProject()
            StageDto createStageDto = this.createStage(createProjectDto.id)
            TaskDto createTaskDto11 = this.createTask(createProjectDto.id, createStageDto.id)
            def acceptOfferResponse = this.acceptProjectOffer(createProjectDto.id)
        when:
            def rejectStageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .post(URI
                                    .create(this.createStageUri(createProjectDto.id, createStageDto.id) + "/reject"))
            ).andReturn().response
        then: "Response code is 200"
            rejectStageResponse.status == HttpStatus.OK.value()
        and: "Stage status is REJECTED"
            StageDto getStageDto = mapper.readValue(rejectStageResponse.contentAsString, StageDto.class)
            getStageDto.status == StageStatus.REJECTED
        and:
            def getProjectResponse = this.getProjectResponse(createProjectDto.id)
            ProjectDto getProjectDto = mapper.readValue(getProjectResponse.contentAsString, ProjectDto.class)
            getProjectDto.status == ProjectStatus.TO_DO
    }

    @Transactional
    def "Creating new task on stage in TO_DO status should not change stage or project status"() {
        given: "Project with accepted offer and stages and tasks in TO DO"
            ProjectDto createProjectDto = this.createProject()
            StageDto createStageDto = this.createStage(createProjectDto.id)
            TaskDto createTaskDto11 = this.createTask(createProjectDto.id, createStageDto.id)
            def acceptOfferResponse = this.acceptProjectOffer(createProjectDto.id)
        when:
            String taskRequestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(taskDto)
            def taskResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .post(URI.create(this.createStageUri(createProjectDto.id, createStageDto.id) + "/tasks"))
                            .header("Content-Type", "application/json")
                            .content(taskRequestBody)
            ).andReturn().response
        then: "Response code is 201"
            taskResponse.status == HttpStatus.CREATED.value()
        and: "Task status is TO_DO"
            TaskDto createdTaskDto12 = mapper.readValue(taskResponse.contentAsString, TaskDto.class)
            def getTaskResponse = getTaskResponse(createProjectDto.id, createStageDto.id, createdTaskDto12.id)
            TaskDto getTaskDto = mapper.readValue(getTaskResponse.contentAsString, TaskDto.class)
            getTaskDto.status == TaskStatus.TO_DO
        and: "Stage status is TO_DO"
            def getStageResponse = this.getStageResponse(createProjectDto.id, createStageDto.id)
            StageDto getStageDto = mapper.readValue(getStageResponse.contentAsString, StageDto.class)
            getStageDto.status == StageStatus.TO_DO
        and: "Project status is TO_DO"
            def getProjectResponse = this.getProjectResponse(createProjectDto.id)
            ProjectDto getProjectDto = mapper.readValue(getProjectResponse.contentAsString, ProjectDto.class)
            getProjectDto.status == ProjectStatus.TO_DO
    }

    @Transactional
    def "Creating new task on stage in IN_PROGRESS status should not change stage or project status"() {
        given: "Project with accepted offer and stages and tasks in TO DO"
            ProjectDto createProjectDto = this.createProject()
            StageDto createStageDto = this.createStage(createProjectDto.id)
            TaskDto createTaskDto11 = this.createTask(createProjectDto.id, createStageDto.id)
            def acceptOfferResponse = this.acceptProjectOffer(createProjectDto.id)
            def changeTaskStatusResponse =
                    this.updateTaskStatus(createProjectDto.id, createStageDto.id, createTaskDto11.id,
                            TaskStatus.IN_PROGRESS)
        when:
            String taskRequestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(taskDto)
            def taskResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .post(URI.create(this.createStageUri(createProjectDto.id, createStageDto.id) + "/tasks"))
                            .header("Content-Type", "application/json")
                            .content(taskRequestBody)
            ).andReturn().response
        then: "Response code is 201"
            taskResponse.status == HttpStatus.CREATED.value()
        and: "Task status is TO_DO"
            TaskDto createdTaskDto12 = mapper.readValue(taskResponse.contentAsString, TaskDto.class)
            def getTaskResponse = getTaskResponse(createProjectDto.id, createStageDto.id, createdTaskDto12.id)
            TaskDto getTaskDto = mapper.readValue(getTaskResponse.contentAsString, TaskDto.class)
            getTaskDto.status == TaskStatus.TO_DO
        and: "Stage status is IN_PROGRESS"
            def getStageResponse = this.getStageResponse(createProjectDto.id, createStageDto.id)
            StageDto getStageDto = mapper.readValue(getStageResponse.contentAsString, StageDto.class)
            getStageDto.status == StageStatus.IN_PROGRESS
        and: "Project status is IN_PROGRESS"
            def getProjectResponse = this.getProjectResponse(createProjectDto.id)
            ProjectDto getProjectDto = mapper.readValue(getProjectResponse.contentAsString, ProjectDto.class)
            getProjectDto.status == ProjectStatus.IN_PROGRESS
    }

    @Transactional
    def "Creating new task on stage in REJECTED status should return code 400, error message and not create new task"() {
        given: "Project with accepted offer and stages and tasks in TO DO"
            ProjectDto createProjectDto = this.createProject()
            StageDto createStageDto = this.createStage(createProjectDto.id)
            TaskDto createTaskDto = this.createTask(createProjectDto.id, createStageDto.id)
            def acceptOfferResponse = this.acceptProjectOffer(createProjectDto.id)
            def rejectStageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .post(URI
                                    .create(this.createStageUri(createProjectDto.id, createStageDto.id) + "/reject"))
            ).andReturn().response
        when:
            String taskRequestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(taskDto)
            def taskResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .post(URI.create(this.createStageUri(createProjectDto.id, createStageDto.id) + "/tasks"))
                            .header("Content-Type", "application/json")
                            .content(taskRequestBody)
            ).andReturn().response
        then: "Response code is 400"
            taskResponse.status == HttpStatus.BAD_REQUEST.value()
        and:
            def errorMessage = mapper.readValue(taskResponse.contentAsString, ErrorMessage.class)
            errorMessage.message == "You cannot change Task status for rejected Stage."
    }

    @Transactional
    def "Creating new task on stage in COMPLETED status should change stage status to IN_PROGRESS and change project status to IN_PROGRESS if was COMPLETED"() {
        given: "Project with accepted offer and stages and tasks in TO DO"
            ProjectDto createProjectDto = this.createProject()
            StageDto createStageDto = this.createStage(createProjectDto.id)
            TaskDto createTaskDto11 = this.createTask(createProjectDto.id, createStageDto.id)
            def acceptOfferResponse = this.acceptProjectOffer(createProjectDto.id)
            def changeTaskStatusResponse =
                    this.updateTaskStatus(createProjectDto.id, createStageDto.id, createTaskDto11.id,
                            TaskStatus.IN_PROGRESS)
            changeTaskStatusResponse =
                    this.updateTaskStatus(createProjectDto.id, createStageDto.id, createTaskDto11.id,
                            TaskStatus.COMPLETED)
        when:
            String taskRequestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(taskDto)
            def taskResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .post(URI.create(this.createStageUri(createProjectDto.id, createStageDto.id) + "/tasks"))
                            .header("Content-Type", "application/json")
                            .content(taskRequestBody)
            ).andReturn().response
        then: "Response code is 201"
            taskResponse.status == HttpStatus.CREATED.value()
        and: "Task status is TO_DO"
            TaskDto createdTaskDto12 = mapper.readValue(taskResponse.contentAsString, TaskDto.class)
            def getTaskResponse = getTaskResponse(createProjectDto.id, createStageDto.id, createdTaskDto12.id)
            TaskDto getTaskDto = mapper.readValue(getTaskResponse.contentAsString, TaskDto.class)
            getTaskDto.status == TaskStatus.TO_DO
        and: "Stage status is IN_PROGRESS"
            def getStageResponse = this.getStageResponse(createProjectDto.id, createStageDto.id)
            StageDto getStageDto = mapper.readValue(getStageResponse.contentAsString, StageDto.class)
            getStageDto.status == StageStatus.IN_PROGRESS
        and: "Project status is IN_PROGRESS"
            def getProjectResponse = this.getProjectResponse(createProjectDto.id)
            ProjectDto getProjectDto = mapper.readValue(getProjectResponse.contentAsString, ProjectDto.class)
            getProjectDto.status == ProjectStatus.IN_PROGRESS
    }

    // HELPER METHODS

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
        def taskResponse = this.mockMvc.perform(
                MockMvcRequestBuilders.post(URI.create(this.createStageUri(projectId, stageId) + "/tasks"))
                        .header("Content-Type", "application/json")
                        .content(taskRequestBody)
        ).andReturn().response
        return mapper.readValue(taskResponse.contentAsString, TaskDto.class)
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

    private MockHttpServletResponse acceptProjectOffer(long projectId) {
        return this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(
                this.createProjectUri(projectId) + "/acceptOffer"))).andReturn().response
    }

    private MockHttpServletResponse updateTaskStatus(long projectId, long stageId, long taskId, TaskStatus status) {
        TaskDto updateStatusDto = new TaskDto(status: status)
        String requestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(updateStatusDto)
        return this.mockMvc.perform(MockMvcRequestBuilders
                .post(URI.create(this.createTaskUri(projectId, stageId, taskId) + "/updateStatus"))
                .header("Content-Type", "application/json")
                .content(requestBody)
        ).andReturn().response
    }

    private MockHttpServletResponse getTaskResponse(long projectId, long stageId, long taskId) {
        return this.mockMvc.perform(
                MockMvcRequestBuilders.get(URI.create(this.createTaskUri(projectId, stageId, taskId)))
        ).andReturn().response
    }

    private MockHttpServletResponse getStageResponse(long projectId, long stageId) {
        return this.mockMvc.perform(MockMvcRequestBuilders.get(URI.create(this.createStageUri(projectId, stageId)))
        ).andReturn().response
    }

    private MockHttpServletResponse getProjectResponse(long projectId) {
        return this.mockMvc.perform(MockMvcRequestBuilders.get(URI.create(this.createProjectUri(projectId)))
        ).andReturn().response
    }
}
