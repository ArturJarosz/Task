package com.arturjarosz.task.project

import com.arturjarosz.task.architect.application.dto.ArchitectBasicDto
import com.arturjarosz.task.architect.application.dto.ArchitectDto
import com.arturjarosz.task.client.application.dto.ClientDto
import com.arturjarosz.task.configuration.BaseTestIT
import com.arturjarosz.task.project.application.dto.ProjectCreateDto
import com.arturjarosz.task.project.application.dto.ProjectDto
import com.arturjarosz.task.project.application.dto.StageDto
import com.arturjarosz.task.project.application.dto.TaskDto
import com.arturjarosz.task.sharedkernel.exceptions.ErrorMessage
import com.arturjarosz.task.utils.TestsHelper
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.transaction.annotation.Transactional

class TaskTestIT extends BaseTestIT {
    private static final String ARCHITECTS_URI = "/architects"
    private static final String CLIENTS_URI = "/clients"
    private static final String PROJECTS_URI = "/projects"
    private static final String STAGES_URI = "/stages"
    private static final String TASKS_URI = "/tasks"
    private static final long NOT_EXISTING_PROJECT_ID = 10000l
    private static final long NOT_EXISTING_STAGE_ID = 10000l
    private static final long NOT_EXISTING_TASK_ID = 10000l
    private static final ObjectMapper MAPPER = new ObjectMapper()

    private final ArchitectBasicDto architect =
            MAPPER.readValue(new File(getClass().classLoader.getResource('json/architect/architect.json').file),
                    ArchitectBasicDto.class)
    private final ClientDto privateClientDto =
            MAPPER.readValue(new File(getClass().classLoader.getResource('json/client/privateClient.json').file),
                    ClientDto.class)
    private final ProjectCreateDto projectDto =
            MAPPER.readValue(new File(getClass().classLoader.getResource('json/project/properProject.json').file),
                    ProjectCreateDto.class)
    private final StageDto stageDto =
            MAPPER.readValue(new File(getClass().classLoader.getResource('json/stage/properStage.json').file),
                    StageDto.class)
    private final TaskDto properTaskDto =
            MAPPER.readValue(new File(getClass().classLoader.getResource('json/task/properTask.json').file),
                    TaskDto.class)
    private final TaskDto notProperTaskDto =
            MAPPER.readValue(new File(getClass().classLoader.getResource('json/task/notProperTask.json').file),
                    TaskDto.class)
    private final TaskDto properTaskUpdateDto =
            MAPPER.readValue(new File(getClass().classLoader.getResource('json/task/properTaskUpdate.json').file),
                    TaskDto.class)
    private final TaskDto notProperTaskUpdateDto =
            MAPPER.readValue(new File(getClass().classLoader.getResource('json/task/notProperTaskUpdate.json').file),
                    TaskDto.class)

    @Autowired
    private MockMvc mockMvc

    @Override
    def setupSpec() {
        MAPPER.findAndRegisterModules()
    }

    @Transactional
    def "Creating task for not existing project should return code 400 and error message"() {
        given:
            String taskRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properTaskDto)
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
            def message = MAPPER.readValue(taskResponse.contentAsString, ErrorMessage.class)
            message.getMessage() == "Project with id 10,000 does not exist."
    }

    @Transactional
    def "Creating task for not existing stage should return code 400 and error message"() {
        given:
            ProjectDto projectDto = this.createProject()
            String taskRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properTaskDto)
        when:
            def taskResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.taskUrlBuilder(projectDto.id, NOT_EXISTING_STAGE_ID)))
                            .header("Content-Type", "application/json")
                            .content(taskRequestBody)
            ).andReturn().response
        then: "Returns code 400"
            taskResponse.status == HttpStatus.BAD_REQUEST.value()
        and:
            def message = MAPPER.readValue(taskResponse.contentAsString, ErrorMessage.class)
            message.getMessage() == "Stage with id 10,000 does not exist."
    }

    @Transactional
    def "Creating task with not proper dto should return code 400 and error message"() {
        given:
            ProjectDto projectDto = this.createProject()
            StageDto stageDto = this.createStage(projectDto.id)
            String taskRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(notProperTaskDto)
        when:
            def taskResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.taskUrlBuilder(projectDto.id, stageDto.id)))
                            .header("Content-Type", "application/json")
                            .content(taskRequestBody)
            ).andReturn().response
        then:
            taskResponse.status == HttpStatus.BAD_REQUEST.value()
        and:
            def message = MAPPER.readValue(taskResponse.contentAsString, ErrorMessage.class)
            message.getMessage() == "Task name cannot be empty."
    }

    @Transactional
    def "Creating task for existing stage and project with proper date should return code 201, created task and location header"() {
        given:
            ProjectDto projectDto = this.createProject()
            StageDto stageDto = this.createStage(projectDto.id)
            String taskRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properTaskDto)
        when:
            def taskResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.taskUrlBuilder(projectDto.id, stageDto.id)))
                            .header("Content-Type", "application/json")
                            .content(taskRequestBody)
            ).andReturn().response
        then:
            TaskDto createdTaskDto = MAPPER.readValue(taskResponse.contentAsString, TaskDto.class)
            createdTaskDto.id != null
            createdTaskDto.name == properTaskDto.name
            createdTaskDto.type == properTaskDto.type
    }

    @Transactional
    def "Deleting not existing task should return code 400 and error message"() {
        given:
            ProjectDto projectDto = this.createProject()
            StageDto stageDto = this.createStage(projectDto.id)
        when:
            def taskResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.delete(URI.create(this.taskUrlBuilder(projectDto.id, stageDto.id) + "/" +
                            NOT_EXISTING_TASK_ID))
            ).andReturn().response
        then:
            taskResponse.status == HttpStatus.BAD_REQUEST.value()
        and:
            def message = MAPPER.readValue(taskResponse.contentAsString, ErrorMessage.class)
            message.getMessage() == "Task with id 10,000 does not exist on stage with id " + stageDto.id + "."
    }

    @Transactional
    def "Deleting existing task should return code 200 and remove it"() {
        given:
            ProjectDto projectDto = this.createProject()
            StageDto stageDto = this.createStage(projectDto.id)
            String taskRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properTaskDto)
            def taskResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.taskUrlBuilder(projectDto.id, stageDto.id)))
                            .header("Content-Type", "application/json")
                            .content(taskRequestBody)
            ).andReturn().response
            TaskDto createdTaskDto = MAPPER.readValue(taskResponse.contentAsString, TaskDto.class)
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
            def message = MAPPER.readValue(getTaskResponse.contentAsString, ErrorMessage.class)
            message.getMessage() == "Task with id " +
                    createdTaskDto.id + " does not exist on stage with id " + stageDto.id + "."
    }

    @Transactional
    def "Updating not existing task should return code 400 and error message"() {
        given:
            ProjectDto projectDto = this.createProject()
            StageDto stageDto = this.createStage(projectDto.id)
            String taskRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properTaskUpdateDto)
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
            def message = MAPPER.readValue(taskResponse.contentAsString, ErrorMessage.class)
            message.getMessage() == "Task with id 10,000 does not exist on stage with id " + stageDto.id + "."
    }

    @Transactional
    def "Updating task with not proper dto should return code 400 and error message"() {
        given:
            ProjectDto projectDto = this.createProject()
            StageDto stageDto = this.createStage(projectDto.id)
            String taskRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properTaskDto)
            def taskResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.taskUrlBuilder(projectDto.id, stageDto.id)))
                            .header("Content-Type", "application/json")
                            .content(taskRequestBody)
            ).andReturn().response
            TaskDto createdTaskDto = MAPPER.readValue(taskResponse.contentAsString, TaskDto.class)
            String taskUpdateRequestBody =
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
            def message = MAPPER.readValue(taskUpdateResponse.contentAsString, ErrorMessage.class)
            message.getMessage() == "Task name cannot be empty."
    }

    @Transactional
    def "Updating existing task with proper dto should return code 200 and dto of updated task"() {
        given:
            ProjectDto projectDto = this.createProject()
            StageDto stageDto = this.createStage(projectDto.id)
            String taskRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properTaskDto)
            def taskResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.taskUrlBuilder(projectDto.id, stageDto.id)))
                            .header("Content-Type", "application/json")
                            .content(taskRequestBody)
            ).andReturn().response
            TaskDto createdTaskDto = MAPPER.readValue(taskResponse.contentAsString, TaskDto.class)
            String taskUpdateRequestBody =
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
            TaskDto updatedTaskDto = MAPPER.readValue(taskUpdateResponse.contentAsString, TaskDto.class)
            updatedTaskDto.name == properTaskUpdateDto.name
            updatedTaskDto.note == properTaskUpdateDto.note
    }

    @Transactional
    def "Getting not existing task should return code 400 and error message"() {
        given:
            ProjectDto projectDto = this.createProject()
            StageDto stageDto = this.createStage(projectDto.id)
        when:
            def taskResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.get(URI.create(this.taskUrlBuilder(projectDto.id, stageDto.id) + "/" +
                            NOT_EXISTING_TASK_ID))
            ).andReturn().response
        then:
            taskResponse.status == HttpStatus.BAD_REQUEST.value()
        and:
            def message = MAPPER.readValue(taskResponse.contentAsString, ErrorMessage.class)
            message.getMessage() == "Task with id 10,000 does not exist on stage with id " + stageDto.id + "."
    }

    @Transactional
    def "Getting existing task should return code 200 and dto of task"() {
        given:
            ProjectDto projectDto = this.createProject()
            StageDto stageDto = this.createStage(projectDto.id)
            String taskRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properTaskDto)
            def taskResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.taskUrlBuilder(projectDto.id, stageDto.id)))
                            .header("Content-Type", "application/json")
                            .content(taskRequestBody)
            ).andReturn().response
            TaskDto createdTaskDto = MAPPER.readValue(taskResponse.contentAsString, TaskDto.class)
        when:
            def getTaskResponse = this.mockMvc.perform(MockMvcRequestBuilders.get(URI
                    .create(this.taskUrlBuilder(projectDto.id, stageDto.id) + "/" + createdTaskDto.id))
            ).andReturn().response
        then:
            getTaskResponse.status == HttpStatus.OK.value()
        and:
            TaskDto getTaskDto = MAPPER.readValue(getTaskResponse.contentAsString, TaskDto.class)
            getTaskDto.id == createdTaskDto.id
    }

    @Transactional
    def "Getting tasks should return code 200 and lis of all tasks"() {
        given:
            ProjectDto projectDto = this.createProject()
            StageDto stageDto = this.createStage(projectDto.id)
            String taskRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(properTaskDto)
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
            List<TaskDto> tasks = MAPPER.readValue(getTasksResponse.contentAsString, List<TaskDto>.class)
            tasks.size() == 2
    }

    private ProjectDto createProject() {
        ArchitectDto architectDto = TestsHelper.createArchitect(architect, this.createArchitectUri(), this.mockMvc)
        this.projectDto.architectId = architectDto.id
        ClientDto clientDto = TestsHelper.createClient(this.privateClientDto, this.createClientUri(), this.mockMvc)
        this.projectDto.clientId = clientDto.id
        return TestsHelper.createProject(this.projectDto, this.createBasicProjectUri(), this.mockMvc)
    }

    private StageDto createStage(long projectId) {
        String stageRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(stageDto)
        def stageResponse = this.mockMvc.perform(
                MockMvcRequestBuilders
                        .post(URI.create(HOST + ":" + port + PROJECTS_URI + "/" + projectId + STAGES_URI))
                        .header("Content-Type", "application/json")
                        .content(stageRequestBody)
        ).andReturn().response
        return MAPPER.readValue(stageResponse.contentAsString, StageDto.class)
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
