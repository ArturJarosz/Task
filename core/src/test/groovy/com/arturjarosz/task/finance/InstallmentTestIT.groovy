package com.arturjarosz.task.finance

import com.arturjarosz.task.configuration.BaseTestIT
import com.arturjarosz.task.dto.*
import com.arturjarosz.task.sharedkernel.exceptions.ErrorMessage
import com.arturjarosz.task.utils.TestsHelper
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import tools.jackson.databind.ObjectMapper

import java.time.LocalDate

/**
 * The deposit is added when the contract is signed, through the contract status change.
 */
class InstallmentTestIT extends BaseTestIT {
    static final String ARCHITECTS_URI = "/architects"
    static final String CLIENTS_URI = "/clients"
    static final String CONTRACTS_URI = "/contracts"
    static final String PROJECTS_URI = "/projects"
    static final String INSTALLMENTS_URI = "/installments"
    static final String INSTALLMENTS_DATA_URI = "/installments-data"

    static final BigDecimal DEPOSIT_VALUE = new BigDecimal("3000.00")

    static final ObjectMapper MAPPER = new ObjectMapper()
    static final CONTENT_TYPE = "Content-Type"
    static final APPLICATION_JSON = "application/json"

    def architectDto = createObjectFromJson('json/architect/architect.json', ArchitectDto)
    def privateClientDto = createObjectFromJson('json/client/privateClient.json', ClientDto)
    def projectDto = createObjectFromJson('json/project/properProject.json', ProjectCreateDto)
    def signContractDto = createObjectFromJson('json/contract/signContract.json', ContractDto)
    def depositDto = new InstallmentDto(value: DEPOSIT_VALUE, hasInvoice: true, paid: false, note: "Paid on signing")

    @Autowired
    private MockMvc mockMvc


    def setup() {
        // ids of the data created by BaseTestIT
        projectDto.typeId = projectTypeId
    }

    @Transactional
    def "signing contract without deposit should return code 400 and error message and keep the contract unsigned"() {
        given:
            def project = this.createProjectWithAcceptedContract()
            this.signContractDto.deposit = null
        when:
            def response = this.changeContractStatus(project.contract.id, this.signContractDto)
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            def errorMessage = MAPPER.readValue(response.contentAsString, ErrorMessage)
            errorMessage.message == "A deposit has to be provided to sign the contract."
            this.getProject(project.id).contract.status == ContractStatusDto.ACCEPTED
    }

    @Transactional
    def "signing contract with deposit should add the deposit without stage to the project"() {
        given:
            def project = this.createProjectWithAcceptedContract()
        when:
            def response = this.changeContractStatus(project.contract.id, this.signContractWithDeposit())
        then:
            response.status == HttpStatus.OK.value()
            MAPPER.readValue(response.contentAsString, ContractDto).status == ContractStatusDto.SIGNED
        and:
            def installmentsData = this.getInstallmentsData(project.id)
            installmentsData.hasDeposit
            installmentsData.installments.size() == 1
            with(installmentsData.installments[0]) {
                type == InstallmentTypeDto.DEPOSIT
                stageId == null
                value == DEPOSIT_VALUE
                note == depositDto.note
            }
            installmentsData.financialData.count == 1
            installmentsData.financialData.netValue == DEPOSIT_VALUE
    }

    @Transactional
    def "signing contract with deposit of value 0 should return code 400 and error message"() {
        given:
            def project = this.createProjectWithAcceptedContract()
            this.depositDto.value = BigDecimal.ZERO
        when:
            def response = this.changeContractStatus(project.contract.id, this.signContractWithDeposit())
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            def errorMessage = MAPPER.readValue(response.contentAsString, ErrorMessage)
            errorMessage.message == "Deposit value has to be greater than 0."
    }

    @Transactional
    def "signing contract with paid deposit without payment date should return code 400 and error message"() {
        given:
            def project = this.createProjectWithAcceptedContract()
            this.depositDto.paid = true
        when:
            def response = this.changeContractStatus(project.contract.id, this.signContractWithDeposit())
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            def errorMessage = MAPPER.readValue(response.contentAsString, ErrorMessage)
            errorMessage.message == "Payment date of a paid installment has to be provided."
            this.getProject(project.id).contract.status == ContractStatusDto.ACCEPTED
    }

    @Transactional
    def "signing contract with deposit without invoice and paid flags should add an unpaid deposit without invoice"() {
        given:
            def project = this.createProjectWithAcceptedContract()
            this.depositDto = new InstallmentDto(value: DEPOSIT_VALUE)
        when:
            def response = this.changeContractStatus(project.contract.id, this.signContractWithDeposit())
        then:
            response.status == HttpStatus.OK.value()
        and:
            def deposit = this.getInstallmentsData(project.id).installments[0]
            !deposit.paid
            !deposit.hasInvoice
    }

    @Transactional
    def "reopening terminated contract should keep the deposit and need no new one"() {
        given:
            def project = this.createProjectWithSignedContract()
            this.terminateContract(project.contract.id)
            this.signContractDto.deposit = null
        when:
            def response = this.changeContractStatus(project.contract.id, this.signContractDto)
        then:
            response.status == HttpStatus.OK.value()
            this.getInstallmentsData(project.id).installments.size() == 1
    }

    @Transactional
    def "reopening terminated contract with another deposit should return code 400 and error message"() {
        given:
            def project = this.createProjectWithSignedContract()
            this.terminateContract(project.contract.id)
        when:
            def response = this.changeContractStatus(project.contract.id, this.signContractWithDeposit())
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            def errorMessage = MAPPER.readValue(response.contentAsString, ErrorMessage)
            errorMessage.message == "Project already has a deposit."
    }

    @Transactional
    def "deleting the deposit should return code 400 and error message"() {
        given:
            def project = this.createProjectWithSignedContract()
            def deposit = this.getInstallmentsData(project.id).installments[0]
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.delete("$PROJECTS_URI/${project.id}$INSTALLMENTS_URI/${deposit.id}"))
                    .andReturn().response
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            def errorMessage = MAPPER.readValue(response.contentAsString, ErrorMessage)
            errorMessage.message == "The deposit is required by the signed contract and cannot be removed."
    }

    @Transactional
    def "fetching deposit should return code 200 and deposit without stage name"() {
        given:
            def project = this.createProjectWithSignedContract()
            def deposit = this.getInstallmentsData(project.id).installments[0]
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.get("$PROJECTS_URI/${project.id}$INSTALLMENTS_URI/${deposit.id}"))
                    .andReturn().response
        then:
            response.status == HttpStatus.OK.value()
        and:
            def fetchedDeposit = MAPPER.readValue(response.contentAsString, InstallmentDto)
            fetchedDeposit.type == InstallmentTypeDto.DEPOSIT
            fetchedDeposit.stageName == null
    }

    @Transactional
    def "fetching installments data should list only this project's stages without an installment"() {
        given:
            def project = this.createProject()
            def stage = this.createStage(project.id, this.projectDto.architectId)
            def otherProject = this.createProject()
            this.createStage(otherProject.id, this.projectDto.architectId)
        when:
            def installmentsData = this.getInstallmentsData(project.id)
        then:
            installmentsData.stagesWithoutInstallment == [stage.id]
            !installmentsData.hasDeposit
    }

    private ContractDto signContractWithDeposit() {
        this.signContractDto.deposit = this.depositDto
        return this.signContractDto
    }

    private InstallmentProjectDataDto getInstallmentsData(long projectId) {
        def response = this.mockMvc.perform(MockMvcRequestBuilders.get("$PROJECTS_URI/$projectId$INSTALLMENTS_DATA_URI"))
                .andReturn().response
        assert response.status == HttpStatus.OK.value()
        return MAPPER.readValue(response.contentAsString, InstallmentProjectDataDto)
    }

    private ProjectDto getProject(long projectId) {
        def response = this.mockMvc.perform(MockMvcRequestBuilders.get("$PROJECTS_URI/$projectId")).andReturn().response
        return MAPPER.readValue(response.contentAsString, ProjectDto)
    }

    private StageDto createStage(long projectId, long architectId) {
        def stageDto = createObjectFromJson('json/stage/properStage.json', StageDto)
        stageDto.architectId = architectId
        def requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(stageDto)
        def response = this.mockMvc.perform(MockMvcRequestBuilders.post("$PROJECTS_URI/$projectId/stages")
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .content(requestBody)).andReturn().response
        assert response.status == HttpStatus.CREATED.value()
        return MAPPER.readValue(response.contentAsString, StageDto)
    }

    private ProjectDto createProjectWithAcceptedContract() {
        def project = this.createProject()
        def response = this.changeContractStatus(project.contract.id, new ContractDto(status: ContractStatusDto.ACCEPTED))
        assert response.status == HttpStatus.OK.value()
        return project
    }

    private ProjectDto createProjectWithSignedContract() {
        def project = this.createProjectWithAcceptedContract()
        def response = this.changeContractStatus(project.contract.id, this.signContractWithDeposit())
        assert response.status == HttpStatus.OK.value()
        return project
    }

    private void terminateContract(long contractId) {
        def response = this.changeContractStatus(contractId,
                new ContractDto(status: ContractStatusDto.TERMINATED, endDate: LocalDate.now()))
        assert response.status == HttpStatus.OK.value()
    }

    private MockHttpServletResponse changeContractStatus(long contractId, ContractDto contractDto) {
        def requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(contractDto)
        return this.mockMvc.perform(MockMvcRequestBuilders.post("$CONTRACTS_URI/$contractId/status")
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .content(requestBody)).andReturn().response
    }

    private ProjectDto createProject() {
        def architectDto = TestsHelper.createArchitect(this.architectDto, "$ARCHITECTS_URI", this.mockMvc)
        this.projectDto.architectId = architectDto.id
        def clientDto = TestsHelper.createClient(this.privateClientDto, "$CLIENTS_URI", this.mockMvc)
        this.projectDto.clientId = clientDto.id
        return TestsHelper.createProject(this.projectDto, "$PROJECTS_URI", this.mockMvc)
    }

    private <T> T createObjectFromJson(String path, Class<T> clazz) {
        return MAPPER.readValue(new File(getClass().classLoader.getResource(path).file), clazz)
    }
}
