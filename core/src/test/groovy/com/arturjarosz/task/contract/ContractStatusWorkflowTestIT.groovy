package com.arturjarosz.task.contract

import com.arturjarosz.task.architect.application.dto.ArchitectBasicDto
import com.arturjarosz.task.architect.application.dto.ArchitectDto
import com.arturjarosz.task.client.application.dto.ClientDto
import com.arturjarosz.task.configuration.BaseTestIT
import com.arturjarosz.task.contract.application.dto.ContractDto
import com.arturjarosz.task.contract.status.ContractStatus
import com.arturjarosz.task.project.application.dto.ProjectCreateDto
import com.arturjarosz.task.project.application.dto.ProjectDto
import com.arturjarosz.task.project.application.dto.StageDto
import com.arturjarosz.task.project.application.dto.TaskDto
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

import java.time.LocalDate

class ContractStatusWorkflowTestIT extends BaseTestIT {
    private static final String ARCHITECTS_URI = "/architects"
    private static final String CLIENTS_URI = "/clients"
    private static final String CONTRACTS_URI = "/contracts"
    private static final String PROJECTS_URI = "/projects"
    private static final ObjectMapper MAPPER = new ObjectMapper()
    private static final Double NEW_OFFER = 55000.00d
    private static final LocalDate END_DATE = LocalDate.of(2022, 10, 10)

    private final ArchitectBasicDto architect =
            MAPPER.readValue(new File(getClass().classLoader.getResource('json/architect/architect.json').file),
                    ArchitectBasicDto.class)
    private final ClientDto privateClientDto =
            MAPPER.readValue(new File(getClass().classLoader.getResource('json/client/privateClient.json').file),
                    ClientDto.class)
    private final ContractDto signContractDto =
            MAPPER.readValue(new File(getClass().classLoader.getResource('json/contract/signContract.json').file),
                    ContractDto.class)
    private final ContractDto offerContractDto =
            MAPPER.readValue(new File(getClass().classLoader.getResource('json/contract/offer.json').file),
                    ContractDto.class)
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
    def "Newly created project contract is in OFFER status"() {
        given:
            ArchitectDto architectDto =
                    TestsHelper.createArchitect(this.architect, this.createBasicArchitectUri(), this.mockMvc)
            properProjectDto.architectId = architectDto.id
            ClientDto clientDto =
                    TestsHelper.createClient(this.privateClientDto, this.createBasicClientUri(), this.mockMvc)
            properProjectDto.clientId = clientDto.id
            String projectRequestBody =
                    MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this.properProjectDto)
        when:
            def projectResponse = mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.createBasicProjectUri()))
                            .header("Content-Type", "application/json")
                            .content(projectRequestBody)
            ).andReturn().response
        then:
            projectResponse.status == HttpStatus.CREATED.value()
        and:
            ProjectDto projectDto = MAPPER.readValue(projectResponse.contentAsString, ProjectDto.class)
            projectDto.contractDto.contractStatus == ContractStatus.OFFER
    }

    @Transactional
    def "Contract in status OFFER allows creating project work objects"() {
        given:
            ProjectDto projectDto = this.createProject()
            String stageRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(stageDto)
        when:
            def stageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.createProjectUri(projectDto.id) + "/stages"))
                            .header("Content-Type", "application/json")
                            .content(stageRequestBody)
            ).andReturn().response
        then:
            stageResponse.status == HttpStatus.CREATED.value()
    }

    @Transactional
    def "Contract in status OFFER does not allow for working on the project"() {
        given:
            ProjectDto projectDto = this.createProject()
            StageDto createdStageDto = this.createStage(projectDto.id)
            TaskDto createdTaskDto = this.createTask(projectDto.id, createdStageDto.id)
        when:
            def updateTaskResponse =
                    this.updateTaskStatus(projectDto.id, createdStageDto.id, createdTaskDto.id, TaskStatus.IN_PROGRESS)
        then:
            updateTaskResponse.status == HttpStatus.BAD_REQUEST.value()
            def message = MAPPER.readValue(updateTaskResponse.contentAsString, ErrorMessage.class)
            message.message == "Contract in status OFFER does not allow for work on the project."
    }

    @Transactional
    def "Accepting offer changes contract status to OFFER"() {
        given:
            ProjectDto projectDto = this.createProject()
        when:
            def updateContractResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .post(this.createContractUri(projectDto.contractDto.id) + "/acceptOffer")
                            .header("Content-Type", "application/json")

            ).andReturn().response
        then:
            updateContractResponse.status == HttpStatus.OK.value()
            ContractDto contractDto = MAPPER.readValue(updateContractResponse.contentAsString, ContractDto.class)
            contractDto.status == ContractStatus.ACCEPTED
    }

    @Transactional
    def "Contract in ACCEPTED status allows creating project work objects"() {
        given:
            ProjectDto projectDto = this.createProject()
            ContractDto contractDto = this.acceptContractOffer(projectDto.contractDto.id)
            String stageRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(stageDto)
        when:
            def stageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.createProjectUri(projectDto.id) + "/stages"))
                            .header("Content-Type", "application/json")
                            .content(stageRequestBody)
            ).andReturn().response
        then:
            stageResponse.status == HttpStatus.CREATED.value()
    }

    @Transactional
    def "Contract in ACCEPTED status allows working on project"() {
        given:
            ProjectDto projectDto = this.createProject()
            ContractDto contractDto = this.acceptContractOffer(projectDto.contractDto.id)
            StageDto createdStageDto = this.createStage(projectDto.id)
            TaskDto createdTaskDto = this.createTask(projectDto.id, createdStageDto.id)
        when:
            def updateTaskResponse =
                    this.updateTaskStatus(projectDto.id, createdStageDto.id, createdTaskDto.id, TaskStatus.IN_PROGRESS)
        then:
            updateTaskResponse.status == HttpStatus.OK.value()
            TaskDto updateTaskDto = MAPPER.readValue(updateTaskResponse.contentAsString, TaskDto.class)
            updateTaskDto.status == TaskStatus.IN_PROGRESS
    }

    @Transactional
    def "Rejecting offer changes status of contract to REJECTED"() {
        given:
            ProjectDto projectDto = this.createProject()
        when:
            def updateContractResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .post(this.createContractUri(projectDto.contractDto.id) + "/reject")
                            .header("Content-Type", "application/json")

            ).andReturn().response
        then:
            updateContractResponse.status == HttpStatus.OK.value()
            ContractDto contractDto = MAPPER.readValue(updateContractResponse.contentAsString, ContractDto.class)
            contractDto.status == ContractStatus.REJECTED
    }

    @Transactional
    def "Contract in REJECTED status does not allow for creating new work objects"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.rejectContract(projectDto.contractDto.id)
            String stageRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(stageDto)
        when:
            def stageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.createProjectUri(projectDto.id) + "/stages"))
                            .header("Content-Type", "application/json")
                            .content(stageRequestBody)
            ).andReturn().response
        then:
            stageResponse.status == HttpStatus.BAD_REQUEST.value()
            def message = MAPPER.readValue(stageResponse.contentAsString, ErrorMessage.class)
            message.message == "Contract in status REJECTED does not allow for creating new working objects."
    }

    @Transactional
    def "Contract in REJECTED status does not allow for working on the project"() {
        given:
            ProjectDto projectDto = this.createProject()
            StageDto createdStageDto = this.createStage(projectDto.id)
            TaskDto createdTaskDto = this.createTask(projectDto.id, createdStageDto.id)
            this.rejectContract(projectDto.contractDto.id)
        when:
            def updateTaskResponse =
                    this.updateTaskStatus(projectDto.id, createdStageDto.id, createdTaskDto.id, TaskStatus.IN_PROGRESS)
        then:
            updateTaskResponse.status == HttpStatus.BAD_REQUEST.value()
            def message = MAPPER.readValue(updateTaskResponse.contentAsString, ErrorMessage.class)
            message.message == "Contract in status REJECTED does not allow for work on the project."
    }

    @Transactional
    def "Contract in REJECTED status cannot be signed"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.rejectContract(projectDto.contractDto.id)
            String signContractText = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this.signContractDto)
        when:
            def signContractResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(this.createContractUri(projectDto.contractDto.id) + "/sign")
                            .header("Content-Type", "application/json")
                            .content(signContractText)
            ).andReturn().response
        then:
            signContractResponse.status == HttpStatus.BAD_REQUEST.value()
            def message = MAPPER.readValue(signContractResponse.contentAsString, ErrorMessage.class)
            message.message == "It is not possible to make contract status transition from REJECTED to SIGNED."
    }

    @Transactional
    def "Making new offer on contract in REJECTED status changes this contract status to OFFER and updates offer value"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.rejectContract(projectDto.contractDto.id)
            this.offerContractDto.setOfferValue(NEW_OFFER)
            String offerRequestText = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this.offerContractDto)
        when:
            def newOfferContractResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .post(this.createContractUri(projectDto.contractDto.id) + "/newOffer")
                            .header("Content-Type", "application/json")
                            .content(offerRequestText)
            ).andReturn().response
        then:
            newOfferContractResponse.status == HttpStatus.OK.value()
            ContractDto contractDto = MAPPER.readValue(newOfferContractResponse.contentAsString, ContractDto.class)
            contractDto.status == ContractStatus.OFFER
            contractDto.offerValue == NEW_OFFER
    }

    @Transactional
    def "Making new offer from contract in status OFFER is not possible and returns error message"() {
        given:
            ProjectDto projectDto = this.createProject()
            String offerRequestText = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this.offerContractDto)
        when:
            def newOfferContractResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .post(this.createContractUri(projectDto.contractDto.id) + "/newOffer")
                            .header("Content-Type", "application/json")
                            .content(offerRequestText)
            ).andReturn().response
        then:
            newOfferContractResponse.status == HttpStatus.BAD_REQUEST.value()
            def message = MAPPER.readValue(newOfferContractResponse.contentAsString, ErrorMessage.class)
            message.message == "It is not possible to make contract status transition from OFFER to OFFER."
    }

    @Transactional
    def "Making new offer from contract in status ACCEPTED is not possible and returns error message"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            String offerRequestText = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this.offerContractDto)
        when:
            def newOfferContractResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .post(this.createContractUri(projectDto.contractDto.id) + "/newOffer")
                            .header("Content-Type", "application/json")
                            .content(offerRequestText)
            ).andReturn().response
        then:
            newOfferContractResponse.status == HttpStatus.BAD_REQUEST.value()
            def message = MAPPER.readValue(newOfferContractResponse.contentAsString, ErrorMessage.class)
            message.message == "It is not possible to make contract status transition from ACCEPTED to OFFER."
    }

    @Transactional
    def "Making new offer from contract in status SIGNED is not possible and returns error message"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            String offerRequestText = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this.offerContractDto)
            String signContractText = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this.signContractDto)
            this.signContract(projectDto.contractDto.id, signContractText)
        when:
            def newOfferContractResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .post(this.createContractUri(projectDto.contractDto.id) + "/newOffer")
                            .header("Content-Type", "application/json")
                            .content(offerRequestText)
            ).andReturn().response
        then:
            newOfferContractResponse.status == HttpStatus.BAD_REQUEST.value()
            def message = MAPPER.readValue(newOfferContractResponse.contentAsString, ErrorMessage.class)
            message.message == "It is not possible to make contract status transition from SIGNED to OFFER."
    }

    @Transactional
    def "Signing contract in status OFFER is not possible and returns error message"() {
        given:
            ProjectDto projectDto = this.createProject()
            String signContractText = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this.signContractDto)
        when:
            def signContractResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(this.createContractUri(projectDto.contractDto.id) + "/sign")
                            .header("Content-Type", "application/json")
                            .content(signContractText)
            ).andReturn().response
        then:
            signContractResponse.status == HttpStatus.BAD_REQUEST.value()
            def message = MAPPER.readValue(signContractResponse.contentAsString, ErrorMessage.class)
            message.message == "It is not possible to make contract status transition from OFFER to SIGNED."
    }

    @Transactional
    def "Signing contract in status ACCEPTED changes contract status to SIGNED"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            String signContractText = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this.signContractDto)
        when:
            def signContractResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(this.createContractUri(projectDto.contractDto.id) + "/sign")
                            .header("Content-Type", "application/json")
                            .content(signContractText)
            ).andReturn().response
        then:
            signContractResponse.status == HttpStatus.OK.value()
            ContractDto contractDto = MAPPER.readValue(signContractResponse.contentAsString, ContractDto.class)
            contractDto.status == ContractStatus.SIGNED
    }

    @Transactional
    def "Contract in SIGNED status allows for creating work objects"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            String signContractText = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this.signContractDto)
            this.signContract(projectDto.contractDto.id, signContractText)
            String stageRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(stageDto)
        when:
            def stageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.createProjectUri(projectDto.id) + "/stages"))
                            .header("Content-Type", "application/json")
                            .content(stageRequestBody)
            ).andReturn().response
        then:
            stageResponse.status == HttpStatus.CREATED.value()
    }

    @Transactional
    def "Contract in SIGNED status allows for working on project"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            String signContractText = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this.signContractDto)
            this.signContract(projectDto.contractDto.id, signContractText)
            StageDto createdStageDto = this.createStage(projectDto.id)
            TaskDto createdTaskDto = this.createTask(projectDto.id, createdStageDto.id)
        when:
            def updateTaskResponse =
                    this.updateTaskStatus(projectDto.id, createdStageDto.id, createdTaskDto.id, TaskStatus.IN_PROGRESS)
        then:
            updateTaskResponse.status == HttpStatus.OK.value()
            TaskDto updateTaskDto = MAPPER.readValue(updateTaskResponse.contentAsString, TaskDto.class)
            updateTaskDto.status == TaskStatus.IN_PROGRESS
    }

    @Transactional
    def "Terminating singed contract changes this contract status to TERMINATED"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            String signContractText = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this.signContractDto)
            this.signContract(projectDto.contractDto.id, signContractText)
            ContractDto terminateContractDto = new ContractDto(endDate: END_DATE)
            String terminateRequest = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(terminateContractDto)
        when:
            def terminateContractResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(this.createContractUri(projectDto.contractDto.id) + "/terminate")
                            .header("Content-Type", "application/json")
                            .content(terminateRequest)
            ).andReturn().response
        then:
            terminateContractResponse.status == HttpStatus.OK.value()
            ContractDto contractDto = MAPPER.readValue(terminateContractResponse.contentAsString, ContractDto.class)
            contractDto.status == ContractStatus.TERMINATED
    }

    @Transactional
    def "Contract in status TERMINATED does not allow for creating working objects"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            String signContractText = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this.signContractDto)
            this.signContract(projectDto.contractDto.id, signContractText)
            ContractDto terminateContractDto = new ContractDto(endDate: END_DATE)
            String terminateRequest = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(terminateContractDto)
            this.terminateContract(projectDto.contractDto.id, terminateRequest)
            String stageRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(stageDto)
        when:
            def stageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.createProjectUri(projectDto.id) + "/stages"))
                            .header("Content-Type", "application/json")
                            .content(stageRequestBody)
            ).andReturn().response
        then:
            stageResponse.status == HttpStatus.BAD_REQUEST.value()
            def message = MAPPER.readValue(stageResponse.contentAsString, ErrorMessage.class)
            message.message == "Contract in status TERMINATED does not allow for creating new working objects."
    }

    @Transactional
    def "Contract in status TERMINATED does not allow for working on project"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            String signContractText = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this.signContractDto)
            this.signContract(projectDto.contractDto.id, signContractText)
            ContractDto terminateContractDto = new ContractDto(endDate: END_DATE)
            String terminateRequest = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(terminateContractDto)
            StageDto createdStageDto = this.createStage(projectDto.id)
            TaskDto createdTaskDto = this.createTask(projectDto.id, createdStageDto.id)
            this.terminateContract(projectDto.contractDto.id, terminateRequest)
        when:
            def updateTaskResponse =
                    this.updateTaskStatus(projectDto.id, createdStageDto.id, createdTaskDto.id, TaskStatus.IN_PROGRESS)
        then:
            updateTaskResponse.status == HttpStatus.BAD_REQUEST.value()
            def message = MAPPER.readValue(updateTaskResponse.contentAsString, ErrorMessage.class)
            message.message == "Contract in status TERMINATED does not allow for work on the project."
    }

    @Transactional
    def "Resuming terminated contract changes its status to SIGNED"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            String signContractText = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this.signContractDto)
            this.signContract(projectDto.contractDto.id, signContractText)
            ContractDto terminateContractDto = new ContractDto(endDate: END_DATE)
            String terminateRequest = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(terminateContractDto)
            this.terminateContract(projectDto.contractDto.id, terminateRequest)
        when:
            def resumeContractResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(this.createContractUri(projectDto.contractDto.id) + "/resume")
                            .header("Content-Type", "application/json")
            ).andReturn().response
        then:
            resumeContractResponse.status == HttpStatus.OK.value()
            ContractDto contractDto = MAPPER.readValue(resumeContractResponse.contentAsString, ContractDto.class)
            contractDto.status == ContractStatus.SIGNED
    }

    @Transactional
    def "Completing signed contract changes its status to COMPLETED"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            String signContractText = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this.signContractDto)
            this.signContract(projectDto.contractDto.id, signContractText)
            ContractDto completeContractDto = new ContractDto(endDate: END_DATE)
            String completeRequest = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(completeContractDto)
        when:
            def completeContractResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(this.createContractUri(projectDto.contractDto.id) + "/complete")
                            .header("Content-Type", "application/json")
                            .content(completeRequest)
            ).andReturn().response
        then:
            completeContractResponse.status == HttpStatus.OK.value()
            ContractDto contractDto = MAPPER.readValue(completeContractResponse.contentAsString, ContractDto.class)
            contractDto.status == ContractStatus.COMPLETED
    }

    @Transactional
    def "Contract in status COMPLETED does not allow for creating work objects on project"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            String signContractText = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this.signContractDto)
            this.signContract(projectDto.contractDto.id, signContractText)
            ContractDto completeContractDto = new ContractDto(endDate: END_DATE)
            String completeRequest = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(completeContractDto)
            this.completeContract(projectDto.contractDto.id, completeRequest)
            String stageRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(stageDto)
        when:
            def stageResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.createProjectUri(projectDto.id) + "/stages"))
                            .header("Content-Type", "application/json")
                            .content(stageRequestBody)
            ).andReturn().response
        then:
            stageResponse.status == HttpStatus.BAD_REQUEST.value()
            def message = MAPPER.readValue(stageResponse.contentAsString, ErrorMessage.class)
            message.message == "Contract in status COMPLETED does not allow for creating new working objects."
    }

    @Transactional
    def "Contract in status COMPLETED does not allow for working on project"() {
        given:
            ProjectDto projectDto = this.createProject()
            this.acceptContractOffer(projectDto.contractDto.id)
            String signContractText = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this.signContractDto)
            this.signContract(projectDto.contractDto.id, signContractText)
            ContractDto completeContractDto = new ContractDto(endDate: END_DATE)
            String completeRequest = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(completeContractDto)
            StageDto createdStageDto = this.createStage(projectDto.id)
            TaskDto createdTaskDto = this.createTask(projectDto.id, createdStageDto.id)
            this.completeContract(projectDto.contractDto.id, completeRequest)
        when:
            def updateTaskResponse =
                    this.updateTaskStatus(projectDto.id, createdStageDto.id, createdTaskDto.id, TaskStatus.IN_PROGRESS)
        then:
            updateTaskResponse.status == HttpStatus.BAD_REQUEST.value()
            def message = MAPPER.readValue(updateTaskResponse.contentAsString, ErrorMessage.class)
            message.message == "Contract in status COMPLETED does not allow for work on the project."
    }

    // helper methods

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

    private MockHttpServletResponse updateTaskStatus(long projectId, long stageId, long taskId, TaskStatus status) {
        TaskDto updateStatusDto = new TaskDto(status: status)
        String requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(updateStatusDto)
        return this.mockMvc.perform(MockMvcRequestBuilders
                .post(URI.create(this.createTaskUri(projectId, stageId, taskId) + "/updateStatus"))
                .header("Content-Type", "application/json")
                .content(requestBody)
        ).andReturn().response
    }

    private ContractDto acceptContractOffer(long contractId) {
        def acceptContractResponse = this.mockMvc.perform(
                MockMvcRequestBuilders
                        .post(this.createContractUri(contractId) + "/acceptOffer")
                        .header("Content-Type", "application/json")

        ).andReturn().response
        return MAPPER.readValue(acceptContractResponse.contentAsString, ContractDto.class)
    }

    private ContractDto signContract(long contractId, String signContractDataText) {
        def signResponse = this.mockMvc.perform(
                MockMvcRequestBuilders.post(this.createContractUri(contractId) + "/sign")
                        .header("Content-Type", "application/json")
                        .content(signContractDataText)
        ).andReturn().response
        return MAPPER.readValue(signResponse.contentAsString, ContractDto.class)
    }

    private ContractDto rejectContract(long contractId) {
        def rejectedContractResponse = this.mockMvc.perform(
                MockMvcRequestBuilders
                        .post(this.createContractUri(contractId) + "/reject")
                        .header("Content-Type", "application/json")

        ).andReturn().response
        return MAPPER.readValue(rejectedContractResponse.contentAsString, ContractDto.class)
    }

    private ContractDto terminateContract(long contractId, String requestText) {
        def terminateContractResponse = this.mockMvc.perform(
                MockMvcRequestBuilders.post(this.createContractUri(contractId) + "/terminate")
                        .header("Content-Type", "application/json")
                        .content(requestText)
        ).andReturn().response
        return MAPPER.readValue(terminateContractResponse.contentAsString, ContractDto.class)
    }

    private ContractDto completeContract(long contractId, String requestText) {
        def completeContractResponse = this.mockMvc.perform(
                MockMvcRequestBuilders.post(this.createContractUri(contractId) + "/complete")
                        .header("Content-Type", "application/json")
                        .content(requestText)
        ).andReturn().response
        return MAPPER.readValue(completeContractResponse.contentAsString, ContractDto.class)
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
