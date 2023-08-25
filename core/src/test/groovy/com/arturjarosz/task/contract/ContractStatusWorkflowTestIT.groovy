package com.arturjarosz.task.contract

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

import java.time.LocalDate

class ContractStatusWorkflowTestIT extends BaseTestIT {
    static final String ARCHITECTS_URI = "/architects"
    static final String CLIENTS_URI = "/clients"
    static final String CONTRACTS_URI = "/contracts"
    static final String PROJECTS_URI = "/projects"
    static final ObjectMapper MAPPER = new ObjectMapper()
    static final Double NEW_OFFER = 55000.00d
    static final LocalDate END_DATE = LocalDate.of(2022, 10, 10)

    final ArchitectDto architect = MAPPER.readValue(new File(getClass().classLoader.getResource('json/architect/architect.json').file),
            ArchitectDto)
    final ClientDto privateClientDto = MAPPER.readValue(new File(getClass().classLoader.getResource('json/client/privateClient.json').file),
            ClientDto)
    final ContractDto signContractDto = MAPPER.readValue(new File(getClass().classLoader.getResource('json/contract/signContract.json').file),
            ContractDto)
    final ContractDto offerContractDto = MAPPER.readValue(new File(getClass().classLoader.getResource('json/contract/offer.json').file),
            ContractDto)
    final ProjectCreateDto properProjectDto = MAPPER.readValue(new File(getClass().classLoader.getResource('json/project/properProject.json').file),
            ProjectCreateDto)
    final StageDto stageDto = MAPPER.readValue(new File(getClass().classLoader.getResource('json/stage/properStage.json').file),
            StageDto)
    final TaskDto taskDto = MAPPER.readValue(new File(getClass().classLoader.getResource('json/task/properTask.json').file),
            TaskDto)

    @Autowired
    private MockMvc mockMvc

    @Override
    def setupSpec() {
        MAPPER.findAndRegisterModules()
    }

    @Transactional
    def "Newly created project contract is in OFFER status"() {
        given:
            def architectDto = TestsHelper.createArchitect(this.architect, this.createBasicArchitectUri(), this.mockMvc)
            properProjectDto.architectId = architectDto.id
            def clientDto = TestsHelper.createClient(this.privateClientDto, this.createBasicClientUri(), this.mockMvc)
            properProjectDto.clientId = clientDto.id
            def projectRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this.properProjectDto)
        when:
            def projectResponse = mockMvc.perform(MockMvcRequestBuilders.post(URI.create(this.createBasicProjectUri()))
                    .header("Content-Type", "application/json")
                    .content(projectRequestBody)).andReturn().response
        then:
            projectResponse.status == HttpStatus.CREATED.value()
        and:
            def projectDto = MAPPER.readValue(projectResponse.contentAsString, ProjectDto)
            projectDto.contract.status == ContractStatusDto.OFFER
    }

    @Transactional
    def "Contract in status OFFER allows creating project work objects"() {
        given:
            def projectDto = this.createProject()
            def stageRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(stageDto)
        when:
            def stageResponse = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(this.createProjectUri(projectDto.id) + "/stages"))
                    .header("Content-Type", "application/json")
                    .content(stageRequestBody)).andReturn().response
        then:
            stageResponse.status == HttpStatus.CREATED.value()
    }

    @Transactional
    def "Contract in status OFFER does not allow for working on the project"() {
        given:
            def projectDto = this.createProject()
            def createdStageDto = this.createStage(projectDto.id)
            def createdTaskDto = this.createTask(projectDto.id, createdStageDto.id)
        when:
            def updateTaskResponse =
                    this.updateTaskStatus(projectDto.id, createdStageDto.id, createdTaskDto.id, TaskStatusDto.IN_PROGRESS)
        then:
            updateTaskResponse.status == HttpStatus.BAD_REQUEST.value()
            def message = MAPPER.readValue(updateTaskResponse.contentAsString, ErrorMessage)
            message.message == "Contract in status OFFER does not allow for work on the project."
    }

    @Transactional
    def "Accepting offer changes contract status to OFFER"() {
        given:
            def projectDto = this.createProject()
        when:
            def updateContractResponse = this.mockMvc.perform(MockMvcRequestBuilders
                    .post(this.createContractUri(projectDto.contract.id) + "/accept-offer")
                    .header("Content-Type", "application/json")

            ).andReturn().response
        then:
            updateContractResponse.status == HttpStatus.OK.value()
            def contractDto = MAPPER.readValue(updateContractResponse.contentAsString, ContractDto)
            contractDto.status == ContractStatusDto.ACCEPTED
            contractDto.id != null
    }

    @Transactional
    def "Contract in ACCEPTED status allows creating project work objects"() {
        given:
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def stageRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(stageDto)
        when:
            def stageResponse = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(this.createProjectUri(projectDto.id) + "/stages"))
                    .header("Content-Type", "application/json")
                    .content(stageRequestBody)).andReturn().response
        then:
            stageResponse.status == HttpStatus.CREATED.value()
    }

    @Transactional
    def "Contract in ACCEPTED status allows working on project"() {
        given:
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def createdStageDto = this.createStage(projectDto.id)
            def createdTaskDto = this.createTask(projectDto.id, createdStageDto.id)
        when:
            def updateTaskResponse =
                    this.updateTaskStatus(projectDto.id, createdStageDto.id, createdTaskDto.id, TaskStatusDto.IN_PROGRESS)
        then:
            updateTaskResponse.status == HttpStatus.OK.value()
            def updateTaskDto = MAPPER.readValue(updateTaskResponse.contentAsString, TaskDto)
            updateTaskDto.status == TaskStatusDto.IN_PROGRESS
    }

    @Transactional
    def "Rejecting offer changes status of contract to REJECTED"() {
        given:
            def projectDto = this.createProject()
        when:
            def updateContractResponse = this.mockMvc.perform(MockMvcRequestBuilders
                    .post(this.createContractUri(projectDto.contract.id) + "/reject")
                    .header("Content-Type", "application/json")

            ).andReturn().response
        then:
            updateContractResponse.status == HttpStatus.OK.value()
            def contractDto = MAPPER.readValue(updateContractResponse.contentAsString, ContractDto)
            contractDto.status == ContractStatusDto.REJECTED
            contractDto.id != null
    }

    @Transactional
    def "Contract in REJECTED status does not allow for creating new work objects"() {
        given:
            def projectDto = this.createProject()
            this.rejectContract(projectDto.contract.id)
            def stageRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(stageDto)
        when:
            def stageResponse = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(this.createProjectUri(projectDto.id) + "/stages"))
                    .header("Content-Type", "application/json")
                    .content(stageRequestBody)).andReturn().response
        then:
            stageResponse.status == HttpStatus.BAD_REQUEST.value()
            def message = MAPPER.readValue(stageResponse.contentAsString, ErrorMessage)
            message.message == "Contract in status REJECTED does not allow for creating new working objects."
    }

    @Transactional
    def "Contract in REJECTED status does not allow for working on the project"() {
        given:
            def projectDto = this.createProject()
            def createdStageDto = this.createStage(projectDto.id)
            def createdTaskDto = this.createTask(projectDto.id, createdStageDto.id)
            this.rejectContract(projectDto.contract.id)
        when:
            def updateTaskResponse =
                    this.updateTaskStatus(projectDto.id, createdStageDto.id, createdTaskDto.id, TaskStatusDto.IN_PROGRESS)
        then:
            updateTaskResponse.status == HttpStatus.BAD_REQUEST.value()
            def message = MAPPER.readValue(updateTaskResponse.contentAsString, ErrorMessage)
            message.message == "Contract in status REJECTED does not allow for work on the project."
    }

    @Transactional
    def "Contract in REJECTED status cannot be signed"() {
        given:
            def projectDto = this.createProject()
            this.rejectContract(projectDto.contract.id)
            def signContractText = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this.signContractDto)
        when:
            def signContractResponse = this.mockMvc.perform(MockMvcRequestBuilders.post(this.createContractUri(projectDto.contract.id) + "/sign")
                    .header("Content-Type", "application/json")
                    .content(signContractText)).andReturn().response
        then:
            signContractResponse.status == HttpStatus.BAD_REQUEST.value()
            def message = MAPPER.readValue(signContractResponse.contentAsString, ErrorMessage)
            message.message == "It is not possible to make contract status transition from REJECTED to SIGNED."
    }

    @Transactional
    def "Making new offer on contract in REJECTED status changes this contract status to OFFER and updates offer value"() {
        given:
            def projectDto = this.createProject()
            this.rejectContract(projectDto.contract.id)
            this.offerContractDto.setOfferValue(NEW_OFFER)
            def offerRequestText = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this.offerContractDto)
        when:
            def newOfferContractResponse = this.mockMvc.perform(MockMvcRequestBuilders
                    .post(this.createContractUri(projectDto.contract.id) + "/new-offer")
                    .header("Content-Type", "application/json")
                    .content(offerRequestText)).andReturn().response
        then:
            newOfferContractResponse.status == HttpStatus.OK.value()
            def contractDto = MAPPER.readValue(newOfferContractResponse.contentAsString, ContractDto)
            contractDto.status == ContractStatusDto.OFFER
            contractDto.offerValue == NEW_OFFER
            contractDto.id != null
    }

    @Transactional
    def "Making new offer from contract in status OFFER is not possible and returns error message"() {
        given:
            def projectDto = this.createProject()
            def offerRequestText = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this.offerContractDto)
        when:
            def newOfferContractResponse = this.mockMvc.perform(MockMvcRequestBuilders
                    .post(this.createContractUri(projectDto.contract.id) + "/new-offer")
                    .header("Content-Type", "application/json")
                    .content(offerRequestText)).andReturn().response
        then:
            newOfferContractResponse.status == HttpStatus.BAD_REQUEST.value()
            def message = MAPPER.readValue(newOfferContractResponse.contentAsString, ErrorMessage)
            message.message == "It is not possible to make contract status transition from OFFER to OFFER."
    }

    @Transactional
    def "Making new offer from contract in status ACCEPTED is not possible and returns error message"() {
        given:
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def offerRequestText = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this.offerContractDto)
        when:
            def newOfferContractResponse = this.mockMvc.perform(MockMvcRequestBuilders
                    .post(this.createContractUri(projectDto.contract.id) + "/new-offer")
                    .header("Content-Type", "application/json")
                    .content(offerRequestText)).andReturn().response
        then:
            newOfferContractResponse.status == HttpStatus.BAD_REQUEST.value()
            def message = MAPPER.readValue(newOfferContractResponse.contentAsString, ErrorMessage)
            message.message == "It is not possible to make contract status transition from ACCEPTED to OFFER."
    }

    @Transactional
    def "Making new offer from contract in status SIGNED is not possible and returns error message"() {
        given:
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def offerRequestText = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this.offerContractDto)
            def signContractText = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this.signContractDto)
            this.signContract(projectDto.contract.id, signContractText)
        when:
            def newOfferContractResponse = this.mockMvc.perform(MockMvcRequestBuilders
                    .post(this.createContractUri(projectDto.contract.id) + "/new-offer")
                    .header("Content-Type", "application/json")
                    .content(offerRequestText)).andReturn().response
        then:
            newOfferContractResponse.status == HttpStatus.BAD_REQUEST.value()
            def message = MAPPER.readValue(newOfferContractResponse.contentAsString, ErrorMessage)
            message.message == "It is not possible to make contract status transition from SIGNED to OFFER."
    }

    @Transactional
    def "Signing contract in status OFFER is not possible and returns error message"() {
        given:
            def projectDto = this.createProject()
            def signContractText = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this.signContractDto)
        when:
            def signContractResponse = this.mockMvc.perform(MockMvcRequestBuilders.post(this.createContractUri(projectDto.contract.id) + "/sign")
                    .header("Content-Type", "application/json")
                    .content(signContractText)).andReturn().response
        then:
            signContractResponse.status == HttpStatus.BAD_REQUEST.value()
            def message = MAPPER.readValue(signContractResponse.contentAsString, ErrorMessage)
            message.message == "It is not possible to make contract status transition from OFFER to SIGNED."
    }

    @Transactional
    def "Signing contract in status ACCEPTED changes contract status to SIGNED"() {
        given:
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def signContractText = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this.signContractDto)
        when:
            def signContractResponse = this.mockMvc.perform(MockMvcRequestBuilders.post(this.createContractUri(projectDto.contract.id) + "/sign")
                    .header("Content-Type", "application/json")
                    .content(signContractText)).andReturn().response
        then:
            signContractResponse.status == HttpStatus.OK.value()
            def contractDto = MAPPER.readValue(signContractResponse.contentAsString, ContractDto)
            contractDto.status == ContractStatusDto.SIGNED
            contractDto.id != null
    }

    @Transactional
    def "Contract in SIGNED status allows for creating work objects"() {
        given:
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def signContractText = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this.signContractDto)
            this.signContract(projectDto.contract.id, signContractText)
            def stageRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(stageDto)
        when:
            def stageResponse = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(this.createProjectUri(projectDto.id) + "/stages"))
                    .header("Content-Type", "application/json")
                    .content(stageRequestBody)).andReturn().response
        then:
            stageResponse.status == HttpStatus.CREATED.value()
    }

    @Transactional
    def "Contract in SIGNED status allows for working on project"() {
        given:
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def signContractText = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this.signContractDto)
            this.signContract(projectDto.contract.id, signContractText)
            def createdStageDto = this.createStage(projectDto.id)
            def createdTaskDto = this.createTask(projectDto.id, createdStageDto.id)
        when:
            def updateTaskResponse =
                    this.updateTaskStatus(projectDto.id, createdStageDto.id, createdTaskDto.id, TaskStatusDto.IN_PROGRESS)
        then:
            updateTaskResponse.status == HttpStatus.OK.value()
            def updateTaskDto = MAPPER.readValue(updateTaskResponse.contentAsString, TaskDto)
            updateTaskDto.status == TaskStatusDto.IN_PROGRESS
    }

    @Transactional
    def "Terminating singed contract changes this contract status to TERMINATED"() {
        given:
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def signContractText = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this.signContractDto)
            this.signContract(projectDto.contract.id, signContractText)
            def terminateContractDto = new ContractDto(endDate: END_DATE)
            def terminateRequest = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(terminateContractDto)
        when:
            def terminateContractResponse = this.mockMvc.perform(MockMvcRequestBuilders.post(this.createContractUri(projectDto.contract.id) + "/terminate")
                    .header("Content-Type", "application/json")
                    .content(terminateRequest)).andReturn().response
        then:
            terminateContractResponse.status == HttpStatus.OK.value()
            def contractDto = MAPPER.readValue(terminateContractResponse.contentAsString, ContractDto)
            contractDto.status == ContractStatusDto.TERMINATED
            contractDto.id != null
    }

    @Transactional
    def "Contract in status TERMINATED does not allow for creating working objects"() {
        given:
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def signContractText = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this.signContractDto)
            this.signContract(projectDto.contract.id, signContractText)
            def terminateContractDto = new ContractDto(endDate: END_DATE)
            def terminateRequest = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(terminateContractDto)
            this.terminateContract(projectDto.contract.id, terminateRequest)
            def stageRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(stageDto)
        when:
            def stageResponse = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(this.createProjectUri(projectDto.id) + "/stages"))
                    .header("Content-Type", "application/json")
                    .content(stageRequestBody)).andReturn().response
        then:
            stageResponse.status == HttpStatus.BAD_REQUEST.value()
            def message = MAPPER.readValue(stageResponse.contentAsString, ErrorMessage)
            message.message == "Contract in status TERMINATED does not allow for creating new working objects."
    }

    @Transactional
    def "Contract in status TERMINATED does not allow for working on project"() {
        given:
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def signContractText = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this.signContractDto)
            this.signContract(projectDto.contract.id, signContractText)
            def terminateContractDto = new ContractDto(endDate: END_DATE)
            def terminateRequest = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(terminateContractDto)
            def createdStageDto = this.createStage(projectDto.id)
            def createdTaskDto = this.createTask(projectDto.id, createdStageDto.id)
            this.terminateContract(projectDto.contract.id, terminateRequest)
        when:
            def updateTaskResponse =
                    this.updateTaskStatus(projectDto.id, createdStageDto.id, createdTaskDto.id, TaskStatusDto.IN_PROGRESS)
        then:
            updateTaskResponse.status == HttpStatus.BAD_REQUEST.value()
            def message = MAPPER.readValue(updateTaskResponse.contentAsString, ErrorMessage)
            message.message == "Contract in status TERMINATED does not allow for work on the project."
    }

    @Transactional
    def "Resuming terminated contract changes its status to SIGNED"() {
        given:
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def signContractText = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this.signContractDto)
            this.signContract(projectDto.contract.id, signContractText)
            def terminateContractDto = new ContractDto(endDate: END_DATE)
            def terminateRequest = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(terminateContractDto)
            this.terminateContract(projectDto.contract.id, terminateRequest)
        when:
            def resumeContractResponse = this.mockMvc.perform(MockMvcRequestBuilders.post(this.createContractUri(projectDto.contract.id) + "/resume")
                    .header("Content-Type", "application/json")).andReturn().response
        then:
            resumeContractResponse.status == HttpStatus.OK.value()
            def contractDto = MAPPER.readValue(resumeContractResponse.contentAsString, ContractDto)
            contractDto.status == ContractStatusDto.SIGNED
    }

    @Transactional
    def "Completing signed contract changes its status to COMPLETED"() {
        given:
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def signContractText = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this.signContractDto)
            this.signContract(projectDto.contract.id, signContractText)
            def completeContractDto = new ContractDto(endDate: END_DATE)
            def completeRequest = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(completeContractDto)
        when:
            def completeContractResponse = this.mockMvc.perform(MockMvcRequestBuilders.post(this.createContractUri(projectDto.contract.id) + "/complete")
                    .header("Content-Type", "application/json")
                    .content(completeRequest)).andReturn().response
        then:
            completeContractResponse.status == HttpStatus.OK.value()
            def contractDto = MAPPER.readValue(completeContractResponse.contentAsString, ContractDto)
            contractDto.status == ContractStatusDto.COMPLETED
            contractDto.id != null
    }

    @Transactional
    def "Contract in status COMPLETED does not allow for creating work objects on project"() {
        given:
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def signContractText = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this.signContractDto)
            this.signContract(projectDto.contract.id, signContractText)
            def completeContractDto = new ContractDto(endDate: END_DATE)
            def completeRequest = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(completeContractDto)
            this.completeContract(projectDto.contract.id, completeRequest)
            def stageRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(stageDto)
        when:
            def stageResponse = this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(this.createProjectUri(projectDto.id) + "/stages"))
                    .header("Content-Type", "application/json")
                    .content(stageRequestBody)).andReturn().response
        then:
            stageResponse.status == HttpStatus.BAD_REQUEST.value()
            def message = MAPPER.readValue(stageResponse.contentAsString, ErrorMessage)
            message.message == "Contract in status COMPLETED does not allow for creating new working objects."
    }

    @Transactional
    def "Contract in status COMPLETED does not allow for working on project"() {
        given:
            def projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contract.id)
            def signContractText = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this.signContractDto)
            this.signContract(projectDto.contract.id, signContractText)
            def completeContractDto = new ContractDto(endDate: END_DATE)
            def completeRequest = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(completeContractDto)
            def createdStageDto = this.createStage(projectDto.id)
            def createdTaskDto = this.createTask(projectDto.id, createdStageDto.id)
            this.completeContract(projectDto.contract.id, completeRequest)
        when:
            def updateTaskResponse =
                    this.updateTaskStatus(projectDto.id, createdStageDto.id, createdTaskDto.id, TaskStatusDto.IN_PROGRESS)
        then:
            updateTaskResponse.status == HttpStatus.BAD_REQUEST.value()
            def message = MAPPER.readValue(updateTaskResponse.contentAsString, ErrorMessage)
            message.message == "Contract in status COMPLETED does not allow for work on the project."
    }

    // helper methods

    private ProjectDto createProject() {
        def architectDto =
                TestsHelper.createArchitect(this.architect, this.createBasicArchitectUri(), this.mockMvc)
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

    private MockHttpServletResponse updateTaskStatus(long projectId, long stageId, long taskId, TaskStatusDto status) {
        def updateStatusDto = new TaskDto(status: status)
        def requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(updateStatusDto)
        return this.mockMvc.perform(MockMvcRequestBuilders
                .post(URI.create(this.createTaskUri(projectId, stageId, taskId) + "/status"))
                .header("Content-Type", "application/json")
                .content(requestBody)).andReturn().response
    }

    private ContractDto acceptContractOffer(long contractId) {
        def acceptContractResponse = this.mockMvc.perform(MockMvcRequestBuilders
                .post(this.createContractUri(contractId) + "/accept-offer")
                .header("Content-Type", "application/json")

        ).andReturn().response
        return MAPPER.readValue(acceptContractResponse.contentAsString, ContractDto)
    }

    private ContractDto signContract(long contractId, String signContractDataText) {
        def signResponse = this.mockMvc.perform(MockMvcRequestBuilders.post(this.createContractUri(contractId) + "/sign")
                .header("Content-Type", "application/json")
                .content(signContractDataText)).andReturn().response
        return MAPPER.readValue(signResponse.contentAsString, ContractDto)
    }

    private ContractDto rejectContract(long contractId) {
        def rejectedContractResponse = this.mockMvc.perform(MockMvcRequestBuilders
                .post(this.createContractUri(contractId) + "/reject")
                .header("Content-Type", "application/json")

        ).andReturn().response
        return MAPPER.readValue(rejectedContractResponse.contentAsString, ContractDto)
    }

    private ContractDto terminateContract(long contractId, String requestText) {
        def terminateContractResponse = this.mockMvc.perform(MockMvcRequestBuilders.post(this.createContractUri(contractId) + "/terminate")
                .header("Content-Type", "application/json")
                .content(requestText)).andReturn().response
        return MAPPER.readValue(terminateContractResponse.contentAsString, ContractDto)
    }

    private ContractDto completeContract(long contractId, String requestText) {
        def completeContractResponse = this.mockMvc.perform(MockMvcRequestBuilders.post(this.createContractUri(contractId) + "/complete")
                .header("Content-Type", "application/json")
                .content(requestText)).andReturn().response
        return MAPPER.readValue(completeContractResponse.contentAsString, ContractDto)
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

    private String createProjectUri(long projectId) {
        HOST + ":" + port + PROJECTS_URI + "/" + projectId
    }

    private String createContractUri(long contractId) {
        HOST + ":" + port + CONTRACTS_URI + "/" + contractId
    }

    private String createStageUri(long projectId, long stageId) {
        this.createProjectUri(projectId) + "/stages/" + stageId
    }

    private String createTaskUri(long projectId, long stageId, long taskId) {
        this.createStageUri(projectId, stageId) + "/tasks/" + taskId
    }
}
