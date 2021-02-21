package com.arturjarosz.task.project.application

import com.arturjarosz.task.project.application.dto.TaskDto
import com.arturjarosz.task.project.model.TaskType
import spock.lang.Specification

class TaskValidatorTest extends Specification {

    private static final String NAME = "name";
    private static final TaskType TASK_TYPE = TaskType.RENDERS;

    def "when taskDto in null, validateCreateTaskDto should throw an exception with specific error message"() {
        given:
            TaskDto taskDto = null;
        when:
            TaskValidator.validateCreateTaskDto(taskDto);
        then:
            Exception exception = thrown();
            exception.message == "isNull.task";
    }

    def "when taskDto name is null, validateCreateTaskDto should throw an exception with specific error message"() {
        given:
            TaskDto taskDto = new TaskDto();
            taskDto.setName(null);
            taskDto.setType(TASK_TYPE);
        when:
            TaskValidator.validateCreateTaskDto(taskDto);
        then:
            Exception exception = thrown();
            exception.message == "isNull.task.name";
    }

    def "when taskDto name is empty, validateCreateTaskDto should throw an exception with specific error message"() {
        given:
            TaskDto taskDto = new TaskDto();
            taskDto.setName("");
            taskDto.setType(TASK_TYPE);
        when:
            TaskValidator.validateCreateTaskDto(taskDto);
        then:
            Exception exception = thrown();
            exception.message == "isEmpty.task.name";
    }

    def "when taskDto type is null, validateCreateTaskDto should throw an exception with specific error message"() {
        given:
            TaskDto taskDto = new TaskDto();
            taskDto.setName(NAME);
            taskDto.setType(null);
        when:
            TaskValidator.validateCreateTaskDto(taskDto);
        then:
            Exception exception = thrown();
            exception.message == "isNull.task.type";
    }
}
