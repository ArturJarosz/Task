package com.arturjarosz.task.project.application

import com.arturjarosz.task.project.application.dto.TaskDto
import com.arturjarosz.task.project.model.Stage
import com.arturjarosz.task.project.model.Task
import com.arturjarosz.task.project.model.TaskType
import com.arturjarosz.task.project.query.impl.ProjectQueryServiceImpl
import com.arturjarosz.task.project.utils.StageBuilder
import com.arturjarosz.task.project.utils.TaskBuilder
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException
import spock.lang.Specification

class TaskValidatorTest extends Specification {
    private static final STAGE_ID = 2L
    private static final TASK_ID = 1l
    private static final String NAME = "name"
    private static final TaskType TASK_TYPE = TaskType.RENDER

    def projectQueryService = Mock(ProjectQueryServiceImpl)

    def taskValidator = new TaskValidator(projectQueryService)


    def "when taskDto in null, validateCreateTaskDto should throw an exception with specific error message"() {
        given:
            TaskDto taskDto = null
        when:
            this.taskValidator.validateCreateTaskDto(taskDto)
        then:
            Exception exception = thrown()
            exception.message == "isNull.task"
    }

    def "when taskDto name is null, validateCreateTaskDto should throw an exception with specific error message"() {
        given:
            TaskDto taskDto = new TaskDto(name: null, type: TASK_TYPE)
        when:
            this.taskValidator.validateCreateTaskDto(taskDto)
        then:
            Exception exception = thrown()
            exception.message == "isNull.task.name"
    }

    def "when taskDto name is empty, validateCreateTaskDto should throw an exception with specific error message"() {
        given:
            TaskDto taskDto = new TaskDto(name: "", type: TASK_TYPE)
        when:
            this.taskValidator.validateCreateTaskDto(taskDto)
        then:
            Exception exception = thrown()
            exception.message == "isEmpty.task.name"
    }

    def "when taskDto type is null, validateCreateTaskDto should throw an exception with specific error message"() {
        given:
            TaskDto taskDto = new TaskDto(name: NAME, type: null)
        when:
            this.taskValidator.validateCreateTaskDto(taskDto)
        then:
            Exception exception = thrown()
            exception.message == "isNull.task.type"
    }

    def "validateExistenceOfTaskInStage should throw an exception when task not present on stage"() {
        given:
            this.mockProjectQueryServiceNotTaskOnStage()
        when:
            this.taskValidator.validateExistenceOfTaskInStage(STAGE_ID, TASK_ID)
        then:
            IllegalArgumentException ex = thrown()
            ex.message == "notExist.stage.task"
    }

    def "validateExistenceOfTaskInStage should not throw any exception when task is present on stage"() {
        given:
            this.mockProjectQueryServiceTaskOnStage()
        when:
            this.taskValidator.validateExistenceOfTaskInStage(STAGE_ID, TASK_ID)
        then:
            noExceptionThrown()
    }

    def "when taskDto name is null, validateUpdateTaskDto should throw an exception with specific error message"() {
        given:
            TaskDto taskDto = new TaskDto(name: null, type: TASK_TYPE)
        when:
            this.taskValidator.validateUpdateTaskDto(taskDto)
        then:
            Exception exception = thrown()
            exception.message == "isNull.task.name"
    }

    def "when taskDto name is empty, validateUpdateTaskDto should throw an exception with specific error message"() {
        given:
            TaskDto taskDto = new TaskDto(name: "", type: TASK_TYPE)
        when:
            this.taskValidator.validateUpdateTaskDto(taskDto)
        then:
            Exception exception = thrown()
            exception.message == "isEmpty.task.name"
    }

    private void mockProjectQueryServiceNotTaskOnStage() {
        this.projectQueryService.getStageById(STAGE_ID) >> this.prepareStageWithNoTask()
    }

    private void mockProjectQueryServiceTaskOnStage() {
        this.projectQueryService.getStageById(STAGE_ID) >> this.prepareStageWithTask()
    }

    private Task prepareTask() {
        return new TaskBuilder().withId(TASK_ID).build()
    }

    private Stage prepareStageWithTask() {
        return new StageBuilder().withId(STAGE_ID).withTask(this.prepareTask()).build()
    }

    private Stage prepareStageWithNoTask() {
        return new StageBuilder().withId(STAGE_ID).build()
    }
}
