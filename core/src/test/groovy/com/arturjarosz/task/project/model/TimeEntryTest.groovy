package com.arturjarosz.task.project.model

import spock.lang.Specification

import java.time.LocalDate

class TimeEntryTest extends Specification {

    def "Updating a time entry changes its architect, work date, duration and note but keeps its task"() {
        given:
            def timeEntry = new TimeEntry(3L, 4L, LocalDate.of(2026, 11, 3), 150, "Second variant")
        when:
            timeEntry.update(5L, LocalDate.of(2026, 11, 2), 120, "Corrected")
        then:
            timeEntry.architectId == 5L
            timeEntry.workDate == LocalDate.of(2026, 11, 2)
            timeEntry.durationMinutes == 120
            timeEntry.note == "Corrected"
        and:
            timeEntry.taskId == 3L
    }
}
