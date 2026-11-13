package com.arturjarosz.task.project

import com.arturjarosz.task.configuration.BaseTestIT
import com.arturjarosz.task.dto.*
import com.arturjarosz.task.sharedkernel.exceptions.ErrorMessage
import com.arturjarosz.task.user.infrastructure.repository.UserRepository
import com.arturjarosz.task.user.model.User
import com.arturjarosz.task.utils.TestsHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import tools.jackson.databind.ObjectMapper

import java.time.LocalDate

/**
 * History of a project, its contract, stages and tasks. Envers writes the history when a transaction commits, so these
 * tests run without a test transaction and remove what they created in cleanup.
 */
class ProjectHistoryTestIT extends BaseTestIT {
    static final String ARCHITECTS_URI = "/architects"
    static final String CLIENTS_URI = "/clients"
    static final String PROJECTS_URI = "/projects"
    static final String AUTH_ID = "auth0|history-test"
    static final long NOT_EXISTING_ID = 10000l
    static final ObjectMapper MAPPER = new ObjectMapper()

    final def architect = this.readJson('json/architect/architect.json', ArchitectDto)
    final def privateClientDto = this.readJson('json/client/privateClient.json', ClientDto)
    final def projectCreateDto = this.readJson('json/project/properProject.json', ProjectCreateDto)
    final def stageDto = this.readJson('json/stage/properStage.json', StageDto)
    final def taskDto = this.readJson('json/task/properTask.json', TaskDto)

    @Autowired
    private MockMvc mockMvc
    @Autowired
    private UserRepository userRepository

    private Long architectId
    private Long clientId
    private ProjectDto project

    def setup() {
        // ids of the data created by BaseTestIT
        projectCreateDto.typeId = projectTypeId
        stageDto.typeId = stageTypeId
        taskDto.typeId = taskTypeId
        architectId = TestsHelper.createArchitect(architect, "$HOST:$port$ARCHITECTS_URI", this.mockMvc).id
        this.userRepository.save(new User("Anna History", "anna.history@example.com", AUTH_ID))
        this.authenticate(AUTH_ID)
    }

    def cleanup() {
        SecurityContextHolder.clearContext()
        this.userRepository.findByAuthId(AUTH_ID).ifPresent { this.userRepository.delete(it) }
        if (project != null) {
            assert this.delete("$PROJECTS_URI/$project.id").status == HttpStatus.OK.value()
            this.delete("$CLIENTS_URI/$clientId")
        }
        this.delete("$ARCHITECTS_URI/$architectId")
    }

    def "History of a new project should show it created by the current user"() {
        given:
            project = this.createProject()
        when:
            def response = this.get("$PROJECTS_URI/$project.id/history")
        then:
            response.status == HttpStatus.OK.value()
            def history = this.read(response, HistoryEntryDto[]).toList()
            history.size() == 1
            with(history[0]) {
                action == HistoryActionDto.CREATED
                recordType == HistoryRecordTypeDto.PROJECT
                recordId == project.id
                recordName == "Lublin 01"
                author.username == "Anna History"
                dateTime != null
                changes.empty
            }
    }

    def "History should list changed fields of the project and its contract with old and new values"() {
        given:
            project = this.createProject()
            def deadline = LocalDate.now().plusMonths(3)
        when:
            this.updateProject(project.id, "Lublin 02", "Bigger kitchen")
            this.changeContractStatus(project.contract.id, new ContractDto(status: ContractStatusDto.REJECTED))
            this.changeContractStatus(project.contract.id,
                    new ContractDto(status: ContractStatusDto.OFFER, offerValue: 9500.5, deadline: deadline))
            def history = this.getHistory(project.id)
        then: "latest first, without an entry for creating the contract"
            history.findAll { it.recordType == HistoryRecordTypeDto.CONTRACT }
                    .collect { entry -> entry.changes.collect { [it.field, it.oldValue, it.newValue] } } == [
                    [[HistoryFieldDto.OFFER_VALUE, "8000", "9500.5"],
                     [HistoryFieldDto.STATUS, "REJECTED", "OFFER"],
                     [HistoryFieldDto.DEADLINE, null, deadline.toString()]],
                    [[HistoryFieldDto.STATUS, "OFFER", "REJECTED"]]]
            history.findAll { it.recordType == HistoryRecordTypeDto.CONTRACT }*.recordName == [null, null]
        and:
            def projectChange = history.find { entry -> entry.changes.any { it.field == HistoryFieldDto.NAME } }
            projectChange.recordType == HistoryRecordTypeDto.PROJECT
            projectChange.recordName == "Lublin 02"
            projectChange.author.username == "Anna History"
            projectChange.changes.collect { [it.field, it.oldValue, it.newValue] } == [
                    [HistoryFieldDto.NAME, "Lublin 01", "Lublin 02"],
                    [HistoryFieldDto.NOTE, null, "Bigger kitchen"]]
            history.last().action == HistoryActionDto.CREATED
    }

    def "History should list stages and tasks with their stage, removed ones too"() {
        given:
            project = this.createProject()
            def stage = this.createStage(project.id)
            def task = this.createTask(project.id, stage.id)
            def removedTask = this.createTask(project.id, stage.id)
        when:
            this.updateTask(project.id, stage.id, task.id, "Wizualizacje piętra")
            assert this.delete("$PROJECTS_URI/$project.id/stages/$stage.id/tasks/$removedTask.id").status ==
                    HttpStatus.OK.value()
            def history = this.getHistory(project.id)
        then:
            def removed = history.find { it.recordId == removedTask.id && it.action == HistoryActionDto.REMOVED }
            removed.recordType == HistoryRecordTypeDto.TASK
            removed.recordName == "Wizualizacje parteru"
            removed.stageId == stage.id
            removed.stageName == "Etap 01"
        and:
            def nameChange = history.find { it.recordId == task.id && it.action == HistoryActionDto.CHANGED }
            nameChange.recordName == "Wizualizacje piętra"
            nameChange.changes.collect { [it.field, it.oldValue, it.newValue] } == [
                    [HistoryFieldDto.NAME, "Wizualizacje parteru", "Wizualizacje piętra"]]
            nameChange.stageId == stage.id
            nameChange.stageName == "Etap 01"
        and:
            history.findAll { it.action == HistoryActionDto.CREATED }
                    .collect { [it.recordType, it.recordId] } as Set == [
                    [HistoryRecordTypeDto.TASK, removedTask.id], [HistoryRecordTypeDto.TASK, task.id],
                    [HistoryRecordTypeDto.STAGE, stage.id], [HistoryRecordTypeDto.PROJECT, project.id]] as Set
        and: "latest first"
            history.indexOf(removed) < history.indexOf(nameChange)
            history.last().recordType == HistoryRecordTypeDto.PROJECT
    }

    def "History should list a removed stage and the tasks removed with it"() {
        given:
            project = this.createProject()
            def stage = this.createStage(project.id)
            def task = this.createTask(project.id, stage.id)
        when:
            assert this.delete("$PROJECTS_URI/$project.id/stages/$stage.id").status == HttpStatus.OK.value()
            def history = this.getHistory(project.id)
        then:
            with(history.find { it.recordId == stage.id && it.action == HistoryActionDto.REMOVED }) {
                recordType == HistoryRecordTypeDto.STAGE
                recordName == "Etap 01"
            }
            with(history.find { it.recordId == task.id && it.action == HistoryActionDto.REMOVED }) {
                recordType == HistoryRecordTypeDto.TASK
                recordName == "Wizualizacje parteru"
                stageId == stage.id
                stageName == "Etap 01"
            }
    }

    def "History of a stage should list only the stage and its tasks"() {
        given:
            project = this.createProject()
            def stage = this.createStage(project.id)
            def otherStage = this.createStage(project.id)
            def task = this.createTask(project.id, stage.id)
            this.createTask(project.id, otherStage.id)
        when:
            def history = this.getHistory(project.id, "?stageId=$stage.id")
        then:
            history.collect { [it.recordType, it.recordId] } as Set == [
                    [HistoryRecordTypeDto.STAGE, stage.id], [HistoryRecordTypeDto.TASK, task.id]] as Set
    }

    def "History of a task should list only the task"() {
        given:
            project = this.createProject()
            def stage = this.createStage(project.id)
            def task = this.createTask(project.id, stage.id)
            this.createTask(project.id, stage.id)
        when:
            this.updateTask(project.id, stage.id, task.id, "Wizualizacje piętra")
            def history = this.getHistory(project.id, "?stageId=$stage.id&taskId=$task.id")
        then:
            history.collect { [it.recordType, it.recordId, it.action] } == [
                    [HistoryRecordTypeDto.TASK, task.id, HistoryActionDto.CHANGED],
                    [HistoryRecordTypeDto.TASK, task.id, HistoryActionDto.CREATED]]
            history.every { it.stageName == "Etap 01" }
    }

    def "Changes made without a person should have no author"() {
        given:
            project = this.createProject()
            SecurityContextHolder.clearContext()
        when:
            this.updateProject(project.id, "Lublin 02", null)
            def history = this.getHistory(project.id)
        then:
            history[0].action == HistoryActionDto.CHANGED
            history[0].author == null
            history[1].author.username == "Anna History"
    }

    def "History of a not existing project should return code 404 and error message"() {
        when:
            def response = this.get("$PROJECTS_URI/$NOT_EXISTING_ID/history")
        then:
            response.status == HttpStatus.NOT_FOUND.value()
            this.read(response, ErrorMessage).message == "Project with id 10,000 does not exist."
    }

    // Helpers

    private ProjectDto createProject() {
        projectCreateDto.architectId = architectId
        clientId = TestsHelper.createClient(privateClientDto, "$HOST:$port$CLIENTS_URI", this.mockMvc).id
        projectCreateDto.clientId = clientId
        return TestsHelper.createProject(projectCreateDto, "$HOST:$port$PROJECTS_URI", this.mockMvc)
    }

    private StageDto createStage(long projectId) {
        stageDto.architectId = architectId
        def response = this.post("$PROJECTS_URI/$projectId/stages", stageDto)
        assert response.status == HttpStatus.CREATED.value()
        return this.read(response, StageDto)
    }

    private TaskDto createTask(long projectId, long stageId) {
        taskDto.architectId = architectId
        def response = this.post("$PROJECTS_URI/$projectId/stages/$stageId/tasks", taskDto)
        assert response.status == HttpStatus.CREATED.value()
        return this.read(response, TaskDto)
    }

    private void updateProject(long projectId, String name, String note) {
        def update = new ProjectDto(name: name, note: note, typeId: projectTypeId,
                architect: new ArchitectDto(id: architectId))
        assert this.put("$PROJECTS_URI/$projectId", update).status == HttpStatus.OK.value()
    }

    private void updateTask(long projectId, long stageId, long taskId, String name) {
        def update = new TaskDto(name: name, typeId: taskTypeId, architectId: architectId)
        assert this.put("$PROJECTS_URI/$projectId/stages/$stageId/tasks/$taskId", update).status ==
                HttpStatus.OK.value()
    }

    private void changeContractStatus(long contractId, ContractDto contract) {
        assert this.post("/contracts/$contractId/status", contract).status == HttpStatus.OK.value()
    }

    private List<HistoryEntryDto> getHistory(long projectId, String query = "") {
        def response = this.get("$PROJECTS_URI/$projectId/history$query")
        assert response.status == HttpStatus.OK.value()
        return this.read(response, HistoryEntryDto[]).toList()
    }

    private MockHttpServletResponse post(String uri, Object body) {
        return this.mockMvc.perform(MockMvcRequestBuilders.post(uri)
                .header("Content-Type", "application/json")
                .content(MAPPER.writeValueAsString(body))).andReturn().response
    }

    private MockHttpServletResponse put(String uri, Object body) {
        return this.mockMvc.perform(MockMvcRequestBuilders.put(uri)
                .header("Content-Type", "application/json")
                .content(MAPPER.writeValueAsString(body))).andReturn().response
    }

    private MockHttpServletResponse get(String uri) {
        return this.mockMvc.perform(MockMvcRequestBuilders.get(uri)).andReturn().response
    }

    private MockHttpServletResponse delete(String uri) {
        return this.mockMvc.perform(MockMvcRequestBuilders.delete(uri)).andReturn().response
    }

    private static <T> T read(MockHttpServletResponse response, Class<T> clazz) {
        return MAPPER.readValue(response.contentAsString, clazz)
    }

    private <T> T readJson(String path, Class<T> clazz) {
        return MAPPER.readValue(new File(getClass().classLoader.getResource(path).file), clazz)
    }
}
