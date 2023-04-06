package com.arturjarosz.task.project

import com.arturjarosz.task.architect.application.dto.ArchitectBasicDto
import com.arturjarosz.task.architect.application.dto.ArchitectDto
import com.arturjarosz.task.client.application.dto.ClientDto
import com.arturjarosz.task.configuration.BaseTestIT
import com.arturjarosz.task.project.application.dto.ProjectCreateDto
import com.arturjarosz.task.project.application.dto.ProjectDto
import com.arturjarosz.task.project.application.dto.StageDto
import com.arturjarosz.task.project.application.dto.TaskDto
import com.arturjarosz.task.project.status.project.ProjectStatus
import com.arturjarosz.task.project.status.stage.StageStatus
import com.arturjarosz.task.project.status.task.TaskStatus
import com.arturjarosz.task.sharedkernel.exceptions.ErrorMessage
import com.arturjarosz.task.utils.TestsHelper
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.transaction.annotation.Transactional

class ProjectStatusWorkflowTestIT extends BaseTestIT {
    private static final String ARCHITECTS_URI = "/architects"
    private static final String CLIENTS_URI = "/clients"
    private static final String CONTRACTS_URI = "/contracts"
    private static final String PROJECTS_URI = "/projects"
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
    private final StageDto stageDto =
            MAPPER.readValue(new File(getClass().classLoader.getResource('json/stage/properStage.json').file),
                    StageDto.class)
    private final TaskDto taskDto =
            MAPPER.readValue(new File(getClass().classLoader.getResource('json/task/properTask.json').file),
                    TaskDto.class)

    @Autowired
    private MockMvc mockMvc

    @Override
    def setupSpec() {
        MAPPER.findAndRegisterModules()
    }

    @Transactional
    def "Creating project should return code 200 and put project in TO_DO status"() {
        given: "Existing architect"
            ArchitectDto architectDto =
                    TestsHelper.createArchitect(this.architect, this.createBasicArchitectUri(), this.mockMvc)
            properProjectDto.architectId = architectDto.id
        and: "Existing client"
            ClientDto clientDto =
                    TestsHelper.createClient(this.privateClientDto, this.createBasicClientUri(), this.mockMvc)
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
        and: "Project status is set to TO_DO"
            this.getProjectStatus(projectResponse) == ProjectStatus.TO_DO
    }

    @Transactional
    def "Creating project creates connected to it contract"() {
        given: "Existing architect"
            ArchitectDto architectDto =
                    TestsHelper.createArchitect(this.architect, this.createBasicArchitectUri(), this.mockMvc)
            properProjectDto.architectId = architectDto.id
        and: "Existing client"
            ClientDto clientDto =
                    TestsHelper.createClient(this.privateClientDto, this.createBasicClientUri(), this.mockMvc)
            properProjectDto.clientId = clientDto.id
        when: "Creating project with proper data"
            String projectRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properProjectDto)
            def projectResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + PROJECTS_URI))
                            .header("Content-Type", "application/json")
                            .content(projectRequestBody)
            ).andReturn().response
            ProjectDto projectDto = MAPPER.readValue(projectResponse.contentAsString, ProjectDto.class)
        then: "Returns code 201"
            projectResponse.status == HttpStatus.CREATED.value()
        and: "Project status is set to TO_DO"
            projectDto.contractDto != null
            projectDto.contractDto.id != null
    }

    @Transactional
    def "Rejecting new project should put it in REJECT status"() {
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
    def "For project with not accepted offer, it should not be possible to start progress in work and it should put leave task, stage and project in TO_DO."() {
        given: "Project with accepted offer and stages and tasks in TO DO"
            ProjectDto projectDto = this.createProject()
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
        when: "Change one task status to IN PROGRESS"
            def changeTaskStatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatus.IN_PROGRESS)
        then: "Response code is 400"
            changeTaskStatusResponse.status == HttpStatus.BAD_REQUEST.value()
            def message = MAPPER.readValue(changeTaskStatusResponse.contentAsString, ErrorMessage.class)
            message.getMessage() == "Contract in status OFFER does not allow for work on the project."
        and: "Task status is TO_DO"
            def getTaskResponse = getTaskResponse(projectDto.id, stageDto.id, taskDto11.id)
            this.getTaskStatus(getTaskResponse) == TaskStatus.TO_DO
        and: "Stage status is TO_DO"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatus.TO_DO
        and: "Project status is TO_DO"
            def getProjectResponse = this.getProjectResponse(projectDto.id)
            this.getProjectStatus(getProjectResponse) == ProjectStatus.TO_DO
    }

    @Transactional
    def "For project with accepted offer, it should be possible to start progress in work"() {
        given:
            ProjectDto projectDto = this.createProject()
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto = this.createTask(projectDto.id, stageDto.id)
            this.acceptContractOffer(projectDto.contractDto.id)
        when:
            def changeTaskStatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto.id, TaskStatus.IN_PROGRESS)
        then:
            changeTaskStatusResponse.status == HttpStatus.OK.value()
        and:
            this.getTaskStatus(changeTaskStatusResponse) == TaskStatus.IN_PROGRESS
    }

/*    @Transactional
    def "8 For project with accepted offer, it should not be possible to make new offer"() {
        given:
            ProjectDto projectDto = this.createProject()
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto = this.createTask(projectDto.id, stageDto.id)
            def acceptOfferResponse = this.acceptContractOffer(projectDto.contractDto.contractId)
            TaskDto updateStatusDto = new TaskDto(status: TaskStatus.IN_PROGRESS)
            ContractDto offerDto = new ContractDto(offerValue: NEW_OFFER_VALUE)
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
    }*/

/*    @Transactional
    def "9 For project with signed contract, it should not be possible to make new offer"() {
        given:
            ProjectDto projectDto = this.createProject()
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto = this.createTask(projectDto.id, stageDto.id)
            def acceptOfferResponse = this.acceptContractOffer(projectDto.contractDto.contractId)
            def projectContractDto = new ProjectContractDto(signingDate: LocalDate.of(2021, 01, 01),
                    startDate: LocalDate.of(2021, 02, 01), deadline: LocalDate.of(2023, 01, 01))
            def projectSignContractRequest =
                    mapper.writerWithDefaultPrettyPrinter().writeValueAsString(projectContractDto)
            def signContractResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.createProjectUri(projectDto.id) + "/sign"))
                            .header("Content-Type", "application/json")
                            .content(projectSignContractRequest)
            ).andReturn().response
            ContractDto offerDto = new ContractDto(offerValue: NEW_OFFER_VALUE)
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
    }*/

    @Transactional
    def "Rejecting project with accepted offer should put it in REJECTED status"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
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
    def "Reopening rejected project with accepted offer and stages only in TO_DO statuses should change project status to TO_DO"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            this.createTask(projectDto.id, stageDto.id)
            this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(this.createProjectUri(projectDto.id) + "/reject"))).andReturn().response
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
    def "Reopening rejected project, that was in IN_PROGRESS status, should be back to IN_PROGRESS"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto = this.createTask(projectDto.id, stageDto.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto.id, TaskStatus.IN_PROGRESS)
            this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(this.createProjectUri(projectDto.id) + "/reject"))).andReturn().response
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
    def "Reject only task in stage, should put that stage in TO_DO status and put PROJECT in TO_DO"() {
        given: "Project with accepted offer and stages and tasks in TO DO"
            ProjectDto projectDto = this.createProject()
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            this.acceptContractOffer(projectDto.contractDto.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatus.IN_PROGRESS)
        when: "Change one task status to IN PROGRESS"
            def changeTaskStatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatus.REJECTED)
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
    def "Finishing work on only task in stage, should put that stage in DONE status and put PROJECT in DONE"() {
        given: "Project with accepted offer and stages and tasks in TO DO"
            ProjectDto projectDto = this.createProject()
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            this.acceptContractOffer(projectDto.contractDto.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatus.IN_PROGRESS)
        when: "Change one task status to IN PROGRESS"
            def changeTaskStatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatus.DONE)
        then: "Response code is 200"
            changeTaskStatusResponse.status == HttpStatus.OK.value()
        and: "Task status is DONE"
            def getTaskResponse = this.getTaskResponse(projectDto.id, stageDto.id, taskDto11.id)
            this.getTaskStatus(getTaskResponse) == TaskStatus.DONE
        and: "Stage status is DONE"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatus.DONE
        and: "Project status is DONE"
            def getProjectResponse = this.getProjectResponse(projectDto.id)
            this.getProjectStatus(getProjectResponse) == ProjectStatus.DONE
    }

    @Transactional
    def "Rejecting only stage from TO_DO should put stage in REJECTED status and not change project status"() {
        given: "Project with accepted offer and stages and tasks in TO DO"
            ProjectDto projectDto = this.createProject()
            StageDto stageDto = this.createStage(projectDto.id)
            this.createTask(projectDto.id, stageDto.id)
            this.acceptContractOffer(projectDto.contractDto.id)
        when:
            def rejectStageResponse = this.mockMvc.perform(MockMvcRequestBuilders
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
    def "Creating new task on stage in TO_DO status should not change stage or project status"() {
        given: "Project with accepted offer and stages and tasks in TO DO"
            ProjectDto projectDto = this.createProject()
            StageDto stageDto = this.createStage(projectDto.id)
            this.createTask(projectDto.id, stageDto.id)
            this.acceptContractOffer(projectDto.contractDto.id)
        when:
            String taskRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(taskDto)
            def taskResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .post(URI.create(this.createStageUri(projectDto.id, stageDto.id) + "/tasks"))
                            .header("Content-Type", "application/json")
                            .content(taskRequestBody)
            ).andReturn().response
        then: "Response code is 201"
            taskResponse.status == HttpStatus.CREATED.value()
        and: "Task status is TO_DO"
            TaskDto createdTaskDto12 = MAPPER.readValue(taskResponse.contentAsString, TaskDto.class)
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
    def "Creating new task on stage in IN_PROGRESS status should not change stage or project status"() {
        given: "Project with accepted offer and stages and tasks in TO DO"
            ProjectDto projectDto = this.createProject()
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            this.acceptContractOffer(projectDto.contractDto.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatus.IN_PROGRESS)
        when:
            String taskRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(taskDto)
            def taskResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .post(URI.create(this.createStageUri(projectDto.id, stageDto.id) + "/tasks"))
                            .header("Content-Type", "application/json")
                            .content(taskRequestBody)
            ).andReturn().response
        then: "Response code is 201"
            taskResponse.status == HttpStatus.CREATED.value()
        and: "Task status is TO_DO"
            TaskDto createdTaskDto12 = MAPPER.readValue(taskResponse.contentAsString, TaskDto.class)
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
    def "Creating new task on stage in REJECTED status should return code 400, error message and not create new task"() {
        given: "Project with accepted offer and stages and tasks in TO DO"
            ProjectDto projectDto = this.createProject()
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto = this.createTask(projectDto.id, stageDto.id)
            this.acceptContractOffer(projectDto.contractDto.id)
            this.mockMvc.perform(MockMvcRequestBuilders
                    .post(URI
                            .create(this.createStageUri(projectDto.id, stageDto.id) + "/reject"))
            ).andReturn().response
        when:
            String taskRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(taskDto)
            def taskResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .post(URI.create(this.createStageUri(projectDto.id, stageDto.id) + "/tasks"))
                            .header("Content-Type", "application/json")
                            .content(taskRequestBody)
            ).andReturn().response
        then: "Response code is 400"
            taskResponse.status == HttpStatus.BAD_REQUEST.value()
        and:
            def errorMessage = MAPPER.readValue(taskResponse.contentAsString, ErrorMessage.class)
            errorMessage.message == "Stage status REJECTED does not allow to make any work on the stage."
    }

    @Transactional
    def "Creating new task on stage in DONE status should change stage status to IN_PROGRESS and change project status to IN_PROGRESS if was DONE"() {
        given: "Project with accepted offer and stages and tasks in TO DO"
            ProjectDto projectDto = this.createProject()
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            this.acceptContractOffer(projectDto.contractDto.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatus.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatus.DONE)
        when:
            String taskRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(taskDto)
            def taskResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .post(URI.create(this.createStageUri(projectDto.id, stageDto.id) + "/tasks"))
                            .header("Content-Type", "application/json")
                            .content(taskRequestBody)
            ).andReturn().response
        then: "Response code is 201"
            taskResponse.status == HttpStatus.CREATED.value()
        and: "Task status is TO_DO"
            TaskDto createdTaskDto12 = MAPPER.readValue(taskResponse.contentAsString, TaskDto.class)
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
    def "Starting work on the only task on stage should change stage status to IN_PROGRESS"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto = this.createTask(projectDto.id, stageDto.id)
        when:
            def changeTaskStatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto.id, TaskStatus.IN_PROGRESS)
        then:
            changeTaskStatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is TO_DO"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatus.IN_PROGRESS
    }

    @Transactional
    def "Starting work on stage in TO_DO with only REJECTED tasks should change stage status to IN_PROGRESS"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto12 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto13 = this.createTask(projectDto.id, stageDto.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatus.REJECTED)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatus.REJECTED)
        when:
            def changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatus.IN_PROGRESS)
        then:
            changeTask13StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is TO_DO"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatus.IN_PROGRESS
    }

    @Transactional
    def "Starting work on stage in TO_DO with tasks in TO_DO and REJECTED statuses should change stage status to IN_PROGRESS"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto12 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto13 = this.createTask(projectDto.id, stageDto.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatus.REJECTED)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatus.REJECTED)
        when:
            def changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatus.IN_PROGRESS)
        then:
            changeTask13StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is TO_DO"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatus.IN_PROGRESS
    }

    @Transactional
    def "Starting work on stage in IN_PROGRESS status should not change stage status"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto12 = this.createTask(projectDto.id, stageDto.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatus.IN_PROGRESS)
        when:
            def changeTask12StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatus.IN_PROGRESS)
        then:
            changeTask12StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is IN_PROGRESS"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatus.IN_PROGRESS
    }

    @Transactional
    def "Rejecting task from TO_DO status on stage in TO_DO should not change stage status"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
        when:
            def changeTask12StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatus.REJECTED)
        then:
            changeTask12StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is TO_DO"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatus.TO_DO
    }

    @Transactional
    def "Rejecting task from TO_DO status on stage in IN_PROGRESS, while there are some tasks in IN_PROGRESS does not change stage status"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto12 = this.createTask(projectDto.id, stageDto.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatus.IN_PROGRESS)
        when:
            def changeTask12StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatus.REJECTED)
        then:
            changeTask12StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is TO_DO"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatus.IN_PROGRESS
    }

    @Transactional
    def "Rejecting task from TO_DO status on stage in IN_PROGRESS, while there are only tasks in REJECTED and DONE changes stage status to DONE"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto12 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto13 = this.createTask(projectDto.id, stageDto.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatus.REJECTED)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatus.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatus.DONE)
        when:
            def changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatus.REJECTED)
        then:
            changeTask13StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is TO_DO"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatus.DONE
    }

    @Transactional
    def "Rejecting task from IN_PROGRESS, with only tasks in REJECTED, should change stage status to TO_DO"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto12 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto13 = this.createTask(projectDto.id, stageDto.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatus.REJECTED)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatus.REJECTED)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatus.IN_PROGRESS)
        when:
            def changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatus.REJECTED)
        then:
            changeTask13StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is TO_DO"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatus.TO_DO
    }

    @Transactional
    def "Rejecting task from IN_PROGRESS, with some tasks in IN_PROGRESS, should not change stage status"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto12 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto13 = this.createTask(projectDto.id, stageDto.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatus.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatus.REJECTED)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatus.IN_PROGRESS)
        when:
            def changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatus.REJECTED)
        then:
            changeTask13StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is IN_PROGRESS"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatus.IN_PROGRESS
    }

    @Transactional
    def "Rejecting task from IN_PROGRESS, with task only in TO_DO and REJECTED, should change stage status to TO_DO"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto12 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto13 = this.createTask(projectDto.id, stageDto.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatus.REJECTED)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatus.IN_PROGRESS)
        when:
            def changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatus.REJECTED)
        then:
            changeTask13StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is TO_DO"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatus.TO_DO
    }

    @Transactional
    def "Rejecting task from IN_PROGRESS, with task only in DONE and REJECTED, should change stage status to DONE"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto12 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto13 = this.createTask(projectDto.id, stageDto.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatus.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatus.DONE)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatus.REJECTED)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatus.IN_PROGRESS)
        when:
            def changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatus.REJECTED)
        then:
            changeTask13StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is DONE"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatus.DONE
    }

    @Transactional
    def "Reopening task on stage in TO_DO status should not change stage status"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            this.createTask(projectDto.id, stageDto.id)
            this.createTask(projectDto.id, stageDto.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatus.REJECTED)
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
    def "Reopening task on stage in IN_PROGRESS status should not change stage status"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto12 = this.createTask(projectDto.id, stageDto.id)
            this.createTask(projectDto.id, stageDto.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatus.REJECTED)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatus.IN_PROGRESS)
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
    def "Reopening task on stage in DONE status should change stage status to IN_PROGRESS"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto12 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto13 = this.createTask(projectDto.id, stageDto.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatus.REJECTED)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatus.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatus.DONE)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatus.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatus.DONE)
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
    def "Changing tasks status from IN_PROGRESS to TO_DO on stage in IN_PROGRESS, with other tasks in IN_PROGRESS should not change stage status"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto12 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto13 = this.createTask(projectDto.id, stageDto.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatus.REJECTED)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatus.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatus.IN_PROGRESS)
        when:
            def changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatus.TO_DO)
        then:
            changeTask13StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is IN_PROGRESS"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatus.IN_PROGRESS
    }

    @Transactional
    def "Changing tasks status from IN_PROGRESS to TO_DO on stage in IN_PROGRESS, with at least on task in DONE should not change stage status"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto12 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto13 = this.createTask(projectDto.id, stageDto.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatus.REJECTED)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatus.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatus.DONE)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatus.IN_PROGRESS)
        when:
            def changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatus.TO_DO)
        then:
            changeTask13StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is IN_PROGRESS"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatus.IN_PROGRESS
    }

    @Transactional
    def "Changing tasks status from IN_PROGRESS to TO_DO on stage in IN_PROGRESS, with tasks only in TO_DO and REJECTED should change stage status to TO_DO"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto13 = this.createTask(projectDto.id, stageDto.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatus.REJECTED)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatus.IN_PROGRESS)
        when:
            def changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatus.TO_DO)
        then:
            changeTask13StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is TO_DO"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatus.TO_DO
    }

    @Transactional
    def "Changing tasks status from IN_PROGRESS to TO_DO on stage in IN_PROGRESS, rest of task in REJECTED should change stage status to TO_DO"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto12 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto13 = this.createTask(projectDto.id, stageDto.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatus.REJECTED)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatus.REJECTED)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatus.IN_PROGRESS)
        when:
            def changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatus.TO_DO)
        then:
            changeTask13StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is TO_DO"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatus.TO_DO
    }

    @Transactional
    def "Changing status of task from IN_PROGRESS to DONE on stage in IN_PROGRESS while there are some task in TO_DO, should not change stage status"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto12 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto13 = this.createTask(projectDto.id, stageDto.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatus.REJECTED)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatus.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatus.IN_PROGRESS)
        when:
            def changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatus.DONE)
        then:
            changeTask13StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is IN_PROGRESS"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatus.IN_PROGRESS
    }

    @Transactional
    def "Changing status of task from IN_PROGRESS to DONE on stage in IN_PROGRESS while there are some task in IN_PROGRESS, should not change stage status"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto12 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto13 = this.createTask(projectDto.id, stageDto.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatus.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatus.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatus.IN_PROGRESS)
        when:
            def changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatus.DONE)
        then:
            changeTask13StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is IN_PROGRESS"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatus.IN_PROGRESS
    }

    @Transactional
    def "Changing status of task from IN_PROGRESS to DONE on stage in IN_PROGRESS while there only tasks in REJECTED and DONE, should change stage status to DONE"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto12 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto13 = this.createTask(projectDto.id, stageDto.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatus.REJECTED)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatus.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatus.DONE)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatus.IN_PROGRESS)
        when:
            def changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatus.DONE)
        then:
            changeTask13StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is DONE"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatus.DONE
    }

    @Transactional
    def "Changing status of task from DONE to IN_PROGRESS on stage in IN_PROGRESS status, should not change stage status "() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto12 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto13 = this.createTask(projectDto.id, stageDto.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatus.REJECTED)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatus.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatus.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatus.DONE)
        when:
            def changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatus.IN_PROGRESS)
        then:
            changeTask13StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is IN_PROGRESS"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatus.IN_PROGRESS
    }

    @Transactional
    def "Changing status of task from DONE to IN_PROGRESS on stage in DONE status, should change stage status to IN_PROGRESS"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto12 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto13 = this.createTask(projectDto.id, stageDto.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatus.REJECTED)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatus.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatus.DONE)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatus.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatus.DONE)
        when:
            def changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatus.IN_PROGRESS)
        then:
            changeTask13StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is IN_PROGRESS"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatus.IN_PROGRESS
    }

    @Transactional
    def "Creating new stage for project in TO_DO status returns code 201 and does not change project status"() {
        given:
            ProjectDto projectDto = this.createProject()
            StageDto stageDto1 = this.createStage(projectDto.id)
            this.createTask(projectDto.id, stageDto1.id)
        when:
            String stageRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(stageDto)
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
    def "Creating new stage for project in DONE status returns code 201 and changes project status to IN_PROGRESS"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            StageDto stageDto1 = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto1.id)
            this.updateTaskStatus(projectDto.id, stageDto1.id, taskDto11.id, TaskStatus.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto1.id, taskDto11.id, TaskStatus.DONE)
        when:
            String stageRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(stageDto)
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
    def "Creating new stage for project in TO_DO status with accepted offer returns code 201 and does not change project status"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            StageDto stageDto1 = this.createStage(projectDto.id)
            this.createTask(projectDto.id, stageDto1.id)
        when:
            String stageRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(stageDto)
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
    def "Creating new stage for project in IN_PROGRESS status returns code 201 and does not change project status"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            StageDto stageDto1 = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto1.id)
            this.updateTaskStatus(projectDto.id, stageDto1.id, taskDto11.id, TaskStatus.IN_PROGRESS)
        when:
            String stageRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(stageDto)
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
    def "Creating new stage for project in REJECTED status returns code 400 and error message"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            this.rejectProject(projectDto.id)
        when:
            String stageRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(stageDto)
            def stageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.createProjectUri(projectDto.id) + "/stages"))
                            .header("Content-Type", "application/json")
                            .content(stageRequestBody)
            ).andReturn().response
        then: "Response code is 400"
            stageResponse.status == HttpStatus.BAD_REQUEST.value()
        and:
            def errorMessage = MAPPER.readValue(stageResponse.contentAsString, ErrorMessage.class)
            errorMessage.message == "Project status REJECTED does not allow to make any work on the project."
    }

    @Transactional
    def "Changing stage status to IN_PROGRESS on project in status TO_DO changes project status to IN_PROGRESS"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
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
    def "Changing stage status to IN_PROGRESS on project in status REJECTED returns code 400 and error message"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            StageDto stageDto1 = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto1.id)
            this.rejectProject(projectDto.id)
        when:
            def updateTaskStatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto1.id, taskDto11.id, TaskStatus.IN_PROGRESS)
        then: "Response code is 400"
            updateTaskStatusResponse.status == HttpStatus.BAD_REQUEST.value()
        and:
            def errorMessage = MAPPER.readValue(updateTaskStatusResponse.contentAsString, ErrorMessage.class)
            errorMessage.message == "Project status REJECTED does not allow to make any work on the project."
    }

    @Transactional
    def "Changing stage status from IN_PROGRESS to TO_DO, while other stages are in TO_DO and REJECTED on project in IN_PROGRESS status, changes project status to TO_DO"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            StageDto stageDto1 = this.createStage(projectDto.id)
            this.rejectStage(projectDto.id, stageDto1.id)
            StageDto stageDto2 = this.createStage(projectDto.id)
            StageDto stageDto3 = this.createStage(projectDto.id)
            this.createTask(projectDto.id, stageDto2.id)
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
    def "Changing stage status from IN_PROGRESS to TO_DO, while there is at least one stage in IN_PROGRESS on project in IN_PROGRESS status, changes project status to TO_DO"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
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
    def "Changing stage status from IN_PROGRESS to DONE on project with other IN_PROGRESS stages does not change project status"() {
        given: "Existing project"
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
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
        when: "Changing stage status from IN_PROGRESS to DONE"
            def updateTaskStatusDto = this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto31.id, TaskStatus.DONE)
        then: "Returns code 200"
            updateTaskStatusDto.status == HttpStatus.OK.value()
        and: "And does not change project status"
            this.getProjectStatus(projectDto.id) == ProjectStatus.IN_PROGRESS
    }

    @Transactional
    def "Changing stage status from IN_PROGRESS to DONE on project with other stages in REJECTED changes project status to DONE"() {
        given: "Existing project"
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            StageDto stageDto1 = this.createStage(projectDto.id)
            StageDto stageDto2 = this.createStage(projectDto.id)
            StageDto stageDto3 = this.createStage(projectDto.id)
            this.createTask(projectDto.id, stageDto2.id)
            TaskDto taskDto31 = this.createTask(projectDto.id, stageDto3.id)
            this.rejectStage(projectDto.id, stageDto1.id)
            this.rejectStage(projectDto.id, stageDto2.id)
            this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto31.id, TaskStatus.IN_PROGRESS)
        expect: "In status IN_PROGRESS and one stage in IN_PROGRESS and other REJECTED"
            this.getStageStatus(projectDto.id, stageDto1.id) == StageStatus.REJECTED
            this.getStageStatus(projectDto.id, stageDto2.id) == StageStatus.REJECTED
            this.getStageStatus(projectDto.id, stageDto3.id) == StageStatus.IN_PROGRESS
            this.getProjectStatus(projectDto.id) == ProjectStatus.IN_PROGRESS
        when: "Changing stage status from IN_PROGRESS to DONE"
            def updateTaskStatusDto = this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto31.id, TaskStatus.DONE)
        then: "Returns code 200"
            updateTaskStatusDto.status == HttpStatus.OK.value()
        and: "And changes project status to DONE"
            this.getProjectStatus(projectDto.id) == ProjectStatus.DONE
    }

    @Transactional
    def "Changing stage status from IN_PROGRESS to DONE on project with other stages in DONE changes project status to DONE"() {
        given: "Existing project"
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            StageDto stageDto1 = this.createStage(projectDto.id)
            StageDto stageDto2 = this.createStage(projectDto.id)
            StageDto stageDto3 = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto1.id)
            TaskDto taskDto21 = this.createTask(projectDto.id, stageDto2.id)
            TaskDto taskDto31 = this.createTask(projectDto.id, stageDto3.id)
            this.updateTaskStatus(projectDto.id, stageDto1.id, taskDto11.id, TaskStatus.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto2.id, taskDto21.id, TaskStatus.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto31.id, TaskStatus.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto1.id, taskDto11.id, TaskStatus.DONE)
            this.updateTaskStatus(projectDto.id, stageDto2.id, taskDto21.id, TaskStatus.DONE)
        expect: "In status IN_PROGRESS and one stage in IN_PROGRESS and other DONE"
            this.getStageStatus(projectDto.id, stageDto1.id) == StageStatus.DONE
            this.getStageStatus(projectDto.id, stageDto2.id) == StageStatus.DONE
            this.getStageStatus(projectDto.id, stageDto3.id) == StageStatus.IN_PROGRESS
            this.getProjectStatus(projectDto.id) == ProjectStatus.IN_PROGRESS
        when: "Changing stage status from IN_PROGRESS to DONE"
            def updateTaskStatusDto = this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto31.id, TaskStatus.DONE)
        then: "Returns code 200"
            updateTaskStatusDto.status == HttpStatus.OK.value()
        and: "And changes project status to DONE"
            this.getProjectStatus(projectDto.id) == ProjectStatus.DONE
    }

    @Transactional
    def "Changing stage status from DONE to IN_PROGRESS on project in status in DONE changes project status to IN_PROGRESS"() {
        given: "Existing project"
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
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
            this.updateTaskStatus(projectDto.id, stageDto2.id, taskDto21.id, TaskStatus.DONE)
            this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto31.id, TaskStatus.DONE)
        expect: "In status DONE"
            this.getStageStatus(projectDto.id, stageDto1.id) == StageStatus.REJECTED
            this.getStageStatus(projectDto.id, stageDto2.id) == StageStatus.DONE
            this.getStageStatus(projectDto.id, stageDto3.id) == StageStatus.DONE
            this.getProjectStatus(projectDto.id) == ProjectStatus.DONE
        when: "Changing stage status from DONE to IN_PROGRESS"
            def updateTaskStatusDto =
                    this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto31.id, TaskStatus.IN_PROGRESS)
        then: "Returns code 200"
            updateTaskStatusDto.status == HttpStatus.OK.value()
        and: "And changes project status to IN_PROGRESS"
            this.getProjectStatus(projectDto.id) == ProjectStatus.IN_PROGRESS
    }

    @Transactional
    def "Changing stage status from DONE to IN_PROGRESS on project in status in IN_PROGRESS does not change project status"() {
        given: "Existing project"
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            StageDto stageDto1 = this.createStage(projectDto.id)
            StageDto stageDto2 = this.createStage(projectDto.id)
            StageDto stageDto3 = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto1.id)
            TaskDto taskDto21 = this.createTask(projectDto.id, stageDto2.id)
            TaskDto taskDto31 = this.createTask(projectDto.id, stageDto3.id)
            this.updateTaskStatus(projectDto.id, stageDto1.id, taskDto11.id, TaskStatus.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto2.id, taskDto21.id, TaskStatus.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto31.id, TaskStatus.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto2.id, taskDto21.id, TaskStatus.DONE)
            this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto31.id, TaskStatus.DONE)
        expect: "In status IN_PROGRESS"
            this.getStageStatus(projectDto.id, stageDto1.id) == StageStatus.IN_PROGRESS
            this.getStageStatus(projectDto.id, stageDto2.id) == StageStatus.DONE
            this.getStageStatus(projectDto.id, stageDto3.id) == StageStatus.DONE
            this.getProjectStatus(projectDto.id) == ProjectStatus.IN_PROGRESS
        when: "Changing stage status from DONE to IN_PROGRESS"
            def updateTaskStatusDto =
                    this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto31.id, TaskStatus.IN_PROGRESS)
        then: "Returns code 200"
            updateTaskStatusDto.status == HttpStatus.OK.value()
        and: "And does not change project status"
            this.getProjectStatus(projectDto.id) == ProjectStatus.IN_PROGRESS
    }

    @Transactional
    def "Rejecting stage from TO_DO on project in status TO_DO does not change project status"() {
        given: "Existing project"
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            StageDto stageDto1 = this.createStage(projectDto.id)
            StageDto stageDto2 = this.createStage(projectDto.id)
            StageDto stageDto3 = this.createStage(projectDto.id)
            this.createTask(projectDto.id, stageDto1.id)
            this.createTask(projectDto.id, stageDto2.id)
            this.createTask(projectDto.id, stageDto3.id)
            this.rejectStage(projectDto.id, stageDto1.id)
        expect: "In status TO_DO"
            this.getProjectStatus(projectDto.id) == ProjectStatus.TO_DO
        when: "Rejecting stage from TO_DO"
            def rejectStageDto = this.rejectStage(projectDto.id, stageDto3.id)
        then: "Returns code 200"
            rejectStageDto.status == HttpStatus.OK.value()
        and: "And does not change project status"
            this.getProjectStatus(projectDto.id) == ProjectStatus.TO_DO
    }

    @Transactional
    def "Rejecting stage from TO_DO on project in status IN_PROGRESS with other stages in DONE and REJECTED changes project status to DONE"() {
        given: "Existing project"
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            StageDto stageDto1 = this.createStage(projectDto.id)
            StageDto stageDto2 = this.createStage(projectDto.id)
            StageDto stageDto3 = this.createStage(projectDto.id)
            this.createTask(projectDto.id, stageDto1.id)
            TaskDto taskDto21 = this.createTask(projectDto.id, stageDto2.id)
            this.createTask(projectDto.id, stageDto3.id)
            this.rejectStage(projectDto.id, stageDto1.id)
            this.updateTaskStatus(projectDto.id, stageDto2.id, taskDto21.id, TaskStatus.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto2.id, taskDto21.id, TaskStatus.DONE)
        expect: "In status IN_PROGRESS with stages in REJECTED and DONE"
            this.getStageStatus(projectDto.id, stageDto1.id) == StageStatus.REJECTED
            this.getStageStatus(projectDto.id, stageDto2.id) == StageStatus.DONE
            this.getStageStatus(projectDto.id, stageDto3.id) == StageStatus.TO_DO
            this.getProjectStatus(projectDto.id) == ProjectStatus.IN_PROGRESS
        when: "Rejecting stage from TO_DO"
            def rejectStageDto = this.rejectStage(projectDto.id, stageDto3.id)
        then: "Returns code 200"
            rejectStageDto.status == HttpStatus.OK.value()
        and: "And does not change project status"
            this.getProjectStatus(projectDto.id) == ProjectStatus.DONE
    }

    @Transactional
    def "Rejecting stage from TO_DO on project in status IN_PROGRESS with at least one stage in IN_PROGRESS does not change project status"() {
        given: "Existing project"
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            StageDto stageDto1 = this.createStage(projectDto.id)
            StageDto stageDto2 = this.createStage(projectDto.id)
            StageDto stageDto3 = this.createStage(projectDto.id)
            this.createTask(projectDto.id, stageDto1.id)
            TaskDto taskDto21 = this.createTask(projectDto.id, stageDto2.id)
            this.createTask(projectDto.id, stageDto3.id)
            this.rejectStage(projectDto.id, stageDto1.id)
            this.updateTaskStatus(projectDto.id, stageDto2.id, taskDto21.id, TaskStatus.IN_PROGRESS)
        expect: "In status IN_PROGRESS with at least one stage in IN_PROGRESS"
            this.getStageStatus(projectDto.id, stageDto1.id) == StageStatus.REJECTED
            this.getStageStatus(projectDto.id, stageDto2.id) == StageStatus.IN_PROGRESS
            this.getStageStatus(projectDto.id, stageDto3.id) == StageStatus.TO_DO
            this.getProjectStatus(projectDto.id) == ProjectStatus.IN_PROGRESS
        when: "Rejecting stage from TO_DO"
            def rejectStageDto = this.rejectStage(projectDto.id, stageDto3.id)
        then: "Returns code 200"
            rejectStageDto.status == HttpStatus.OK.value()
        and: "And does not change project status"
            this.getProjectStatus(projectDto.id) == ProjectStatus.IN_PROGRESS
    }

    @Transactional
    def "Rejecting the only stage from status IN_PROGRESS on project in IN_PROGRESS status changes project status to TO_DO"() {
        given: "Existing project"
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            StageDto stageDto1 = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto1.id)
            this.updateTaskStatus(projectDto.id, stageDto1.id, taskDto11.id, TaskStatus.IN_PROGRESS)
        expect: "In status IN_PROGRESS with one stage"
            this.getStageStatus(projectDto.id, stageDto1.id) == StageStatus.IN_PROGRESS
            this.getProjectStatus(projectDto.id) == ProjectStatus.IN_PROGRESS
        when: "Rejecting stage from IN_PROGRESS"
            def rejectStageDto = this.rejectStage(projectDto.id, stageDto1.id)
        then: "Returns code 200"
            rejectStageDto.status == HttpStatus.OK.value()
        and: "And changes project status to TO_DO"
            this.getProjectStatus(projectDto.id) == ProjectStatus.TO_DO
    }

    @Transactional
    def "Rejecting stage from status IN_PROGRESS on project in IN_PROGRESS status with other stages in REJECTED changes project status to TO_DO"() {
        given: "Existing project"
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            StageDto stageDto1 = this.createStage(projectDto.id)
            StageDto stageDto2 = this.createStage(projectDto.id)
            StageDto stageDto3 = this.createStage(projectDto.id)
            TaskDto taskDto31 = this.createTask(projectDto.id, stageDto3.id)
            this.rejectStage(projectDto.id, stageDto1.id)
            this.rejectStage(projectDto.id, stageDto2.id)
            this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto31.id, TaskStatus.IN_PROGRESS)
        expect: "In status IN_PROGRESS with other stages in REJECTED"
            this.getStageStatus(projectDto.id, stageDto1.id) == StageStatus.REJECTED
            this.getStageStatus(projectDto.id, stageDto2.id) == StageStatus.REJECTED
            this.getStageStatus(projectDto.id, stageDto3.id) == StageStatus.IN_PROGRESS
            this.getProjectStatus(projectDto.id) == ProjectStatus.IN_PROGRESS
        when: "Rejecting stage from IN_PROGRESS"
            def rejectStageDto = this.rejectStage(projectDto.id, stageDto3.id)
        then: "Returns code 200"
            rejectStageDto.status == HttpStatus.OK.value()
        and: "And changes project status to TO_DO"
            this.getProjectStatus(projectDto.id) == ProjectStatus.TO_DO
    }

    @Transactional
    def "Rejecting stage from status IN_PROGRESS on project in IN_PROGRESS status with other stages in TO_DO and REJECTED changes project status to TO_DO"() {
        given: "Existing project"
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            StageDto stageDto1 = this.createStage(projectDto.id)
            StageDto stageDto2 = this.createStage(projectDto.id)
            StageDto stageDto3 = this.createStage(projectDto.id)
            TaskDto taskDto31 = this.createTask(projectDto.id, stageDto3.id)
            this.rejectStage(projectDto.id, stageDto1.id)
            this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto31.id, TaskStatus.IN_PROGRESS)
        expect: "In status IN_PROGRESS with other stages in REJECTED and TO_DO"
            this.getStageStatus(projectDto.id, stageDto1.id) == StageStatus.REJECTED
            this.getStageStatus(projectDto.id, stageDto2.id) == StageStatus.TO_DO
            this.getStageStatus(projectDto.id, stageDto3.id) == StageStatus.IN_PROGRESS
            this.getProjectStatus(projectDto.id) == ProjectStatus.IN_PROGRESS
        when: "Rejecting stage from IN_PROGRESS"
            def rejectStageDto = this.rejectStage(projectDto.id, stageDto3.id)
        then: "Returns code 200"
            rejectStageDto.status == HttpStatus.OK.value()
        and: "And changes project status to TO_DO"
            this.getProjectStatus(projectDto.id) == ProjectStatus.TO_DO
    }

    @Transactional
    def "Rejecting stage from status IN_PROGRESS on project in IN_PROGRESS status with other stages in DONE and REJECTED changes project status to DONE"() {
        given: "Existing project"
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            StageDto stageDto1 = this.createStage(projectDto.id)
            StageDto stageDto2 = this.createStage(projectDto.id)
            StageDto stageDto3 = this.createStage(projectDto.id)
            TaskDto taskDto21 = this.createTask(projectDto.id, stageDto2.id)
            TaskDto taskDto31 = this.createTask(projectDto.id, stageDto3.id)
            this.rejectStage(projectDto.id, stageDto1.id)
            this.updateTaskStatus(projectDto.id, stageDto2.id, taskDto21.id, TaskStatus.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto2.id, taskDto21.id, TaskStatus.DONE)
            this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto31.id, TaskStatus.IN_PROGRESS)
        expect: "In status IN_PROGRESS with other stages in REJECTED and TO_DO"
            this.getStageStatus(projectDto.id, stageDto1.id) == StageStatus.REJECTED
            this.getStageStatus(projectDto.id, stageDto2.id) == StageStatus.DONE
            this.getStageStatus(projectDto.id, stageDto3.id) == StageStatus.IN_PROGRESS
            this.getProjectStatus(projectDto.id) == ProjectStatus.IN_PROGRESS
        when: "Rejecting stage from IN_PROGRESS"
            def rejectStageDto = this.rejectStage(projectDto.id, stageDto3.id)
        then: "Returns code 200"
            rejectStageDto.status == HttpStatus.OK.value()
        and: "And changes project status to DONE"
            this.getProjectStatus(projectDto.id) == ProjectStatus.DONE
    }

    @Transactional
    def "Rejecting stage from status IN_PROGRESS on project in IN_PROGRESS status with at least one stage in IN_PROGRESS does not change project status"() {
        given: "Existing project"
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            StageDto stageDto1 = this.createStage(projectDto.id)
            StageDto stageDto2 = this.createStage(projectDto.id)
            StageDto stageDto3 = this.createStage(projectDto.id)
            TaskDto taskDto21 = this.createTask(projectDto.id, stageDto2.id)
            TaskDto taskDto31 = this.createTask(projectDto.id, stageDto3.id)
            this.rejectStage(projectDto.id, stageDto1.id)
            this.updateTaskStatus(projectDto.id, stageDto2.id, taskDto21.id, TaskStatus.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto31.id, TaskStatus.IN_PROGRESS)
        expect: "In status IN_PROGRESS with at least on other stage in IN_PROGRESS"
            this.getStageStatus(projectDto.id, stageDto1.id) == StageStatus.REJECTED
            this.getStageStatus(projectDto.id, stageDto2.id) == StageStatus.IN_PROGRESS
            this.getStageStatus(projectDto.id, stageDto3.id) == StageStatus.IN_PROGRESS
            this.getProjectStatus(projectDto.id) == ProjectStatus.IN_PROGRESS
        when: "Rejecting stage from IN_PROGRESS"
            def rejectStageDto = this.rejectStage(projectDto.id, stageDto3.id)
        then: "Returns code 200"
            rejectStageDto.status == HttpStatus.OK.value()
        and: "Does not change project status"
            this.getProjectStatus(projectDto.id) == ProjectStatus.IN_PROGRESS
    }

    @Transactional
    def "Reopening stage with tasks only in TO_DO and REJECTED status reopens stage to TO_DO status"() {
        given: "Rejected stage with tasks only in TO_DO and REJECTED statuses"
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            this.createTask(projectDto.id, stageDto.id)
            this.createTask(projectDto.id, stageDto.id)
            this.rejectTask(projectDto.id, stageDto.id, taskDto11.id)
            this.rejectStage(projectDto.id, stageDto.id)
        expect: "Stage is in REJECTED status"
            this.getStageStatus(projectDto.id, stageDto.id) == StageStatus.REJECTED
        when: "Reopening stage"
            def stageReopenResponse = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(
                    this.createStageUri(projectDto.id, stageDto.id) + "/reopen"
            ))).andReturn().response
        then: "Response code is 200"
            stageReopenResponse.getStatus() == HttpStatus.OK.value()
        and: "Changes stage status to TO_DO"
            this.getStageStatus(stageReopenResponse) == StageStatus.TO_DO
    }

    @Transactional
    def "Reopening stage with at least one task in IN_PROGRESS changes stage status to IN_PROGRESS"() {
        given: "Rejected stage at least one task in IN_PROGRESS status"
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto12 = this.createTask(projectDto.id, stageDto.id)
            this.createTask(projectDto.id, stageDto.id)
            this.rejectTask(projectDto.id, stageDto.id, taskDto11.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatus.IN_PROGRESS)
            this.rejectStage(projectDto.id, stageDto.id)
        expect: "Stage is in REJECTED status"
            this.getStageStatus(projectDto.id, stageDto.id) == StageStatus.REJECTED
        when: "Reopening stage"
            def stageReopenResponse = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(
                    this.createStageUri(projectDto.id, stageDto.id) + "/reopen"
            ))).andReturn().response
        then: "Response code is 200"
            stageReopenResponse.getStatus() == HttpStatus.OK.value()
        and: "Changes stage status to IN_PROGRESS"
            this.getStageStatus(stageReopenResponse) == StageStatus.IN_PROGRESS
    }

    @Transactional
    def "Reopening stage with at least one task in DONE changes stage status to IN_PROGRESS"() {
        given: "Rejected stage at least one task in DONE status"
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto.id)
            TaskDto taskDto12 = this.createTask(projectDto.id, stageDto.id)
            this.createTask(projectDto.id, stageDto.id)
            this.rejectTask(projectDto.id, stageDto.id, taskDto11.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatus.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatus.DONE)
            this.rejectStage(projectDto.id, stageDto.id)
        expect: "Stage is in REJECTED status"
            this.getStageStatus(projectDto.id, stageDto.id) == StageStatus.REJECTED
        when: "Reopening stage"
            def stageReopenResponse = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(
                    this.createStageUri(projectDto.id, stageDto.id) + "/reopen"
            ))).andReturn().response
        then: "Response code is 200"
            stageReopenResponse.getStatus() == HttpStatus.OK.value()
        and: "Changes stage status to IN_PROGRESS"
            this.getStageStatus(stageReopenResponse) == StageStatus.IN_PROGRESS
    }

    @Transactional
    def "Reopening stage to TO_DO on project in DONE status changes project status to IN_PROGRESS"() {
        given: "Existing project with stages in DONE and REJECTED"
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            StageDto stageDto1 = this.createStage(projectDto.id)
            StageDto stageDto2 = this.createStage(projectDto.id)
            StageDto stageDto3 = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto1.id)
            this.createTask(projectDto.id, stageDto2.id)
            this.createTask(projectDto.id, stageDto3.id)
            this.updateTaskStatus(projectDto.id, stageDto1.id, taskDto11.id, TaskStatus.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto1.id, taskDto11.id, TaskStatus.DONE)
            this.rejectStage(projectDto.id, stageDto2.id)
            this.rejectStage(projectDto.id, stageDto3.id)
        expect: "Project is in DONE "
            this.getProjectStatus(projectDto.id) == ProjectStatus.DONE
        when: "Reopening stage to TO_DO"
            def stageReopenResponse = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(
                    this.createStageUri(projectDto.id, stageDto3.id) + "/reopen"
            ))).andReturn().response
        then: "Response code is 200"
            stageReopenResponse.getStatus() == HttpStatus.OK.value()
        and: "Changes project status to IN_PROGRESS"
            this.getProjectStatus(projectDto.id) == ProjectStatus.IN_PROGRESS
    }

    @Transactional
    def "Reopening stage to IN_PROGRESS on project in TO_DO status changes project status to IN_PROGRESS"() {
        given: "Existing project with stages in DONE and REJECTED"
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            StageDto stageDto1 = this.createStage(projectDto.id)
            StageDto stageDto2 = this.createStage(projectDto.id)
            StageDto stageDto3 = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto1.id)
            this.createTask(projectDto.id, stageDto2.id)
            this.createTask(projectDto.id, stageDto3.id)
            this.updateTaskStatus(projectDto.id, stageDto1.id, taskDto11.id, TaskStatus.IN_PROGRESS)
            this.rejectStage(projectDto.id, stageDto1.id)
            this.rejectStage(projectDto.id, stageDto2.id)
        expect: "Project is in TO_DO "
            this.getProjectStatus(projectDto.id) == ProjectStatus.TO_DO
        when: "Reopening stage to IN_PROGRESS"
            def stageReopenResponse = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(
                    this.createStageUri(projectDto.id, stageDto1.id) + "/reopen"
            ))).andReturn().response
        then: "Response code is 200"
            stageReopenResponse.getStatus() == HttpStatus.OK.value()
        and: "Changes project status to IN_PROGRESS"
            this.getProjectStatus(projectDto.id) == ProjectStatus.IN_PROGRESS
    }

    @Transactional
    def "Reopening stage to IN_PROGRESS on project in DONE status changes project status to IN_PROGRESS"() {
        given: "Existing project with stages in DONE and REJECTED"
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            StageDto stageDto1 = this.createStage(projectDto.id)
            StageDto stageDto2 = this.createStage(projectDto.id)
            StageDto stageDto3 = this.createStage(projectDto.id)
            TaskDto taskDto11 = this.createTask(projectDto.id, stageDto1.id)
            this.createTask(projectDto.id, stageDto2.id)
            TaskDto taskDto13 = this.createTask(projectDto.id, stageDto3.id)
            this.updateTaskStatus(projectDto.id, stageDto1.id, taskDto11.id, TaskStatus.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto13.id, TaskStatus.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto13.id, TaskStatus.DONE)
            this.rejectStage(projectDto.id, stageDto1.id)
            this.rejectStage(projectDto.id, stageDto2.id)
        expect: "Project is in DONE "
            this.getProjectStatus(projectDto.id) == ProjectStatus.DONE
        when: "Reopening stage to IN_PROGRESS"
            def stageReopenResponse = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(
                    this.createStageUri(projectDto.id, stageDto3.id) + "/reopen"
            ))).andReturn().response
        then: "Response code is 200"
            stageReopenResponse.getStatus() == HttpStatus.OK.value()
        and: "Changes project status to IN_PROGRESS"
            this.getProjectStatus(projectDto.id) == ProjectStatus.IN_PROGRESS
    }

    // HELPER METHODS

    private ProjectDto createProject() {
        ArchitectDto architectDto =
                TestsHelper.createArchitect(this.architect, this.createBasicArchitectUri(), this.mockMvc)
        properProjectDto.architectId = architectDto.id
        ClientDto clientDto = TestsHelper.createClient(this.privateClientDto, this.createBasicClientUri(), this.mockMvc)
        properProjectDto.clientId = clientDto.id
        return TestsHelper.createProject(this.properProjectDto, this.createBasicProjectUri(), this.mockMvc)
    }

    private StageDto createStage(long projectId) {
        String stageRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(stageDto)
        def stageResponse = this.mockMvc.perform(
                MockMvcRequestBuilders.post(URI.create(this.createProjectUri(projectId) + "/stages"))
                        .header("Content-Type", "application/json")
                        .content(stageRequestBody)
        ).andReturn().response
        return MAPPER.readValue(stageResponse.contentAsString, StageDto.class)
    }

    private TaskDto createTask(long projectId, long stageId) {
        String taskRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(taskDto)
        def taskResponse = this.mockMvc.perform(
                MockMvcRequestBuilders.post(URI.create(this.createStageUri(projectId, stageId) + "/tasks"))
                        .header("Content-Type", "application/json")
                        .content(taskRequestBody)
        ).andReturn().response
        return MAPPER.readValue(taskResponse.contentAsString, TaskDto.class)
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

    private String createContractUri(long contractId) {
        HOST + ":" + port + CONTRACTS_URI + "/" + contractId
    }

    private MockHttpServletResponse acceptContractOffer(long contractId) {
        return this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(
                this.createContractUri(contractId) + "/acceptOffer"))).andReturn().response
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

    private MockHttpServletResponse rejectTask(long projectId, long stageId, long taskId) {
        return this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(
                this.createTaskUri(projectId, stageId, taskId) + "/reject"
        ))).andReturn().response
    }

    private MockHttpServletResponse updateTaskStatus(long projectId, long stageId, long taskId, TaskStatus status) {
        TaskDto updateStatusDto = new TaskDto(status: status)
        String requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(updateStatusDto)
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
        TaskDto getTaskDto = MAPPER.readValue(getTaskResponse.contentAsString, TaskDto.class)
        return getTaskDto.status
    }

    private StageStatus getStageStatus(MockHttpServletResponse getStageResponse) {
        StageDto getStageDto = MAPPER.readValue(getStageResponse.contentAsString, StageDto.class)
        return getStageDto.status
    }

    private StageStatus getStageStatus(long projectId, long stageId) {
        def getStageResponse =
                this.mockMvc.perform(MockMvcRequestBuilders.get(URI.create(this.createStageUri(projectId, stageId)))
                ).andReturn().response
        StageDto getStageDto = MAPPER.readValue(getStageResponse.contentAsString, StageDto.class)
        return getStageDto.status
    }

    private ProjectStatus getProjectStatus(MockHttpServletResponse getProjectResponse) {
        ProjectDto getProjectDto = MAPPER.readValue(getProjectResponse.contentAsString, ProjectDto.class)
        return getProjectDto.status
    }

    private ProjectStatus getProjectStatus(long projectId) {
        def getProjectResponse =
                this.mockMvc.perform(MockMvcRequestBuilders.get(URI.create(this.createProjectUri(projectId)))
                ).andReturn().response
        ProjectDto getProjectDto = MAPPER.readValue(getProjectResponse.contentAsString, ProjectDto.class)
        return getProjectDto.status
    }

    private String createBasicArchitectUri() {
        return HOST + ":" + port + ARCHITECTS_URI
    }

    private String createBasicClientUri() {
        return HOST + ":" + port + CLIENTS_URI
    }

    private String createBasicProjectUri() {
        return HOST + ":" + port + PROJECTS_URI
    }
}
