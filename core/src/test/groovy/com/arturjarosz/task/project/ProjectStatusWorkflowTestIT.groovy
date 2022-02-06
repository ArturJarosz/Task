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

import java.time.LocalDate

class ProjectStatusWorkflowTestIT extends BaseTestIT {
    private static final String ARCHITECT_FIRST_NAME = "First Name"
    private static final String ARCHITECT_LAST_NAME = "Last Name"
    private static final String ARCHITECTS_URI = "/architects"
    private static final String CLIENTS_URI = "/clients"
    private static final String PROJECTS_URI = "/projects"
    private static final Double NEW_OFFER_VALUE = 10000.00D;

    @Shared
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

    @Autowired
    private MockMvc mockMvc

    @Override
    def setupSpec() {
        mapper.findAndRegisterModules()
    }

    @Transactional
    def "1 Creating project should return code 200 and put project in OFFER status"() {
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
        then: "Returns code 201"
            projectResponse.status == HttpStatus.CREATED.value()
        and: "Project status is set to offer"
            this.getProjectStatus(projectResponse) == ProjectStatus.OFFER
    }

    @Transactional
    def "2 Rejecting new project should put it in REJECT status"() {
        given: "New project"
            ProjectDto projectDto = this.createProject()
        when: "Rejecting new project"
            def rejectResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.createProjectUri(projectDto.id) + "/reject"))
            ).andReturn().response
        then: "Response code should be 200"
            rejectResponse.status == HttpStatus.OK.value()
        and: "Project status is REJECTED"
            this.getProjectStatus(rejectResponse) == ProjectStatus.REJECTED
    }

    @Transactional
    def "3 Making new offer for rejected project, should put that project in OFFER status and change its value"() {
        given: "Create and project and reject it"
            ProjectDto projectDto = this.createProject()
            def rejectResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.createProjectUri(projectDto.id) + "/reject"))
            ).andReturn().response
            OfferDto offerDto = new OfferDto(offerValue: NEW_OFFER_VALUE)
            String offerRequestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(offerDto)
        when: "Making new offer for rejected project"
            def offerResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.createProjectUri(projectDto.id) + "/newOffer"))
                            .header("Content-Type", "application/json")
                            .content(offerRequestBody)
            ).andReturn().response
        then: "Return code is 200"
            offerResponse.status == HttpStatus.OK.value()
        and: "Project status is OFFER"
            ProjectDto offerProjectDto = mapper.readValue(offerResponse.contentAsString, ProjectDto.class)
            offerProjectDto.status == ProjectStatus.OFFER
        and: "Project value is same as new offer value"
            offerProjectDto.projectValue == NEW_OFFER_VALUE
    }

    @Transactional
    def "4 Making new offer for new project, should change offer value to new value"() {
        given: "Create and project and reject it"
            ProjectDto projectDto = this.createProject()
            OfferDto offerDto = new OfferDto(offerValue: NEW_OFFER_VALUE)
            String offerRequestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(offerDto)
        when: "Making new offer for rejected project"
            def offerResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.createProjectUri(projectDto.id) + "/newOffer"))
                            .header("Content-Type", "application/json")
                            .content(offerRequestBody)
            ).andReturn().response
        then: "Return code is 200"
            offerResponse.status == HttpStatus.OK.value()
            ProjectDto offerProjectDto = mapper.readValue(offerResponse.contentAsString, ProjectDto.class)
        and: "Project value is same as new offer value"
            offerProjectDto.projectValue == NEW_OFFER_VALUE
    }

    @Transactional
    def "5 Accepting an offer for Project should change project status to TO DO"() {
        given: "Create and project and reject it"
            ProjectDto projectDto = this.createProject()
        when: "Making new offer for rejected project"
            def offerResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.createProjectUri(projectDto.id) + "/acceptOffer"))
            ).andReturn().response
        then: "Return code is 200"
            offerResponse.status == HttpStatus.OK.value()
        and: "Project status it TO_DO"
            this.getProjectStatus(offerResponse) == ProjectStatus.TO_DO
    }

    @Transactional
    def "6 For project with not accepted offer, it should not be possible to start progress in work and it should put task, stage and project in IN_PROGRESS statuses"() {
        given: "Project with accepted offer and stages and tasks in TO DO"
            ProjectDto projectDto = this.createProject()
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
        when: "Change one task status to IN PROGRESS"
            def changeTaskStatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id,
                            TaskStatus.IN_PROGRESS)
        then: "Response code is 400"
            changeTaskStatusResponse.status == HttpStatus.BAD_REQUEST.value()
            def message = mapper.readValue(changeTaskStatusResponse.contentAsString, ErrorMessage.class)
            message.getMessage() == "You cannot start progress for Project, for which offer was not accepted."
        and: "Task status is TO_DO"
            def getTaskResponse = getTaskResponse(projectDto.id, stageDto.id, taskDto11.id)
            this.getTaskStatus(getTaskResponse) == TaskStatus.TO_DO
        and: "Stage status is TO_DO"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatus.TO_DO
        and: "Project status is OFFER"
            def getProjectResponse = this.getProjectResponse(projectDto.id)
            this.getProjectStatus(getProjectResponse) == ProjectStatus.OFFER
    }

    @Transactional
    def "7 For project with accepted offer, it should be possible to start progress in work"() {
        given:
            ProjectDto projectDto = this.createProject()
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto = this.createTask(projectDto.id, stageDto.id)
            def acceptOfferResponse = this.acceptProjectOffer(projectDto.id)
        when:
            def changeTaskStatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto.id,
                            TaskStatus.IN_PROGRESS)
        then:
            changeTaskStatusResponse.status == HttpStatus.OK.value()
        and:
            this.getTaskStatus(changeTaskStatusResponse) == TaskStatus.IN_PROGRESS
    }

    @Transactional
    def "8 For project with accepted offer, it should not be possible to make new offer"() {
        given:
            ProjectDto projectDto = this.createProject()
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto = this.createTask(projectDto.id, stageDto.id)
            def acceptOfferResponse = this.acceptProjectOffer(projectDto.id)
            TaskDto updateStatusDto = new TaskDto(status: TaskStatus.IN_PROGRESS)
            OfferDto offerDto = new OfferDto(offerValue: NEW_OFFER_VALUE)
            String offerRequestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(offerDto)
        when:
            def offerResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.createProjectUri(projectDto.id) + "/newOffer"))
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
    def "9 For project with signed contract, it should not be possible to make new offer"() {
        given:
            ProjectDto projectDto = this.createProject()
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto = this.createTask(projectDto.id, stageDto.id)
            def acceptOfferResponse = this.acceptProjectOffer(projectDto.id)
            def projectContractDto = new ProjectContractDto(signingDate: LocalDate.of(2021, 01, 01),
                    startDate: LocalDate.of(2021, 02, 01), deadline: LocalDate.of(2023, 01, 01))
            def projectSignContractRequest =
                    mapper.writerWithDefaultPrettyPrinter().writeValueAsString(projectContractDto)
            def signContractResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.createProjectUri(projectDto.id) + "/sign"))
                            .header("Content-Type", "application/json")
                            .content(projectSignContractRequest)
            ).andReturn().response
            OfferDto offerDto = new OfferDto(offerValue: NEW_OFFER_VALUE)
            String offerRequestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(offerDto)
        when:
            def offerResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.createProjectUri(projectDto.id) + "/newOffer"))
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
    def "10 Rejecting project with accepted offer should put it in REJECTED status"() {
        given:
            ProjectDto projectDto = this.createProject()
            def acceptOfferResponse = this.acceptProjectOffer(projectDto.id)
        when:
            def rejectResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.createProjectUri(projectDto.id) + "/reject"))
            ).andReturn().response
        then: "Response code should be 200"
            rejectResponse.status == HttpStatus.OK.value()
        and: "Project status is REJECTED"
            this.getProjectStatus(rejectResponse) == ProjectStatus.REJECTED
    }

    @Transactional
    def "11 Reopening rejected project with accepted offer and stages only in TO_DO statuses should change project status to TO_DO"() {
        given:
            ProjectDto projectDto = this.createProject()
            def acceptOfferResponse = this.acceptProjectOffer(projectDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto = this.createTask(projectDto.id, stageDto.id)
            def rejectResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.createProjectUri(projectDto.id) + "/reject"))
            ).andReturn().response
        when:
            def reopenResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.createProjectUri(projectDto.id) + "/reopen"))
            ).andReturn().response
        then: "Response code should be 200"
            reopenResponse.status == HttpStatus.OK.value()
        and: "Project status is REJECTED"
            this.getProjectStatus(reopenResponse) == ProjectStatus.TO_DO
    }

    @Transactional
    def "12 Reopening rejected project, that was in IN_PROGRESS status, should be back to IN_PROGRESS"() {
        given:
            ProjectDto projectDto = this.createProject()
            def acceptOfferResponse = this.acceptProjectOffer(projectDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto = this.createTask(projectDto.id, stageDto.id)
            def changeTaskStatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto.id,
                            TaskStatus.IN_PROGRESS)
            def rejectResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.createProjectUri(projectDto.id) + "/reject"))
            ).andReturn().response
        when:
            def reopenResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.createProjectUri(projectDto.id) + "/reopen"))
            ).andReturn().response
        then: "Response code should be 200"
            reopenResponse.status == HttpStatus.OK.value()
        and: "Project status is IN_PROGRESS"
            this.getProjectStatus(reopenResponse) == ProjectStatus.IN_PROGRESS
    }

    @Transactional
    def "13 Reject only task in stage, should put that stage in TO_DO status and put PROJECT in TO_DO"() {
        given: "Project with accepted offer and stages and tasks in TO DO"
            ProjectDto projectDto = this.createProject()
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            def acceptOfferResponse = this.acceptProjectOffer(projectDto.id)
            def changeTaskStatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id,
                            TaskStatus.IN_PROGRESS)
        when: "Change one task status to IN PROGRESS"
            changeTaskStatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id,
                            TaskStatus.REJECTED)
        then: "Response code is 200"
            changeTaskStatusResponse.status == HttpStatus.OK.value()
        and: "Task status is REJECTED"
            def getTaskResponse = getTaskResponse(projectDto.id, stageDto.id, taskDto11.id)
            this.getTaskStatus(getTaskResponse) == TaskStatus.REJECTED
        and: "Stage status is TO_DO"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatus.TO_DO
        and: "Project status is TO_DO"
            def getProjectResponse = this.getProjectResponse(projectDto.id)
            this.getProjectStatus(getProjectResponse) == ProjectStatus.TO_DO
    }

    @Transactional
    def "14 Finishing work on only task in stage, should put that stage in COMPLETED status and put PROJECT in COMPLETED"() {
        given: "Project with accepted offer and stages and tasks in TO DO"
            ProjectDto projectDto = this.createProject()
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            def acceptOfferResponse = this.acceptProjectOffer(projectDto.id)
            def changeTaskStatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id,
                            TaskStatus.IN_PROGRESS)
        when: "Change one task status to IN PROGRESS"
            changeTaskStatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id,
                            TaskStatus.COMPLETED)
        then: "Response code is 200"
            changeTaskStatusResponse.status == HttpStatus.OK.value()
        and: "Task status is COMPLETED"
            def getTaskResponse = this.getTaskResponse(projectDto.id, stageDto.id, taskDto11.id);
            this.getTaskStatus(getTaskResponse) == TaskStatus.COMPLETED
        and: "Stage status is COMPLETED"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatus.COMPLETED
        and: "Project status is COMPLETED"
            def getProjectResponse = this.getProjectResponse(projectDto.id)
            this.getProjectStatus(getProjectResponse) == ProjectStatus.COMPLETED
    }

    @Transactional
    def "15 Rejecting only stage from TO_DO should put stage in REJECTED status and not change project status"() {
        given: "Project with accepted offer and stages and tasks in TO DO"
            ProjectDto projectDto = this.createProject()
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            def acceptOfferResponse = this.acceptProjectOffer(projectDto.id)
        when:
            def rejectStageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .post(URI
                                    .create(this.createStageUri(projectDto.id, stageDto.id) + "/reject"))
            ).andReturn().response
        then: "Response code is 200"
            rejectStageResponse.status == HttpStatus.OK.value()
        and: "Stage status is REJECTED"
            this.getStageStatus(rejectStageResponse) == StageStatus.REJECTED
        and:
            def getProjectResponse = this.getProjectResponse(projectDto.id)
            this.getProjectStatus(getProjectResponse) == ProjectStatus.TO_DO
    }

    @Transactional
    def "16 Creating new task on stage in TO_DO status should not change stage or project status"() {
        given: "Project with accepted offer and stages and tasks in TO DO"
            ProjectDto projectDto = this.createProject()
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            def acceptOfferResponse = this.acceptProjectOffer(projectDto.id)
        when:
            String taskRequestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(taskDto)
            def taskResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .post(URI.create(this.createStageUri(projectDto.id, stageDto.id) + "/tasks"))
                            .header("Content-Type", "application/json")
                            .content(taskRequestBody)
            ).andReturn().response
        then: "Response code is 201"
            taskResponse.status == HttpStatus.CREATED.value()
        and: "Task status is TO_DO"
            TaskDto createdTaskDto12 = mapper.readValue(taskResponse.contentAsString, TaskDto.class)
            def getTaskResponse = getTaskResponse(projectDto.id, stageDto.id, createdTaskDto12.id)
            this.getTaskStatus(getTaskResponse) == TaskStatus.TO_DO
        and: "Stage status is TO_DO"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatus.TO_DO
        and: "Project status is TO_DO"
            def getProjectResponse = this.getProjectResponse(projectDto.id)
            this.getProjectStatus(getProjectResponse) == ProjectStatus.TO_DO
    }

    @Transactional
    def "17 Creating new task on stage in IN_PROGRESS status should not change stage or project status"() {
        given: "Project with accepted offer and stages and tasks in TO DO"
            ProjectDto projectDto = this.createProject()
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            def acceptOfferResponse = this.acceptProjectOffer(projectDto.id)
            def changeTaskStatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id,
                            TaskStatus.IN_PROGRESS)
        when:
            String taskRequestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(taskDto)
            def taskResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .post(URI.create(this.createStageUri(projectDto.id, stageDto.id) + "/tasks"))
                            .header("Content-Type", "application/json")
                            .content(taskRequestBody)
            ).andReturn().response
        then: "Response code is 201"
            taskResponse.status == HttpStatus.CREATED.value()
        and: "Task status is TO_DO"
            TaskDto createdTaskDto12 = mapper.readValue(taskResponse.contentAsString, TaskDto.class)
            def getTaskResponse = getTaskResponse(projectDto.id, stageDto.id, createdTaskDto12.id)
            this.getTaskStatus(getTaskResponse) == TaskStatus.TO_DO
        and: "Stage status is IN_PROGRESS"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatus.IN_PROGRESS
        and: "Project status is IN_PROGRESS"
            def getProjectResponse = this.getProjectResponse(projectDto.id)
            this.getProjectStatus(getProjectResponse) == ProjectStatus.IN_PROGRESS
    }

    @Transactional
    def "18 Creating new task on stage in REJECTED status should return code 400, error message and not create new task"() {
        given: "Project with accepted offer and stages and tasks in TO DO"
            ProjectDto projectDto = this.createProject()
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto = this.createTask(projectDto.id, stageDto.id)
            def acceptOfferResponse = this.acceptProjectOffer(projectDto.id)
            def rejectStageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .post(URI
                                    .create(this.createStageUri(projectDto.id, stageDto.id) + "/reject"))
            ).andReturn().response
        when:
            String taskRequestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(taskDto)
            def taskResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .post(URI.create(this.createStageUri(projectDto.id, stageDto.id) + "/tasks"))
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
    def "19 Creating new task on stage in COMPLETED status should change stage status to IN_PROGRESS and change project status to IN_PROGRESS if was COMPLETED"() {
        given: "Project with accepted offer and stages and tasks in TO DO"
            ProjectDto projectDto = this.createProject()
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            def acceptOfferResponse = this.acceptProjectOffer(projectDto.id)
            def changeTaskStatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id,
                            TaskStatus.IN_PROGRESS)
            changeTaskStatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id,
                            TaskStatus.COMPLETED)
        when:
            String taskRequestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(taskDto)
            def taskResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .post(URI.create(this.createStageUri(projectDto.id, stageDto.id) + "/tasks"))
                            .header("Content-Type", "application/json")
                            .content(taskRequestBody)
            ).andReturn().response
        then: "Response code is 201"
            taskResponse.status == HttpStatus.CREATED.value()
        and: "Task status is TO_DO"
            TaskDto createdTaskDto12 = mapper.readValue(taskResponse.contentAsString, TaskDto.class)
            def getTaskResponse = getTaskResponse(projectDto.id, stageDto.id, createdTaskDto12.id)
            this.getTaskStatus(getTaskResponse) == TaskStatus.TO_DO
        and: "Stage status is IN_PROGRESS"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatus.IN_PROGRESS
        and: "Project status is IN_PROGRESS"
            def getProjectResponse = this.getProjectResponse(projectDto.id)
            this.getProjectStatus(getProjectResponse) == ProjectStatus.IN_PROGRESS
    }

    @Transactional
    def "20 Starting work on the only task on stage should change stage status to IN_PROGRESS"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptProjectOffer(projectDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto = this.createTask(projectDto.id, stageDto.id)
        when:
            def changeTaskStatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto.id,
                            TaskStatus.IN_PROGRESS)
        then:
            changeTaskStatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is TO_DO"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatus.IN_PROGRESS
    }

    @Transactional
    def "21 Starting work on stage in TO_DO with only REJECTED tasks should change stage status to IN_PROGRESS"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptProjectOffer(projectDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto12 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto13 = this.createTask(projectDto.id, stageDto.id)
            def changeTask11StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id,
                            TaskStatus.REJECTED)
            def changeTask12StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id,
                            TaskStatus.REJECTED)
        when:
            def changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id,
                            TaskStatus.IN_PROGRESS)
        then:
            changeTask13StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is TO_DO"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatus.IN_PROGRESS
    }

    @Transactional
    def "22 Starting work on stage in TO_DO with tasks in TO_DO and REJECTED statuses should change stage status to IN_PROGRESS"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptProjectOffer(projectDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto12 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto13 = this.createTask(projectDto.id, stageDto.id)
            def changeTask11StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id,
                            TaskStatus.REJECTED)
            def changeTask12StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id,
                            TaskStatus.REJECTED)
        when:
            def changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id,
                            TaskStatus.IN_PROGRESS)
        then:
            changeTask13StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is TO_DO"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatus.IN_PROGRESS
    }

    @Transactional
    def "23 Starting work on stage in IN_PROGRESS status should not change stage status"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptProjectOffer(projectDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto12 = this.createTask(projectDto.id, stageDto.id)
            def changeTask11StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id,
                            TaskStatus.IN_PROGRESS)
        when:
            def changeTask12StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id,
                            TaskStatus.IN_PROGRESS)
        then:
            changeTask12StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is IN_PROGRESS"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatus.IN_PROGRESS
    }

    @Transactional
    def "24 Rejecting task from TO_DO status on stage in TO_DO should not change stage status"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptProjectOffer(projectDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
        when:
            def changeTask12StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id,
                            TaskStatus.REJECTED)
        then:
            changeTask12StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is TO_DO"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatus.TO_DO
    }

    @Transactional
    def "25 Rejecting task from TO_DO status on stage in IN_PROGRESS, while there are some tasks in IN_PROGRESS does not change stage status"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptProjectOffer(projectDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto12 = this.createTask(projectDto.id, stageDto.id)
            def changeTask11StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id,
                            TaskStatus.IN_PROGRESS)
        when:
            def changeTask12StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id,
                            TaskStatus.REJECTED)
        then:
            changeTask12StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is TO_DO"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatus.IN_PROGRESS
    }

    @Transactional
    def "26 Rejecting task from TO_DO status on stage in IN_PROGRESS, while there are only tasks in REJECTED and COMPLETED changes stage status to COMPLETED"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptProjectOffer(projectDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto12 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto13 = this.createTask(projectDto.id, stageDto.id)
            def changeTask11StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id,
                            TaskStatus.REJECTED)
            def changeTask12StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id,
                            TaskStatus.IN_PROGRESS)
            changeTask12StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id,
                            TaskStatus.COMPLETED)
        when:
            def changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id,
                            TaskStatus.REJECTED)
        then:
            changeTask13StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is TO_DO"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatus.COMPLETED
    }

    @Transactional
    def "27 Rejecting task from IN_PROGRESS, with only tasks in REJECTED, should change stage status to TO_DO"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptProjectOffer(projectDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto12 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto13 = this.createTask(projectDto.id, stageDto.id)
            def changeTask11StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id,
                            TaskStatus.REJECTED)
            def changeTask12StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id,
                            TaskStatus.REJECTED)
            def changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id,
                            TaskStatus.IN_PROGRESS)
        when:
            changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id,
                            TaskStatus.REJECTED)
        then:
            changeTask13StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is TO_DO"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatus.TO_DO
    }

    @Transactional
    def "28 Rejecting task from IN_PROGRESS, with some tasks in IN_PROGRESS, should not change stage status"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptProjectOffer(projectDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto12 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto13 = this.createTask(projectDto.id, stageDto.id)
            def changeTask11StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id,
                            TaskStatus.IN_PROGRESS)
            def changeTask12StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id,
                            TaskStatus.REJECTED)
            def changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id,
                            TaskStatus.IN_PROGRESS)
        when:
            changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id,
                            TaskStatus.REJECTED)
        then:
            changeTask13StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is IN_PROGRESS"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatus.IN_PROGRESS
    }

    @Transactional
    def "29 Rejecting task from IN_PROGRESS, with task only in TO_DO and REJECTED, should change stage status to TO_DO"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptProjectOffer(projectDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto12 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto13 = this.createTask(projectDto.id, stageDto.id)
            def changeTask12StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id,
                            TaskStatus.REJECTED)
            def changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id,
                            TaskStatus.IN_PROGRESS)
        when:
            changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id,
                            TaskStatus.REJECTED)
        then:
            changeTask13StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is TO_DO"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatus.TO_DO
    }

    @Transactional
    def "30 Rejecting task from IN_PROGRESS, with task only in COMPLETED and REJECTED, should change stage status to COMPLETED"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptProjectOffer(projectDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto12 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto13 = this.createTask(projectDto.id, stageDto.id)
            def changeTask11StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id,
                            TaskStatus.IN_PROGRESS)
            changeTask11StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id,
                            TaskStatus.COMPLETED)
            def changeTask12StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id,
                            TaskStatus.REJECTED)
            def changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id,
                            TaskStatus.IN_PROGRESS)
        when:
            changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id,
                            TaskStatus.REJECTED)
        then:
            changeTask13StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is COMPLETED"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatus.COMPLETED
    }

    @Transactional
    def "31 Reopening task on stage in TO_DO status should not change stage status"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptProjectOffer(projectDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto12 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto13 = this.createTask(projectDto.id, stageDto.id)
            def changeTask11StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id,
                            TaskStatus.REJECTED)
        when:
            def reopenTask11 = this.mockMvc.perform(MockMvcRequestBuilders
                    .post(URI
                            .create(this.createTaskUri(projectDto.id, stageDto.id,
                                    taskDto11.id) + "/reopen"))
            ).andReturn().response
        then:
            reopenTask11.status == HttpStatus.OK.value()
        and: "Stage status is TO_DO"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatus.TO_DO
    }

    @Transactional
    def "32 Reopening task on stage in IN_PROGRESS status should not change stage status"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptProjectOffer(projectDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto12 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto13 = this.createTask(projectDto.id, stageDto.id)
            def changeTask11StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id,
                            TaskStatus.REJECTED)
            def changeTask12StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id,
                            TaskStatus.IN_PROGRESS)
        when:
            def reopenTask11 = this.mockMvc.perform(MockMvcRequestBuilders
                    .post(URI
                            .create(this.createTaskUri(projectDto.id, stageDto.id,
                                    taskDto11.id) + "/reopen"))
            ).andReturn().response
        then:
            reopenTask11.status == HttpStatus.OK.value()
        and: "Stage status is IN_PROGRESS"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatus.IN_PROGRESS
    }

    @Transactional
    def "33 Reopening task on stage in COMPLETED status should change stage status to IN_PROGRESS"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptProjectOffer(projectDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto12 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto13 = this.createTask(projectDto.id, stageDto.id)
            def changeTask11StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id,
                            TaskStatus.REJECTED)
            def changeTask12StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id,
                            TaskStatus.IN_PROGRESS)
            changeTask12StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id,
                            TaskStatus.COMPLETED)
            def changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id,
                            TaskStatus.IN_PROGRESS)
            changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id,
                            TaskStatus.COMPLETED)
        when:
            def reopenTask11 = this.mockMvc.perform(MockMvcRequestBuilders
                    .post(URI
                            .create(this.createTaskUri(projectDto.id, stageDto.id,
                                    taskDto11.id) + "/reopen"))
            ).andReturn().response
        then:
            reopenTask11.status == HttpStatus.OK.value()
        and: "Stage status is IN_PROGRESS"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatus.IN_PROGRESS
    }

    @Transactional
    def "34 Changing tasks status from IN_PROGRESS to TO_DO on stage in IN_PROGRESS, with other tasks in IN_PROGRESS should not change stage status"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptProjectOffer(projectDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto12 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto13 = this.createTask(projectDto.id, stageDto.id)
            def changeTask11StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id,
                            TaskStatus.REJECTED)
            def changeTask12StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id,
                            TaskStatus.IN_PROGRESS)
            def changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id,
                            TaskStatus.IN_PROGRESS)
        when:
            changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id,
                            TaskStatus.TO_DO)
        then:
            changeTask13StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is IN_PROGRESS"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatus.IN_PROGRESS
    }

    @Transactional
    def "35 Changing tasks status from IN_PROGRESS to TO_DO on stage in IN_PROGRESS, with at least on task in COMPLETED should not change stage status"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptProjectOffer(projectDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto12 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto13 = this.createTask(projectDto.id, stageDto.id)
            def changeTask11StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id,
                            TaskStatus.REJECTED)
            def changeTask12StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id,
                            TaskStatus.IN_PROGRESS)
            changeTask12StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id,
                            TaskStatus.COMPLETED)
            def changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id,
                            TaskStatus.IN_PROGRESS)
        when:
            changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id,
                            TaskStatus.TO_DO)
        then:
            changeTask13StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is IN_PROGRESS"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatus.IN_PROGRESS
    }

    @Transactional
    def "36 Changing tasks status from IN_PROGRESS to TO_DO on stage in IN_PROGRESS, with tasks only in TO_DO and REJECTED should change stage staus to TO_Do"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptProjectOffer(projectDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto12 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto13 = this.createTask(projectDto.id, stageDto.id)
            def changeTask11StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id,
                            TaskStatus.REJECTED)
            def changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id,
                            TaskStatus.IN_PROGRESS)
        when:
            changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id,
                            TaskStatus.TO_DO)
        then:
            changeTask13StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is TO_DO"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatus.TO_DO
    }

    @Transactional
    def "37 Changing tasks status from IN_PROGRESS to TO_DO on stage in IN_PROGRESS, rest of task in REJECTED should change stage status to TO_DO"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptProjectOffer(projectDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto12 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto13 = this.createTask(projectDto.id, stageDto.id)
            def changeTask11StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id,
                            TaskStatus.REJECTED)
            def changeTask12StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id,
                            TaskStatus.REJECTED)
            def changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id,
                            TaskStatus.IN_PROGRESS)
        when:
            changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id,
                            TaskStatus.TO_DO)
        then:
            changeTask13StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is TO_DO"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatus.TO_DO
    }

    @Transactional
    def "38 Changing status of task from IN_PROGRESS to COMPLETED on stage in IN_PROGRESS while there are some task in TO_DO, should not change stage status"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptProjectOffer(projectDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto12 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto13 = this.createTask(projectDto.id, stageDto.id)
            def changeTask11StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id,
                            TaskStatus.REJECTED)
            def changeTask12StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id,
                            TaskStatus.IN_PROGRESS)
            def changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id,
                            TaskStatus.IN_PROGRESS)
        when:
            changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id,
                            TaskStatus.COMPLETED)
        then:
            changeTask13StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is IN_PROGRESS"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatus.IN_PROGRESS
    }

    @Transactional
    def "39 Changing status of task from IN_PROGRESS to COMPLETED on stage in IN_PROGRESS while there are some task in IN_PROGRESS, should not change stage status"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptProjectOffer(projectDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto12 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto13 = this.createTask(projectDto.id, stageDto.id)
            def changeTask11StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id,
                            TaskStatus.IN_PROGRESS)
            def changeTask12StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id,
                            TaskStatus.IN_PROGRESS)
            def changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id,
                            TaskStatus.IN_PROGRESS)
        when:
            changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id,
                            TaskStatus.COMPLETED)
        then:
            changeTask13StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is IN_PROGRESS"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatus.IN_PROGRESS
    }

    @Transactional
    def "40 Changing status of task from IN_PROGRESS to COMPLETED on stage in IN_PROGRESS while there only tasks in REJECTED and COMPLETED, should change stage status to COMPLETED"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptProjectOffer(projectDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto12 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto13 = this.createTask(projectDto.id, stageDto.id)
            def changeTask11StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id,
                            TaskStatus.REJECTED)
            def changeTask12StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id,
                            TaskStatus.IN_PROGRESS)
            changeTask12StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id,
                            TaskStatus.COMPLETED)
            def changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id,
                            TaskStatus.IN_PROGRESS)
        when:
            changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id,
                            TaskStatus.COMPLETED)
        then:
            changeTask13StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is COMPLETED"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatus.COMPLETED
    }

    @Transactional
    def "41 Changing status of task from COMPLETED to IN_PROGRESS on stage in IN_PROGRESS status, should not change stage status "() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptProjectOffer(projectDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto12 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto13 = this.createTask(projectDto.id, stageDto.id)
            def changeTask11StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id,
                            TaskStatus.REJECTED)
            def changeTask12StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id,
                            TaskStatus.IN_PROGRESS)
            def changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id,
                            TaskStatus.IN_PROGRESS)
            changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id,
                            TaskStatus.COMPLETED)
        when:
            changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id,
                            TaskStatus.IN_PROGRESS)
        then:
            changeTask13StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is IN_PROGRESS"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatus.IN_PROGRESS
    }

    @Transactional
    def "42 Changing status of task from COMPLETED to IN_PROGRESS on stage in COMPLETED status, should change stage status to IN_PROGRESS"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptProjectOffer(projectDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto12 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto13 = this.createTask(projectDto.id, stageDto.id)
            def changeTask11StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id,
                            TaskStatus.REJECTED)
            def changeTask12StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id,
                            TaskStatus.IN_PROGRESS)
            changeTask12StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id,
                            TaskStatus.COMPLETED)
            def changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id,
                            TaskStatus.IN_PROGRESS)
            changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id,
                            TaskStatus.COMPLETED)
        when:
            changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id,
                            TaskStatus.IN_PROGRESS)
        then:
            changeTask13StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is IN_PROGRESS"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatus.IN_PROGRESS
    }

    @Transactional
    def "43 Creating new stage for project in OFFER status returns code 201 and does not change project status"() {
        given:
            ProjectDto projectDto = this.createProject()
            StageDto stageDto1 = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto1.id)
        when:
            String stageRequestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(stageDto)
            def stageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.createProjectUri(projectDto.id) + "/stages"))
                            .header("Content-Type", "application/json")
                            .content(stageRequestBody)
            ).andReturn().response
        then:
            stageResponse.status == HttpStatus.CREATED.value()
        and:
            def getProjectResponse = this.getProjectResponse(projectDto.id)
            this.getProjectStatus(getProjectResponse) == ProjectStatus.OFFER
    }

    @Transactional
    def "44 Creating new stage for project in COMPLETED status returns code 201 and changes project status to IN_PROGRESS"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptProjectOffer(projectDto.id)
            StageDto stageDto1 = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto1.id)
            this.updateTaskStatus(projectDto.id, stageDto1.id, taskDto11.id, TaskStatus.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto1.id, taskDto11.id, TaskStatus.COMPLETED)
        when:
            String stageRequestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(stageDto)
            def stageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.createProjectUri(projectDto.id) + "/stages"))
                            .header("Content-Type", "application/json")
                            .content(stageRequestBody)
            ).andReturn().response
        then:
            stageResponse.status == HttpStatus.CREATED.value()
        and:
            def getProjectResponse = this.getProjectResponse(projectDto.id)
            this.getProjectStatus(getProjectResponse) == ProjectStatus.IN_PROGRESS
    }

    @Transactional
    def "45 Creating new stage for project in TO_DO status returns code 201 and does not change project status"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptProjectOffer(projectDto.id)
            StageDto stageDto1 = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto1.id)
        when:
            String stageRequestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(stageDto)
            def stageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.createProjectUri(projectDto.id) + "/stages"))
                            .header("Content-Type", "application/json")
                            .content(stageRequestBody)
            ).andReturn().response
        then:
            stageResponse.status == HttpStatus.CREATED.value()
        and:
            def getProjectResponse = this.getProjectResponse(projectDto.id)
            this.getProjectStatus(getProjectResponse) == ProjectStatus.TO_DO
    }

    @Transactional
    def "46 Creating new stage for project in IN_PROGRESS status returns code 201 and does not change project status"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptProjectOffer(projectDto.id)
            StageDto stageDto1 = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto1.id)
            this.updateTaskStatus(projectDto.id, stageDto1.id, taskDto11.id, TaskStatus.IN_PROGRESS)
        when:
            String stageRequestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(stageDto)
            def stageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.createProjectUri(projectDto.id) + "/stages"))
                            .header("Content-Type", "application/json")
                            .content(stageRequestBody)
            ).andReturn().response
        then:
            stageResponse.status == HttpStatus.CREATED.value()
        and:
            def getProjectResponse = this.getProjectResponse(projectDto.id)
            this.getProjectStatus(getProjectResponse) == ProjectStatus.IN_PROGRESS
    }

    @Transactional
    def "47 Creating new stage for project in REJECTED status returns code 400 and error message"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptProjectOffer(projectDto.id)
            this.rejectProject(projectDto.id)
        when:
            String stageRequestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(stageDto)
            def stageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.createProjectUri(projectDto.id) + "/stages"))
                            .header("Content-Type", "application/json")
                            .content(stageRequestBody)
            ).andReturn().response
        then: "Response code is 400"
            stageResponse.status == HttpStatus.BAD_REQUEST.value()
        and:
            def errorMessage = mapper.readValue(stageResponse.contentAsString, ErrorMessage.class)
            errorMessage.message == "Cannot change stage status for project in status REJECTED."
    }

    @Transactional
    def "48 Changing stage status to IN_PROGRESS on project in status TO_DO changes project status to IN_PROGRESS"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptProjectOffer(projectDto.id)
            StageDto stageDto1 = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto1.id)
        when:
            def updateTaskStatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto1.id, taskDto11.id, TaskStatus.IN_PROGRESS)
        then:
            updateTaskStatusResponse.status == HttpStatus.OK.value()
        and:
            def getProjectResponse = this.getProjectResponse(projectDto.id)
            this.getProjectStatus(getProjectResponse) == ProjectStatus.IN_PROGRESS
    }

    @Transactional
    def "49 Changing stage status to IN_PROGRESS on project in status OFFER returns code 400 and error message"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptProjectOffer(projectDto.id)
            StageDto stageDto1 = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto1.id)
            this.rejectProject(projectDto.id)
        when:
            def updateTaskStatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto1.id, taskDto11.id, TaskStatus.IN_PROGRESS)
        then: "Response code is 400"
            updateTaskStatusResponse.status == HttpStatus.BAD_REQUEST.value()
        and:
            def errorMessage = mapper.readValue(updateTaskStatusResponse.contentAsString, ErrorMessage.class)
            errorMessage.message == "You cannot change Task status for rejected Project."
    }

    @Transactional
    def "50 Changing stage status from IN_PROGRESS to TO_DO, while other stages are in TO_DO and REJECTED on project in IN_PROGRESS status, changes project status to TO_DO"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptProjectOffer(projectDto.id)
            StageDto stageDto1 = this.createStage(projectDto.id)
            this.rejectStage(projectDto.id, stageDto1.id)
            StageDto stageDto2 = this.createStage(projectDto.id)
            StageDto stageDto3 = this.createStage(projectDto.id)
            TaskDto taskDto21 = this.createTask(projectDto.id, stageDto2.id)
            TaskDto taskDto31 = this.createTask(projectDto.id, stageDto3.id)
            this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto31.id, TaskStatus.IN_PROGRESS)
        when:
            def updateTaskStatusDto = this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto31.id, TaskStatus.TO_DO)
        then: "Response code is 400"
            updateTaskStatusDto.status == HttpStatus.OK.value()
        and:
            def getProjectResponse = this.getProjectResponse(projectDto.id)
            this.getProjectStatus(getProjectResponse) == ProjectStatus.TO_DO
    }

    @Transactional
    def "51 Changing stage status from IN_PROGRESS to TO_DO, while there is at least one stage in IN_PROGRESS on project in IN_PROGRESS status, changes project status to TO_DO"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptProjectOffer(projectDto.id)
            StageDto stageDto1 = this.createStage(projectDto.id)
            this.rejectStage(projectDto.id, stageDto1.id)
            StageDto stageDto2 = this.createStage(projectDto.id)
            StageDto stageDto3 = this.createStage(projectDto.id)
            TaskDto taskDto21 = this.createTask(projectDto.id, stageDto2.id)
            this.updateTaskStatus(projectDto.id, stageDto2.id, taskDto21.id, TaskStatus.IN_PROGRESS)
            TaskDto taskDto31 = this.createTask(projectDto.id, stageDto3.id)
            this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto31.id, TaskStatus.IN_PROGRESS)
        when:
            def updateTaskStatusDto = this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto31.id, TaskStatus.TO_DO)
        then: "Response code is 400"
            updateTaskStatusDto.status == HttpStatus.OK.value()
        and:
            def getProjectResponse = this.getProjectResponse(projectDto.id)
            this.getProjectStatus(getProjectResponse) == ProjectStatus.IN_PROGRESS
    }

    @Transactional
    def "52 Changing stage status from IN_PROGRESS to COMPLETED on project with other IN_PROGRESS stages does not change project status"() {
        given: "Existing project"
            ProjectDto projectDto = this.createProject()
            this.acceptProjectOffer(projectDto.id)
            StageDto stageDto1 = this.createStage(projectDto.id)
            StageDto stageDto2 = this.createStage(projectDto.id)
            StageDto stageDto3 = this.createStage(projectDto.id)
            TaskDto taskDto21 = this.createTask(projectDto.id, stageDto2.id)
            this.updateTaskStatus(projectDto.id, stageDto2.id, taskDto21.id, TaskStatus.IN_PROGRESS)
            TaskDto taskDto31 = this.createTask(projectDto.id, stageDto3.id)
            this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto31.id, TaskStatus.IN_PROGRESS)
        expect: "In status IN_PROGRESS and at least one stage in status IN_PROGRESS"
            this.getStageStatus(projectDto.id, stageDto1.id) == StageStatus.TO_DO
            this.getStageStatus(projectDto.id, stageDto2.id) == StageStatus.IN_PROGRESS
            this.getStageStatus(projectDto.id, stageDto3.id) == StageStatus.IN_PROGRESS
            this.getProjectStatus(projectDto.id) == ProjectStatus.IN_PROGRESS
        when: "Changing stage status from IN_PROGRESS to COMPLETED"
            def updateTaskStatusDto =
                    this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto31.id, TaskStatus.COMPLETED)
        then: "Returns code 200"
            updateTaskStatusDto.status == HttpStatus.OK.value()
        and: "And does not change project status"
            this.getProjectStatus(projectDto.id) == ProjectStatus.IN_PROGRESS
    }

    @Transactional
    def "53 Changing stage status from IN_PROGRESS to COMPLETED on project with other stages in REJECTED changes project status to COMPLETED"() {
        given: "Existing project"
            ProjectDto projectDto = this.createProject()
            this.acceptProjectOffer(projectDto.id)
            StageDto stageDto1 = this.createStage(projectDto.id)
            StageDto stageDto2 = this.createStage(projectDto.id)
            StageDto stageDto3 = this.createStage(projectDto.id)
            TaskDto taskDto21 = this.createTask(projectDto.id, stageDto2.id)
            TaskDto taskDto31 = this.createTask(projectDto.id, stageDto3.id)
            this.rejectStage(projectDto.id, stageDto1.id)
            this.rejectStage(projectDto.id, stageDto2.id)
            this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto31.id, TaskStatus.IN_PROGRESS)
        expect: "In status IN_PROGRESS and one stage in IN_PROGRESS and other REJECTED"
            this.getStageStatus(projectDto.id, stageDto1.id) == StageStatus.REJECTED
            this.getStageStatus(projectDto.id, stageDto2.id) == StageStatus.REJECTED
            this.getStageStatus(projectDto.id, stageDto3.id) == StageStatus.IN_PROGRESS
            this.getProjectStatus(projectDto.id) == ProjectStatus.IN_PROGRESS
        when: "Changing stage status from IN_PROGRESS to COMPLETED"
            def updateTaskStatusDto =
                    this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto31.id, TaskStatus.COMPLETED)
        then: "Returns code 200"
            updateTaskStatusDto.status == HttpStatus.OK.value()
        and: "And changes project status to COMPLETED"
            this.getProjectStatus(projectDto.id) == ProjectStatus.COMPLETED
    }

    @Transactional
    def "54 Changing stage status from IN_PROGRESS to COMPLETED on project with other stages in COMPLETED changes project status to COMPLETED"() {
        given: "Existing project"
            ProjectDto projectDto = this.createProject()
            this.acceptProjectOffer(projectDto.id)
            StageDto stageDto1 = this.createStage(projectDto.id)
            StageDto stageDto2 = this.createStage(projectDto.id)
            StageDto stageDto3 = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto1.id)
            TaskDto taskDto21 = this.createTask(projectDto.id, stageDto2.id)
            TaskDto taskDto31 = this.createTask(projectDto.id, stageDto3.id)
            this.updateTaskStatus(projectDto.id, stageDto1.id, taskDto11.id, TaskStatus.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto2.id, taskDto21.id, TaskStatus.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto31.id, TaskStatus.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto1.id, taskDto11.id, TaskStatus.COMPLETED)
            this.updateTaskStatus(projectDto.id, stageDto2.id, taskDto21.id, TaskStatus.COMPLETED)
        expect: "In status IN_PROGRESS and one stage in IN_PROGRESS and other COMPLETED"
            this.getStageStatus(projectDto.id, stageDto1.id) == StageStatus.COMPLETED
            this.getStageStatus(projectDto.id, stageDto2.id) == StageStatus.COMPLETED
            this.getStageStatus(projectDto.id, stageDto3.id) == StageStatus.IN_PROGRESS
            this.getProjectStatus(projectDto.id) == ProjectStatus.IN_PROGRESS
        when: "Changing stage status from IN_PROGRESS to COMPLETED"
            def updateTaskStatusDto =
                    this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto31.id, TaskStatus.COMPLETED)
        then: "Returns code 200"
            updateTaskStatusDto.status == HttpStatus.OK.value()
        and: "And changes project status to COMPLETED"
            this.getProjectStatus(projectDto.id) == ProjectStatus.COMPLETED
    }

    @Transactional
    def "55 Changing stage status from COMPLETED to IN_PROGRESS on project in status in COMPLETED changes project status to IN_PROGRESS"() {
        given: "Existing project"
            ProjectDto projectDto = this.createProject()
            this.acceptProjectOffer(projectDto.id)
            StageDto stageDto1 = this.createStage(projectDto.id)
            StageDto stageDto2 = this.createStage(projectDto.id)
            StageDto stageDto3 = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto1.id)
            TaskDto taskDto21 = this.createTask(projectDto.id, stageDto2.id)
            TaskDto taskDto31 = this.createTask(projectDto.id, stageDto3.id)
            this.updateTaskStatus(projectDto.id, stageDto1.id, taskDto11.id, TaskStatus.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto2.id, taskDto21.id, TaskStatus.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto31.id, TaskStatus.IN_PROGRESS)
            this.rejectStage(projectDto.id, stageDto1.id)
            this.updateTaskStatus(projectDto.id, stageDto2.id, taskDto21.id, TaskStatus.COMPLETED)
            this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto31.id, TaskStatus.COMPLETED)
        expect: "In status COMPLETED"
            this.getStageStatus(projectDto.id, stageDto1.id) == StageStatus.REJECTED
            this.getStageStatus(projectDto.id, stageDto2.id) == StageStatus.COMPLETED
            this.getStageStatus(projectDto.id, stageDto3.id) == StageStatus.COMPLETED
            this.getProjectStatus(projectDto.id) == ProjectStatus.COMPLETED
        when: "Changing stage status from COMPLETED to IN_PROGRESS"
            def updateTaskStatusDto =
                    this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto31.id, TaskStatus.IN_PROGRESS)
        then: "Returns code 200"
            updateTaskStatusDto.status == HttpStatus.OK.value()
        and: "And changes project status to IN_PROGRESS"
            this.getProjectStatus(projectDto.id) == ProjectStatus.IN_PROGRESS
    }

    @Transactional
    def "56 Changing stage status from COMPLETED to IN_PROGRESS on project in status in IN_PROGRESS does not change project status"() {
        given: "Existing project"
            ProjectDto projectDto = this.createProject()
            this.acceptProjectOffer(projectDto.id)
            StageDto stageDto1 = this.createStage(projectDto.id)
            StageDto stageDto2 = this.createStage(projectDto.id)
            StageDto stageDto3 = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto1.id)
            TaskDto taskDto21 = this.createTask(projectDto.id, stageDto2.id)
            TaskDto taskDto31 = this.createTask(projectDto.id, stageDto3.id)
            this.updateTaskStatus(projectDto.id, stageDto1.id, taskDto11.id, TaskStatus.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto2.id, taskDto21.id, TaskStatus.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto31.id, TaskStatus.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto2.id, taskDto21.id, TaskStatus.COMPLETED)
            this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto31.id, TaskStatus.COMPLETED)
        expect: "In status IN_PROGRESS"
            this.getStageStatus(projectDto.id, stageDto1.id) == StageStatus.IN_PROGRESS
            this.getStageStatus(projectDto.id, stageDto2.id) == StageStatus.COMPLETED
            this.getStageStatus(projectDto.id, stageDto3.id) == StageStatus.COMPLETED
            this.getProjectStatus(projectDto.id) == ProjectStatus.IN_PROGRESS
        when: "Changing stage status from COMPLETED to IN_PROGRESS"
            def updateTaskStatusDto =
                    this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto31.id, TaskStatus.IN_PROGRESS)
        then: "Returns code 200"
            updateTaskStatusDto.status == HttpStatus.OK.value()
        and: "And does not change project status"
            this.getProjectStatus(projectDto.id) == ProjectStatus.IN_PROGRESS
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

    private MockHttpServletResponse rejectProject(long projectId) {
        return this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(
                this.createProjectUri(projectId) + "/reject"
        ))).andReturn().response
    }

    private MockHttpServletResponse rejectStage(long projectId, long stageId) {
        return this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(
                this.createStageUri(projectId, stageId) + "/reject"
        ))).andReturn().response
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

    private TaskStatus getTaskStatus(MockHttpServletResponse getTaskResponse) {
        TaskDto getTaskDto = mapper.readValue(getTaskResponse.contentAsString, TaskDto.class)
        return getTaskDto.status
    }

    private StageStatus getStageStatus(MockHttpServletResponse getStageResponse) {
        StageDto getStageDto = mapper.readValue(getStageResponse.contentAsString, StageDto.class)
        return getStageDto.status
    }

    private StageStatus getStageStatus(long projectId, long stageId) {
        def getStageResponse =
                this.mockMvc.perform(MockMvcRequestBuilders.get(URI.create(this.createStageUri(projectId, stageId)))
                ).andReturn().response
        StageDto getStageDto = mapper.readValue(getStageResponse.contentAsString, StageDto.class)
        return getStageDto.status
    }

    private ProjectStatus getProjectStatus(MockHttpServletResponse getProjectResponse) {
        ProjectDto getProjectDto = mapper.readValue(getProjectResponse.contentAsString, ProjectDto.class)
        return getProjectDto.status
    }

    private ProjectStatus getProjectStatus(long projectId) {
        def getProjectResponse =
                this.mockMvc.perform(MockMvcRequestBuilders.get(URI.create(this.createProjectUri(projectId)))
                ).andReturn().response
        ProjectDto getProjectDto = mapper.readValue(getProjectResponse.contentAsString, ProjectDto.class)
        return getProjectDto.status
    }
}
