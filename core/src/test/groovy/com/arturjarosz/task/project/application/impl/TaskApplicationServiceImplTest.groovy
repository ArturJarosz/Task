package com.arturjarosz.task.project.application.impl

import com.arturjarosz.task.contract.status.validator.ContractWorkflowValidator
import com.arturjarosz.task.dto.TaskDto
import com.arturjarosz.task.dto.TaskStatusDto
import com.arturjarosz.task.dto.TaskTypeDto
import com.arturjarosz.task.project.application.ProjectValidator
import com.arturjarosz.task.project.application.StageValidator
import com.arturjarosz.task.project.application.TaskValidator
import com.arturjarosz.task.project.domain.TaskDomainService
import com.arturjarosz.task.project.infrastructure.repositor.ProjectRepository
import com.arturjarosz.task.project.model.Project
import com.arturjarosz.task.project.model.Stage
import com.arturjarosz.task.project.model.Task
import com.arturjarosz.task.project.model.dto.TaskInnerDto
import com.arturjarosz.task.project.query.ProjectQueryService
import com.arturjarosz.task.project.status.project.ProjectStatus
import com.arturjarosz.task.project.status.task.TaskStatus
import com.arturjarosz.task.utils.ProjectBuilder
import com.arturjarosz.task.utils.StageBuilder
import com.arturjarosz.task.utils.TaskBuilder
import spock.lang.Specification

class TaskApplicationServiceImplTest extends Specification {
    static final Long PROJECT_ID = 1L
    static final Long STAGE_ID = 10L
    static final Long TASK_ID = 20L
    static final String PROJECT_NAME = "projectName"
    static final String STAGE_NAME = "stageName"
    static final String TASK_NAME = "taskName"
    static final String NEW_TASK_NAME = "newTaskName"
    static final TaskStatusDto NEW_TASK_STATUS = TaskStatusDto.IN_PROGRESS

    def projectQueryService = Mock(ProjectQueryService)
    def projectRepository = Mock(ProjectRepository)
    def projectValidator = Mock(ProjectValidator)
    def stageValidator = Mock(StageValidator)
    def taskDomainService = Mock(TaskDomainService)
    def taskValidator = Mock(TaskValidator)
    def contractWorkflowValidator = Mock(ContractWorkflowValidator)

    def taskApplicationService = new TaskApplicationServiceImpl(projectQueryService, projectRepository,
            projectValidator, stageValidator, taskDomainService, taskValidator, contractWorkflowValidator)

    def "createTask should call validateProjectExistence on ProjectValidator"() {
        given:
            def taskDto = this.prepareNewTaskDto()
            this.mockProjectRepositoryLoad()
            this.mockTaskDomainServiceCreateTask()
            this.mockProjectRepositorySaveProjectWithStageAndTask()
        when:
            this.taskApplicationService.createTask(PROJECT_ID, STAGE_ID, taskDto)
        then:
            1 * this.projectValidator.validateProjectExistence(_ as Optional<Project>, PROJECT_ID)
    }

    def "createTask should call validateExistenceOfStageInProject on StageValidator"() {
        given:
            def taskDto = this.prepareNewTaskDto()
            this.mockProjectRepositoryLoad()
            this.mockTaskDomainServiceCreateTask()
            this.mockProjectRepositorySaveProjectWithStageAndTask()
        when:
            this.taskApplicationService.createTask(PROJECT_ID, STAGE_ID, taskDto)
        then:
            1 * this.stageValidator.validateExistenceOfStageInProject(PROJECT_ID, STAGE_ID)
    }

    def "createTask should call validateCreateTaskDto on TaskValidator"() {
        given:
            def taskDto = this.prepareNewTaskDto()
            this.mockProjectRepositoryLoad()
            this.mockTaskDomainServiceCreateTask()
            this.mockProjectRepositorySaveProjectWithStageAndTask()
        when:
            this.taskApplicationService.createTask(PROJECT_ID, STAGE_ID, taskDto)
        then:
            1 * this.taskValidator.validateCreateTaskDto(_ as TaskDto)
    }

    def "createTask should call validateContractAllowsForWorkObjectsCreation on contractWorkflowValidator"() {
        given:
            def taskDto = this.prepareNewTaskDto()
            this.mockProjectRepositoryLoad()
            this.mockTaskDomainServiceCreateTask()
            this.mockProjectRepositorySaveProjectWithStageAndTask()
        when:
            this.taskApplicationService.createTask(PROJECT_ID, STAGE_ID, taskDto)
        then:
            1 * this.contractWorkflowValidator.validateContractAllowsForWorkObjectsCreation(PROJECT_ID)
    }

    def "createTask should load project from ProjectRepository"() {
        given:
            def taskDto = this.prepareNewTaskDto()
            this.mockProjectRepositoryLoad()
            this.mockTaskDomainServiceCreateTask()
            this.mockProjectRepositorySaveProjectWithStageAndTask()
        when:
            this.taskApplicationService.createTask(PROJECT_ID, STAGE_ID, taskDto)
        then:
            1 * this.projectRepository.findById(PROJECT_ID) >> Optional.of(Mock(Project))
    }

    def "createTask should call createTask from TaskDomainRepository"() {
        given:
            def taskDto = this.prepareNewTaskDto()
            this.mockProjectRepositoryLoad()
            this.mockTaskDomainServiceCreateTask()
            this.mockProjectRepositorySaveProjectWithStageAndTask()
        when:
            this.taskApplicationService.createTask(PROJECT_ID, STAGE_ID, taskDto)
        then:
            1 * this.taskDomainService.createTask(_ as Project, STAGE_ID, _ as TaskDto) >> this.prepareNewTask()
    }

    def "createTask should call createTask on taskDomainService"() {
        given:
            def taskDto = this.prepareNewTaskDto()
            this.mockProjectRepositoryLoad()
            this.mockTaskDomainServiceCreateTask()
            this.mockProjectRepositorySaveProjectWithStageAndTask()
        when:
            this.taskApplicationService.createTask(PROJECT_ID, STAGE_ID, taskDto)
        then:
            1 * this.taskDomainService.createTask(_ as Project, STAGE_ID, _ as TaskDto)
    }

    def "createTask should save project with save on ProjectRepository"() {
        given:
            def taskDto = this.prepareNewTaskDto()
            this.mockProjectRepositoryLoad()
            this.mockTaskDomainServiceCreateTask()
        when:
            this.taskApplicationService.createTask(PROJECT_ID, STAGE_ID, taskDto)
        then:
            1 * this.projectRepository.save(_ as Project) >> this.prepareProjectWithStageWithTask()
    }

    def "updateTask should call validateProjectExistence on projectValidator"() {
        given:
            def taskDto = this.prepareUpdateTaskDto()
            this.mockProjectRepositoryLoadProjectWithStageAndTask()
        when:
            this.taskApplicationService.updateTask(PROJECT_ID, STAGE_ID, TASK_ID, taskDto)
        then:
            1 * this.projectValidator.validateProjectExistence(_ as Optional<Project>, PROJECT_ID)
    }

    def "updateTask should call validateExistenceOfStageInProject on stageValidator"() {
        given:
            def taskDto = this.prepareUpdateTaskDto()
            this.mockProjectRepositoryLoadProjectWithStageAndTask()
        when:
            this.taskApplicationService.updateTask(PROJECT_ID, STAGE_ID, TASK_ID, taskDto)
        then:
            1 * this.stageValidator.validateExistenceOfStageInProject(PROJECT_ID, STAGE_ID)
    }

    def "updateTask should call validateExistenceOfTaskInStage on taskValidator"() {
        given:
            def taskDto = this.prepareUpdateTaskDto()
            this.mockProjectRepositoryLoadProjectWithStageAndTask()
        when:
            this.taskApplicationService.updateTask(PROJECT_ID, STAGE_ID, TASK_ID, taskDto)
        then:
            1 * this.taskValidator.validateExistenceOfTaskInStage(STAGE_ID, TASK_ID)
    }

    def "updateTask should call validateUpdateTaskDto on taskValidator"() {
        given:
            def taskDto = this.prepareUpdateTaskDto()
            this.mockProjectRepositoryLoadProjectWithStageAndTask()
        when:
            this.taskApplicationService.updateTask(PROJECT_ID, STAGE_ID, TASK_ID, taskDto)
        then:
            1 * this.taskValidator.validateUpdateTaskDto(_ as TaskDto)
    }

    def "updateTask should load project from ProjectRepository"() {
        given:
            def taskDto = this.prepareUpdateTaskDto()
            this.mockProjectRepositoryLoadProjectWithStageAndTask()
        when:
            this.taskApplicationService.updateTask(PROJECT_ID, STAGE_ID, TASK_ID, taskDto)
        then:
            1 * this.projectRepository.findById(PROJECT_ID) >> Optional.of(this.prepareProjectWithStageWithTask())
    }

    def "updateTask should call updateTask on taskDomainService"() {
        given:
            def taskDto = this.prepareUpdateTaskDto()
            this.mockProjectRepositoryLoadProjectWithStageAndTask()
        when:
            this.taskApplicationService.updateTask(PROJECT_ID, STAGE_ID, TASK_ID, taskDto)
        then:
            1 * this.taskDomainService.updateTask(_ as Project, STAGE_ID, TASK_ID, _ as TaskInnerDto)
    }

    def "updateTask should save project with updated task on ProjectRepository"() {
        given:
            TaskDto taskDto = this.prepareUpdateTaskDto()
            this.mockProjectRepositoryLoadProjectWithStageAndTask()
        when:
            this.taskApplicationService.updateTask(PROJECT_ID, STAGE_ID, TASK_ID, taskDto)
        then:
            1 * this.projectRepository.save(_)
    }

    def "updateTaskStatus should call validateProjectExistence on projectValidator"() {
        given:
            def taskDto = this.prepareUpdateStatusTaskDto()
            this.mockProjectRepositoryLoadProjectWithStageAndTask()
        when:
            this.taskApplicationService.updateTaskStatus(PROJECT_ID, STAGE_ID, TASK_ID, taskDto)
        then:
            1 * this.projectValidator.validateProjectExistence(_ as Optional<Project>, PROJECT_ID)
    }

    def "updateTaskStatus should call validateExistenceOfStageInProject on stageValidator"() {
        given:
            def taskDto = this.prepareUpdateStatusTaskDto()
            this.mockProjectRepositoryLoadProjectWithStageAndTask()
        when:
            this.taskApplicationService.updateTaskStatus(PROJECT_ID, STAGE_ID, TASK_ID, taskDto)
        then:
            1 * this.stageValidator.validateExistenceOfStageInProject(PROJECT_ID, STAGE_ID)
    }

    def "updateTaskStatus should call validateExistenceTaskInStatus on taskValidator"() {
        given:
            def taskDto = this.prepareUpdateStatusTaskDto()
            this.mockProjectRepositoryLoadProjectWithStageAndTask()
        when:
            this.taskApplicationService.updateTaskStatus(PROJECT_ID, STAGE_ID, TASK_ID, taskDto)
        then:
            1 * this.taskValidator.validateExistenceOfTaskInStage(STAGE_ID, TASK_ID)
    }

    def "updateTaskStatus should load project from projectRepository"() {
        given:
            def taskDto = this.prepareUpdateStatusTaskDto()
            this.mockProjectRepositoryLoadProjectWithStageAndTask()
        when:
            this.taskApplicationService.updateTaskStatus(PROJECT_ID, STAGE_ID, TASK_ID, taskDto)
        then:
            1 * this.projectRepository.findById(PROJECT_ID) >> Optional.of(this.prepareProjectWithStageWithTask())
    }

    def "updateStatus should call updateTaskStatus on taskDomainService"() {
        given:
            def taskDto = this.prepareUpdateStatusTaskDto()
            this.mockProjectRepositoryLoadProjectWithStageAndTask()
        when:
            this.taskApplicationService.updateTaskStatus(PROJECT_ID, STAGE_ID, TASK_ID, taskDto)
        then:
            1 * this.taskDomainService.updateTaskStatus(_ as Project, STAGE_ID, TASK_ID, _ as TaskStatus)
    }

    def "updateStatus should save project on projectRepository"() {
        given:
            def taskDto = this.prepareUpdateStatusTaskDto()
            this.mockProjectRepositoryLoadProjectWithStageAndTask()
        when:
            this.taskApplicationService.updateTaskStatus(PROJECT_ID, STAGE_ID, TASK_ID, taskDto)
        then:
            1 * this.projectRepository.save(_)
    }

    def "getTask should call validateProjectExistence on projectValidator"() {
        given:
            this.mockProjectQueryServiceGetTask()
        when:
            this.taskApplicationService.getTask(PROJECT_ID, STAGE_ID, TASK_ID)
        then:
            1 * this.projectValidator.validateProjectExistence(PROJECT_ID)
    }

    def "getTask should call validateExistenceOFStageInProject on stageValidator"() {
        given:
            this.mockProjectQueryServiceGetTask()
        when:
            this.taskApplicationService.getTask(PROJECT_ID, STAGE_ID, TASK_ID)
        then:
            1 * this.stageValidator.validateExistenceOfStageInProject(PROJECT_ID, STAGE_ID)
    }

    def "getTask should call validateExistenceOfTaskInStage on taskValidator"() {
        given:
            this.mockProjectQueryServiceGetTask()
        when:
            this.taskApplicationService.getTask(PROJECT_ID, STAGE_ID, TASK_ID)
        then:
            1 * this.taskValidator.validateExistenceOfTaskInStage(STAGE_ID, TASK_ID)
    }

    def "getTask should load task by getTaskByTaskId on projectQueryService"() {
        given:
            this.mockProjectQueryServiceGetTask()
        when:
            this.taskApplicationService.getTask(PROJECT_ID, STAGE_ID, TASK_ID)
        then:
            1 * this.projectQueryService.getTaskByTaskId(TASK_ID)
    }

    def "getTaskList should call validateProjectExistence on projectValidator"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStageAndTask()
        when:
            this.taskApplicationService.getTaskList(PROJECT_ID, STAGE_ID)
        then:
            1 * this.projectValidator.validateProjectExistence(_ as Optional<Project>, PROJECT_ID)
    }

    def "getTaskList should call validateExistenceOfStageInProject on stageValidator"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStageAndTask()
        when:
            this.taskApplicationService.getTaskList(PROJECT_ID, STAGE_ID)
        then:
            1 * this.stageValidator.validateExistenceOfStageInProject(PROJECT_ID, STAGE_ID)

    }

    def "getTaskList should load project from projectRepository"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStageAndTask()
        when:
            this.taskApplicationService.getTaskList(PROJECT_ID, STAGE_ID)
        then:
            1 * this.projectRepository.findById(PROJECT_ID) >> Optional.of(this.prepareProjectWithStageWithTask())
    }

    def "getTaskList should return list of task from given stage"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStageAndTask()
        when:
            List<TaskDto> taskDtos = this.taskApplicationService.getTaskList(PROJECT_ID, STAGE_ID)
        then:
            taskDtos.size() == 1
    }

    def "rejectTask should call validateProjectExistence on projectValidator"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStageAndTask()
        when:
            this.taskApplicationService.rejectTask(PROJECT_ID, STAGE_ID, TASK_ID)
        then:
            1 * this.projectValidator.validateProjectExistence(_ as Optional<Project>, PROJECT_ID)
    }

    def "rejectTask should call validateExistenceOFStageInProject on stageValidator"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStageAndTask()
        when:
            this.taskApplicationService.rejectTask(PROJECT_ID, STAGE_ID, TASK_ID)
        then:
            1 * this.stageValidator.validateExistenceOfStageInProject(PROJECT_ID, STAGE_ID)
    }

    def "rejectTask should call validateExistenceOfTaskInStage on taskValidator"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStageAndTask()
        when:
            this.taskApplicationService.rejectTask(PROJECT_ID, STAGE_ID, TASK_ID)
        then:
            1 * this.taskValidator.validateExistenceOfTaskInStage(STAGE_ID, TASK_ID)
    }

    def "rejectTask should load project from projectRepository"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStageAndTask()
        when:
            this.taskApplicationService.rejectTask(PROJECT_ID, STAGE_ID, TASK_ID)
        then:
            1 * this.projectRepository.findById(PROJECT_ID) >> Optional.of(this.prepareProjectWithStageWithTask())
    }

    def "rejectTask should call rejectTask on taskDomainService"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStageAndTask()
        when:
            this.taskApplicationService.rejectTask(PROJECT_ID, STAGE_ID, TASK_ID)
        then:
            1 * this.taskDomainService.rejectTask(_ as Project, STAGE_ID, TASK_ID)
    }

    def "rejectTask should save project on projectRepository"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStageAndTask()
        when:
            this.taskApplicationService.rejectTask(PROJECT_ID, STAGE_ID, TASK_ID)
        then:
            1 * this.projectRepository.save(_ as Project)
    }

    def "reopenTask should call validateProjectExistence on projectValidator"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStageAndTask()
        when:
            this.taskApplicationService.reopenTask(PROJECT_ID, STAGE_ID, TASK_ID)
        then:
            1 * this.projectValidator.validateProjectExistence(_ as Optional<Project>, PROJECT_ID)
    }

    def "reopenTask should call validateExistenceOfStageInProject on stageValidator"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStageAndTask()
        when:
            this.taskApplicationService.reopenTask(PROJECT_ID, STAGE_ID, TASK_ID)
        then:
            1 * this.stageValidator.validateExistenceOfStageInProject(PROJECT_ID, STAGE_ID)
    }

    def "reopenTask should call validateExistenceOfTaskInStage on taskValidator"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStageAndTask()
        when:
            this.taskApplicationService.reopenTask(PROJECT_ID, STAGE_ID, TASK_ID)
        then:
            1 * this.taskValidator.validateExistenceOfTaskInStage(STAGE_ID, TASK_ID)
    }

    def "reopenTask should load project from projectRepository"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStageAndTask()
        when:
            this.taskApplicationService.reopenTask(PROJECT_ID, STAGE_ID, TASK_ID)
        then:
            1 * this.projectRepository.findById(PROJECT_ID) >> Optional.of(this.prepareProjectWithStageWithTask())
    }

    def "reopenTask should call reopenTask on taskDomainService"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStageAndTask()
        when:
            this.taskApplicationService.reopenTask(PROJECT_ID, STAGE_ID, TASK_ID)
        then:
            1 * this.taskDomainService.reopenTask(_ as Project, STAGE_ID, TASK_ID)
    }

    def "reopenTask should save project on projectRepository"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStageAndTask()
        when:
            this.taskApplicationService.reopenTask(PROJECT_ID, STAGE_ID, TASK_ID)
        then:
            1 * this.projectRepository.save(_)
    }

    private TaskDto prepareNewTaskDto() {
        new TaskDto(name: TASK_NAME, id: TASK_ID, type: TaskTypeDto.CONCEPT)
    }

    private TaskDto prepareUpdateTaskDto() {
        new TaskDto(name: NEW_TASK_NAME, id: TASK_ID, type: TaskTypeDto.CONCEPT)

    }

    private TaskDto prepareUpdateStatusTaskDto() {
        TaskDto taskDto = new TaskDto(id: TASK_ID, status: NEW_TASK_STATUS)
        taskDto
    }

    private Project prepareProjectWithStage() {
        return new ProjectBuilder()
                .withName(PROJECT_NAME)
                .withId(PROJECT_ID)
                .withStage(this.prepareStage())
                .withStatus(ProjectStatus.TO_DO)
                .build()
    }

    private Project prepareProjectWithStageWithTask() {
        return new ProjectBuilder()
                .withName(PROJECT_NAME)
                .withId(PROJECT_ID)
                .withStage(this.prepareStageWithTask())
                .withStatus(ProjectStatus.TO_DO)
                .build()
    }

    private Stage prepareStage() {
        return new StageBuilder()
                .withName(STAGE_NAME)
                .withId(STAGE_ID)
                .build()
    }

    private Stage prepareStageWithTask() {
        return new StageBuilder()
                .withName(STAGE_NAME)
                .withId(STAGE_ID)
                .withTask(this.prepareNewTask())
                .build()
    }

    private void mockProjectRepositoryLoad() {
        def project = this.prepareProjectWithStage()
        this.projectRepository.findById(PROJECT_ID) >> Optional.of(project)
    }

    private void mockProjectRepositoryLoadProjectWithStageAndTask() {
        def project = this.prepareProjectWithStageWithTask()
        this.projectRepository.findById(PROJECT_ID) >> Optional.of(project)
    }

    private void mockTaskDomainServiceCreateTask() {
        def task = this.prepareNewTask()
        this.taskDomainService.createTask(_ as Project, _ as Long, _ as TaskDto) >> task
    }

    private void mockProjectQueryServiceGetTask() {
        def taskDto = this.prepareNewTaskDto()
        this.projectQueryService.getTaskByTaskId(TASK_ID) >> taskDto
    }

    private Task prepareNewTask() {
        return new TaskBuilder()
                .withId(TASK_ID)
                .withName(TASK_NAME)
                .withStatus(TaskStatus.TO_DO)
                .build()
    }

    private void mockProjectRepositorySaveProjectWithStageAndTask() {
        1 * this.projectRepository.save(_ as Project) >> this.prepareProjectWithStageWithTask()
    }
}
