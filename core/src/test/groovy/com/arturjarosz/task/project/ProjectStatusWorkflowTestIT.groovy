package com.arturjarosz.task.project

import com.arturjarosz.task.configuration.BaseTestIT
import com.arturjarosz.task.dto.*
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
    static final String ARCHITECTS_URI = "/architects"
    static final String CLIENTS_URI = "/clients"
    static final String CONTRACTS_URI = "/contracts"
    static final String PROJECTS_URI = "/projects"
    static final ObjectMapper MAPPER = new ObjectMapper()

    final def architect = MAPPER.readValue(new File(getClass().classLoader.getResource('json/architect/architect.json').file),
            ArchitectDto)
    final def privateClientDto = MAPPER.readValue(new File(getClass().classLoader.getResource('json/client/privateClient.json').file),
            ClientDto)
    final def properProjectDto = MAPPER.readValue(new File(getClass().classLoader.getResource('json/project/properProject.json').file),
            ProjectCreateDto)
    final def stageDto = MAPPER.readValue(new File(getClass().classLoader.getResource('json/stage/properStage.json').file),
            StageDto)
    final def taskDto = MAPPER.readValue(new File(getClass().classLoader.getResource('json/task/properTask.json').file),
            TaskDto)

    @Autowired
    private MockMvc mockMvc

    @Override
    def setupSpec() {
        MAPPER.findAndRegisterModules()
    }

    @Transactional
    def "Creating project should return code 200 and put project in TO_DO status"() {
        given: "Existing architect"
            def architectDto = TestsHelper.createArchitect(this.architect, this.createBasicArchitectUri(), this.mockMvc)
            properProjectDto.architectId = architectDto.id
        and: "Existing client"
            def clientDto = TestsHelper.createClient(this.privateClientDto, this.createBasicClientUri(), this.mockMvc)
            properProjectDto.clientId = clientDto.id
        when: "Creating project with proper data"
            def projectRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properProjectDto)
            def projectResponse = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + PROJECTS_URI))
                    .header("Content-Type", "application/json")
                    .content(projectRequestBody)).andReturn().response
        then: "Returns code 201"
            projectResponse.status == HttpStatus.CREATED.value()
        and: "Project status is set to TO_DO"
            this.getProjectStatus(projectResponse) == ProjectStatusDto.TO_DO
    }

    @Transactional
    def "Creating project creates connected to it contract"() {
        given: "Existing architect"
            def architectDto = TestsHelper.createArchitect(this.architect, this.createBasicArchitectUri(), this.mockMvc)
            properProjectDto.architectId = architectDto.id
        and: "Existing client"
            def clientDto = TestsHelper.createClient(this.privateClientDto, this.createBasicClientUri(), this.mockMvc)
            properProjectDto.clientId = clientDto.id
        when: "Creating project with proper data"
            def projectRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properProjectDto)
            def projectResponse = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + PROJECTS_URI))
                    .header("Content-Type", "application/json")
                    .content(projectRequestBody)).andReturn().response
            def projectDto = MAPPER.readValue(projectResponse.contentAsString, ProjectDto)
        then: "Returns code 201"
            projectResponse.status == HttpStatus.CREATED.value()
        and: "Project status is set to TO_DO"
            projectDto.contract != null
            projectDto.contract.id != null
    }

    @Transactional
    def "Rejecting new project should put it in REJECT status"() {
        given: "New project"
            def projectDto = this.createProject()
        when: "Rejecting new project"
            def rejectResponse = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(this.createProjectUri(projectDto.id) + "/reject"))).andReturn().response
        then: "Response code should be 200"
            rejectResponse.status == HttpStatus.OK.value()
        and: "Project status is REJECTED"
            this.getProjectStatus(rejectResponse) == ProjectStatusDto.REJECTED
    }

    @Transactional
    def "For project with not accepted offer, it should not be possible to start progress in work and it should put leave task, stage and project in TO_DO."() {
        given: "Project with accepted offer and stages and tasks in TO DO"
            def projectDto = this.createProject()
            def stageDto = this.createStage(projectDto.id)
            def taskDto11 = this.createTask(projectDto.id, stageDto.id)
        when: "Change one task status to IN PROGRESS"
            def changeTaskStatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatusDto.IN_PROGRESS)
        then: "Response code is 400"
            changeTaskStatusResponse.status == HttpStatus.BAD_REQUEST.value()
            def message = MAPPER.readValue(changeTaskStatusResponse.contentAsString, ErrorMessage)
            message.getMessage() == "Contract in status OFFER does not allow for work on the project."
        and: "Task status is TO_DO"
            def getTaskResponse = getTaskResponse(projectDto.id, stageDto.id, taskDto11.id)
            this.getTaskStatus(getTaskResponse) == TaskStatusDto.TO_DO
        and: "Stage status is TO_DO"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatusDto.TO_DO
        and: "Project status is TO_DO"
            def getProjectResponse = this.getProjectResponse(projectDto.id)
            this.getProjectStatus(getProjectResponse) == ProjectStatusDto.TO_DO
    }

    @Transactional
    def "For project with accepted offer, it should be possible to start progress in work"() {
        given:
            def projectDto = this.createProject()
            def stageDto = this.createStage(projectDto.id)
            def taskDto = this.createTask(projectDto.id, stageDto.id)
            this.acceptContractOffer(projectDto.contract.id)
        when:
            def changeTaskStatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto.id, TaskStatusDto.IN_PROGRESS)
        then:
            changeTaskStatusResponse.status == HttpStatus.OK.value()
        and:
            this.getTaskStatus(changeTaskStatusResponse) == TaskStatusDto.IN_PROGRESS
    }

/*    @Transactional
    def "8 For project with accepted offer, it should not be possible to make new offer"() {
        given:
            ProjectDto projectDto = this.createProject()
            StageDto stageDto = this.createStage(projectDto.id)
            TaskDto taskDto = this.createTask(projectDto.id, stageDto.id)
            def acceptOfferResponse = this.acceptContractOffer(projectDto.contract.contractId)
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
            def acceptOfferResponse = this.acceptContractOffer(projectDto.contract.contractId)
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
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
        when:
            def rejectResponse = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(this.createProjectUri(projectDto.id) + "/reject"))).andReturn().response
        then: "Response code should be 200"
            rejectResponse.status == HttpStatus.OK.value()
        and: "Project status is REJECTED"
            this.getProjectStatus(rejectResponse) == ProjectStatusDto.REJECTED
    }

    @Transactional
    def "Reopening rejected project with accepted offer and stages only in TO_DO statuses should change project status to TO_DO"() {
        given:
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageDto = this.createStage(projectDto.id)
            this.createTask(projectDto.id, stageDto.id)
            this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(this.createProjectUri(projectDto.id) + "/reject"))).andReturn().response
        when:
            def reopenResponse = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(this.createProjectUri(projectDto.id) + "/reopen"))).andReturn().response
        then: "Response code should be 200"
            reopenResponse.status == HttpStatus.OK.value()
        and: "Project status is REJECTED"
            this.getProjectStatus(reopenResponse) == ProjectStatusDto.TO_DO
    }

    @Transactional
    def "Reopening rejected project, that was in IN_PROGRESS status, should be back to IN_PROGRESS"() {
        given:
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageDto = this.createStage(projectDto.id)
            def taskDto = this.createTask(projectDto.id, stageDto.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto.id, TaskStatusDto.IN_PROGRESS)
            this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(this.createProjectUri(projectDto.id) + "/reject"))).andReturn().response
        when:
            def reopenResponse = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(this.createProjectUri(projectDto.id) + "/reopen"))).andReturn().response
        then: "Response code should be 200"
            reopenResponse.status == HttpStatus.OK.value()
        and: "Project status is IN_PROGRESS"
            this.getProjectStatus(reopenResponse) == ProjectStatusDto.IN_PROGRESS
    }

    @Transactional
    def "Reject only task in stage, should put that stage in TO_DO status and put PROJECT in TO_DO"() {
        given: "Project with accepted offer and stages and tasks in TO DO"
            def projectDto = this.createProject()
            def stageDto = this.createStage(projectDto.id)
            def taskDto11 = this.createTask(projectDto.id, stageDto.id)
            this.acceptContractOffer(projectDto.contract.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatusDto.IN_PROGRESS)
        when: "Change one task status to IN PROGRESS"
            def changeTaskStatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatusDto.REJECTED)
        then: "Response code is 200"
            changeTaskStatusResponse.status == HttpStatus.OK.value()
        and: "Task status is REJECTED"
            def getTaskResponse = getTaskResponse(projectDto.id, stageDto.id, taskDto11.id)
            this.getTaskStatus(getTaskResponse) == TaskStatusDto.REJECTED
        and: "Stage status is TO_DO"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatusDto.TO_DO
        and: "Project status is TO_DO"
            def getProjectResponse = this.getProjectResponse(projectDto.id)
            this.getProjectStatus(getProjectResponse) == ProjectStatusDto.TO_DO
    }

    @Transactional
    def "Finishing work on only task in stage, should put that stage in DONE status and put PROJECT in DONE"() {
        given: "Project with accepted offer and stages and tasks in TO DO"
            def projectDto = this.createProject()
            def stageDto = this.createStage(projectDto.id)
            def taskDto11 = this.createTask(projectDto.id, stageDto.id)
            this.acceptContractOffer(projectDto.contract.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatusDto.IN_PROGRESS)
        when: "Change one task status to IN PROGRESS"
            def changeTaskStatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatusDto.DONE)
        then: "Response code is 200"
            changeTaskStatusResponse.status == HttpStatus.OK.value()
        and: "Task status is DONE"
            def getTaskResponse = this.getTaskResponse(projectDto.id, stageDto.id, taskDto11.id)
            this.getTaskStatus(getTaskResponse) == TaskStatusDto.DONE
        and: "Stage status is DONE"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatusDto.DONE
        and: "Project status is DONE"
            def getProjectResponse = this.getProjectResponse(projectDto.id)
            this.getProjectStatus(getProjectResponse) == ProjectStatusDto.DONE
    }

    @Transactional
    def "Rejecting only stage from TO_DO should put stage in REJECTED status and not change project status"() {
        given: "Project with accepted offer and stages and tasks in TO DO"
            def projectDto = this.createProject()
            def stageDto = this.createStage(projectDto.id)
            this.createTask(projectDto.id, stageDto.id)
            this.acceptContractOffer(projectDto.contract.id)
        when:
            def rejectStageResponse = this.mockMvc.perform(MockMvcRequestBuilders
                    .post(URI
                            .create(this.createStageUri(projectDto.id, stageDto.id) + "/reject"))).andReturn().response
        then: "Response code is 200"
            rejectStageResponse.status == HttpStatus.OK.value()
        and: "Stage status is REJECTED"
            this.getStageStatus(rejectStageResponse) == StageStatusDto.REJECTED
        and:
            def getProjectResponse = this.getProjectResponse(projectDto.id)
            this.getProjectStatus(getProjectResponse) == ProjectStatusDto.TO_DO
    }

    @Transactional
    def "Creating new task on stage in TO_DO status should not change stage or project status"() {
        given: "Project with accepted offer and stages and tasks in TO DO"
            def projectDto = this.createProject()
            def stageDto = this.createStage(projectDto.id)
            this.createTask(projectDto.id, stageDto.id)
            this.acceptContractOffer(projectDto.contract.id)
        when:
            def taskRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(taskDto)
            def taskResponse = this.mockMvc.perform(MockMvcRequestBuilders
                    .post(URI.create(this.createStageUri(projectDto.id, stageDto.id) + "/tasks"))
                    .header("Content-Type", "application/json")
                    .content(taskRequestBody)).andReturn().response
        then: "Response code is 201"
            taskResponse.status == HttpStatus.CREATED.value()
        and: "Task status is TO_DO"
            def createdTaskDto12 = MAPPER.readValue(taskResponse.contentAsString, TaskDto)
            def getTaskResponse = getTaskResponse(projectDto.id, stageDto.id, createdTaskDto12.id)
            this.getTaskStatus(getTaskResponse) == TaskStatusDto.TO_DO
        and: "Stage status is TO_DO"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatusDto.TO_DO
        and: "Project status is TO_DO"
            def getProjectResponse = this.getProjectResponse(projectDto.id)
            this.getProjectStatus(getProjectResponse) == ProjectStatusDto.TO_DO
    }

    @Transactional
    def "Creating new task on stage in IN_PROGRESS status should not change stage or project status"() {
        given: "Project with accepted offer and stages and tasks in TO DO"
            def projectDto = this.createProject()
            def stageDto = this.createStage(projectDto.id)
            def taskDto11 = this.createTask(projectDto.id, stageDto.id)
            this.acceptContractOffer(projectDto.contract.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatusDto.IN_PROGRESS)
        when:
            def taskRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(taskDto)
            def taskResponse = this.mockMvc.perform(MockMvcRequestBuilders
                    .post(URI.create(this.createStageUri(projectDto.id, stageDto.id) + "/tasks"))
                    .header("Content-Type", "application/json")
                    .content(taskRequestBody)).andReturn().response
        then: "Response code is 201"
            taskResponse.status == HttpStatus.CREATED.value()
        and: "Task status is TO_DO"
            def createdTaskDto12 = MAPPER.readValue(taskResponse.contentAsString, TaskDto)
            def getTaskResponse = getTaskResponse(projectDto.id, stageDto.id, createdTaskDto12.id)
            this.getTaskStatus(getTaskResponse) == TaskStatusDto.TO_DO
        and: "Stage status is IN_PROGRESS"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatusDto.IN_PROGRESS
        and: "Project status is IN_PROGRESS"
            def getProjectResponse = this.getProjectResponse(projectDto.id)
            this.getProjectStatus(getProjectResponse) == ProjectStatusDto.IN_PROGRESS
    }

    @Transactional
    def "Creating new task on stage in REJECTED status should return code 400, error message and not create new task"() {
        given: "Project with accepted offer and stages and tasks in TO DO"
            def projectDto = this.createProject()
            def stageDto = this.createStage(projectDto.id)
            def taskDto = this.createTask(projectDto.id, stageDto.id)
            this.acceptContractOffer(projectDto.contract.id)
            this.mockMvc.perform(MockMvcRequestBuilders
                    .post(URI
                            .create(this.createStageUri(projectDto.id, stageDto.id) + "/reject"))).andReturn().response
        when:
            def taskRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(taskDto)
            def taskResponse = this.mockMvc.perform(MockMvcRequestBuilders
                    .post(URI.create(this.createStageUri(projectDto.id, stageDto.id) + "/tasks"))
                    .header("Content-Type", "application/json")
                    .content(taskRequestBody)).andReturn().response
        then: "Response code is 400"
            taskResponse.status == HttpStatus.BAD_REQUEST.value()
        and:
            def errorMessage = MAPPER.readValue(taskResponse.contentAsString, ErrorMessage)
            errorMessage.message == "Stage status REJECTED does not allow to make any work on the stage."
    }

    @Transactional
    def "Creating new task on stage in DONE status should change stage status to IN_PROGRESS and change project status to IN_PROGRESS if was DONE"() {
        given: "Project with accepted offer and stages and tasks in TO DO"
            def projectDto = this.createProject()
            def stageDto = this.createStage(projectDto.id)
            def taskDto11 = this.createTask(projectDto.id, stageDto.id)
            this.acceptContractOffer(projectDto.contract.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatusDto.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatusDto.DONE)
        when:
            def taskRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(taskDto)
            def taskResponse = this.mockMvc.perform(MockMvcRequestBuilders
                    .post(URI.create(this.createStageUri(projectDto.id, stageDto.id) + "/tasks"))
                    .header("Content-Type", "application/json")
                    .content(taskRequestBody)).andReturn().response
        then: "Response code is 201"
            taskResponse.status == HttpStatus.CREATED.value()
        and: "Task status is TO_DO"
            def createdTaskDto12 = MAPPER.readValue(taskResponse.contentAsString, TaskDto)
            def getTaskResponse = getTaskResponse(projectDto.id, stageDto.id, createdTaskDto12.id)
            this.getTaskStatus(getTaskResponse) == TaskStatusDto.TO_DO
        and: "Stage status is IN_PROGRESS"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatusDto.IN_PROGRESS
        and: "Project status is IN_PROGRESS"
            def getProjectResponse = this.getProjectResponse(projectDto.id)
            this.getProjectStatus(getProjectResponse) == ProjectStatusDto.IN_PROGRESS
    }

    @Transactional
    def "Starting work on the only task on stage should change stage status to IN_PROGRESS"() {
        given:
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageDto = this.createStage(projectDto.id)
            def taskDto = this.createTask(projectDto.id, stageDto.id)
        when:
            def changeTaskStatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto.id, TaskStatusDto.IN_PROGRESS)
        then:
            changeTaskStatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is TO_DO"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatusDto.IN_PROGRESS
    }

    @Transactional
    def "Starting work on stage in TO_DO with only REJECTED tasks should change stage status to IN_PROGRESS"() {
        given:
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageDto = this.createStage(projectDto.id)
            def taskDto11 = this.createTask(projectDto.id, stageDto.id)
            def taskDto12 = this.createTask(projectDto.id, stageDto.id)
            def taskDto13 = this.createTask(projectDto.id, stageDto.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatusDto.REJECTED)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatusDto.REJECTED)
        when:
            def changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatusDto.IN_PROGRESS)
        then:
            changeTask13StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is TO_DO"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatusDto.IN_PROGRESS
    }

    @Transactional
    def "Starting work on stage in TO_DO with tasks in TO_DO and REJECTED statuses should change stage status to IN_PROGRESS"() {
        given:
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageDto = this.createStage(projectDto.id)
            def taskDto11 = this.createTask(projectDto.id, stageDto.id)
            def taskDto12 = this.createTask(projectDto.id, stageDto.id)
            def taskDto13 = this.createTask(projectDto.id, stageDto.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatusDto.REJECTED)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatusDto.REJECTED)
        when:
            def changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatusDto.IN_PROGRESS)
        then:
            changeTask13StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is TO_DO"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatusDto.IN_PROGRESS
    }

    @Transactional
    def "Starting work on stage in IN_PROGRESS status should not change stage status"() {
        given:
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageDto = this.createStage(projectDto.id)
            def taskDto11 = this.createTask(projectDto.id, stageDto.id)
            def taskDto12 = this.createTask(projectDto.id, stageDto.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatusDto.IN_PROGRESS)
        when:
            def changeTask12StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatusDto.IN_PROGRESS)
        then:
            changeTask12StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is IN_PROGRESS"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatusDto.IN_PROGRESS
    }

    @Transactional
    def "Rejecting task from TO_DO status on stage in TO_DO should not change stage status"() {
        given:
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageDto = this.createStage(projectDto.id)
            def taskDto11 = this.createTask(projectDto.id, stageDto.id)
        when:
            def changeTask12StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatusDto.REJECTED)
        then:
            changeTask12StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is TO_DO"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatusDto.TO_DO
    }

    @Transactional
    def "Rejecting task from TO_DO status on stage in IN_PROGRESS, while there are some tasks in IN_PROGRESS does not change stage status"() {
        given:
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageDto = this.createStage(projectDto.id)
            def taskDto11 = this.createTask(projectDto.id, stageDto.id)
            def taskDto12 = this.createTask(projectDto.id, stageDto.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatusDto.IN_PROGRESS)
        when:
            def changeTask12StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatusDto.REJECTED)
        then:
            changeTask12StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is TO_DO"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatusDto.IN_PROGRESS
    }

    @Transactional
    def "Rejecting task from TO_DO status on stage in IN_PROGRESS, while there are only tasks in REJECTED and DONE changes stage status to DONE"() {
        given:
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageDto = this.createStage(projectDto.id)
            def taskDto11 = this.createTask(projectDto.id, stageDto.id)
            def taskDto12 = this.createTask(projectDto.id, stageDto.id)
            def taskDto13 = this.createTask(projectDto.id, stageDto.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatusDto.REJECTED)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatusDto.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatusDto.DONE)
        when:
            def changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatusDto.REJECTED)
        then:
            changeTask13StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is TO_DO"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatusDto.DONE
    }

    @Transactional
    def "Rejecting task from IN_PROGRESS, with only tasks in REJECTED, should change stage status to TO_DO"() {
        given:
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageDto = this.createStage(projectDto.id)
            def taskDto11 = this.createTask(projectDto.id, stageDto.id)
            def taskDto12 = this.createTask(projectDto.id, stageDto.id)
            def taskDto13 = this.createTask(projectDto.id, stageDto.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatusDto.REJECTED)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatusDto.REJECTED)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatusDto.IN_PROGRESS)
        when:
            def changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatusDto.REJECTED)
        then:
            changeTask13StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is TO_DO"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatusDto.TO_DO
    }

    @Transactional
    def "Rejecting task from IN_PROGRESS, with some tasks in IN_PROGRESS, should not change stage status"() {
        given:
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageDto = this.createStage(projectDto.id)
            def taskDto11 = this.createTask(projectDto.id, stageDto.id)
            def taskDto12 = this.createTask(projectDto.id, stageDto.id)
            def taskDto13 = this.createTask(projectDto.id, stageDto.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatusDto.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatusDto.REJECTED)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatusDto.IN_PROGRESS)
        when:
            def changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatusDto.REJECTED)
        then:
            changeTask13StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is IN_PROGRESS"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatusDto.IN_PROGRESS
    }

    @Transactional
    def "Rejecting task from IN_PROGRESS, with task only in TO_DO and REJECTED, should change stage status to TO_DO"() {
        given:
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageDto = this.createStage(projectDto.id)
            this.createTask(projectDto.id, stageDto.id)
            def taskDto12 = this.createTask(projectDto.id, stageDto.id)
            def taskDto13 = this.createTask(projectDto.id, stageDto.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatusDto.REJECTED)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatusDto.IN_PROGRESS)
        when:
            def changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatusDto.REJECTED)
        then:
            changeTask13StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is TO_DO"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatusDto.TO_DO
    }

    @Transactional
    def "Rejecting task from IN_PROGRESS, with task only in DONE and REJECTED, should change stage status to DONE"() {
        given:
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageDto = this.createStage(projectDto.id)
            def taskDto11 = this.createTask(projectDto.id, stageDto.id)
            def taskDto12 = this.createTask(projectDto.id, stageDto.id)
            def taskDto13 = this.createTask(projectDto.id, stageDto.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatusDto.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatusDto.DONE)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatusDto.REJECTED)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatusDto.IN_PROGRESS)
        when:
            def changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatusDto.REJECTED)
        then:
            changeTask13StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is DONE"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatusDto.DONE
    }

    @Transactional
    def "Reopening task on stage in TO_DO status should not change stage status"() {
        given:
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageDto = this.createStage(projectDto.id)
            def taskDto11 = this.createTask(projectDto.id, stageDto.id)
            this.createTask(projectDto.id, stageDto.id)
            this.createTask(projectDto.id, stageDto.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatusDto.REJECTED)
            def updateStatusDto = new TaskDto(status: TaskStatusDto.TO_DO)
            def requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(updateStatusDto)
        when:
            def reopenTask11 = this.mockMvc.perform(MockMvcRequestBuilders
                    .post(URI
                            .create(this.createTaskUri(projectDto.id, stageDto.id,
                                    taskDto11.id) + "/status"))
                    .header("Content-Type", "application/json")
                    .content(requestBody))
                    .andReturn().response

        then:
            reopenTask11.status == HttpStatus.OK.value()
        and: "Stage status is TO_DO"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatusDto.TO_DO
    }

    @Transactional
    def "Reopening task on stage in IN_PROGRESS status should not change stage status"() {
        given:
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageDto = this.createStage(projectDto.id)
            def taskDto11 = this.createTask(projectDto.id, stageDto.id)
            def taskDto12 = this.createTask(projectDto.id, stageDto.id)
            this.createTask(projectDto.id, stageDto.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatusDto.REJECTED)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatusDto.IN_PROGRESS)
            def updateStatusDto = new TaskDto(status: TaskStatusDto.TO_DO)
            def requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(updateStatusDto)
        when:
            def reopenTask11 = this.mockMvc.perform(MockMvcRequestBuilders
                    .post(URI
                            .create(this.createTaskUri(projectDto.id, stageDto.id,
                                    taskDto11.id) + "/status"))
                    .header("Content-Type", "application/json")
                    .content(requestBody))
                    .andReturn().response
        then:
            reopenTask11.status == HttpStatus.OK.value()
        and: "Stage status is IN_PROGRESS"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatusDto.IN_PROGRESS
    }

    @Transactional
    def "Reopening task on stage in DONE status should change stage status to IN_PROGRESS"() {
        given:
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageDto = this.createStage(projectDto.id)
            def taskDto11 = this.createTask(projectDto.id, stageDto.id)
            def taskDto12 = this.createTask(projectDto.id, stageDto.id)
            def taskDto13 = this.createTask(projectDto.id, stageDto.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatusDto.REJECTED)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatusDto.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatusDto.DONE)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatusDto.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatusDto.DONE)
            def updateStatusDto = new TaskDto(status: TaskStatusDto.TO_DO)
            def requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(updateStatusDto)
        when:
            def reopenTask11 = this.mockMvc.perform(MockMvcRequestBuilders
                    .post(URI
                            .create(this.createTaskUri(projectDto.id, stageDto.id,
                                    taskDto11.id) + "/status"))
                    .header("Content-Type", "application/json")
                    .content(requestBody))
                    .andReturn().response
        then:
            reopenTask11.status == HttpStatus.OK.value()
        and: "Stage status is IN_PROGRESS"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatusDto.IN_PROGRESS
    }

    @Transactional
    def "Changing tasks status from IN_PROGRESS to TO_DO on stage in IN_PROGRESS, with other tasks in IN_PROGRESS should not change stage status"() {
        given:
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageDto = this.createStage(projectDto.id)
            def taskDto11 = this.createTask(projectDto.id, stageDto.id)
            def taskDto12 = this.createTask(projectDto.id, stageDto.id)
            def taskDto13 = this.createTask(projectDto.id, stageDto.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatusDto.REJECTED)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatusDto.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatusDto.IN_PROGRESS)
        when:
            def changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatusDto.TO_DO)
        then:
            changeTask13StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is IN_PROGRESS"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatusDto.IN_PROGRESS
    }

    @Transactional
    def "Changing tasks status from IN_PROGRESS to TO_DO on stage in IN_PROGRESS, with at least on task in DONE should not change stage status"() {
        given:
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageDto = this.createStage(projectDto.id)
            def taskDto11 = this.createTask(projectDto.id, stageDto.id)
            def taskDto12 = this.createTask(projectDto.id, stageDto.id)
            def taskDto13 = this.createTask(projectDto.id, stageDto.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatusDto.REJECTED)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatusDto.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatusDto.DONE)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatusDto.IN_PROGRESS)
        when:
            def changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatusDto.TO_DO)
        then:
            changeTask13StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is IN_PROGRESS"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatusDto.IN_PROGRESS
    }

    @Transactional
    def "Changing tasks status from IN_PROGRESS to TO_DO on stage in IN_PROGRESS, with tasks only in TO_DO and REJECTED should change stage status to TO_DO"() {
        given:
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageDto = this.createStage(projectDto.id)
            def taskDto11 = this.createTask(projectDto.id, stageDto.id)
            this.createTask(projectDto.id, stageDto.id)
            def taskDto13 = this.createTask(projectDto.id, stageDto.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatusDto.REJECTED)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatusDto.IN_PROGRESS)
        when:
            def changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatusDto.TO_DO)
        then:
            changeTask13StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is TO_DO"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatusDto.TO_DO
    }

    @Transactional
    def "Changing tasks status from IN_PROGRESS to TO_DO on stage in IN_PROGRESS, rest of task in REJECTED should change stage status to TO_DO"() {
        given:
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageDto = this.createStage(projectDto.id)
            def taskDto11 = this.createTask(projectDto.id, stageDto.id)
            def taskDto12 = this.createTask(projectDto.id, stageDto.id)
            def taskDto13 = this.createTask(projectDto.id, stageDto.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatusDto.REJECTED)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatusDto.REJECTED)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatusDto.IN_PROGRESS)
        when:
            def changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatusDto.TO_DO)
        then:
            changeTask13StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is TO_DO"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatusDto.TO_DO
    }

    @Transactional
    def "Changing status of task from IN_PROGRESS to DONE on stage in IN_PROGRESS while there are some task in TO_DO, should not change stage status"() {
        given:
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageDto = this.createStage(projectDto.id)
            def taskDto11 = this.createTask(projectDto.id, stageDto.id)
            def taskDto12 = this.createTask(projectDto.id, stageDto.id)
            def taskDto13 = this.createTask(projectDto.id, stageDto.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatusDto.REJECTED)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatusDto.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatusDto.IN_PROGRESS)
        when:
            def changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatusDto.DONE)
        then:
            changeTask13StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is IN_PROGRESS"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatusDto.IN_PROGRESS
    }

    @Transactional
    def "Changing status of task from IN_PROGRESS to DONE on stage in IN_PROGRESS while there are some task in IN_PROGRESS, should not change stage status"() {
        given:
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageDto = this.createStage(projectDto.id)
            def taskDto11 = this.createTask(projectDto.id, stageDto.id)
            def taskDto12 = this.createTask(projectDto.id, stageDto.id)
            def taskDto13 = this.createTask(projectDto.id, stageDto.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatusDto.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatusDto.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatusDto.IN_PROGRESS)
        when:
            def changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatusDto.DONE)
        then:
            changeTask13StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is IN_PROGRESS"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatusDto.IN_PROGRESS
    }

    @Transactional
    def "Changing status of task from IN_PROGRESS to DONE on stage in IN_PROGRESS while there only tasks in REJECTED and DONE, should change stage status to DONE"() {
        given:
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageDto = this.createStage(projectDto.id)
            def taskDto11 = this.createTask(projectDto.id, stageDto.id)
            def taskDto12 = this.createTask(projectDto.id, stageDto.id)
            def taskDto13 = this.createTask(projectDto.id, stageDto.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatusDto.REJECTED)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatusDto.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatusDto.DONE)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatusDto.IN_PROGRESS)
        when:
            def changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatusDto.DONE)
        then:
            changeTask13StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is DONE"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatusDto.DONE
    }

    @Transactional
    def "Changing status of task from DONE to IN_PROGRESS on stage in IN_PROGRESS status, should not change stage status "() {
        given:
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageDto = this.createStage(projectDto.id)
            def taskDto11 = this.createTask(projectDto.id, stageDto.id)
            def taskDto12 = this.createTask(projectDto.id, stageDto.id)
            def taskDto13 = this.createTask(projectDto.id, stageDto.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatusDto.REJECTED)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatusDto.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatusDto.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatusDto.DONE)
        when:
            def changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatusDto.IN_PROGRESS)
        then:
            changeTask13StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is IN_PROGRESS"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatusDto.IN_PROGRESS
    }

    @Transactional
    def "Changing status of task from DONE to IN_PROGRESS on stage in DONE status, should change stage status to IN_PROGRESS"() {
        given:
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageDto = this.createStage(projectDto.id)
            def taskDto11 = this.createTask(projectDto.id, stageDto.id)
            def taskDto12 = this.createTask(projectDto.id, stageDto.id)
            def taskDto13 = this.createTask(projectDto.id, stageDto.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto11.id, TaskStatusDto.REJECTED)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatusDto.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatusDto.DONE)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatusDto.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatusDto.DONE)
        when:
            def changeTask13StatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto.id, taskDto13.id, TaskStatusDto.IN_PROGRESS)
        then:
            changeTask13StatusResponse.status == HttpStatus.OK.value()
        and: "Stage status is IN_PROGRESS"
            def getStageResponse = this.getStageResponse(projectDto.id, stageDto.id)
            this.getStageStatus(getStageResponse) == StageStatusDto.IN_PROGRESS
    }

    @Transactional
    def "Creating new stage for project in TO_DO status returns code 201 and does not change project status"() {
        given:
            def projectDto = this.createProject()
            def stageDto1 = this.createStage(projectDto.id)
            this.createTask(projectDto.id, stageDto1.id)
        when:
            String stageRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(stageDto)
            def stageResponse = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(this.createProjectUri(projectDto.id) + "/stages"))
                    .header("Content-Type", "application/json")
                    .content(stageRequestBody)).andReturn().response
        then:
            stageResponse.status == HttpStatus.CREATED.value()
        and:
            def getProjectResponse = this.getProjectResponse(projectDto.id)
            this.getProjectStatus(getProjectResponse) == ProjectStatusDto.TO_DO
    }

    @Transactional
    def "Creating new stage for project in DONE status returns code 201 and changes project status to IN_PROGRESS"() {
        given:
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageDto1 = this.createStage(projectDto.id)
            def taskDto11 = this.createTask(projectDto.id, stageDto1.id)
            this.updateTaskStatus(projectDto.id, stageDto1.id, taskDto11.id, TaskStatusDto.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto1.id, taskDto11.id, TaskStatusDto.DONE)
        when:
            def stageRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(stageDto)
            def stageResponse = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(this.createProjectUri(projectDto.id) + "/stages"))
                    .header("Content-Type", "application/json")
                    .content(stageRequestBody)).andReturn().response
        then:
            stageResponse.status == HttpStatus.CREATED.value()
        and:
            def getProjectResponse = this.getProjectResponse(projectDto.id)
            this.getProjectStatus(getProjectResponse) == ProjectStatusDto.IN_PROGRESS
    }

    @Transactional
    def "Creating new stage for project in TO_DO status with accepted offer returns code 201 and does not change project status"() {
        given:
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageDto1 = this.createStage(projectDto.id)
            this.createTask(projectDto.id, stageDto1.id)
        when:
            def stageRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(stageDto)
            def stageResponse = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(this.createProjectUri(projectDto.id) + "/stages"))
                    .header("Content-Type", "application/json")
                    .content(stageRequestBody)).andReturn().response
        then:
            stageResponse.status == HttpStatus.CREATED.value()
        and:
            def getProjectResponse = this.getProjectResponse(projectDto.id)
            this.getProjectStatus(getProjectResponse) == ProjectStatusDto.TO_DO
    }

    @Transactional
    def "Creating new stage for project in IN_PROGRESS status returns code 201 and does not change project status"() {
        given:
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageDto1 = this.createStage(projectDto.id)
            def taskDto11 = this.createTask(projectDto.id, stageDto1.id)
            this.updateTaskStatus(projectDto.id, stageDto1.id, taskDto11.id, TaskStatusDto.IN_PROGRESS)
        when:
            def stageRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(stageDto)
            def stageResponse = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(this.createProjectUri(projectDto.id) + "/stages"))
                    .header("Content-Type", "application/json")
                    .content(stageRequestBody)).andReturn().response
        then:
            stageResponse.status == HttpStatus.CREATED.value()
        and:
            def getProjectResponse = this.getProjectResponse(projectDto.id)
            this.getProjectStatus(getProjectResponse) == ProjectStatusDto.IN_PROGRESS
    }

    @Transactional
    def "Creating new stage for project in REJECTED status returns code 400 and error message"() {
        given:
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            this.rejectProject(projectDto.id)
        when:
            def stageRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(stageDto)
            def stageResponse = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(this.createProjectUri(projectDto.id) + "/stages"))
                    .header("Content-Type", "application/json")
                    .content(stageRequestBody)).andReturn().response
        then: "Response code is 400"
            stageResponse.status == HttpStatus.BAD_REQUEST.value()
        and:
            def errorMessage = MAPPER.readValue(stageResponse.contentAsString, ErrorMessage)
            errorMessage.message == "Project status REJECTED does not allow to make any work on the project."
    }

    @Transactional
    def "Changing stage status to IN_PROGRESS on project in status TO_DO changes project status to IN_PROGRESS"() {
        given:
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageDto1 = this.createStage(projectDto.id)
            def taskDto11 = this.createTask(projectDto.id, stageDto1.id)
        when:
            def updateTaskStatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto1.id, taskDto11.id, TaskStatusDto.IN_PROGRESS)
        then:
            updateTaskStatusResponse.status == HttpStatus.OK.value()
        and:
            def getProjectResponse = this.getProjectResponse(projectDto.id)
            this.getProjectStatus(getProjectResponse) == ProjectStatusDto.IN_PROGRESS
    }

    @Transactional
    def "Changing stage status to IN_PROGRESS on project in status REJECTED returns code 400 and error message"() {
        given:
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageDto1 = this.createStage(projectDto.id)
            def taskDto11 = this.createTask(projectDto.id, stageDto1.id)
            this.rejectProject(projectDto.id)
        when:
            def updateTaskStatusResponse =
                    this.updateTaskStatus(projectDto.id, stageDto1.id, taskDto11.id, TaskStatusDto.IN_PROGRESS)
        then: "Response code is 400"
            updateTaskStatusResponse.status == HttpStatus.BAD_REQUEST.value()
        and:
            def errorMessage = MAPPER.readValue(updateTaskStatusResponse.contentAsString, ErrorMessage)
            errorMessage.message == "Project status REJECTED does not allow to make any work on the project."
    }

    @Transactional
    def "Changing stage status from IN_PROGRESS to TO_DO, while other stages are in TO_DO and REJECTED on project in IN_PROGRESS status, changes project status to TO_DO"() {
        given:
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageDto1 = this.createStage(projectDto.id)
            this.rejectStage(projectDto.id, stageDto1.id)
            def stageDto2 = this.createStage(projectDto.id)
            def stageDto3 = this.createStage(projectDto.id)
            this.createTask(projectDto.id, stageDto2.id)
            def taskDto31 = this.createTask(projectDto.id, stageDto3.id)
            this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto31.id, TaskStatusDto.IN_PROGRESS)
        when:
            def updateTaskStatusDto = this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto31.id, TaskStatusDto.TO_DO)
        then: "Response code is 400"
            updateTaskStatusDto.status == HttpStatus.OK.value()
        and:
            def getProjectResponse = this.getProjectResponse(projectDto.id)
            this.getProjectStatus(getProjectResponse) == ProjectStatusDto.TO_DO
    }

    @Transactional
    def "Changing stage status from IN_PROGRESS to TO_DO, while there is at least one stage in IN_PROGRESS on project in IN_PROGRESS status, changes project status to TO_DO"() {
        given:
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageDto1 = this.createStage(projectDto.id)
            this.rejectStage(projectDto.id, stageDto1.id)
            def stageDto2 = this.createStage(projectDto.id)
            def stageDto3 = this.createStage(projectDto.id)
            def taskDto21 = this.createTask(projectDto.id, stageDto2.id)
            this.updateTaskStatus(projectDto.id, stageDto2.id, taskDto21.id, TaskStatusDto.IN_PROGRESS)
            def taskDto31 = this.createTask(projectDto.id, stageDto3.id)
            this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto31.id, TaskStatusDto.IN_PROGRESS)
        when:
            def updateTaskStatusDto = this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto31.id, TaskStatusDto.TO_DO)
        then: "Response code is 400"
            updateTaskStatusDto.status == HttpStatus.OK.value()
        and:
            def getProjectResponse = this.getProjectResponse(projectDto.id)
            this.getProjectStatus(getProjectResponse) == ProjectStatusDto.IN_PROGRESS
    }

    @Transactional
    def "Changing stage status from IN_PROGRESS to DONE on project with other IN_PROGRESS stages does not change project status"() {
        given: "Existing project"
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageDto1 = this.createStage(projectDto.id)
            def stageDto2 = this.createStage(projectDto.id)
            def stageDto3 = this.createStage(projectDto.id)
            def taskDto21 = this.createTask(projectDto.id, stageDto2.id)
            this.updateTaskStatus(projectDto.id, stageDto2.id, taskDto21.id, TaskStatusDto.IN_PROGRESS)
            def taskDto31 = this.createTask(projectDto.id, stageDto3.id)
            this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto31.id, TaskStatusDto.IN_PROGRESS)
        expect: "In status IN_PROGRESS and at least one stage in status IN_PROGRESS"
            this.getStageStatus(projectDto.id, stageDto1.id) == StageStatusDto.TO_DO
            this.getStageStatus(projectDto.id, stageDto2.id) == StageStatusDto.IN_PROGRESS
            this.getStageStatus(projectDto.id, stageDto3.id) == StageStatusDto.IN_PROGRESS
            this.getProjectStatus(projectDto.id) == ProjectStatusDto.IN_PROGRESS
        when: "Changing stage status from IN_PROGRESS to DONE"
            def updateTaskStatusDto = this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto31.id, TaskStatusDto.DONE)
        then: "Returns code 200"
            updateTaskStatusDto.status == HttpStatus.OK.value()
        and: "And does not change project status"
            this.getProjectStatus(projectDto.id) == ProjectStatusDto.IN_PROGRESS
    }

    @Transactional
    def "Changing stage status from IN_PROGRESS to DONE on project with other stages in REJECTED changes project status to DONE"() {
        given: "Existing project"
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageDto1 = this.createStage(projectDto.id)
            def stageDto2 = this.createStage(projectDto.id)
            def stageDto3 = this.createStage(projectDto.id)
            this.createTask(projectDto.id, stageDto2.id)
            def taskDto31 = this.createTask(projectDto.id, stageDto3.id)
            this.rejectStage(projectDto.id, stageDto1.id)
            this.rejectStage(projectDto.id, stageDto2.id)
            this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto31.id, TaskStatusDto.IN_PROGRESS)
        expect: "In status IN_PROGRESS and one stage in IN_PROGRESS and other REJECTED"
            this.getStageStatus(projectDto.id, stageDto1.id) == StageStatusDto.REJECTED
            this.getStageStatus(projectDto.id, stageDto2.id) == StageStatusDto.REJECTED
            this.getStageStatus(projectDto.id, stageDto3.id) == StageStatusDto.IN_PROGRESS
            this.getProjectStatus(projectDto.id) == ProjectStatusDto.IN_PROGRESS
        when: "Changing stage status from IN_PROGRESS to DONE"
            def updateTaskStatusDto = this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto31.id, TaskStatusDto.DONE)
        then: "Returns code 200"
            updateTaskStatusDto.status == HttpStatus.OK.value()
        and: "And changes project status to DONE"
            this.getProjectStatus(projectDto.id) == ProjectStatusDto.DONE
    }

    @Transactional
    def "Changing stage status from IN_PROGRESS to DONE on project with other stages in DONE changes project status to DONE"() {
        given: "Existing project"
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageDto1 = this.createStage(projectDto.id)
            def stageDto2 = this.createStage(projectDto.id)
            def stageDto3 = this.createStage(projectDto.id)
            def taskDto11 = this.createTask(projectDto.id, stageDto1.id)
            def taskDto21 = this.createTask(projectDto.id, stageDto2.id)
            def taskDto31 = this.createTask(projectDto.id, stageDto3.id)
            this.updateTaskStatus(projectDto.id, stageDto1.id, taskDto11.id, TaskStatusDto.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto2.id, taskDto21.id, TaskStatusDto.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto31.id, TaskStatusDto.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto1.id, taskDto11.id, TaskStatusDto.DONE)
            this.updateTaskStatus(projectDto.id, stageDto2.id, taskDto21.id, TaskStatusDto.DONE)
        expect: "In status IN_PROGRESS and one stage in IN_PROGRESS and other DONE"
            this.getStageStatus(projectDto.id, stageDto1.id) == StageStatusDto.DONE
            this.getStageStatus(projectDto.id, stageDto2.id) == StageStatusDto.DONE
            this.getStageStatus(projectDto.id, stageDto3.id) == StageStatusDto.IN_PROGRESS
            this.getProjectStatus(projectDto.id) == ProjectStatusDto.IN_PROGRESS
        when: "Changing stage status from IN_PROGRESS to DONE"
            def updateTaskStatusDto = this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto31.id, TaskStatusDto.DONE)
        then: "Returns code 200"
            updateTaskStatusDto.status == HttpStatus.OK.value()
        and: "And changes project status to DONE"
            this.getProjectStatus(projectDto.id) == ProjectStatusDto.DONE
    }

    @Transactional
    def "Changing stage status from DONE to IN_PROGRESS on project in status in DONE changes project status to IN_PROGRESS"() {
        given: "Existing project"
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageDto1 = this.createStage(projectDto.id)
            def stageDto2 = this.createStage(projectDto.id)
            def stageDto3 = this.createStage(projectDto.id)
            def taskDto11 = this.createTask(projectDto.id, stageDto1.id)
            def taskDto21 = this.createTask(projectDto.id, stageDto2.id)
            def taskDto31 = this.createTask(projectDto.id, stageDto3.id)
            this.updateTaskStatus(projectDto.id, stageDto1.id, taskDto11.id, TaskStatusDto.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto2.id, taskDto21.id, TaskStatusDto.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto31.id, TaskStatusDto.IN_PROGRESS)
            this.rejectStage(projectDto.id, stageDto1.id)
            this.updateTaskStatus(projectDto.id, stageDto2.id, taskDto21.id, TaskStatusDto.DONE)
            this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto31.id, TaskStatusDto.DONE)
        expect: "In status DONE"
            this.getStageStatus(projectDto.id, stageDto1.id) == StageStatusDto.REJECTED
            this.getStageStatus(projectDto.id, stageDto2.id) == StageStatusDto.DONE
            this.getStageStatus(projectDto.id, stageDto3.id) == StageStatusDto.DONE
            this.getProjectStatus(projectDto.id) == ProjectStatusDto.DONE
        when: "Changing stage status from DONE to IN_PROGRESS"
            def updateTaskStatusDto =
                    this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto31.id, TaskStatusDto.IN_PROGRESS)
        then: "Returns code 200"
            updateTaskStatusDto.status == HttpStatus.OK.value()
        and: "And changes project status to IN_PROGRESS"
            this.getProjectStatus(projectDto.id) == ProjectStatusDto.IN_PROGRESS
    }

    @Transactional
    def "Changing stage status from DONE to IN_PROGRESS on project in status in IN_PROGRESS does not change project status"() {
        given: "Existing project"
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageDto1 = this.createStage(projectDto.id)
            def stageDto2 = this.createStage(projectDto.id)
            def stageDto3 = this.createStage(projectDto.id)
            def taskDto11 = this.createTask(projectDto.id, stageDto1.id)
            def taskDto21 = this.createTask(projectDto.id, stageDto2.id)
            def taskDto31 = this.createTask(projectDto.id, stageDto3.id)
            this.updateTaskStatus(projectDto.id, stageDto1.id, taskDto11.id, TaskStatusDto.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto2.id, taskDto21.id, TaskStatusDto.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto31.id, TaskStatusDto.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto2.id, taskDto21.id, TaskStatusDto.DONE)
            this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto31.id, TaskStatusDto.DONE)
        expect: "In status IN_PROGRESS"
            this.getStageStatus(projectDto.id, stageDto1.id) == StageStatusDto.IN_PROGRESS
            this.getStageStatus(projectDto.id, stageDto2.id) == StageStatusDto.DONE
            this.getStageStatus(projectDto.id, stageDto3.id) == StageStatusDto.DONE
            this.getProjectStatus(projectDto.id) == ProjectStatusDto.IN_PROGRESS
        when: "Changing stage status from DONE to IN_PROGRESS"
            def updateTaskStatusDto =
                    this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto31.id, TaskStatusDto.IN_PROGRESS)
        then: "Returns code 200"
            updateTaskStatusDto.status == HttpStatus.OK.value()
        and: "And does not change project status"
            this.getProjectStatus(projectDto.id) == ProjectStatusDto.IN_PROGRESS
    }

    @Transactional
    def "Rejecting stage from TO_DO on project in status TO_DO does not change project status"() {
        given: "Existing project"
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageDto1 = this.createStage(projectDto.id)
            def stageDto2 = this.createStage(projectDto.id)
            def stageDto3 = this.createStage(projectDto.id)
            this.createTask(projectDto.id, stageDto1.id)
            this.createTask(projectDto.id, stageDto2.id)
            this.createTask(projectDto.id, stageDto3.id)
            this.rejectStage(projectDto.id, stageDto1.id)
        expect: "In status TO_DO"
            this.getProjectStatus(projectDto.id) == ProjectStatusDto.TO_DO
        when: "Rejecting stage from TO_DO"
            def rejectStageDto = this.rejectStage(projectDto.id, stageDto3.id)
        then: "Returns code 200"
            rejectStageDto.status == HttpStatus.OK.value()
        and: "And does not change project status"
            this.getProjectStatus(projectDto.id) == ProjectStatusDto.TO_DO
    }

    @Transactional
    def "Rejecting stage from TO_DO on project in status IN_PROGRESS with other stages in DONE and REJECTED changes project status to DONE"() {
        given: "Existing project"
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageDto1 = this.createStage(projectDto.id)
            def stageDto2 = this.createStage(projectDto.id)
            def stageDto3 = this.createStage(projectDto.id)
            this.createTask(projectDto.id, stageDto1.id)
            def taskDto21 = this.createTask(projectDto.id, stageDto2.id)
            this.createTask(projectDto.id, stageDto3.id)
            this.rejectStage(projectDto.id, stageDto1.id)
            this.updateTaskStatus(projectDto.id, stageDto2.id, taskDto21.id, TaskStatusDto.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto2.id, taskDto21.id, TaskStatusDto.DONE)
        expect: "In status IN_PROGRESS with stages in REJECTED and DONE"
            this.getStageStatus(projectDto.id, stageDto1.id) == StageStatusDto.REJECTED
            this.getStageStatus(projectDto.id, stageDto2.id) == StageStatusDto.DONE
            this.getStageStatus(projectDto.id, stageDto3.id) == StageStatusDto.TO_DO
            this.getProjectStatus(projectDto.id) == ProjectStatusDto.IN_PROGRESS
        when: "Rejecting stage from TO_DO"
            def rejectStageDto = this.rejectStage(projectDto.id, stageDto3.id)
        then: "Returns code 200"
            rejectStageDto.status == HttpStatus.OK.value()
        and: "And does not change project status"
            this.getProjectStatus(projectDto.id) == ProjectStatusDto.DONE
    }

    @Transactional
    def "Rejecting stage from TO_DO on project in status IN_PROGRESS with at least one stage in IN_PROGRESS does not change project status"() {
        given: "Existing project"
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageDto1 = this.createStage(projectDto.id)
            def stageDto2 = this.createStage(projectDto.id)
            def stageDto3 = this.createStage(projectDto.id)
            this.createTask(projectDto.id, stageDto1.id)
            def taskDto21 = this.createTask(projectDto.id, stageDto2.id)
            this.createTask(projectDto.id, stageDto3.id)
            this.rejectStage(projectDto.id, stageDto1.id)
            this.updateTaskStatus(projectDto.id, stageDto2.id, taskDto21.id, TaskStatusDto.IN_PROGRESS)
        expect: "In status IN_PROGRESS with at least one stage in IN_PROGRESS"
            this.getStageStatus(projectDto.id, stageDto1.id) == StageStatusDto.REJECTED
            this.getStageStatus(projectDto.id, stageDto2.id) == StageStatusDto.IN_PROGRESS
            this.getStageStatus(projectDto.id, stageDto3.id) == StageStatusDto.TO_DO
            this.getProjectStatus(projectDto.id) == ProjectStatusDto.IN_PROGRESS
        when: "Rejecting stage from TO_DO"
            def rejectStageDto = this.rejectStage(projectDto.id, stageDto3.id)
        then: "Returns code 200"
            rejectStageDto.status == HttpStatus.OK.value()
        and: "And does not change project status"
            this.getProjectStatus(projectDto.id) == ProjectStatusDto.IN_PROGRESS
    }

    @Transactional
    def "Rejecting the only stage from status IN_PROGRESS on project in IN_PROGRESS status changes project status to TO_DO"() {
        given: "Existing project"
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageDto1 = this.createStage(projectDto.id)
            def taskDto11 = this.createTask(projectDto.id, stageDto1.id)
            this.updateTaskStatus(projectDto.id, stageDto1.id, taskDto11.id, TaskStatusDto.IN_PROGRESS)
        expect: "In status IN_PROGRESS with one stage"
            this.getStageStatus(projectDto.id, stageDto1.id) == StageStatusDto.IN_PROGRESS
            this.getProjectStatus(projectDto.id) == ProjectStatusDto.IN_PROGRESS
        when: "Rejecting stage from IN_PROGRESS"
            def rejectStageDto = this.rejectStage(projectDto.id, stageDto1.id)
        then: "Returns code 200"
            rejectStageDto.status == HttpStatus.OK.value()
        and: "And changes project status to TO_DO"
            this.getProjectStatus(projectDto.id) == ProjectStatusDto.TO_DO
    }

    @Transactional
    def "Rejecting stage from status IN_PROGRESS on project in IN_PROGRESS status with other stages in REJECTED changes project status to TO_DO"() {
        given: "Existing project"
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageDto1 = this.createStage(projectDto.id)
            def stageDto2 = this.createStage(projectDto.id)
            def stageDto3 = this.createStage(projectDto.id)
            def taskDto31 = this.createTask(projectDto.id, stageDto3.id)
            this.rejectStage(projectDto.id, stageDto1.id)
            this.rejectStage(projectDto.id, stageDto2.id)
            this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto31.id, TaskStatusDto.IN_PROGRESS)
        expect: "In status IN_PROGRESS with other stages in REJECTED"
            this.getStageStatus(projectDto.id, stageDto1.id) == StageStatusDto.REJECTED
            this.getStageStatus(projectDto.id, stageDto2.id) == StageStatusDto.REJECTED
            this.getStageStatus(projectDto.id, stageDto3.id) == StageStatusDto.IN_PROGRESS
            this.getProjectStatus(projectDto.id) == ProjectStatusDto.IN_PROGRESS
        when: "Rejecting stage from IN_PROGRESS"
            def rejectStageDto = this.rejectStage(projectDto.id, stageDto3.id)
        then: "Returns code 200"
            rejectStageDto.status == HttpStatus.OK.value()
        and: "And changes project status to TO_DO"
            this.getProjectStatus(projectDto.id) == ProjectStatusDto.TO_DO
    }

    @Transactional
    def "Rejecting stage from status IN_PROGRESS on project in IN_PROGRESS status with other stages in TO_DO and REJECTED changes project status to TO_DO"() {
        given: "Existing project"
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageDto1 = this.createStage(projectDto.id)
            def stageDto2 = this.createStage(projectDto.id)
            def stageDto3 = this.createStage(projectDto.id)
            def taskDto31 = this.createTask(projectDto.id, stageDto3.id)
            this.rejectStage(projectDto.id, stageDto1.id)
            this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto31.id, TaskStatusDto.IN_PROGRESS)
        expect: "In status IN_PROGRESS with other stages in REJECTED and TO_DO"
            this.getStageStatus(projectDto.id, stageDto1.id) == StageStatusDto.REJECTED
            this.getStageStatus(projectDto.id, stageDto2.id) == StageStatusDto.TO_DO
            this.getStageStatus(projectDto.id, stageDto3.id) == StageStatusDto.IN_PROGRESS
            this.getProjectStatus(projectDto.id) == ProjectStatusDto.IN_PROGRESS
        when: "Rejecting stage from IN_PROGRESS"
            def rejectStageDto = this.rejectStage(projectDto.id, stageDto3.id)
        then: "Returns code 200"
            rejectStageDto.status == HttpStatus.OK.value()
        and: "And changes project status to TO_DO"
            this.getProjectStatus(projectDto.id) == ProjectStatusDto.TO_DO
    }

    @Transactional
    def "Rejecting stage from status IN_PROGRESS on project in IN_PROGRESS status with other stages in DONE and REJECTED changes project status to DONE"() {
        given: "Existing project"
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageDto1 = this.createStage(projectDto.id)
            def stageDto2 = this.createStage(projectDto.id)
            def stageDto3 = this.createStage(projectDto.id)
            def taskDto21 = this.createTask(projectDto.id, stageDto2.id)
            def taskDto31 = this.createTask(projectDto.id, stageDto3.id)
            this.rejectStage(projectDto.id, stageDto1.id)
            this.updateTaskStatus(projectDto.id, stageDto2.id, taskDto21.id, TaskStatusDto.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto2.id, taskDto21.id, TaskStatusDto.DONE)
            this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto31.id, TaskStatusDto.IN_PROGRESS)
        expect: "In status IN_PROGRESS with other stages in REJECTED and TO_DO"
            this.getStageStatus(projectDto.id, stageDto1.id) == StageStatusDto.REJECTED
            this.getStageStatus(projectDto.id, stageDto2.id) == StageStatusDto.DONE
            this.getStageStatus(projectDto.id, stageDto3.id) == StageStatusDto.IN_PROGRESS
            this.getProjectStatus(projectDto.id) == ProjectStatusDto.IN_PROGRESS
        when: "Rejecting stage from IN_PROGRESS"
            def rejectStageDto = this.rejectStage(projectDto.id, stageDto3.id)
        then: "Returns code 200"
            rejectStageDto.status == HttpStatus.OK.value()
        and: "And changes project status to DONE"
            this.getProjectStatus(projectDto.id) == ProjectStatusDto.DONE
    }

    @Transactional
    def "Rejecting stage from status IN_PROGRESS on project in IN_PROGRESS status with at least one stage in IN_PROGRESS does not change project status"() {
        given: "Existing project"
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageDto1 = this.createStage(projectDto.id)
            def stageDto2 = this.createStage(projectDto.id)
            def stageDto3 = this.createStage(projectDto.id)
            def taskDto21 = this.createTask(projectDto.id, stageDto2.id)
            def taskDto31 = this.createTask(projectDto.id, stageDto3.id)
            this.rejectStage(projectDto.id, stageDto1.id)
            this.updateTaskStatus(projectDto.id, stageDto2.id, taskDto21.id, TaskStatusDto.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto31.id, TaskStatusDto.IN_PROGRESS)
        expect: "In status IN_PROGRESS with at least on other stage in IN_PROGRESS"
            this.getStageStatus(projectDto.id, stageDto1.id) == StageStatusDto.REJECTED
            this.getStageStatus(projectDto.id, stageDto2.id) == StageStatusDto.IN_PROGRESS
            this.getStageStatus(projectDto.id, stageDto3.id) == StageStatusDto.IN_PROGRESS
            this.getProjectStatus(projectDto.id) == ProjectStatusDto.IN_PROGRESS
        when: "Rejecting stage from IN_PROGRESS"
            def rejectStageDto = this.rejectStage(projectDto.id, stageDto3.id)
        then: "Returns code 200"
            rejectStageDto.status == HttpStatus.OK.value()
        and: "Does not change project status"
            this.getProjectStatus(projectDto.id) == ProjectStatusDto.IN_PROGRESS
    }

    @Transactional
    def "Reopening stage with tasks only in TO_DO and REJECTED status reopens stage to TO_DO status"() {
        given: "Rejected stage with tasks only in TO_DO and REJECTED statuses"
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageDto = this.createStage(projectDto.id)
            def taskDto11 = this.createTask(projectDto.id, stageDto.id)
            this.createTask(projectDto.id, stageDto.id)
            this.createTask(projectDto.id, stageDto.id)
            this.rejectTask(projectDto.id, stageDto.id, taskDto11.id)
            this.rejectStage(projectDto.id, stageDto.id)
        expect: "Stage is in REJECTED status"
            this.getStageStatus(projectDto.id, stageDto.id) == StageStatusDto.REJECTED
        when: "Reopening stage"
            def stageReopenResponse = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(this.createStageUri(projectDto.id, stageDto.id) + "/reopen"))).andReturn().response
        then: "Response code is 200"
            stageReopenResponse.getStatus() == HttpStatus.OK.value()
        and: "Changes stage status to TO_DO"
            this.getStageStatus(stageReopenResponse) == StageStatusDto.TO_DO
    }

    @Transactional
    def "Reopening stage with at least one task in IN_PROGRESS changes stage status to IN_PROGRESS"() {
        given: "Rejected stage at least one task in IN_PROGRESS status"
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageDto = this.createStage(projectDto.id)
            def taskDto11 = this.createTask(projectDto.id, stageDto.id)
            def taskDto12 = this.createTask(projectDto.id, stageDto.id)
            this.createTask(projectDto.id, stageDto.id)
            this.rejectTask(projectDto.id, stageDto.id, taskDto11.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatusDto.IN_PROGRESS)
            this.rejectStage(projectDto.id, stageDto.id)
        expect: "Stage is in REJECTED status"
            this.getStageStatus(projectDto.id, stageDto.id) == StageStatusDto.REJECTED
        when: "Reopening stage"
            def stageReopenResponse = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(this.createStageUri(projectDto.id, stageDto.id) + "/reopen"))).andReturn().response
        then: "Response code is 200"
            stageReopenResponse.getStatus() == HttpStatus.OK.value()
        and: "Changes stage status to IN_PROGRESS"
            this.getStageStatus(stageReopenResponse) == StageStatusDto.IN_PROGRESS
    }

    @Transactional
    def "Reopening stage with at least one task in DONE changes stage status to IN_PROGRESS"() {
        given: "Rejected stage at least one task in DONE status"
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageDto = this.createStage(projectDto.id)
            def taskDto11 = this.createTask(projectDto.id, stageDto.id)
            def taskDto12 = this.createTask(projectDto.id, stageDto.id)
            this.createTask(projectDto.id, stageDto.id)
            this.rejectTask(projectDto.id, stageDto.id, taskDto11.id)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatusDto.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto.id, taskDto12.id, TaskStatusDto.DONE)
            this.rejectStage(projectDto.id, stageDto.id)
        expect: "Stage is in REJECTED status"
            this.getStageStatus(projectDto.id, stageDto.id) == StageStatusDto.REJECTED
        when: "Reopening stage"
            def stageReopenResponse = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(this.createStageUri(projectDto.id, stageDto.id) + "/reopen"))).andReturn().response
        then: "Response code is 200"
            stageReopenResponse.getStatus() == HttpStatus.OK.value()
        and: "Changes stage status to IN_PROGRESS"
            this.getStageStatus(stageReopenResponse) == StageStatusDto.IN_PROGRESS
    }

    @Transactional
    def "Reopening stage to TO_DO on project in DONE status changes project status to IN_PROGRESS"() {
        given: "Existing project with stages in DONE and REJECTED"
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageDto1 = this.createStage(projectDto.id)
            def stageDto2 = this.createStage(projectDto.id)
            def stageDto3 = this.createStage(projectDto.id)
            def taskDto11 = this.createTask(projectDto.id, stageDto1.id)
            this.createTask(projectDto.id, stageDto2.id)
            this.createTask(projectDto.id, stageDto3.id)
            this.updateTaskStatus(projectDto.id, stageDto1.id, taskDto11.id, TaskStatusDto.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto1.id, taskDto11.id, TaskStatusDto.DONE)
            this.rejectStage(projectDto.id, stageDto2.id)
            this.rejectStage(projectDto.id, stageDto3.id)
        expect: "Project is in DONE "
            this.getProjectStatus(projectDto.id) == ProjectStatusDto.DONE
        when: "Reopening stage to TO_DO"
            def stageReopenResponse = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(this.createStageUri(projectDto.id, stageDto3.id) + "/reopen"))).andReturn().response
        then: "Response code is 200"
            stageReopenResponse.getStatus() == HttpStatus.OK.value()
        and: "Changes project status to IN_PROGRESS"
            this.getProjectStatus(projectDto.id) == ProjectStatusDto.IN_PROGRESS
    }

    @Transactional
    def "Reopening stage to IN_PROGRESS on project in TO_DO status changes project status to IN_PROGRESS"() {
        given: "Existing project with stages in DONE and REJECTED"
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageDto1 = this.createStage(projectDto.id)
            def stageDto2 = this.createStage(projectDto.id)
            def stageDto3 = this.createStage(projectDto.id)
            def taskDto11 = this.createTask(projectDto.id, stageDto1.id)
            this.createTask(projectDto.id, stageDto2.id)
            this.createTask(projectDto.id, stageDto3.id)
            this.updateTaskStatus(projectDto.id, stageDto1.id, taskDto11.id, TaskStatusDto.IN_PROGRESS)
            this.rejectStage(projectDto.id, stageDto1.id)
            this.rejectStage(projectDto.id, stageDto2.id)
        expect: "Project is in TO_DO "
            this.getProjectStatus(projectDto.id) == ProjectStatusDto.TO_DO
        when: "Reopening stage to IN_PROGRESS"
            def stageReopenResponse = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(this.createStageUri(projectDto.id, stageDto1.id) + "/reopen"))).andReturn().response
        then: "Response code is 200"
            stageReopenResponse.getStatus() == HttpStatus.OK.value()
        and: "Changes project status to IN_PROGRESS"
            this.getProjectStatus(projectDto.id) == ProjectStatusDto.IN_PROGRESS
    }

    @Transactional
    def "Reopening stage to IN_PROGRESS on project in DONE status changes project status to IN_PROGRESS"() {
        given: "Existing project with stages in DONE and REJECTED"
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageDto1 = this.createStage(projectDto.id)
            def stageDto2 = this.createStage(projectDto.id)
            def stageDto3 = this.createStage(projectDto.id)
            def taskDto11 = this.createTask(projectDto.id, stageDto1.id)
            this.createTask(projectDto.id, stageDto2.id)
            def taskDto13 = this.createTask(projectDto.id, stageDto3.id)
            this.updateTaskStatus(projectDto.id, stageDto1.id, taskDto11.id, TaskStatusDto.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto13.id, TaskStatusDto.IN_PROGRESS)
            this.updateTaskStatus(projectDto.id, stageDto3.id, taskDto13.id, TaskStatusDto.DONE)
            this.rejectStage(projectDto.id, stageDto1.id)
            this.rejectStage(projectDto.id, stageDto2.id)
        expect: "Project is in DONE "
            this.getProjectStatus(projectDto.id) == ProjectStatusDto.DONE
        when: "Reopening stage to IN_PROGRESS"
            def stageReopenResponse = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(this.createStageUri(projectDto.id, stageDto3.id) + "/reopen"))).andReturn().response
        then: "Response code is 200"
            stageReopenResponse.getStatus() == HttpStatus.OK.value()
        and: "Changes project status to IN_PROGRESS"
            this.getProjectStatus(projectDto.id) == ProjectStatusDto.IN_PROGRESS
    }

    // HELPER METHODS

    private ProjectDto createProject() {
        def architectDto = TestsHelper.createArchitect(this.architect, this.createBasicArchitectUri(), this.mockMvc)
        properProjectDto.architectId = architectDto.id
        def clientDto = TestsHelper.createClient(this.privateClientDto, this.createBasicClientUri(), this.mockMvc)
        properProjectDto.clientId = clientDto.id
        return TestsHelper.createProject(this.properProjectDto, this.createBasicProjectUri(), this.mockMvc)
    }

    private StageDto createStage(long projectId) {
        def stageRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(stageDto)
        def stageResponse = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(this.createProjectUri(projectId) + "/stages"))
                .header("Content-Type", "application/json")
                .content(stageRequestBody)).andReturn().response
        return MAPPER.readValue(stageResponse.contentAsString, StageDto)
    }

    private TaskDto createTask(long projectId, long stageId) {
        def taskRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(taskDto)
        def taskResponse = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(this.createStageUri(projectId, stageId) + "/tasks"))
                .header("Content-Type", "application/json")
                .content(taskRequestBody)).andReturn().response
        return MAPPER.readValue(taskResponse.contentAsString, TaskDto)
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
        return this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(this.createContractUri(contractId) + "/accept-offer"))).andReturn().response
    }

    private MockHttpServletResponse rejectProject(long projectId) {
        return this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(this.createProjectUri(projectId) + "/reject"))).andReturn().response
    }

    private MockHttpServletResponse rejectStage(long projectId, long stageId) {
        return this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(this.createStageUri(projectId, stageId) + "/reject"))).andReturn().response
    }

    private MockHttpServletResponse rejectTask(long projectId, long stageId, long taskId) {
        return this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(this.createTaskUri(projectId, stageId, taskId) + "/reject"))).andReturn().response
    }

    private MockHttpServletResponse updateTaskStatus(long projectId, long stageId, long taskId, TaskStatusDto status) {
        def updateStatusDto = new TaskDto(status: status)
        def requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(updateStatusDto)
        return this.mockMvc.perform(MockMvcRequestBuilders
                .post(URI.create(this.createTaskUri(projectId, stageId, taskId) + "/status"))
                .header("Content-Type", "application/json")
                .content(requestBody)).andReturn().response
    }

    private MockHttpServletResponse getTaskResponse(long projectId, long stageId, long taskId) {
        return this.mockMvc.perform(MockMvcRequestBuilders.get(URI.create(this.createTaskUri(projectId, stageId, taskId)))).andReturn().response
    }

    private MockHttpServletResponse getStageResponse(long projectId, long stageId) {
        return this.mockMvc.perform(MockMvcRequestBuilders.get(URI.create(this.createStageUri(projectId, stageId)))).andReturn().response
    }

    private MockHttpServletResponse getProjectResponse(long projectId) {
        return this.mockMvc.perform(MockMvcRequestBuilders.get(URI.create(this.createProjectUri(projectId)))).andReturn().response
    }

    private TaskStatusDto getTaskStatus(MockHttpServletResponse getTaskResponse) {
        def getTaskDto = MAPPER.readValue(getTaskResponse.contentAsString, TaskDto)
        return getTaskDto.status
    }

    private StageStatusDto getStageStatus(MockHttpServletResponse getStageResponse) {
        def getStageDto = MAPPER.readValue(getStageResponse.contentAsString, StageDto)
        return getStageDto.status
    }

    private StageStatusDto getStageStatus(long projectId, long stageId) {
        def getStageResponse =
                this.mockMvc.perform(MockMvcRequestBuilders.get(URI.create(this.createStageUri(projectId, stageId)))).andReturn().response
        def getStageDto = MAPPER.readValue(getStageResponse.contentAsString, StageDto)
        return getStageDto.status
    }

    private ProjectStatusDto getProjectStatus(MockHttpServletResponse getProjectResponse) {
        def getProjectDto = MAPPER.readValue(getProjectResponse.contentAsString, ProjectDto)
        return getProjectDto.status
    }

    private ProjectStatusDto getProjectStatus(long projectId) {
        def getProjectResponse =
                this.mockMvc.perform(MockMvcRequestBuilders.get(URI.create(this.createProjectUri(projectId)))).andReturn().response
        def getProjectDto = MAPPER.readValue(getProjectResponse.contentAsString, ProjectDto)
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
