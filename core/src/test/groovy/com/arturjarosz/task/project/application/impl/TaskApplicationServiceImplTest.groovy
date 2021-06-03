package com.arturjarosz.task.project.application.impl

import com.arturjarosz.task.project.application.ProjectValidator
import com.arturjarosz.task.project.application.StageValidator
import com.arturjarosz.task.project.application.TaskValidator
import com.arturjarosz.task.project.application.dto.TaskDto
import com.arturjarosz.task.project.domain.TaskDomainService
import com.arturjarosz.task.project.infrastructure.repositor.ProjectRepository
import com.arturjarosz.task.project.model.Project
import com.arturjarosz.task.project.model.Stage
import com.arturjarosz.task.project.model.Task
import com.arturjarosz.task.project.model.TaskType
import com.arturjarosz.task.project.model.dto.TaskInnerDto
import com.arturjarosz.task.project.query.ProjectQueryService
import com.arturjarosz.task.project.status.project.ProjectStatus
import com.arturjarosz.task.project.status.task.TaskStatus
import com.arturjarosz.task.project.status.task.TaskWorkflowService
import com.arturjarosz.task.project.utils.ProjectBuilder
import com.arturjarosz.task.project.utils.StageBuilder
import com.arturjarosz.task.project.utils.TaskBuilder
import spock.lang.Specification

class TaskApplicationServiceImplTest extends Specification {
    private static final Long PROJECT_ID = 1L;
    private static final Long STAGE_ID = 10L;
    private static final Long TASK_ID = 20L;
    private static final String PROJECT_NAME = "projectName";
    private static final String STAGE_NAME = "stageName";
    private static final String TASK_NAME = "taskName";
    private static final String NEW_TASK_NAME = "newTaskName"
    private static final String NOTE = "note";
    private static final TaskStatus NEW_TASK_STATUS = TaskStatus.IN_PROGRESS;

    private projectQueryService = Mock(ProjectQueryService);
    private projectRepository = Mock(ProjectRepository);
    private projectValidator = Mock(ProjectValidator);
    private stageValidator = Mock(StageValidator);
    private taskDomainService = Mock(TaskDomainService);
    private taskWorkflowService = Mock(TaskWorkflowService);
    private taskValidator = Mock(TaskValidator);

    def taskApplicationService = new TaskApplicationServiceImpl(projectQueryService, projectRepository,
            projectValidator, stageValidator, taskDomainService, taskValidator);

    def "createTask should call validateProjectExistence on ProjectValidator"() {
        given:
            TaskDto taskDto = this.prepareNewTaskDto();
            this.mockProjectRepositoryLoad();
            this.mockTaskDomainServiceCreateTask();
            this.mockProjectRepositorySaveProjectWithStageAndTask();
        when:
            this.taskApplicationService.createTask(PROJECT_ID, STAGE_ID, taskDto);
        then:
            1 * this.projectValidator.validateProjectExistence(PROJECT_ID);
    }

    def "createTask should call validateExistenceOfStageInProject on StageValidator"() {
        given:
            TaskDto taskDto = this.prepareNewTaskDto();
            this.mockProjectRepositoryLoad();
            this.mockTaskDomainServiceCreateTask();
            this.mockProjectRepositorySaveProjectWithStageAndTask();
        when:
            this.taskApplicationService.createTask(PROJECT_ID, STAGE_ID, taskDto);
        then:
            1 * this.stageValidator.validateExistenceOfStageInProject(PROJECT_ID, STAGE_ID);
    }

    def "createTask should call validateCreateTaskDto on TaskValidator"() {
        given:
            TaskDto taskDto = this.prepareNewTaskDto();
            this.mockProjectRepositoryLoad();
            this.mockTaskDomainServiceCreateTask();
            this.mockProjectRepositorySaveProjectWithStageAndTask();
        when:
            this.taskApplicationService.createTask(PROJECT_ID, STAGE_ID, taskDto);
        then:
            1 * this.taskValidator.validateCreateTaskDto(_ as TaskDto);
    }

    def "createTask should load project from ProjectRepository"() {
        given:
            TaskDto taskDto = this.prepareNewTaskDto();
            this.mockProjectRepositoryLoad();
            this.mockTaskDomainServiceCreateTask();
            this.mockProjectRepositorySaveProjectWithStageAndTask();
        when:
            this.taskApplicationService.createTask(PROJECT_ID, STAGE_ID, taskDto);
        then:
            1 * this.projectRepository.load(PROJECT_ID) >> Mock(Project);
    }

    def "createTask should call createTask from TaskDomainRepository"() {
        given:
            TaskDto taskDto = this.prepareNewTaskDto();
            this.mockProjectRepositoryLoad();
            this.mockTaskDomainServiceCreateTask();
            this.mockProjectRepositorySaveProjectWithStageAndTask();
        when:
            this.taskApplicationService.createTask(PROJECT_ID, STAGE_ID, taskDto);
        then:
            1 * this.taskDomainService.createTask(_ as Project, STAGE_ID, _ as TaskDto) >> this.prepareNewTask();
    }

    def "createTask should call createTask on taskDomainService"() {
        given:
            TaskDto taskDto = this.prepareNewTaskDto();
            this.mockProjectRepositoryLoad();
            this.mockTaskDomainServiceCreateTask();
            this.mockProjectRepositorySaveProjectWithStageAndTask();
        when:
            this.taskApplicationService.createTask(PROJECT_ID, STAGE_ID, taskDto);
        then:
            1 * this.taskDomainService.createTask(_ as Project, STAGE_ID, _ as TaskDto);
    }

    def "createTask should save project with save on ProjectRepository"() {
        given:
            TaskDto taskDto = this.prepareNewTaskDto();
            this.mockProjectRepositoryLoad();
            this.mockTaskDomainServiceCreateTask();
        when:
            this.taskApplicationService.createTask(PROJECT_ID, STAGE_ID, taskDto);
        then:
            1 * this.projectRepository.save(_ as Project) >> this.prepareProjectWithStageWithTask();
    }

    def "updateTask should call validateProjectExistence on projectValidator"() {
        given:
            TaskDto taskDto = this.prepareUpdateTaskDto();
            this.mockProjectRepositoryLoadProjectWithStageAndTask();
        when:
            this.taskApplicationService.updateTask(PROJECT_ID, STAGE_ID, TASK_ID, taskDto);
        then:
            1 * this.projectValidator.validateProjectExistence(PROJECT_ID);
    }

    def "updateTask should call validateExistenceOfStageInProject on stageValidator"() {
        given:
            TaskDto taskDto = this.prepareUpdateTaskDto();
            this.mockProjectRepositoryLoadProjectWithStageAndTask();
        when:
            this.taskApplicationService.updateTask(PROJECT_ID, STAGE_ID, TASK_ID, taskDto);
        then:
            1 * this.stageValidator.validateExistenceOfStageInProject(PROJECT_ID, STAGE_ID);
    }

    def "updateTask should call validateExistenceOfTaskInStage on taskValidator"() {
        given:
            TaskDto taskDto = this.prepareUpdateTaskDto();
            this.mockProjectRepositoryLoadProjectWithStageAndTask();
        when:
            this.taskApplicationService.updateTask(PROJECT_ID, STAGE_ID, TASK_ID, taskDto);
        then:
            1 * this.taskValidator.validateExistenceOfTaskInStage(STAGE_ID, TASK_ID);
    }

    def "updateTask should load project from ProjectRepository"() {
        given:
            TaskDto taskDto = this.prepareUpdateTaskDto();
            this.mockProjectRepositoryLoadProjectWithStageAndTask();
        when:
            this.taskApplicationService.updateTask(PROJECT_ID, STAGE_ID, TASK_ID, taskDto);
        then:
            1 * this.projectRepository.load(PROJECT_ID) >> this.prepareProjectWithStageWithTask();
    }

    def "updateTask should call updateTask on taskDomainService"() {
        given:
            TaskDto taskDto = this.prepareUpdateTaskDto();
            this.mockProjectRepositoryLoadProjectWithStageAndTask();
        when:
            this.taskApplicationService.updateTask(PROJECT_ID, STAGE_ID, TASK_ID, taskDto);
        then:
            1 * this.taskDomainService.updateTask(_ as Project, STAGE_ID, TASK_ID, _ as TaskInnerDto);
    }

    def "updateTask should save project with updated task on ProjectRepository"() {
        given:
            TaskDto taskDto = this.prepareUpdateTaskDto();
            this.mockProjectRepositoryLoadProjectWithStageAndTask();
        when:
            this.taskApplicationService.updateTask(PROJECT_ID, STAGE_ID, TASK_ID, taskDto);
        then:
            1 * this.projectRepository.save(_);
    }

    def "updateTaskStatus should call validateProjectExistence on projectValidator"() {
        given:
            TaskDto taskDto = this.prepareUpdateStatusTaskDto();
            this.mockProjectRepositoryLoadProjectWithStageAndTask();
        when:
            this.taskApplicationService.updateTaskStatus(PROJECT_ID, STAGE_ID, TASK_ID, taskDto);
        then:
            1 * this.projectValidator.validateProjectExistence(PROJECT_ID);
    }

    def "updateTaskStatus should call validateExistenceOfStageInProject on stageValidator"() {
        given:
            TaskDto taskDto = this.prepareUpdateStatusTaskDto();
            this.mockProjectRepositoryLoadProjectWithStageAndTask();
        when:
            this.taskApplicationService.updateTaskStatus(PROJECT_ID, STAGE_ID, TASK_ID, taskDto);
        then:
            1 * this.stageValidator.validateExistenceOfStageInProject(PROJECT_ID, STAGE_ID);
    }

    def "updateTaskStatus should call validateExistenceTaskInStatus on taskValidator"() {
        given:
            TaskDto taskDto = this.prepareUpdateStatusTaskDto();
            this.mockProjectRepositoryLoadProjectWithStageAndTask();
        when:
            this.taskApplicationService.updateTaskStatus(PROJECT_ID, STAGE_ID, TASK_ID, taskDto);
        then:
            1 * this.taskValidator.validateExistenceOfTaskInStage(STAGE_ID, TASK_ID);
    }

    def "updateTaskStatus should load project from projectRepository"() {
        given:
            TaskDto taskDto = this.prepareUpdateStatusTaskDto();
            this.mockProjectRepositoryLoadProjectWithStageAndTask();
        when:
            this.taskApplicationService.updateTaskStatus(PROJECT_ID, STAGE_ID, TASK_ID, taskDto);
        then:
            1 * this.projectRepository.load(PROJECT_ID) >> this.prepareProjectWithStageWithTask();
    }

    def "updateStatus should call updateTaskStatus on taskDomainService"() {
        given:
            TaskDto taskDto = this.prepareUpdateStatusTaskDto();
            this.mockProjectRepositoryLoadProjectWithStageAndTask();
        when:
            this.taskApplicationService.updateTaskStatus(PROJECT_ID, STAGE_ID, TASK_ID, taskDto);
        then:
            1 * this.taskDomainService.updateTaskStatus(_ as Project, STAGE_ID, TASK_ID, _ as TaskStatus);
    }

    def "updateStatus should save project on projectRepository"() {
        given:
            TaskDto taskDto = this.prepareUpdateStatusTaskDto();
            this.mockProjectRepositoryLoadProjectWithStageAndTask();
        when:
            this.taskApplicationService.updateTaskStatus(PROJECT_ID, STAGE_ID, TASK_ID, taskDto);
        then:
            1 * this.projectRepository.save(_);
    }

    def "getTask should call validateProjectExistence on projectValidator"() {
        given:
            this.mockProjectQueryServiceGetTask();
        when:
            this.taskApplicationService.getTask(PROJECT_ID, STAGE_ID, TASK_ID);
        then:
            1 * this.projectValidator.validateProjectExistence(PROJECT_ID);
    }

    def "getTask should call validateExistenceOFStageInProject on stageValidator"() {
        given:
            this.mockProjectQueryServiceGetTask();
        when:
            this.taskApplicationService.getTask(PROJECT_ID, STAGE_ID, TASK_ID);
        then:
            1 * this.stageValidator.validateExistenceOfStageInProject(PROJECT_ID, STAGE_ID);
    }

    def "getTask should call validateExistenceOfTaskInStage on taskValidator"() {
        given:
            this.mockProjectQueryServiceGetTask();
        when:
            this.taskApplicationService.getTask(PROJECT_ID, STAGE_ID, TASK_ID);
        then:
            1 * this.taskValidator.validateExistenceOfTaskInStage(STAGE_ID, TASK_ID);
    }

    def "getTask should load task by getTaskByTaskId on projectQueryService"() {
        given:
            this.mockProjectQueryServiceGetTask();
        when:
            this.taskApplicationService.getTask(PROJECT_ID, STAGE_ID, TASK_ID);
        then:
            1 * this.projectQueryService.getTaskByTaskId(TASK_ID);
    }

    def "getTaskList should call validateProjectExistence on projectValidator"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStageAndTask();
        when:
            this.taskApplicationService.getTaskList(PROJECT_ID, STAGE_ID);
        then:
            1 * this.projectValidator.validateProjectExistence(PROJECT_ID);
    }

    def "getTaskList should call validateExistenceOfStageInProject on stageValidator"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStageAndTask();
        when:
            this.taskApplicationService.getTaskList(PROJECT_ID, STAGE_ID);
        then:
            1 * this.stageValidator.validateExistenceOfStageInProject(PROJECT_ID, STAGE_ID);

    }

    def "getTaskList should load project from projectRepository"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStageAndTask();
        when:
            this.taskApplicationService.getTaskList(PROJECT_ID, STAGE_ID);
        then:
            1 * this.projectRepository.load(PROJECT_ID) >> this.prepareProjectWithStageWithTask();
    }

    def "getTaskList should return list of task from given stage"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStageAndTask();
        when:
            List<TaskDto> taskDtos = this.taskApplicationService.getTaskList(PROJECT_ID, STAGE_ID);
        then:
            taskDtos.size() == 1;
    }

    def "rejectTask should call validateProjectExistence on projectValidator"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStageAndTask();
        when:
            this.taskApplicationService.rejectTask(PROJECT_ID, STAGE_ID, TASK_ID);
        then:
            1 * this.projectValidator.validateProjectExistence(PROJECT_ID);
    }

    def "rejectTask should call validateExistenceOFStageInProject on stageValidator"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStageAndTask();
        when:
            this.taskApplicationService.rejectTask(PROJECT_ID, STAGE_ID, TASK_ID);
        then:
            1 * this.stageValidator.validateExistenceOfStageInProject(PROJECT_ID, STAGE_ID);
    }

    def "rejectTask should call validateExistenceOfTaskInStage on taskValidator"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStageAndTask();
        when:
            this.taskApplicationService.rejectTask(PROJECT_ID, STAGE_ID, TASK_ID);
        then:
            1 * this.taskValidator.validateExistenceOfTaskInStage(STAGE_ID, TASK_ID);
    }

    def "rejectTask should load project from projectRepository"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStageAndTask();
        when:
            this.taskApplicationService.rejectTask(PROJECT_ID, STAGE_ID, TASK_ID);
        then:
            1 * this.projectRepository.load(PROJECT_ID) >> this.prepareProjectWithStageWithTask();
    }

    def "rejectTask should call rejectTask on taskDomainService"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStageAndTask();
        when:
            this.taskApplicationService.rejectTask(PROJECT_ID, STAGE_ID, TASK_ID);
        then:
            1 * this.taskDomainService.rejectTask(_ as Project, STAGE_ID, TASK_ID);
    }

    def "rejectTask should save project on projectRepository"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStageAndTask();
        when:
            this.taskApplicationService.rejectTask(PROJECT_ID, STAGE_ID, TASK_ID);
        then:
            1 * this.projectRepository.save(_ as Project);
    }

    def "reopenTask should call validateProjectExistence on projectValidator"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStageAndTask();
        when:
            this.taskApplicationService.reopenTask(PROJECT_ID, STAGE_ID, TASK_ID);
        then:
            1 * this.projectValidator.validateProjectExistence(PROJECT_ID);
    }

    def "reopenTask should call validateExistenceOfStageInProject on stageValidator"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStageAndTask();
        when:
            this.taskApplicationService.reopenTask(PROJECT_ID, STAGE_ID, TASK_ID);
        then:
            1 * this.stageValidator.validateExistenceOfStageInProject(PROJECT_ID, STAGE_ID);
    }

    def "reopenTask should call validateExistenceOfTaskInStage on taskValidator"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStageAndTask();
        when:
            this.taskApplicationService.reopenTask(PROJECT_ID, STAGE_ID, TASK_ID);
        then:
            1 * this.taskValidator.validateExistenceOfTaskInStage(STAGE_ID, TASK_ID);
    }

    def "reopenTask should load project from projectRepository"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStageAndTask();
        when:
            this.taskApplicationService.reopenTask(PROJECT_ID, STAGE_ID, TASK_ID);
        then:
            1 * this.projectRepository.load(PROJECT_ID) >> this.prepareProjectWithStageWithTask();
    }

    def "reopenTask should call reopenTask on taskDomainService"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStageAndTask();
        when:
            this.taskApplicationService.reopenTask(PROJECT_ID, STAGE_ID, TASK_ID);
        then:
            1 * this.taskDomainService.reopenTask(_ as Project, STAGE_ID, TASK_ID);
    }

    def "reopenTask should save project on projectRepository"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStageAndTask();
        when:
            this.taskApplicationService.reopenTask(PROJECT_ID, STAGE_ID, TASK_ID);
        then:
            1 * this.projectRepository.save(_);
    }

    private TaskDto prepareNewTaskDto() {
        TaskDto taskDto = new TaskDto();
        taskDto.setName(TASK_NAME);
        taskDto.setId(TASK_ID);
        taskDto.setType(TaskType.CONCEPT);
        taskDto;
    }

    private TaskDto prepareUpdateTaskDto() {
        TaskDto taskDto = new TaskDto();
        taskDto.setName(NEW_TASK_NAME);
        taskDto.setId(TASK_ID);
        taskDto.setNote(NOTE);
        taskDto.setType(TaskType.CONCEPT);
        taskDto;
    }

    private TaskDto prepareUpdateStatusTaskDto() {
        TaskDto taskDto = new TaskDto();
        taskDto.setId(TASK_ID);
        taskDto.setStatus(NEW_TASK_STATUS);
        taskDto;
    }

    private Project prepareProjectWithStage() {
        return new ProjectBuilder()
                .withName(PROJECT_NAME)
                .withId(PROJECT_ID)
                .withStage(this.prepareStage())
                .withStatus(ProjectStatus.OFFER)
                .build();
    }

    private Project prepareProjectWithStageWithTask() {
        return new ProjectBuilder()
                .withName(PROJECT_NAME)
                .withId(PROJECT_ID)
                .withStage(this.prepareStageWithTask())
                .withStatus(ProjectStatus.OFFER)
                .build();
    }

    private Stage prepareStage() {
        return new StageBuilder()
                .withName(STAGE_NAME)
                .withId(STAGE_ID)
                .build();
    }

    private Stage prepareStageWithTask() {
        return new StageBuilder()
                .withName(STAGE_NAME)
                .withId(STAGE_ID)
                .withTask(this.prepareNewTask())
                .build();
    }

    private void mockProjectRepositoryLoad() {
        Project project = this.prepareProjectWithStage();
        this.projectRepository.load(PROJECT_ID) >> project;
    }

    private void mockProjectRepositoryLoadProjectWithStageAndTask() {
        Project project = this.prepareProjectWithStageWithTask();
        this.projectRepository.load(PROJECT_ID) >> project;
    }

    private void mockTaskDomainServiceCreateTask() {
        Task task = this.prepareNewTask();
        this.taskDomainService.createTask(_ as Project, _ as Long, _ as TaskDto) >> task;
    }

    private void mockProjectQueryServiceGetTask() {
        TaskDto taskDto = this.prepareNewTaskDto();
        this.projectQueryService.getTaskByTaskId(TASK_ID) >> taskDto;
    }

    private Task prepareNewTask() {
        return new TaskBuilder()
                .withId(TASK_ID)
                .withName(TASK_NAME)
                .build();
    }

    private void mockProjectRepositorySaveProjectWithStageAndTask() {
        1 * this.projectRepository.save(_ as Project) >> this.prepareProjectWithStageWithTask();
    }
}
