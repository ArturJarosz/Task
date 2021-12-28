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
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.transaction.annotation.Transactional
import spock.lang.Shared

class TaskTestIT extends BaseTestIT {
    private static final String ARCHITECT_FIRST_NAME = "First Name"
    private static final String ARCHITECT_LAST_NAME = "Last Name"
    private static final String ARCHITECTS_URI = "/architects"
    private static final String CLIENTS_URI = "/clients"
    private static final String PROJECTS_URI = "/projects"
    private static final String STAGES_URI = "/stages"
    private static final String TASKS_URI = "/tasks"
    private static final long NOT_EXISTING_PROJECT_ID = 10000l
    private static final long NOT_EXISTING_STAGE_ID = 10000l
    private static final long NOT_EXISTING_TASK_ID = 10000l

    @Shared
    private final ObjectMapper mapper = new ObjectMapper()
    private final ClientDto privateClientDto =
            mapper.readValue(new File(getClass().classLoader.getResource('json/client/privateClient.json').file),
                    ClientDto.class)
    private final ProjectCreateDto projectDto =
            mapper.readValue(new File(getClass().classLoader.getResource('json/project/properProject.json').file),
                    ProjectCreateDto.class)
    private final StageDto stageDto =
            mapper.readValue(new File(getClass().classLoader.getResource('json/stage/properStage.json').file),
                    StageDto.class)
    private final TaskDto properTaskDto =
            mapper.readValue(new File(getClass().classLoader.getResource('json/task/properTask.json').file),
                    TaskDto.class)
    private final TaskDto notProperTaskDto =
            mapper.readValue(new File(getClass().classLoader.getResource('json/task/notProperTask.json').file),
                    TaskDto.class)
    private final TaskDto properTaskUpdateDto =
            mapper.readValue(new File(getClass().classLoader.getResource('json/task/properTaskUpdate.json').file),
                    TaskDto.class)
    private final TaskDto notProperTaskUpdateDto =
            mapper.readValue(new File(getClass().classLoader.getResource('json/task/notProperTaskUpdate.json').file),
                    TaskDto.class)

    @Autowired
    private MockMvc mockMvc

    @Override
    def setupSpec() {
        mapper.findAndRegisterModules()
    }

    @Transactional
    def "Creating task for not existing project should return code 400 and error message"() {
        given:
            String taskRequestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(properTaskDto)
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
            def message = mapper.readValue(taskResponse.contentAsString, ErrorMessage.class)
            message.getMessage() == "Project with id 10,000 does not exist."
    }

    @Transactional
    def "Creating task for not existing stage should return code 400 and error message"() {
        given:
            ProjectDto projectDto = this.createProject()
            String taskRequestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(properTaskDto)
        when:
            def taskResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.taskUrlBuilder(projectDto.id, NOT_EXISTING_STAGE_ID)))
                            .header("Content-Type", "application/json")
                            .content(taskRequestBody)
            ).andReturn().response
        then: "Returns code 400"
            taskResponse.status == HttpStatus.BAD_REQUEST.value()
        and:
            def message = mapper.readValue(taskResponse.contentAsString, ErrorMessage.class)
            message.getMessage() == "Stage with id 10,000 does not exist."
    }

    @Transactional
    def "Creating task with not proper dto should return code 400 and error message"() {
        given:
            ProjectDto projectDto = this.createProject()
            StageDto stageDto = this.createStage(projectDto.id)
            String taskRequestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(notProperTaskDto)
        when:
            def taskResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.taskUrlBuilder(projectDto.id, stageDto.id)))
                            .header("Content-Type", "application/json")
                            .content(taskRequestBody)
            ).andReturn().response
        then:
            taskResponse.status == HttpStatus.BAD_REQUEST.value()
        and:
            def message = mapper.readValue(taskResponse.contentAsString, ErrorMessage.class)
            message.getMessage() == "Task name cannot be empty."
    }

    @Transactional
    def "Creating task for existing stage and project with proper date should return code 201, created task and location header"() {
        given:
            ProjectDto projectDto = this.createProject()
            StageDto stageDto = this.createStage(projectDto.id)
            String taskRequestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(properTaskDto)
        when:
            def taskResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.taskUrlBuilder(projectDto.id, stageDto.id)))
                            .header("Content-Type", "application/json")
                            .content(taskRequestBody)
            ).andReturn().response
        then:
            TaskDto createdTaskDto = mapper.readValue(taskResponse.contentAsString, TaskDto.class)
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
            def message = mapper.readValue(taskResponse.contentAsString, ErrorMessage.class)
            message.getMessage() == "Task with id 10,000 does not exist on stage with id " + stageDto.id + "."
    }

    @Transactional
    def "Deleting existing task should return code 200 and remove it"() {
        given:
            ProjectDto projectDto = this.createProject()
            StageDto stageDto = this.createStage(projectDto.id)
            String taskRequestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(properTaskDto)
            def taskResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.taskUrlBuilder(projectDto.id, stageDto.id)))
                            .header("Content-Type", "application/json")
                            .content(taskRequestBody)
            ).andReturn().response
            TaskDto createdTaskDto = mapper.readValue(taskResponse.contentAsString, TaskDto.class)
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
            def message = mapper.readValue(getTaskResponse.contentAsString, ErrorMessage.class)
            message.getMessage() == "Task with id " +
                    createdTaskDto.id + " does not exist on stage with id " + stageDto.id + "."
    }

    @Transactional
    def "Updating not existing task should return code 400 and error message"() {
        given:
            ProjectDto projectDto = this.createProject()
            StageDto stageDto = this.createStage(projectDto.id)
            String taskRequestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(properTaskUpdateDto)
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
            def message = mapper.readValue(taskResponse.contentAsString, ErrorMessage.class)
            message.getMessage() == "Task with id 10,000 does not exist on stage with id " + stageDto.id + "."
    }

    @Transactional
    def "Updating task with not proper dto should return code 400 and error message"() {
        given:
            ProjectDto projectDto = this.createProject()
            StageDto stageDto = this.createStage(projectDto.id)
            String taskRequestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(properTaskDto)
            def taskResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.taskUrlBuilder(projectDto.id, stageDto.id)))
                            .header("Content-Type", "application/json")
                            .content(taskRequestBody)
            ).andReturn().response
            TaskDto createdTaskDto = mapper.readValue(taskResponse.contentAsString, TaskDto.class)
            String taskUpdateRequestBody =
                    mapper.writerWithDefaultPrettyPrinter().writeValueAsString(notProperTaskUpdateDto)
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
            def message = mapper.readValue(taskUpdateResponse.contentAsString, ErrorMessage.class)
            message.getMessage() == "Task name cannot be empty."
    }

    @Transactional
    def "Updating existing task with proper dto should return code 200 and dto of updated task"() {
        given:
            ProjectDto projectDto = this.createProject()
            StageDto stageDto = this.createStage(projectDto.id)
            String taskRequestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(properTaskDto)
            def taskResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.taskUrlBuilder(projectDto.id, stageDto.id)))
                            .header("Content-Type", "application/json")
                            .content(taskRequestBody)
            ).andReturn().response
            TaskDto createdTaskDto = mapper.readValue(taskResponse.contentAsString, TaskDto.class)
            String taskUpdateRequestBody =
                    mapper.writerWithDefaultPrettyPrinter().writeValueAsString(properTaskUpdateDto)
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
            TaskDto updatedTaskDto = mapper.readValue(taskUpdateResponse.contentAsString, TaskDto.class)
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
            def message = mapper.readValue(taskResponse.contentAsString, ErrorMessage.class)
            message.getMessage() == "Task with id 10,000 does not exist on stage with id " + stageDto.id + "."
    }

    @Transactional
    def "Getting existing task should return code 200 and dto of task"() {
        given:
            ProjectDto projectDto = this.createProject()
            StageDto stageDto = this.createStage(projectDto.id)
            String taskRequestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(properTaskDto)
            def taskResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.taskUrlBuilder(projectDto.id, stageDto.id)))
                            .header("Content-Type", "application/json")
                            .content(taskRequestBody)
            ).andReturn().response
            TaskDto createdTaskDto = mapper.readValue(taskResponse.contentAsString, TaskDto.class)
        when:
            def getTaskResponse = this.mockMvc.perform(MockMvcRequestBuilders.get(URI
                    .create(this.taskUrlBuilder(projectDto.id, stageDto.id) + "/" + createdTaskDto.id))
            ).andReturn().response
        then:
            getTaskResponse.status == HttpStatus.OK.value()
        and:
            TaskDto getTaskDto = mapper.readValue(getTaskResponse.contentAsString, TaskDto.class)
            getTaskDto.id == createdTaskDto.id
    }

    @Transactional
    def "Getting tasks should return code 200 and lis of all tasks"() {
        given:
            ProjectDto projectDto = this.createProject()
            StageDto stageDto = this.createStage(projectDto.id)
            String taskRequestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(properTaskDto)
            def taskResponse = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.taskUrlBuilder(projectDto.id, stageDto.id)))
                            .header("Content-Type", "application/json")
                            .content(taskRequestBody)
            ).andReturn().response
            def taskResponse2 = this.mockMvc.perform(
                    MockMvcRequestBuilders.post(URI.create(this.taskUrlBuilder(projectDto.id, stageDto.id)))
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
            List<TaskDto> tasks = mapper.readValue(getTasksResponse.contentAsString, List<TaskDto>.class)
            tasks.size() == 2
    }

    private ArchitectDto createArchitect() {
        ArchitectBasicDto architectBasicDto = new ArchitectBasicDto(firstName: ARCHITECT_FIRST_NAME,
                lastName: ARCHITECT_LAST_NAME)
        String architectRequestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(architectBasicDto)
        def architectResponse = this.mockMvc.perform(
                MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + ARCHITECTS_URI))
                        .header("Content-Type", "application/json")
                        .content(architectRequestBody)
        ).andReturn().response.contentAsString
        return mapper.readValue(architectResponse, ArchitectDto.class)
    }

    private ClientDto createClient() {
        String clientRequestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(privateClientDto)
        def clientResponse = this.mockMvc.perform(
                MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + CLIENTS_URI))
                        .header("Content-Type", "application/json")
                        .content(clientRequestBody)
        ).andReturn().response.contentAsString
        return mapper.readValue(clientResponse, ClientDto.class)
    }

    private ProjectDto createProject() {
        ArchitectDto architectDto = this.createArchitect()
        this.projectDto.architectId = architectDto.id
        ClientDto clientDto = this.createClient()
        this.projectDto.clientId = clientDto.id
        String projectRequestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(projectDto)
        def projectResponse = this.mockMvc.perform(
                MockMvcRequestBuilders.post(URI.create(HOST + ":" + port + PROJECTS_URI))
                        .header("Content-Type", "application/json")
                        .content(projectRequestBody)
        ).andReturn().response
        return mapper.readValue(projectResponse.contentAsString, ProjectDto.class)
    }

    private StageDto createStage(long projectId) {
        String stageRequestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(stageDto)
        def stageResponse = this.mockMvc.perform(
                MockMvcRequestBuilders
                        .post(URI.create(HOST + ":" + port + PROJECTS_URI + "/" + projectId + STAGES_URI))
                        .header("Content-Type", "application/json")
                        .content(stageRequestBody)
        ).andReturn().response
        return mapper.readValue(stageResponse.contentAsString, StageDto.class)
    }

    private String taskUrlBuilder(long projectId, long stageId) {
        return HOST + ":" + port + PROJECTS_URI + "/" + projectId + STAGES_URI + "/" + stageId + TASKS_URI
    }
}
