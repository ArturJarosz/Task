package com.arturjarosz.task.project.application.impl

import com.arturjarosz.task.project.application.ProjectValidator
import com.arturjarosz.task.project.application.StageValidator
import com.arturjarosz.task.project.application.TaskValidator
import com.arturjarosz.task.project.application.dto.TaskDto
import com.arturjarosz.task.project.infrastructure.repositor.impl.ProjectRepositoryImpl
import com.arturjarosz.task.project.model.Project
import com.arturjarosz.task.project.model.Task
import com.arturjarosz.task.project.model.TaskType
import com.arturjarosz.task.project.query.impl.ProjectQueryServiceImpl
import com.arturjarosz.task.project.utils.ProjectBuilder
import com.arturjarosz.task.project.utils.StageBuilder
import com.arturjarosz.task.sharedkernel.model.CreatedEntityDto
import com.arturjarosz.task.sharedkernel.utils.TestUtils
import spock.lang.Specification

class TaskApplicationServiceImplTest extends Specification {

    private static final Long TASK_ID = 1L;
    private static final Long NOT_EXISTING_PROJECT_ID = 99L;
    private static final Long EXISTING_PROJECT_ID = 11L;
    private static final Long NOT_EXISTING_STAGE_ID = 199L;
    private static final Long EXISTING_STAGE_ID = 101L;
    private static final String NAME = "name";
    private static final TaskType TASK_TYPE = TaskType.RENDERS;

    def stage = new StageBuilder().withId(EXISTING_STAGE_ID).build();
    def project = new ProjectBuilder().withId(EXISTING_PROJECT_ID).withStage(stage).build();

    def projectRepositoryMock = Mock(ProjectRepositoryImpl) {
        load(NOT_EXISTING_PROJECT_ID) >> null;
        load(EXISTING_PROJECT_ID) >> project;
        save(_ as Project) >> {
            Task task = project.getStages().get(0).getTasks().get(0);
            TestUtils.setFieldForObject(task, "id", TASK_ID);
            return project;
        }
    }

    def projectQueryServiceMock = Mock(ProjectQueryServiceImpl) {
        getStageById(NOT_EXISTING_STAGE_ID) >> null;
        getStageById(EXISTING_STAGE_ID) >> stage;
    }

    def projectValidator = new ProjectValidator(projectRepositoryMock);
    def stageValidator = new StageValidator(projectQueryServiceMock);
    def taskValidator = new TaskValidator(projectQueryServiceMock);

    def taskApplicationServiceTest = new TaskApplicationServiceImpl(projectRepositoryMock, projectValidator,
            stageValidator, taskValidator);

    def "createTask should throw an exception on passing wrong project id"() {
        given: "proper task dto data"
            TaskDto taskDto = new TaskDto();
            taskDto.setName(NAME);
            taskDto.setType(TASK_TYPE);
        when: "passing not existing project id"
            this.taskApplicationServiceTest.createTask(NOT_EXISTING_PROJECT_ID, EXISTING_STAGE_ID, taskDto);
        then: "exception should be thrown with specific message and repository should not save changes"
            Exception exception = thrown();
            exception.message == "notExists.project";
            0 * this.projectRepositoryMock.save(_);
    }

    def "createTask should throw an exception on passing wrong stage id"() {
        given: "proper task dto data"
            TaskDto taskDto = new TaskDto();
            taskDto.setName(NAME);
            taskDto.setType(TASK_TYPE);
        when: "passing not existing stage id"
            CreatedEntityDto createdEntityDto =
                    this.taskApplicationServiceTest.createTask(EXISTING_PROJECT_ID, NOT_EXISTING_STAGE_ID, taskDto);
        then: "exception should be thrown with specific message and repository should not save changes"
            Exception exception = thrown();
            exception.message == "notExists.stage";
            0 * this.projectRepositoryMock.save(_);
    }

    def "createTask should throw an exception on passing not proper taskDto"() {
        given: "proper task dto data"
            TaskDto taskDto = null;
        when: "passing existing stage id and existing project id"
            CreatedEntityDto createdEntityDto =
                    this.taskApplicationServiceTest.createTask(EXISTING_PROJECT_ID, EXISTING_STAGE_ID, taskDto);
        then: "exception should be thrown with specific message and repository should not save changes"
            Exception exception = thrown();
            exception.message == "isNull.task";
            0 * this.projectRepositoryMock.save(_);
    }

    def "createTask should create and save task for given stage when proper data are passed"() {
        given: "proper task dto data"
            TaskDto taskDto = new TaskDto();
            taskDto.setName(NAME);
            taskDto.setType(TASK_TYPE);
        when: "passing existing stage id and existing project id"
            CreatedEntityDto createdEntityDto =
                    this.taskApplicationServiceTest.createTask(EXISTING_PROJECT_ID, EXISTING_STAGE_ID, taskDto);
        then:
            noExceptionThrown();
            createdEntityDto.getId() == TASK_ID;
    }
}
