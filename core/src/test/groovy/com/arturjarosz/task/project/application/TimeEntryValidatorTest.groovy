package com.arturjarosz.task.project.application

import com.arturjarosz.task.architect.application.ArchitectValidator
import com.arturjarosz.task.dto.TimeEntryDto
import com.arturjarosz.task.project.query.TimeEntryQueryService
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException
import com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDate
import java.time.ZoneId

class TimeEntryValidatorTest extends Specification {
    static final Long ARCHITECT_ID = 1L
    static final Long TASK_ID = 2L
    static final Long TIME_ENTRY_ID = 3L

    def architectValidator = Mock(ArchitectValidator)
    def timeEntryQueryService = Mock(TimeEntryQueryService)

    def timeEntryValidator = new TimeEntryValidator(architectValidator, timeEntryQueryService)

    def "validateTimeEntryDto should accept a proper time entry and check that its architect exists"() {
        given:
            def timeEntryDto = new TimeEntryDto(durationMinutes: 150, workDate: LocalDate.now(), architectId: ARCHITECT_ID,
                    note: "Second variant")
        when:
            this.timeEntryValidator.validateTimeEntryDto(timeEntryDto)
        then:
            noExceptionThrown()
            1 * this.architectValidator.validateArchitectExistence(ARCHITECT_ID)
    }

    @Unroll
    def "validateTimeEntryDto should accept #description"() {
        when:
            this.timeEntryValidator.validateTimeEntryDto(timeEntryDto)
        then:
            noExceptionThrown()
        where:
            description             | timeEntryDto
            "1 minute"              | new TimeEntryDto(durationMinutes: 1, workDate: LocalDate.now(), architectId: ARCHITECT_ID)
            "a whole day"           | new TimeEntryDto(durationMinutes: 1440, workDate: LocalDate.now(), architectId: ARCHITECT_ID)
            "today in Poland"       | new TimeEntryDto(durationMinutes: 60, workDate: LocalDate.now(ZoneId.of("Europe/Warsaw")), architectId: ARCHITECT_ID)
            "a past work date"      | new TimeEntryDto(durationMinutes: 60, workDate: LocalDate.of(2020, 1, 1), architectId: ARCHITECT_ID)
            "a 255-character note"  | new TimeEntryDto(durationMinutes: 60, workDate: LocalDate.now(), architectId: ARCHITECT_ID, note: "a" * 255)
    }

    @Unroll
    def "validateTimeEntryDto should reject #description with message code #messageCode"() {
        when:
            this.timeEntryValidator.validateTimeEntryDto(timeEntryDto)
        then:
            def exception = thrown(IllegalArgumentException)
            exception.message == messageCode
        and: "the architect is not looked up"
            0 * this.architectValidator.validateArchitectExistence(_)
        where:
            description            | timeEntryDto                                                                                                           || messageCode
            "no data"              | null                                                                                                                   || "isNull.timeEntry"
            "no duration"          | new TimeEntryDto(durationMinutes: null, workDate: LocalDate.now(), architectId: ARCHITECT_ID)                          || "isNull.timeEntry.duration"
            "zero minutes"         | new TimeEntryDto(durationMinutes: 0, workDate: LocalDate.now(), architectId: ARCHITECT_ID)                             || "notValid.timeEntry.duration"
            "negative minutes"     | new TimeEntryDto(durationMinutes: -30, workDate: LocalDate.now(), architectId: ARCHITECT_ID)                           || "notValid.timeEntry.duration"
            "more than a day"      | new TimeEntryDto(durationMinutes: 1441, workDate: LocalDate.now(), architectId: ARCHITECT_ID)                          || "notValid.timeEntry.duration.tooLong"
            "no work date"         | new TimeEntryDto(durationMinutes: 60, workDate: null, architectId: ARCHITECT_ID)                                       || "isNull.timeEntry.workDate"
            "a future work date"   | new TimeEntryDto(durationMinutes: 60, workDate: LocalDate.now().plusDays(2), architectId: ARCHITECT_ID)                || "notValid.timeEntry.workDate"
            "no architect"         | new TimeEntryDto(durationMinutes: 60, workDate: LocalDate.now(), architectId: null)                                    || "isNull.timeEntry.architect"
            "a 256-character note" | new TimeEntryDto(durationMinutes: 60, workDate: LocalDate.now(), architectId: ARCHITECT_ID, note: "a" * 256)           || "notValid.timeEntry.note.tooLong"
    }

    def "validateTimeEntryDto should pass on the error of a not existing architect"() {
        given:
            def timeEntryDto = new TimeEntryDto(durationMinutes: 60, workDate: LocalDate.now(), architectId: ARCHITECT_ID)
            this.architectValidator.validateArchitectExistence(ARCHITECT_ID) >> {
                throw new ResourceNotFoundException("notExist.architect")
            }
        when:
            this.timeEntryValidator.validateTimeEntryDto(timeEntryDto)
        then:
            thrown(ResourceNotFoundException)
    }

    def "validateExistenceOfTimeEntryInTask should accept an entry of the task"() {
        given:
            this.timeEntryQueryService.timeEntryExistsInTask(TASK_ID, TIME_ENTRY_ID) >> true
        when:
            this.timeEntryValidator.validateExistenceOfTimeEntryInTask(TASK_ID, TIME_ENTRY_ID)
        then:
            noExceptionThrown()
    }

    def "validateExistenceOfTimeEntryInTask should reject an entry that is not in the task"() {
        given:
            this.timeEntryQueryService.timeEntryExistsInTask(TASK_ID, TIME_ENTRY_ID) >> false
        when:
            this.timeEntryValidator.validateExistenceOfTimeEntryInTask(TASK_ID, TIME_ENTRY_ID)
        then:
            def exception = thrown(ResourceNotFoundException)
            exception.message == "notExist.task.timeEntry"
    }
}
