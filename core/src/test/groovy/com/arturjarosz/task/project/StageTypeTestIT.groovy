package com.arturjarosz.task.project

import com.arturjarosz.task.configuration.BaseTestIT
import com.arturjarosz.task.dto.ApplicationConfigurationDto
import com.arturjarosz.task.dto.ArchitectDto
import com.arturjarosz.task.dto.ClientDto
import com.arturjarosz.task.dto.ProjectCreateDto
import com.arturjarosz.task.dto.StageDto
import com.arturjarosz.task.dto.StageTypeDto
import com.arturjarosz.task.project.application.StageTypeApplicationService
import com.arturjarosz.task.project.infrastructure.repositor.StageTypeRepository
import com.arturjarosz.task.sharedkernel.exceptions.ErrorMessage
import com.arturjarosz.task.utils.TestsHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import tools.jackson.databind.ObjectMapper

import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class StageTypeTestIT extends BaseTestIT {
    static final String STAGE_TYPES_URI = "/stage-types"
    static final String ARCHITECTS_URI = "/architects"
    static final String CLIENTS_URI = "/clients"
    static final String PROJECTS_URI = "/projects"
    static final String CONTENT_TYPE = "Content-Type"
    static final String APPLICATION_JSON = "application/json"
    static final long NOT_EXISTING_STAGE_TYPE_ID = 10000L
    static final ObjectMapper MAPPER = new ObjectMapper()

    final def architect = readJson('json/architect/architect.json', ArchitectDto)
    final def privateClientDto = readJson('json/client/privateClient.json', ClientDto)
    final def properProjectDto = readJson('json/project/properProject.json', ProjectCreateDto)
    final def properStageDto = readJson('json/stage/properStage.json', StageDto)

    @Autowired
    private MockMvc mockMvc
    @Autowired
    private PlatformTransactionManager transactionManager
    @Autowired
    private StageTypeRepository stageTypeRepository
    @Autowired
    private StageTypeApplicationService stageTypeApplicationService

    @Transactional
    def "Creating stage type should return code 201, created stage type and its location"() {
        when:
            def response = this.postStageType(new StageTypeDto(name: "  Site visits  "))
        then:
            response.status == HttpStatus.CREATED.value()
        and:
            def createdStageType = MAPPER.readValue(response.contentAsString, StageTypeDto)
            createdStageType.id != null
            createdStageType.name == "Site visits"
            createdStageType.numberOfStages == 0
            response.getHeader("Location") == "$STAGE_TYPES_URI/${createdStageType.id}"
    }

    @Transactional
    def "Creating stage type with empty name should return code 400 and error message"() {
        when:
            def response = this.postStageType(new StageTypeDto(name: " "))
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            MAPPER.readValue(response.contentAsString, ErrorMessage).message == "Stage type name cannot be empty."
    }

    @Transactional
    def "Creating stage type with name of existing type should return code 400 and error message"() {
        when:
            def response = this.postStageType(new StageTypeDto(name: FIXTURE_STAGE_TYPE_NAME.toUpperCase()))
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            MAPPER.readValue(response.contentAsString, ErrorMessage).message ==
                    "Stage type with name ${FIXTURE_STAGE_TYPE_NAME.toUpperCase()} already exists."
    }

    @Transactional
    def "Renaming stage type should return code 200 and renamed stage type"() {
        given:
            def stageType = this.createStageType("Site visits")
        when:
            def response = this.putStageType(stageType.id, new StageTypeDto(name: "Site inspections"))
        then:
            response.status == HttpStatus.OK.value()
        and:
            MAPPER.readValue(response.contentAsString, StageTypeDto).name == "Site inspections"
        and:
            def loaded = MAPPER.readValue(this.mockMvc.perform(
                    MockMvcRequestBuilders.get("$STAGE_TYPES_URI/${stageType.id}")).andReturn().response
                    .contentAsString, StageTypeDto)
            loaded.name == "Site inspections"
    }

    @Transactional
    def "Renaming stage type to its own name with different letter case should be allowed"() {
        given:
            def stageType = this.createStageType("Site visits")
        when:
            def response = this.putStageType(stageType.id, new StageTypeDto(name: "Site Visits"))
        then:
            response.status == HttpStatus.OK.value()
    }

    @Transactional
    def "Renaming stage type to name of another type should return code 400"() {
        given:
            def stageType = this.createStageType("Site visits")
        when:
            def response = this.putStageType(stageType.id, new StageTypeDto(name: FIXTURE_STAGE_TYPE_NAME))
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
    }

    @Transactional
    def "Updating not existing stage type should return code 404 and error message"() {
        when:
            def response = this.putStageType(NOT_EXISTING_STAGE_TYPE_ID, new StageTypeDto(name: "Any"))
        then:
            response.status == HttpStatus.NOT_FOUND.value()
        and:
            MAPPER.readValue(response.contentAsString, ErrorMessage).message ==
                    "Stage type with id 10,000 does not exist."
    }

    @Transactional
    def "Deleting stage type without stages should return code 200 and remove it"() {
        given:
            def stageType = this.createStageType("Site visits")
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.delete("$STAGE_TYPES_URI/${stageType.id}"))
                    .andReturn().response
        then:
            response.status == HttpStatus.OK.value()
        and:
            this.mockMvc.perform(MockMvcRequestBuilders.get("$STAGE_TYPES_URI/${stageType.id}"))
                    .andReturn().response.status == HttpStatus.NOT_FOUND.value()
    }

    @Transactional
    def "Deleting stage type used by a stage should return code 400 and keep it"() {
        given:
            def stageType = this.createStageType("Site visits")
            this.createStage(stageType.id)
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.delete("$STAGE_TYPES_URI/${stageType.id}"))
                    .andReturn().response
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            MAPPER.readValue(response.contentAsString, ErrorMessage).message ==
                    "Stage type used by stages cannot be removed."
        and:
            this.mockMvc.perform(MockMvcRequestBuilders.get("$STAGE_TYPES_URI/${stageType.id}"))
                    .andReturn().response.status == HttpStatus.OK.value()
    }

    @Transactional
    def "Getting stage types should return them sorted by name with the number of their stages"() {
        given:
            def siteVisits = this.createStageType("Site visits")
            def concept = this.createStageType("Concept design")
            this.createStage(siteVisits.id)
            this.createStage(siteVisits.id)
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.get(STAGE_TYPES_URI)).andReturn().response
        then:
            response.status == HttpStatus.OK.value()
        and:
            List<StageTypeDto> stageTypes = MAPPER.readValue(response.contentAsString, StageTypeDto[]).toList()
            stageTypes*.name == ["Concept design", "Site visits", FIXTURE_STAGE_TYPE_NAME]
            stageTypes.find { it.id == siteVisits.id }.numberOfStages == 2
            stageTypes.find { it.id == concept.id }.numberOfStages == 0
    }

    @Transactional
    def "Creating stage with not existing type should return code 404 and error message"() {
        when:
            def response = this.postStage(NOT_EXISTING_STAGE_TYPE_ID)
        then:
            response.status == HttpStatus.NOT_FOUND.value()
        and:
            MAPPER.readValue(response.contentAsString, ErrorMessage).message ==
                    "Stage type with id 10,000 does not exist."
    }

    @Transactional
    def "Application configuration should list stage types with their id and name"() {
        given:
            def stageType = this.createStageType("Site visits")
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.get("/configuration")).andReturn().response
        then:
            def configuration = MAPPER.readValue(response.contentAsString, ApplicationConfigurationDto)
            configuration.stageTypes.find { it.id == String.valueOf(stageType.id) }.label == "Site visits"
    }

    // Not transactional: the delete runs in its own transaction while another one holds the lock
    def "Deleting stage type should wait while a stage being saved holds the type"() {
        given:
            def stageType = this.stageTypeApplicationService.create(new StageTypeDto(name: "Locked type"))
            def transaction = new TransactionTemplate(this.transactionManager)
            CompletableFuture<Void> delete = null
        when: "a transaction checks the type, as saving a stage does, and keeps it open"
            def deleteWaited = transaction.execute {
                this.stageTypeRepository.findLockedById(stageType.id)
                delete = CompletableFuture.runAsync { this.stageTypeApplicationService.delete(stageType.id) }
                Thread.sleep(500)
                return !delete.isDone()
            }
        then: "the delete waits until that transaction ends, and only then removes the unused type"
            deleteWaited
            delete.get(10, TimeUnit.SECONDS) == null
            this.stageTypeRepository.findById(stageType.id).isEmpty()
    }

    private def postStageType(StageTypeDto stageTypeDto) {
        return this.mockMvc.perform(MockMvcRequestBuilders.post(STAGE_TYPES_URI)
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(stageTypeDto))).andReturn().response
    }

    private def putStageType(Long stageTypeId, StageTypeDto stageTypeDto) {
        return this.mockMvc.perform(MockMvcRequestBuilders.put("$STAGE_TYPES_URI/$stageTypeId")
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(stageTypeDto))).andReturn().response
    }

    private StageTypeDto createStageType(String name) {
        return MAPPER.readValue(this.postStageType(new StageTypeDto(name: name)).contentAsString, StageTypeDto)
    }

    private void createStage(Long stageTypeId) {
        assert this.postStage(stageTypeId).status == HttpStatus.CREATED.value()
    }

    private def postStage(Long stageTypeId) {
        def architectId = TestsHelper.createArchitect(architect, "$HOST:$port$ARCHITECTS_URI", this.mockMvc).id
        properProjectDto.architectId = architectId
        properProjectDto.clientId = TestsHelper.createClient(privateClientDto, "$HOST:$port$CLIENTS_URI",
                this.mockMvc).id
        properProjectDto.typeId = projectTypeId
        def project = TestsHelper.createProject(properProjectDto, "$HOST:$port$PROJECTS_URI", this.mockMvc)
        properStageDto.architectId = architectId
        properStageDto.typeId = stageTypeId
        return this.mockMvc.perform(MockMvcRequestBuilders.post("$PROJECTS_URI/${project.id}/stages")
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(properStageDto))).andReturn().response
    }

    private <T> T readJson(String path, Class<T> clazz) {
        return MAPPER.readValue(new File(getClass().classLoader.getResource(path).file), clazz)
    }
}
