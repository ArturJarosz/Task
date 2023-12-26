package com.arturjarosz.task.project.application

import com.arturjarosz.task.dto.TaskDto
import com.arturjarosz.task.dto.TaskTypeDto
import com.arturjarosz.task.project.model.Stage
import com.arturjarosz.task.project.model.Task
import com.arturjarosz.task.project.query.impl.ProjectQueryServiceImpl
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException
import com.arturjarosz.task.utils.StageBuilder
import com.arturjarosz.task.utils.TaskBuilder
import spock.lang.Specification

class TaskValidatorTest extends Specification {
    static final STAGE_ID = 2L
    static final TASK_ID = 1l
    static final String NAME = "name"
    static final TaskTypeDto TASK_TYPE = TaskTypeDto.RENDER

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
            def taskDto = new TaskDto(name: null, type: TASK_TYPE)
        when:
            this.taskValidator.validateCreateTaskDto(taskDto)
        then:
            Exception exception = thrown()
            exception.message == "isNull.task.name"
    }

    def "when taskDto name is empty, validateCreateTaskDto should throw an exception with specific error message"() {
        given:
            def taskDto = new TaskDto(name: "", type: TASK_TYPE)
        when:
            this.taskValidator.validateCreateTaskDto(taskDto)
        then:
            Exception exception = thrown()
            exception.message == "isEmpty.task.name"
    }

    def "when taskDto type is null, validateCreateTaskDto should throw an exception with specific error message"() {
        given:
            def taskDto = new TaskDto(name: NAME, type: null)
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
            def taskDto = new TaskDto(name: null, type: TASK_TYPE)
        when:
            this.taskValidator.validateUpdateTaskDto(taskDto)
        then:
            Exception exception = thrown()
            exception.message == "isNull.task.name"
    }

    def "when taskDto name is empty, validateUpdateTaskDto should throw an exception with specific error message"() {
        given:
            def taskDto = new TaskDto(name: "", type: TASK_TYPE)
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
