package com.arturjarosz.task.project.model

import com.arturjarosz.task.project.status.stage.StageStatus
import com.arturjarosz.task.utils.StageBuilder
import spock.lang.Specification

import java.time.LocalDate

class StageTest extends Specification {
    static final LocalDate START_DATE = LocalDate.of(2024, 01, 01)
    static final LocalDate END_DATE = LocalDate.of(2024, 02, 01)

    def "Moving stage from #oldStatus to IN_PROGRESS without start date sets start date to today"() {
        given:
            def stage = new StageBuilder().withStatus(oldStatus).build()
        when:
            stage.changeStatus(StageStatus.IN_PROGRESS)
        then:
            stage.startDate == LocalDate.now()
            stage.endDate == null
        where:
            oldStatus << [StageStatus.TO_DO, StageStatus.REJECTED]
    }

    def "Starting progress on stage with start date does not change that date"() {
        given:
            def stage = new StageBuilder().withStatus(StageStatus.TO_DO).withStartDate(START_DATE).build()
        when:
            stage.changeStatus(StageStatus.IN_PROGRESS)
        then:
            stage.startDate == START_DATE
    }

    def "Completing stage without end date sets end date to today"() {
        given:
            def stage = new StageBuilder().withStatus(StageStatus.IN_PROGRESS).withStartDate(START_DATE).build()
        when:
            stage.changeStatus(StageStatus.DONE)
        then:
            stage.startDate == START_DATE
            stage.endDate == LocalDate.now()
    }

    def "Completing stage with end date does not change that date"() {
        given:
            def stage = new StageBuilder().withStatus(StageStatus.IN_PROGRESS)
                    .withStartDate(START_DATE)
                    .withEndDate(END_DATE)
                    .build()
        when:
            stage.changeStatus(StageStatus.DONE)
        then:
            stage.endDate == END_DATE
    }

    def "Moving stage back from DONE to IN_PROGRESS removes end date and keeps start date"() {
        given:
            def stage = new StageBuilder().withStatus(StageStatus.DONE)
                    .withStartDate(START_DATE)
                    .withEndDate(END_DATE)
                    .build()
        when:
            stage.changeStatus(StageStatus.IN_PROGRESS)
        then:
            stage.startDate == START_DATE
            stage.endDate == null
    }

    def "Changing stage status to #newStatus does not set any dates"() {
        given:
            def stage = new StageBuilder().withStatus(oldStatus).build()
        when:
            stage.changeStatus(newStatus)
        then:
            stage.startDate == null
            stage.endDate == null
        where:
            oldStatus            | newStatus
            null                 | StageStatus.TO_DO
            StageStatus.TO_DO    | StageStatus.REJECTED
            StageStatus.REJECTED | StageStatus.TO_DO
    }
}
