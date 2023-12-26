package com.arturjarosz.task.project

import com.arturjarosz.task.configuration.BaseTestIT
import com.arturjarosz.task.dto.*
import com.arturjarosz.task.sharedkernel.exceptions.ErrorMessage
import com.arturjarosz.task.utils.TestsHelper
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.transaction.annotation.Transactional

class TaskTestIT extends BaseTestIT {
    static final String ARCHITECTS_URI = "/architects"
    static final String CLIENTS_URI = "/clients"
    static final String PROJECTS_URI = "/projects"
    static final String STAGES_URI = "/stages"
    static final String TASKS_URI = "/tasks"
    static final long NOT_EXISTING_PROJECT_ID = 10000l
    static final long NOT_EXISTING_STAGE_ID = 10000l
    static final long NOT_EXISTING_TASK_ID = 10000l
    static final ObjectMapper MAPPER = new ObjectMapper()

    final def architect = MAPPER.readValue(new File(getClass().classLoader.getResource('json/architect/architect.json').file),
            ArchitectDto)
    final def privateClientDto = MAPPER.readValue(new File(getClass().classLoader.getResource('json/client/privateClient.json').file),
            ClientDto)
    final def projectDto = MAPPER.readValue(new File(getClass().classLoader.getResource('json/project/properProject.json').file),
            ProjectCreateDto)
    final def stageDto = MAPPER.readValue(new File(getClass().classLoader.getResource('json/stage/properStage.json').file),
            StageDto)
    final def properTaskDto = MAPPER.readValue(new File(getClass().classLoader.getResource('json/task/properTask.json').file),
            TaskDto)
    final def notProperTaskDto = MAPPER.readValue(new File(getClass().classLoader.getResource('json/task/notProperTask.json').file),
            TaskDto)
    final def properTaskUpdateDto = MAPPER.readValue(new File(getClass().classLoader.getResource('json/task/properTaskUpdate.json').file),
            TaskDto)
    final def notProperTaskUpdateDto = MAPPER.readValue(new File(getClass().classLoader.getResource('json/task/notProperTaskUpdate.json').file),
            TaskDto)

    @Autowired
    private MockMvc mockMvc

    @Override
    def setupSpec() {
        MAPPER.findAndRegisterModules()
    }

    @Transactional
    def "Creating task for not existing project should return code 400 and error message"() {
        given:
            def taskRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properTaskDto)
        when:
            def taskResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders
                            .post(URI.create(this.taskUrlBuilder(NOT_EXISTING_PROJECT_ID, NOT_EXISTING_STAGE_ID)))
                            .header("Content-Type", "application/json")
                            .content(taskRequestBody)
            ).andReturn().response
        then: "Returns code 400"
            taskResponse.status == HttpStatus.BAD_REQUEST.value()
        and:
            def message = MAPPER.readValue(taskResponse.contentAsString, ErrorMessage)
            message.getMessage() == "Project with id 10,000 does not exist."
    }

    @Transactional
    def "Creating task for not existing stage should return code 400 and error message"() {
        given:
            def projectDto = this.createProject()
            def taskRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properTaskDto)
        when:
            def taskResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.taskUrlBuilder(projectDto.id, NOT_EXISTING_STAGE_ID)))
                            .header("Content-Type", "application/json")
                            .content(taskRequestBody)
            ).andReturn().response
        then: "Returns code 400"
            taskResponse.status == HttpStatus.BAD_REQUEST.value()
        and:
            def message = MAPPER.readValue(taskResponse.contentAsString, ErrorMessage)
            message.getMessage() == "Stage with id 10,000 does not exist."
    }

    @Transactional
    def "Creating task with not proper dto should return code 400 and error message"() {
        given:
            def projectDto = this.createProject()
            def stageDto = this.createStage(projectDto.id)
            def taskRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(notProperTaskDto)
        when:
            def taskResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.taskUrlBuilder(projectDto.id, stageDto.id)))
                            .header("Content-Type", "application/json")
                            .content(taskRequestBody)
            ).andReturn().response
        then:
            taskResponse.status == HttpStatus.BAD_REQUEST.value()
        and:
            def message = MAPPER.readValue(taskResponse.contentAsString, ErrorMessage)
            message.getMessage() == "Task name cannot be empty."
    }

    @Transactional
    def "Creating task for existing stage and project with proper date should return code 201, created task and location header"() {
        given:
            def projectDto = this.createProject()
            def stageDto = this.createStage(projectDto.id)
            def taskRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properTaskDto)
        when:
            def taskResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.taskUrlBuilder(projectDto.id, stageDto.id)))
                            .header("Content-Type", "application/json")
                            .content(taskRequestBody)
            ).andReturn().response
        then:
            def createdTaskDto = MAPPER.readValue(taskResponse.contentAsString, TaskDto)
            createdTaskDto.id != null
            createdTaskDto.name == properTaskDto.name
            createdTaskDto.type == properTaskDto.type
        and:
            !createdTaskDto.nextStatuses.empty
    }

    @Transactional
    def "Deleting not existing task should return code 400 and error message"() {
        given:
            def projectDto = this.createProject()
            def stageDto = this.createStage(projectDto.id)
        when:
            def taskResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.delete(URI.create(this.taskUrlBuilder(projectDto.id, stageDto.id) + "/" +
                            NOT_EXISTING_TASK_ID))
            ).andReturn().response
        then:
            taskResponse.status == HttpStatus.BAD_REQUEST.value()
        and:
            def message = MAPPER.readValue(taskResponse.contentAsString, ErrorMessage)
            message.getMessage() == "Task with id 10,000 does not exist on stage with id " + stageDto.id + "."
    }

    @Transactional
    def "Deleting existing task should return code 200 and remove it"() {
        given:
            def projectDto = this.createProject()
            def stageDto = this.createStage(projectDto.id)
            def taskRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properTaskDto)
            def taskResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.taskUrlBuilder(projectDto.id, stageDto.id)))
                            .header("Content-Type", "application/json")
                            .content(taskRequestBody)
            ).andReturn().response
            def createdTaskDto = MAPPER.readValue(taskResponse.contentAsString, TaskDto)
        when:
            def removeTaskResponse = this.mockMvc.perform(MockMvcRequestBuilders.delete(URI
                    .create(this.taskUrlBuilder(projectDto.id, stageDto.id) + "/" + createdTaskDto.id))
            ).andReturn().response
        then:
            removeTaskResponse.status == HttpStatus.OK.value()
        and:
            def getTaskResponse = this.mockMvc.perform(MockMvcRequestBuilders.get(URI
                    .create(this.taskUrlBuilder(projectDto.id, stageDto.id) + "/" + createdTaskDto.id))
            ).andReturn().response
            getTaskResponse.status == HttpStatus.BAD_REQUEST.value()
            def message = MAPPER.readValue(getTaskResponse.contentAsString, ErrorMessage)
            message.getMessage() == "Task with id " +
                    createdTaskDto.id + " does not exist on stage with id " + stageDto.id + "."
    }

    @Transactional
    def "Updating not existing task should return code 400 and error message"() {
        given:
            def projectDto = this.createProject()
            def stageDto = this.createStage(projectDto.id)
            def taskRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properTaskUpdateDto)
        when:
            def taskResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.put(URI.create(this.taskUrlBuilder(projectDto.id, stageDto.id) + "/" +
                            NOT_EXISTING_TASK_ID))
                            .header("Content-Type", "application/json")
                            .content(taskRequestBody)
            ).andReturn().response
        then:
            taskResponse.status == HttpStatus.BAD_REQUEST.value()
        and:
            def message = MAPPER.readValue(taskResponse.contentAsString, ErrorMessage)
            message.getMessage() == "Task with id 10,000 does not exist on stage with id " + stageDto.id + "."
    }

    @Transactional
    def "Updating task with not proper dto should return code 400 and error message"() {
        given:
            def projectDto = this.createProject()
            def stageDto = this.createStage(projectDto.id)
            def taskRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properTaskDto)
            def taskResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.taskUrlBuilder(projectDto.id, stageDto.id)))
                            .header("Content-Type", "application/json")
                            .content(taskRequestBody)
            ).andReturn().response
            def createdTaskDto = MAPPER.readValue(taskResponse.contentAsString, TaskDto)
            def taskUpdateRequestBody =
                    MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(notProperTaskUpdateDto)
        when:
            def taskUpdateResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.put(URI.create(this.taskUrlBuilder(projectDto.id, stageDto.id) + "/" +
                            createdTaskDto.id))
                            .header("Content-Type", "application/json")
                            .content(taskUpdateRequestBody)
            ).andReturn().response
        then:
            taskUpdateResponse.status == HttpStatus.BAD_REQUEST.value()
        and:
            def message = MAPPER.readValue(taskUpdateResponse.contentAsString, ErrorMessage)
            message.getMessage() == "Task name cannot be empty."
    }

    @Transactional
    def "Updating existing task with proper dto should return code 200 and dto of updated task"() {
        given:
            def projectDto = this.createProject()
            def stageDto = this.createStage(projectDto.id)
            def taskRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properTaskDto)
            def taskResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.taskUrlBuilder(projectDto.id, stageDto.id)))
                            .header("Content-Type", "application/json")
                            .content(taskRequestBody)
            ).andReturn().response
            def createdTaskDto = MAPPER.readValue(taskResponse.contentAsString, TaskDto)
            def taskUpdateRequestBody =
                    MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properTaskUpdateDto)
        when:
            def taskUpdateResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.put(URI.create(this.taskUrlBuilder(projectDto.id, stageDto.id) + "/" +
                            createdTaskDto.id))
                            .header("Content-Type", "application/json")
                            .content(taskUpdateRequestBody)
            ).andReturn().response
        then:
            taskUpdateResponse.status == HttpStatus.OK.value()
        and:
            def updatedTaskDto = MAPPER.readValue(taskUpdateResponse.contentAsString, TaskDto)
            updatedTaskDto.name == properTaskUpdateDto.name
            updatedTaskDto.note == properTaskUpdateDto.note
        and:
            !createdTaskDto.nextStatuses.empty
    }

    @Transactional
    def "Getting not existing task should return code 400 and error message"() {
        given:
            def projectDto = this.createProject()
            def stageDto = this.createStage(projectDto.id)
        when:
            def taskResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.get(URI.create(this.taskUrlBuilder(projectDto.id, stageDto.id) + "/" +
                            NOT_EXISTING_TASK_ID))
            ).andReturn().response
        then:
            taskResponse.status == HttpStatus.BAD_REQUEST.value()
        and:
            def message = MAPPER.readValue(taskResponse.contentAsString, ErrorMessage)
            message.getMessage() == "Task with id 10,000 does not exist on stage with id " + stageDto.id + "."
    }

    @Transactional
    def "Getting existing task should return code 200 and dto of task"() {
        given:
            def projectDto = this.createProject()
            def stageDto = this.createStage(projectDto.id)
            def taskRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properTaskDto)
            def taskResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.taskUrlBuilder(projectDto.id, stageDto.id)))
                            .header("Content-Type", "application/json")
                            .content(taskRequestBody)
            ).andReturn().response
            TaskDto createdTaskDto = MAPPER.readValue(taskResponse.contentAsString, TaskDto)
        when:
            def getTaskResponse = this.mockMvc.perform(MockMvcRequestBuilders.get(URI
                    .create(this.taskUrlBuilder(projectDto.id, stageDto.id) + "/" + createdTaskDto.id))
            ).andReturn().response
        then:
            getTaskResponse.status == HttpStatus.OK.value()
        and:
            def getTaskDto = MAPPER.readValue(getTaskResponse.contentAsString, TaskDto)
            getTaskDto.id == createdTaskDto.id
        and:
            !createdTaskDto.nextStatuses.empty
    }

    @Transactional
    def "Getting tasks should return code 200 and lis of all tasks"() {
        given:
            def projectDto = this.createProject()
            def stageDto = this.createStage(projectDto.id)
            def taskRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properTaskDto)
            this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(this.taskUrlBuilder(projectDto.id, stageDto.id)))
                    .header("Content-Type", "application/json")
                    .content(taskRequestBody)
            ).andReturn().response
            this.mockMvc.perform(MockMvcRequestBuilders.post(URI.create(this.taskUrlBuilder(projectDto.id, stageDto.id)))
                    .header("Content-Type", "application/json")
                    .content(taskRequestBody)
            ).andReturn().response
        when:
            def getTasksResponse = this.mockMvc.perform(MockMvcRequestBuilders.get(URI
                    .create(this.taskUrlBuilder(projectDto.id, stageDto.id)))
            ).andReturn().response
        then:
            getTasksResponse.status == HttpStatus.OK.value()
        and:
            List<TaskDto> tasks = MAPPER.readValue(getTasksResponse.contentAsString, List<TaskDto>)
            tasks.size() == 2
    }

    private ProjectDto createProject() {
        def architectDto = TestsHelper.createArchitect(architect, this.createArchitectUri(), this.mockMvc)
        this.projectDto.architectId = architectDto.id
        def clientDto = TestsHelper.createClient(this.privateClientDto, this.createClientUri(), this.mockMvc)
        this.projectDto.clientId = clientDto.id
        return TestsHelper.createProject(this.projectDto, this.createBasicProjectUri(), this.mockMvc)
    }

    private StageDto createStage(long projectId) {
        def stageRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(stageDto)
        def stageResponse = this.mockMvc.perform(
                MockMvcRequestBuilders
                        .post(URI.create(HOST + ":" + port + PROJECTS_URI + "/" + projectId + STAGES_URI))
                        .header("Content-Type", "application/json")
                        .content(stageRequestBody)
        ).andReturn().response
        return MAPPER.readValue(stageResponse.contentAsString, StageDto)
    }

    private String taskUrlBuilder(long projectId, long stageId) {
        return HOST + ":" + port + PROJECTS_URI + "/" + projectId + STAGES_URI + "/" + stageId + TASKS_URI
    }

    private String createArchitectUri() {
        return HOST + ":" + port + ARCHITECTS_URI
    }

    private String createClientUri() {
        return HOST + ":" + port + CLIENTS_URI
    }

    private String createBasicProjectUri() {
        return HOST + ":" + port + PROJECTS_URI
    }
}
