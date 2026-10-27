package com.arturjarosz.task.project.model

import com.arturjarosz.task.project.status.task.TaskStatus
import com.arturjarosz.task.utils.TaskBuilder
import spock.lang.Specification

import java.time.LocalDate

class TaskTest extends Specification {
    static final LocalDate START_DATE = LocalDate.of(2024, 01, 01)
    static final LocalDate END_DATE = LocalDate.of(2024, 02, 01)

    def "Starting progress on task without start date sets start date to today"() {
        given:
            def task = new TaskBuilder().withStatus(TaskStatus.TO_DO).build()
        when:
            task.changeStatus(TaskStatus.IN_PROGRESS)
        then:
            task.startDate == LocalDate.now()
            task.endDate == null
    }

    def "Starting progress on task with start date does not change that date"() {
        given:
            def task = new TaskBuilder().withStatus(TaskStatus.TO_DO).withStartDate(START_DATE).build()
        when:
            task.changeStatus(TaskStatus.IN_PROGRESS)
        then:
            task.startDate == START_DATE
    }

    def "Completing task without end date sets end date to today"() {
        given:
            def task = new TaskBuilder().withStatus(TaskStatus.IN_PROGRESS).withStartDate(START_DATE).build()
        when:
            task.changeStatus(TaskStatus.DONE)
        then:
            task.startDate == START_DATE
            task.endDate == LocalDate.now()
    }

    def "Completing task with end date does not change that date"() {
        given:
            def task = new TaskBuilder().withStatus(TaskStatus.IN_PROGRESS)
                    .withStartDate(START_DATE)
                    .withEndDate(END_DATE)
                    .build()
        when:
            task.changeStatus(TaskStatus.DONE)
        then:
            task.endDate == END_DATE
    }

    def "Moving task back from DONE to IN_PROGRESS removes end date and keeps start date"() {
        given:
            def task = new TaskBuilder().withStatus(TaskStatus.DONE)
                    .withStartDate(START_DATE)
                    .withEndDate(END_DATE)
                    .build()
        when:
            task.changeStatus(TaskStatus.IN_PROGRESS)
        then:
            task.startDate == START_DATE
            task.endDate == null
    }

    def "Changing task status to #newStatus does not set any dates"() {
        given:
            def task = new TaskBuilder().withStatus(oldStatus).build()
        when:
            task.changeStatus(newStatus)
        then:
            task.startDate == null
            task.endDate == null
        where:
            oldStatus           | newStatus
            null                | TaskStatus.TO_DO
            TaskStatus.TO_DO    | TaskStatus.REJECTED
            TaskStatus.REJECTED | TaskStatus.TO_DO
    }
}
