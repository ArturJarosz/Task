package com.arturjarosz.task.project

import com.arturjarosz.task.configuration.BaseTestIT
import com.arturjarosz.task.dto.ApplicationConfigurationDto
import com.arturjarosz.task.dto.ArchitectDto
import com.arturjarosz.task.dto.ClientDto
import com.arturjarosz.task.dto.ProjectCreateDto
import com.arturjarosz.task.dto.StageDto
import com.arturjarosz.task.dto.TaskDto
import com.arturjarosz.task.dto.TaskTypeDto
import com.arturjarosz.task.sharedkernel.exceptions.ErrorMessage
import com.arturjarosz.task.utils.TestsHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper

class TaskTypeTestIT extends BaseTestIT {
    static final String TASK_TYPES_URI = "/task-types"
    static final String ARCHITECTS_URI = "/architects"
    static final String CLIENTS_URI = "/clients"
    static final String PROJECTS_URI = "/projects"
    static final String CONTENT_TYPE = "Content-Type"
    static final String APPLICATION_JSON = "application/json"
    static final long NOT_EXISTING_TASK_TYPE_ID = 10000L
    static final ObjectMapper MAPPER = new ObjectMapper()

    final def architect = readJson('json/architect/architect.json', ArchitectDto)
    final def privateClientDto = readJson('json/client/privateClient.json', ClientDto)
    final def properProjectDto = readJson('json/project/properProject.json', ProjectCreateDto)
    final def properStageDto = readJson('json/stage/properStage.json', StageDto)
    final def properTaskDto = readJson('json/task/properTask.json', TaskDto)

    @Autowired
    private MockMvc mockMvc

    @Transactional
    def "Creating task type should return code 201, created task type and its location"() {
        when:
            def response = this.postTaskType(new TaskTypeDto(name: "  Moodboard  "))
        then:
            response.status == HttpStatus.CREATED.value()
        and:
            def createdTaskType = MAPPER.readValue(response.contentAsString, TaskTypeDto)
            createdTaskType.id != null
            createdTaskType.name == "Moodboard"
            createdTaskType.numberOfTasks == 0
            response.getHeader("Location") == "$TASK_TYPES_URI/${createdTaskType.id}"
    }

    @Transactional
    def "Creating task type with empty name should return code 400 and error message"() {
        when:
            def response = this.postTaskType(new TaskTypeDto(name: " "))
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            MAPPER.readValue(response.contentAsString, ErrorMessage).message == "Task type name cannot be empty."
    }

    @Transactional
    def "Creating task type with name of existing type should return code 400 and error message"() {
        when:
            def response = this.postTaskType(new TaskTypeDto(name: FIXTURE_TASK_TYPE_NAME.toUpperCase()))
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            MAPPER.readValue(response.contentAsString, ErrorMessage).message ==
                    "Task type with name ${FIXTURE_TASK_TYPE_NAME.toUpperCase()} already exists."
    }

    @Transactional
    def "Renaming task type should return code 200 and renamed task type"() {
        given:
            def taskType = this.createTaskType("Moodboard")
        when:
            def response = this.putTaskType(taskType.id, new TaskTypeDto(name: "Mood board"))
        then:
            response.status == HttpStatus.OK.value()
        and:
            MAPPER.readValue(response.contentAsString, TaskTypeDto).name == "Mood board"
        and:
            def loaded = MAPPER.readValue(this.mockMvc.perform(
                    MockMvcRequestBuilders.get("$TASK_TYPES_URI/${taskType.id}")).andReturn().response
                    .contentAsString, TaskTypeDto)
            loaded.name == "Mood board"
    }

    @Transactional
    def "Renaming task type to its own name with different letter case should be allowed"() {
        given:
            def taskType = this.createTaskType("Moodboard")
        when:
            def response = this.putTaskType(taskType.id, new TaskTypeDto(name: "MoodBoard"))
        then:
            response.status == HttpStatus.OK.value()
    }

    @Transactional
    def "Renaming task type to name of another type should return code 400"() {
        given:
            def taskType = this.createTaskType("Moodboard")
        when:
            def response = this.putTaskType(taskType.id, new TaskTypeDto(name: FIXTURE_TASK_TYPE_NAME))
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
    }

    @Transactional
    def "Updating not existing task type should return code 404 and error message"() {
        when:
            def response = this.putTaskType(NOT_EXISTING_TASK_TYPE_ID, new TaskTypeDto(name: "Any"))
        then:
            response.status == HttpStatus.NOT_FOUND.value()
        and:
            MAPPER.readValue(response.contentAsString, ErrorMessage).message ==
                    "Task type with id 10,000 does not exist."
    }

    @Transactional
    def "Deleting task type without tasks should return code 200 and remove it"() {
        given:
            def taskType = this.createTaskType("Moodboard")
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.delete("$TASK_TYPES_URI/${taskType.id}"))
                    .andReturn().response
        then:
            response.status == HttpStatus.OK.value()
        and:
            this.mockMvc.perform(MockMvcRequestBuilders.get("$TASK_TYPES_URI/${taskType.id}"))
                    .andReturn().response.status == HttpStatus.NOT_FOUND.value()
    }

    @Transactional
    def "Deleting task type used by a task should return code 400 and keep it"() {
        given:
            def taskType = this.createTaskType("Moodboard")
            this.createTask(taskType.id)
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.delete("$TASK_TYPES_URI/${taskType.id}"))
                    .andReturn().response
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            MAPPER.readValue(response.contentAsString, ErrorMessage).message ==
                    "Task type used by tasks cannot be removed."
        and:
            this.mockMvc.perform(MockMvcRequestBuilders.get("$TASK_TYPES_URI/${taskType.id}"))
                    .andReturn().response.status == HttpStatus.OK.value()
    }

    @Transactional
    def "Getting task types should return them sorted by name with the number of their tasks"() {
        given:
            def moodboard = this.createTaskType("Moodboard")
            def budget = this.createTaskType("Budget estimate")
            this.createTask(moodboard.id)
            this.createTask(moodboard.id)
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.get(TASK_TYPES_URI)).andReturn().response
        then:
            response.status == HttpStatus.OK.value()
        and:
            List<TaskTypeDto> taskTypes = MAPPER.readValue(response.contentAsString, TaskTypeDto[]).toList()
            taskTypes*.name == ["Budget estimate", "Moodboard", FIXTURE_TASK_TYPE_NAME]
            taskTypes.find { it.id == moodboard.id }.numberOfTasks == 2
            taskTypes.find { it.id == budget.id }.numberOfTasks == 0
    }

    @Transactional
    def "Creating task with not existing type should return code 404 and error message"() {
        when:
            def response = this.postTask(NOT_EXISTING_TASK_TYPE_ID)
        then:
            response.status == HttpStatus.NOT_FOUND.value()
        and:
            MAPPER.readValue(response.contentAsString, ErrorMessage).message ==
                    "Task type with id 10,000 does not exist."
    }

    @Transactional
    def "Application configuration should list task types with their id and name"() {
        given:
            def taskType = this.createTaskType("Moodboard")
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.get("/configuration")).andReturn().response
        then:
            def configuration = MAPPER.readValue(response.contentAsString, ApplicationConfigurationDto)
            configuration.taskTypes.find { it.id == String.valueOf(taskType.id) }.label == "Moodboard"
    }

    private def postTaskType(TaskTypeDto taskTypeDto) {
        return this.mockMvc.perform(MockMvcRequestBuilders.post(TASK_TYPES_URI)
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(taskTypeDto))).andReturn().response
    }

    private def putTaskType(Long taskTypeId, TaskTypeDto taskTypeDto) {
        return this.mockMvc.perform(MockMvcRequestBuilders.put("$TASK_TYPES_URI/$taskTypeId")
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(taskTypeDto))).andReturn().response
    }

    private TaskTypeDto createTaskType(String name) {
        return MAPPER.readValue(this.postTaskType(new TaskTypeDto(name: name)).contentAsString, TaskTypeDto)
    }

    private void createTask(Long taskTypeId) {
        assert this.postTask(taskTypeId).status == HttpStatus.CREATED.value()
    }

    private def postTask(Long taskTypeId) {
        def architectId = TestsHelper.createArchitect(architect, "$HOST:$port$ARCHITECTS_URI", this.mockMvc).id
        properProjectDto.architectId = architectId
        properProjectDto.clientId = TestsHelper.createClient(privateClientDto, "$HOST:$port$CLIENTS_URI",
                this.mockMvc).id
        properProjectDto.typeId = projectTypeId
        def project = TestsHelper.createProject(properProjectDto, "$HOST:$port$PROJECTS_URI", this.mockMvc)
        properStageDto.architectId = architectId
        properStageDto.typeId = stageTypeId
        def stage = MAPPER.readValue(this.mockMvc.perform(
                MockMvcRequestBuilders.post("$PROJECTS_URI/${project.id}/stages")
                        .header(CONTENT_TYPE, APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(properStageDto))).andReturn().response.contentAsString,
                StageDto)
        properTaskDto.architectId = architectId
        properTaskDto.typeId = taskTypeId
        return this.mockMvc.perform(MockMvcRequestBuilders.post("$PROJECTS_URI/${project.id}/stages/${stage.id}/tasks")
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(properTaskDto))).andReturn().response
    }

    private <T> T readJson(String path, Class<T> clazz) {
        return MAPPER.readValue(new File(getClass().classLoader.getResource(path).file), clazz)
    }
}
