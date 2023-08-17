package com.arturjarosz.task.finance

import com.arturjarosz.task.architect.application.dto.ArchitectBasicDto
import com.arturjarosz.task.client.application.dto.ClientDto
import com.arturjarosz.task.configuration.BaseTestIT
import com.arturjarosz.task.contractor.application.dto.ContractorDto
import com.arturjarosz.task.finance.application.dto.ContractorJobDto
import com.arturjarosz.task.project.application.dto.ProjectCreateDto
import com.arturjarosz.task.project.application.dto.ProjectDto
import com.arturjarosz.task.sharedkernel.exceptions.ErrorMessage
import com.arturjarosz.task.utils.TestsHelper
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

class ContractorJobTestIT extends BaseTestIT {
    static final String ARCHITECTS_URI = "/architects"
    static final String CLIENTS_URI = "/clients"
    static final String PROJECTS_URI = "/projects"
    static final String CONTRACTORS_URI = "/contractors"
    static final String CONTRACTOR_JOBS_URI = "/contractorJobs"

    static final NOT_EXISTING_CONTRACTOR_JOB_ID = Integer.MAX_VALUE

    static final ObjectMapper MAPPER = new ObjectMapper()
    static final CONTENT_TYPE = "Content-Type"
    static final APPLICATION_JSON = "application/json"
    static final String LOCATION = "Location"

    def architectDto = createObjectFromJson('json/architect/architect.json', ArchitectBasicDto)
    def privateClientDto = createObjectFromJson('json/client/privateClient.json', ClientDto)
    def projectDto = createObjectFromJson('json/project/properProject.json', ProjectCreateDto)
    def createContractorDto = createObjectFromJson("json/contractor/createContractor.json", ContractorDto)
    def createContractorJobDto = createObjectFromJson("json/finance/contractor-job/createContractorJob.json", ContractorJobDto)
    def updateContractorJobDto = createObjectFromJson("json/finance/contractor-job/updateContractorJob.json", ContractorJobDto)

    @Autowired
    private MockMvc mockMvc

    @Transactional
    def "creating contractorJob with wrong input should return code 400 and error message"() {
        given:
            def project = createProject()
            def contractor = createContractor()
            createContractorJobDto.contractorId = contractor.id
            createContractorJobDto.name = null
            def requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(createContractorJobDto)
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.post("$PROJECTS_URI/${project.id}$CONTRACTOR_JOBS_URI")
                    .header(CONTENT_TYPE, APPLICATION_JSON)
                    .content(requestBody)).andReturn().response
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            ErrorMessage errorMessage = MAPPER.readValue(response.contentAsString, ErrorMessage.class)
            errorMessage.message == "Name of the contractorJob was not specified."
    }

    @Transactional
    def "creating contractorJob should return code 201, created object and its location"() {
        given:
            def project = createProject()
            def contractor = createContractor()
            createContractorJobDto.contractorId = contractor.id
            def requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(createContractorJobDto)
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.post("$PROJECTS_URI/${project.id}$CONTRACTOR_JOBS_URI")
                    .header(CONTENT_TYPE, APPLICATION_JSON)
                    .content(requestBody)).andReturn().response
        then:
            response.status == HttpStatus.CREATED.value()
        and:
            def createdContractorJob = MAPPER.readValue(response.contentAsString, ContractorJobDto)
            createdContractorJob.name == createContractorJobDto.name
            createdContractorJob.value == createContractorJobDto.value
        and:
            response.getHeader(LOCATION) == "$PROJECTS_URI/${project.id}$CONTRACTOR_JOBS_URI/${createdContractorJob.id}"
    }

    @Transactional
    def "updating not existing contractorJob should return code 400 and error message"() {
        given:
            def project = createProject()
            def requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(updateContractorJobDto)
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.put("$PROJECTS_URI/${project.id}$CONTRACTOR_JOBS_URI/${NOT_EXISTING_CONTRACTOR_JOB_ID}")
                    .header(CONTENT_TYPE, APPLICATION_JSON)
                    .content(requestBody)).andReturn().response
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            def errorMessage = MAPPER.readValue(response.contentAsString, ErrorMessage.class)
            errorMessage.message == "Project with id ${project.id} does not have a contractorJob with id ${String.format("%,d", NOT_EXISTING_CONTRACTOR_JOB_ID)}."
    }

    @Transactional
    def "updating contractorJob with incorrect data should return code 400 and error message"() {
        given:
            def project = createProject()
            def contractor = createContractor()
            def contractorJob = createContractorJob(project.id, contractor.id)
            updateContractorJobDto.name = null
            def requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(updateContractorJobDto)
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.put("$PROJECTS_URI/${project.id}$CONTRACTOR_JOBS_URI/${contractorJob.id}")
                    .header(CONTENT_TYPE, APPLICATION_JSON)
                    .content(requestBody)).andReturn().response
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            def errorMessage = MAPPER.readValue(response.contentAsString, ErrorMessage.class)
            errorMessage.message == "Name of the contractorJob was not specified."
    }

    @Transactional
    def "updating contractorJob should return code 200 and update object"() {
        given:
            def project = createProject()
            def contractor = createContractor()
            def contractorJob = createContractorJob(project.id, contractor.id)
            def requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(updateContractorJobDto)
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.put("$PROJECTS_URI/${project.id}$CONTRACTOR_JOBS_URI/${contractorJob.id}")
                    .header(CONTENT_TYPE, APPLICATION_JSON)
                    .content(requestBody)).andReturn().response
        then:
            response.status == HttpStatus.OK.value()
        and:
            def updatedContractorJob = MAPPER.readValue(response.contentAsString, ContractorJobDto)
            updatedContractorJob.name == updateContractorJobDto.name
            updatedContractorJob.note == updateContractorJobDto.note
            updatedContractorJob.value == updateContractorJobDto.value
    }

    @Transactional
    def "removing not existing contractorJob should return code 400 and error message"() {
        given:
            def project = createProject()
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.delete("$PROJECTS_URI/${project.id}$CONTRACTOR_JOBS_URI/$NOT_EXISTING_CONTRACTOR_JOB_ID"))
                    .andReturn().response
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            def errorMessage = MAPPER.readValue(response.contentAsString, ErrorMessage.class)
            errorMessage.message == "Project with id ${project.id} does not have a contractorJob with id ${String.format("%,d", NOT_EXISTING_CONTRACTOR_JOB_ID)}."
    }

    @Transactional
    def "removing contractorJob should return code 200 and remove object"() {
        given:
            def project = createProject()
            def contractor = createContractor()
            def contractorJob = createContractorJob(project.id, contractor.id)
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.delete("$PROJECTS_URI/${project.id}$CONTRACTOR_JOBS_URI/${contractorJob.id}"))
                    .andReturn().response
        then:
            response.status == HttpStatus.OK.value()
        and:
            def fetchContractorJobResponse = this.mockMvc.perform(MockMvcRequestBuilders.get("$PROJECTS_URI/${project.id}$CONTRACTOR_JOBS_URI/${contractorJob.id}"))
                    .andReturn().response
            fetchContractorJobResponse.status == HttpStatus.BAD_REQUEST.value()
            def errorMessage = MAPPER.readValue(fetchContractorJobResponse.contentAsString, ErrorMessage.class)
            errorMessage.message == "Project with id ${project.id} does not have a contractorJob with id ${String.format("%,d", contractorJob.id)}."
    }

    @Transactional
    def "fetching not existing contractorJob should return code 400 and error message"() {
        given:
            def project = createProject()
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.get("$PROJECTS_URI/${project.id}$CONTRACTOR_JOBS_URI/$NOT_EXISTING_CONTRACTOR_JOB_ID"))
                    .andReturn().response
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            def errorMessage = MAPPER.readValue(response.contentAsString, ErrorMessage.class)
            errorMessage.message == "Project with id ${project.id} does not have a contractorJob with id ${String.format("%,d", NOT_EXISTING_CONTRACTOR_JOB_ID)}."
    }

    @Transactional
    def "fetching contractorJob should return code 200 and proper contractorJob"() {
        given:
            def project = createProject()
            def contractor = createContractor()
            def contractorJob = createContractorJob(project.id, contractor.id)
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.get("$PROJECTS_URI/${project.id}$CONTRACTOR_JOBS_URI/${contractorJob.id}"))
                    .andReturn().response
        then:
            response.status == HttpStatus.OK.value()
        and:
            def fetchedContractorJob = MAPPER.readValue(response.contentAsString, ContractorJobDto)
            fetchedContractorJob.id == contractorJob.id
    }

    private ProjectDto createProject() {
        def architectDto = TestsHelper.createArchitect(this.architectDto, "$ARCHITECTS_URI", this.mockMvc)
        this.projectDto.architectId = architectDto.id
        def clientDto = TestsHelper.createClient(this.privateClientDto, "$CLIENTS_URI", this.mockMvc)
        this.projectDto.clientId = clientDto.id
        return TestsHelper.createProject(this.projectDto, "$PROJECTS_URI", this.mockMvc)
    }

    private ContractorDto createContractor() {
        def requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(createContractorDto)
        def response = this.mockMvc.perform(MockMvcRequestBuilders.post("$CONTRACTORS_URI")
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .content(requestBody)).andReturn().response
        return MAPPER.readValue(response.contentAsString, ContractorDto)
    }

    private ContractorJobDto createContractorJob(long projectId, long contractorId) {
        createContractorJobDto.contractorId = contractorId
        def requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(createContractorJobDto)
        def response = this.mockMvc.perform(MockMvcRequestBuilders.post("$PROJECTS_URI/${projectId}$CONTRACTOR_JOBS_URI")
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .content(requestBody)).andReturn().response
        return MAPPER.readValue(response.contentAsString, ContractorJobDto)
    }

    private <T> T createObjectFromJson(String path, Class<T> clazz) {
        return MAPPER.readValue(new File(getClass().classLoader.getResource(path).file), clazz)
    }
}
