package com.arturjarosz.task.project

import com.arturjarosz.task.configuration.BaseTestIT
import com.arturjarosz.task.dto.*
import com.arturjarosz.task.project.infrastructure.repositor.TimeEntryRepository
import com.arturjarosz.task.sharedkernel.exceptions.ErrorMessage
import com.arturjarosz.task.utils.TestsHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.transaction.annotation.Transactional
import spock.lang.Unroll
import tools.jackson.databind.ObjectMapper

import java.math.RoundingMode
import java.time.LocalDate

/**
 * Time entries of tasks, the time spent on tasks, stages and projects, and the project time summary.
 */
class TimeEntryTestIT extends BaseTestIT {
    static final String ARCHITECTS_URI = "/architects"
    static final String CLIENTS_URI = "/clients"
    static final String CONTRACTS_URI = "/contracts"
    static final String PROJECTS_URI = "/projects"
    static final long NOT_EXISTING_ID = 10000l
    static final ObjectMapper MAPPER = new ObjectMapper()

    final def architect = this.readJson('json/architect/architect.json', ArchitectDto)
    final def privateClientDto = this.readJson('json/client/privateClient.json', ClientDto)
    final def projectCreateDto = this.readJson('json/project/properProject.json', ProjectCreateDto)
    final def stageDto = this.readJson('json/stage/properStage.json', StageDto)
    final def taskDto = this.readJson('json/task/properTask.json', TaskDto)

    @Autowired
    private MockMvc mockMvc
    @Autowired
    private TimeEntryRepository timeEntryRepository

    private Long architectId

    def setup() {
        // ids of the data created by BaseTestIT
        projectCreateDto.typeId = projectTypeId
        stageDto.typeId = stageTypeId
        taskDto.typeId = taskTypeId
        architectId = TestsHelper.createArchitect(architect, "$HOST:$port$ARCHITECTS_URI", this.mockMvc).id
    }

    // Logging time

    @Transactional
    def "Logging time on a task should return code 201 and the saved entry"() {
        given:
            def project = this.createProject()
            def stage = this.createStage(project.id)
            def task = this.createTask(project.id, stage.id)
            def timeEntry = this.timeEntry(150, LocalDate.of(2026, 11, 3), "Second variant")
        when:
            def response = this.postTimeEntry(project.id, stage.id, task.id, timeEntry)
        then:
            response.status == HttpStatus.CREATED.value()
            response.getHeader("Location") ==
                    "/projects/$project.id/stages/$stage.id/tasks/$task.id/time-entries/${this.read(response, TimeEntryDto).id}"
        and:
            def created = this.read(response, TimeEntryDto)
            created.id != null
            created.workDate == LocalDate.of(2026, 11, 3)
            created.architectId == architectId
            created.durationMinutes == 150
            created.note == "Second variant"
            created.createdDateTime != null
    }

    @Transactional
    def "Logging time without a note should save the entry with an empty note"() {
        given:
            def project = this.createProject()
            def stage = this.createStage(project.id)
            def task = this.createTask(project.id, stage.id)
        when:
            def response = this.postTimeEntry(project.id, stage.id, task.id, this.timeEntry(60, LocalDate.now(), null))
        then:
            response.status == HttpStatus.CREATED.value()
            this.read(response, TimeEntryDto).note == null
    }

    @Transactional
    def "Logging time on a finished task should save the entry"() {
        given:
            def project = this.createProject()
            def stage = this.createStage(project.id)
            def task = this.createTask(project.id, stage.id)
            this.acceptContractOffer(project.contract.id)
            assert this.updateTaskStatus(project.id, stage.id, task.id, TaskStatusDto.IN_PROGRESS).status == 200
            assert this.updateTaskStatus(project.id, stage.id, task.id, TaskStatusDto.DONE).status == 200
        when:
            def response = this.postTimeEntry(project.id, stage.id, task.id, this.timeEntry(60, LocalDate.now(), null))
        then:
            response.status == HttpStatus.CREATED.value()
    }

    @Unroll
    @Transactional
    def "Logging time with #description should return code 400 and error message"() {
        given:
            def project = this.createProject()
            def stage = this.createStage(project.id)
            def task = this.createTask(project.id, stage.id)
            def timeEntry = new TimeEntryDto(durationMinutes: duration, workDate: workDate, note: note,
                    architectId: withArchitect ? architectId : null)
        when:
            def response = this.postTimeEntry(project.id, stage.id, task.id, timeEntry)
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
            this.read(response, ErrorMessage).message == message
        and: "nothing is saved"
            this.getTimeEntries(project.id, stage.id, task.id).empty
        where:
            description            | duration | workDate                     | note        | withArchitect || message
            "no duration"          | null     | LocalDate.now()              | null        | true          || "Duration of the time entry is not specified."
            "zero minutes"         | 0        | LocalDate.now()              | null        | true          || "Duration of the time entry must be greater than 0 minutes."
            "more than a day"      | 1441     | LocalDate.now()              | null        | true          || "Duration of the time entry cannot be longer than 24 hours."
            "no work date"         | 60       | null                         | null        | true          || "Work date of the time entry is not specified."
            "a future work date"   | 60       | LocalDate.now().plusDays(2)  | null        | true          || "Work date of the time entry cannot be a future date."
            "no architect"         | 60       | LocalDate.now()              | null        | false         || "Architect of the time entry is not specified."
            "a too long note"      | 60       | LocalDate.now()              | "a" * 256   | true          || "Note of the time entry cannot be longer than 255 characters."
    }

    @Transactional
    def "Logging a whole day should save the entry"() {
        given:
            def project = this.createProject()
            def stage = this.createStage(project.id)
            def task = this.createTask(project.id, stage.id)
        when:
            def response = this.postTimeEntry(project.id, stage.id, task.id,
                    this.timeEntry(1440, LocalDate.now(), "a" * 255))
        then:
            response.status == HttpStatus.CREATED.value()
    }

    @Transactional
    def "Logging time for a not existing architect should return code 404 and error message"() {
        given:
            def project = this.createProject()
            def stage = this.createStage(project.id)
            def task = this.createTask(project.id, stage.id)
            def timeEntry = this.timeEntry(60, LocalDate.now(), null)
            timeEntry.architectId = NOT_EXISTING_ID
        when:
            def response = this.postTimeEntry(project.id, stage.id, task.id, timeEntry)
        then:
            response.status == HttpStatus.NOT_FOUND.value()
            this.read(response, ErrorMessage).message == "Architect with id 10,000 does not exist."
    }

    @Transactional
    def "Logging time on a task that is not in the stage should return code 404 and error message"() {
        given:
            def project = this.createProject()
            def stage = this.createStage(project.id)
            def otherStage = this.createStage(project.id)
            def taskOnOtherStage = this.createTask(project.id, otherStage.id)
        when:
            def response = this.postTimeEntry(project.id, stage.id, taskOnOtherStage.id,
                    this.timeEntry(60, LocalDate.now(), null))
        then:
            response.status == HttpStatus.NOT_FOUND.value()
            this.read(response, ErrorMessage).message ==
                    "Task with id $taskOnOtherStage.id does not exist on stage with id $stage.id."
    }

    @Transactional
    def "Logging time on a not existing project should return code 404 and error message"() {
        when:
            def response = this.postTimeEntry(NOT_EXISTING_ID, NOT_EXISTING_ID, NOT_EXISTING_ID,
                    this.timeEntry(60, LocalDate.now(), null))
        then:
            response.status == HttpStatus.NOT_FOUND.value()
            this.read(response, ErrorMessage).message == "Project with id 10,000 does not exist."
    }

    // Listing, editing and deleting time entries

    @Transactional
    def "Getting time entries should return them latest work date first"() {
        given:
            def project = this.createProject()
            def stage = this.createStage(project.id)
            def task = this.createTask(project.id, stage.id)
            this.createTimeEntry(project.id, stage.id, task.id, 420, LocalDate.of(2026, 10, 21))
            this.createTimeEntry(project.id, stage.id, task.id, 180, LocalDate.of(2026, 11, 3))
            this.createTimeEntry(project.id, stage.id, task.id, 150, LocalDate.of(2026, 10, 28))
        when:
            def timeEntries = this.getTimeEntries(project.id, stage.id, task.id)
        then:
            timeEntries*.workDate == [LocalDate.of(2026, 11, 3), LocalDate.of(2026, 10, 28),
                                      LocalDate.of(2026, 10, 21)]
    }

    @Transactional
    def "Getting time entries with the same work date should return the latest created first"() {
        given:
            def project = this.createProject()
            def stage = this.createStage(project.id)
            def task = this.createTask(project.id, stage.id)
            def first = this.createTimeEntry(project.id, stage.id, task.id, 60, LocalDate.of(2026, 11, 3))
            def second = this.createTimeEntry(project.id, stage.id, task.id, 30, LocalDate.of(2026, 11, 3))
        when:
            def timeEntries = this.getTimeEntries(project.id, stage.id, task.id)
        then:
            timeEntries*.id == [second.id, first.id]
    }

    @Transactional
    def "Getting time entries of a task without them should return an empty list"() {
        given:
            def project = this.createProject()
            def stage = this.createStage(project.id)
            def task = this.createTask(project.id, stage.id)
        when:
            def response = this.getTimeEntriesResponse(project.id, stage.id, task.id)
        then:
            response.status == HttpStatus.OK.value()
            this.read(response, TimeEntryDto[]).length == 0
    }

    @Transactional
    def "Updating a time entry should return code 200 and the updated entry, and change the task's time spent"() {
        given:
            def project = this.createProject()
            def stage = this.createStage(project.id)
            def task = this.createTask(project.id, stage.id)
            def created = this.createTimeEntry(project.id, stage.id, task.id, 150, LocalDate.of(2026, 11, 3))
            def update = this.timeEntry(120, LocalDate.of(2026, 11, 2), "Corrected")
        when:
            def response = this.putTimeEntry(project.id, stage.id, task.id, created.id, update)
        then:
            response.status == HttpStatus.OK.value()
            def updated = this.read(response, TimeEntryDto)
            updated.id == created.id
            updated.durationMinutes == 120
            updated.workDate == LocalDate.of(2026, 11, 2)
            updated.note == "Corrected"
        and:
            this.getTask(project.id, stage.id, task.id).timeSpentMinutes == 120
    }

    @Transactional
    def "Updating a time entry with invalid data should return code 400 and leave the entry unchanged"() {
        given:
            def project = this.createProject()
            def stage = this.createStage(project.id)
            def task = this.createTask(project.id, stage.id)
            def created = this.createTimeEntry(project.id, stage.id, task.id, 150, LocalDate.of(2026, 11, 3))
        when:
            def response = this.putTimeEntry(project.id, stage.id, task.id, created.id,
                    this.timeEntry(0, LocalDate.of(2026, 11, 3), null))
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
            this.getTimeEntries(project.id, stage.id, task.id)*.durationMinutes == [150]
    }

    @Transactional
    def "Updating a time entry through another task should return code 404 and leave the entry unchanged"() {
        given:
            def project = this.createProject()
            def stage = this.createStage(project.id)
            def task = this.createTask(project.id, stage.id)
            def otherTask = this.createTask(project.id, stage.id)
            def created = this.createTimeEntry(project.id, stage.id, task.id, 150, LocalDate.of(2026, 11, 3))
        when:
            def response = this.putTimeEntry(project.id, stage.id, otherTask.id, created.id,
                    this.timeEntry(120, LocalDate.of(2026, 11, 3), null))
        then:
            response.status == HttpStatus.NOT_FOUND.value()
            this.read(response, ErrorMessage).message ==
                    "Task with id $otherTask.id does not have a time entry with id $created.id."
        and:
            this.getTimeEntries(project.id, stage.id, task.id)*.durationMinutes == [150]
    }

    @Transactional
    def "Deleting a time entry should return code 200 and remove it from the task's entries and time spent"() {
        given:
            def project = this.createProject()
            def stage = this.createStage(project.id)
            def task = this.createTask(project.id, stage.id)
            def toDelete = this.createTimeEntry(project.id, stage.id, task.id, 60, LocalDate.of(2026, 11, 3))
            def toKeep = this.createTimeEntry(project.id, stage.id, task.id, 120, LocalDate.of(2026, 11, 2))
        when:
            def response = this.deleteTimeEntry(project.id, stage.id, task.id, toDelete.id)
        then:
            response.status == HttpStatus.OK.value()
            this.getTimeEntries(project.id, stage.id, task.id)*.id == [toKeep.id]
            this.getTask(project.id, stage.id, task.id).timeSpentMinutes == 120
    }

    @Transactional
    def "Deleting a not existing time entry should return code 404 and error message"() {
        given:
            def project = this.createProject()
            def stage = this.createStage(project.id)
            def task = this.createTask(project.id, stage.id)
        when:
            def response = this.deleteTimeEntry(project.id, stage.id, task.id, NOT_EXISTING_ID)
        then:
            response.status == HttpStatus.NOT_FOUND.value()
            this.read(response, ErrorMessage).message ==
                    "Task with id $task.id does not have a time entry with id 10,000."
    }

    // Time spent on tasks, stages and projects

    @Transactional
    def "Task should return the sum of its time entries as time spent, and 0 without entries"() {
        given:
            def project = this.createProject()
            def stage = this.createStage(project.id)
            def task = this.createTask(project.id, stage.id)
            def taskWithoutTime = this.createTask(project.id, stage.id)
            [180, 150, 420].each { this.createTimeEntry(project.id, stage.id, task.id, it, LocalDate.now()) }
        expect:
            this.getTask(project.id, stage.id, task.id).timeSpentMinutes == 750
            this.getTask(project.id, stage.id, taskWithoutTime.id).timeSpentMinutes == 0
        and: "the tasks of the stage carry it too"
            def tasks = this.read(this.get("$PROJECTS_URI/$project.id/stages/$stage.id/tasks"), TaskDto[])
            tasks.find { it.id == task.id }.timeSpentMinutes == 750
            tasks.find { it.id == taskWithoutTime.id }.timeSpentMinutes == 0
    }

    @Transactional
    def "Stage should return the sum of its tasks' time as time spent"() {
        given:
            def project = this.createProject()
            def stage = this.createStage(project.id)
            def otherStage = this.createStage(project.id)
            [750, 375, 240].each {
                def task = this.createTask(project.id, stage.id)
                this.createTimeEntry(project.id, stage.id, task.id, it, LocalDate.now())
            }
        when:
            def loadedStage = this.read(this.get("$PROJECTS_URI/$project.id/stages/$stage.id"), StageDto)
        then:
            loadedStage.timeSpentMinutes == 1365
            loadedStage.tasks*.timeSpentMinutes.sort() == [240, 375, 750]
        and: "the stage list carries it too"
            def stages = this.read(this.get("$PROJECTS_URI/$project.id/stages"), StageDto[])
            stages.find { it.id == stage.id }.timeSpentMinutes == 1365
            stages.find { it.id == otherStage.id }.timeSpentMinutes == 0
    }

    // The project's Stages tab lists the stages of the project, so they need their time there too
    @Transactional
    def "Project should return the time spent on its stages and their tasks"() {
        given:
            def project = this.createProject()
            def stage = this.createStage(project.id)
            def stageWithoutTime = this.createStage(project.id)
            def task = this.createTask(project.id, stage.id)
            this.logMinutes(project.id, stage.id, task.id, 4830)
        when:
            def loadedProject = this.read(this.get("$PROJECTS_URI/$project.id"), ProjectDto)
        then:
            loadedProject.stages.find { it.id == stage.id }.timeSpentMinutes == 4830
            loadedProject.stages.find { it.id == stage.id }.tasks.find { it.id == task.id }.timeSpentMinutes == 4830
            loadedProject.stages.find { it.id == stageWithoutTime.id }.timeSpentMinutes == 0
    }

    @Transactional
    def "Project without supervision should return the time of its stages in its time summary"() {
        given:
            def project = this.createProject()
            def stage = this.createStage(project.id)
            def task = this.createTask(project.id, stage.id)
            this.createTimeEntry(project.id, stage.id, task.id, 90, LocalDate.now())
        expect:
            this.getTimeSummary(project.id).totalMinutes == 90
    }

    // Deleting an architect with time entries

    @Transactional
    def "Deleting an architect with time entries should return code 400 and error message"() {
        given:
            def project = this.createProject()
            def stage = this.createStage(project.id)
            def task = this.createTask(project.id, stage.id)
            def otherArchitect = TestsHelper.createArchitect(architect, "$HOST:$port$ARCHITECTS_URI", this.mockMvc)
            def timeEntry = this.timeEntry(60, LocalDate.now(), null)
            timeEntry.architectId = otherArchitect.id
            assert this.postTimeEntry(project.id, stage.id, task.id, timeEntry).status == HttpStatus.CREATED.value()
        when:
            def response = this.delete("$ARCHITECTS_URI/$otherArchitect.id")
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
            this.read(response, ErrorMessage).message == "Architect with logged time cannot be removed."
    }

    // Deleting tasks, stages and projects with time entries

    @Transactional
    def "Deleting a task with time entries should delete them"() {
        given:
            def project = this.createProject()
            def stage = this.createStage(project.id)
            def task = this.createTask(project.id, stage.id)
            def otherTask = this.createTask(project.id, stage.id)
            this.createTimeEntry(project.id, stage.id, task.id, 120, LocalDate.now())
            this.createTimeEntry(project.id, stage.id, otherTask.id, 30, LocalDate.now())
        when:
            def response = this.delete("$PROJECTS_URI/$project.id/stages/$stage.id/tasks/$task.id")
        then:
            response.status == HttpStatus.OK.value()
            this.timeEntriesOfTasks([task.id, otherTask.id])*.taskId == [otherTask.id]
            this.read(this.get("$PROJECTS_URI/$project.id/stages/$stage.id"), StageDto).timeSpentMinutes == 30
            this.getTimeSummary(project.id).totalMinutes == 30
    }

    @Transactional
    def "Deleting a stage with time entries should delete them"() {
        given:
            def project = this.createProject()
            def stage = this.createStage(project.id)
            def otherStage = this.createStage(project.id)
            def task = this.createTask(project.id, stage.id)
            def otherTask = this.createTask(project.id, otherStage.id)
            this.createTimeEntry(project.id, stage.id, task.id, 120, LocalDate.now())
            this.createTimeEntry(project.id, otherStage.id, otherTask.id, 30, LocalDate.now())
        when:
            def response = this.delete("$PROJECTS_URI/$project.id/stages/$stage.id")
        then:
            response.status == HttpStatus.OK.value()
            this.timeEntriesOfTasks([task.id, otherTask.id])*.taskId == [otherTask.id]
            this.getTimeSummary(project.id).totalMinutes == 30
    }

    @Transactional
    def "Deleting a project with time entries should delete them"() {
        given:
            def project = this.createProject()
            def stage = this.createStage(project.id)
            def task = this.createTask(project.id, stage.id)
            def otherProject = this.createProject()
            def otherStage = this.createStage(otherProject.id)
            def otherTask = this.createTask(otherProject.id, otherStage.id)
            this.createTimeEntry(project.id, stage.id, task.id, 120, LocalDate.now())
            this.createTimeEntry(otherProject.id, otherStage.id, otherTask.id, 30, LocalDate.now())
        when:
            def response = this.delete("$PROJECTS_URI/$project.id")
        then:
            response.status == HttpStatus.OK.value()
            this.timeEntriesOfTasks([task.id, otherTask.id])*.taskId == [otherTask.id]
            this.getTimeSummary(otherProject.id).totalMinutes == 30
    }

    // Project time summary

    @Transactional
    def "Project time summary should return the time on tasks, on supervision and in total, and the rates per hour"() {
        given:
            def project = this.createProject()
            [2475, 1365, 990].each { minutes ->
                def stage = this.createStage(project.id)
                def task = this.createTask(project.id, stage.id)
                this.logMinutes(project.id, stage.id, task.id, minutes)
            }
            def supervision = this.createSupervision(project.id)
            this.createSupervisionVisit(supervision.id, 6, true)
            def netValue = this.read(this.get("$PROJECTS_URI/$project.id/total-financial-summary"),
                    TotalProjectFinancialSummaryDto).netValue
        when:
            def response = this.get("$PROJECTS_URI/$project.id/time-summary")
        then:
            response.status == HttpStatus.OK.value()
            def summary = this.read(response, ProjectTimeSummaryDto)
            summary.tasksMinutes == 4830
            summary.supervisionMinutes == 360
            summary.totalMinutes == 5190
        and: "rates are the values over 86.5 hours"
            summary.netValue == netValue
            summary.netValuePerHour == BigDecimal.valueOf(netValue).multiply(BigDecimal.valueOf(60))
                    .divide(BigDecimal.valueOf(5190), 2, RoundingMode.HALF_UP).doubleValue()
            summary.contractValue == 8000.00d
            summary.contractValuePerHour == 92.49d
    }

    @Transactional
    def "Project time summary without time logged should return no rates"() {
        given:
            def project = this.createProject()
            this.createStage(project.id)
        when:
            def summary = this.read(this.get("$PROJECTS_URI/$project.id/time-summary"), ProjectTimeSummaryDto)
        then:
            summary.totalMinutes == 0
            summary.netValuePerHour == null
            summary.contractValuePerHour == null
    }

    @Transactional
    def "Project time summary of a not existing project should return code 404 and error message"() {
        when:
            def response = this.get("$PROJECTS_URI/$NOT_EXISTING_ID/time-summary")
        then:
            response.status == HttpStatus.NOT_FOUND.value()
            this.read(response, ErrorMessage).message == "Project with id 10,000 does not exist."
    }

    // Outside a test transaction

    // Without @Transactional, as in the running app: anything loaded lazily outside the service's own transaction fails
    // here, while the tests above would hide it. The data is committed, so it is removed again in cleanup.
    def "Time tracking endpoints should work without a surrounding transaction"() {
        given:
            def project = this.createProject()
            def clientId = projectCreateDto.clientId
            def stage = this.createStage(project.id)
            def task = this.createTask(project.id, stage.id)
            def taskToDelete = this.createTask(project.id, stage.id)
        when:
            def created = this.createTimeEntry(project.id, stage.id, task.id, 90, LocalDate.now())
            def updateResponse = this.putTimeEntry(project.id, stage.id, task.id, created.id,
                    this.timeEntry(120, LocalDate.now(), "Corrected"))
            def listResponse = this.getTimeEntriesResponse(project.id, stage.id, task.id)
            def summaryResponse = this.get("$PROJECTS_URI/$project.id/time-summary")
        then:
            updateResponse.status == HttpStatus.OK.value()
            listResponse.status == HttpStatus.OK.value()
            this.read(listResponse, TimeEntryDto[])*.durationMinutes == [120]
            summaryResponse.status == HttpStatus.OK.value()
            this.read(summaryResponse, ProjectTimeSummaryDto).totalMinutes == 120
        and: "the task, stage and project summary carry the time"
            this.getTask(project.id, stage.id, task.id).timeSpentMinutes == 120
            this.read(this.get("$PROJECTS_URI/$project.id/stages/$stage.id/tasks"), TaskDto[])
                    .find { it.id == task.id }.timeSpentMinutes == 120
            this.read(this.get("$PROJECTS_URI/$project.id/stages/$stage.id"), StageDto).timeSpentMinutes == 120
            this.read(this.get("$PROJECTS_URI/$project.id/stages"), StageDto[])[0].timeSpentMinutes == 120
            this.getTimeSummary(project.id).totalMinutes == 120
        and: "an entry, and a task with entries, can be deleted"
            this.deleteTimeEntry(project.id, stage.id, task.id, created.id).status == HttpStatus.OK.value()
            this.createTimeEntry(project.id, stage.id, taskToDelete.id, 30, LocalDate.now())
            this.delete("$PROJECTS_URI/$project.id/stages/$stage.id/tasks/$taskToDelete.id").status ==
                    HttpStatus.OK.value()
            this.timeEntriesOfTasks([task.id, taskToDelete.id]).empty
        cleanup:
            this.createTimeEntry(project.id, stage.id, task.id, 15, LocalDate.now())
            assert this.delete("$PROJECTS_URI/$project.id").status == HttpStatus.OK.value()
            assert this.timeEntriesOfTasks([task.id, taskToDelete.id]).empty
            this.delete("/clients/$clientId")
            this.delete("$ARCHITECTS_URI/$architectId")
    }

    // Helpers

    private ProjectDto createProject() {
        projectCreateDto.architectId = architectId
        projectCreateDto.clientId = TestsHelper.createClient(privateClientDto, "$HOST:$port$CLIENTS_URI",
                this.mockMvc).id
        return TestsHelper.createProject(projectCreateDto, "$HOST:$port$PROJECTS_URI", this.mockMvc)
    }

    private StageDto createStage(long projectId) {
        stageDto.architectId = architectId
        def response = this.post("$PROJECTS_URI/$projectId/stages", stageDto)
        assert response.status == HttpStatus.CREATED.value()
        return this.read(response, StageDto)
    }

    private TaskDto createTask(long projectId, long stageId) {
        taskDto.architectId = architectId
        def response = this.post("$PROJECTS_URI/$projectId/stages/$stageId/tasks", taskDto)
        assert response.status == HttpStatus.CREATED.value()
        return this.read(response, TaskDto)
    }

    private TimeEntryDto timeEntry(Integer durationMinutes, LocalDate workDate, String note) {
        def timeEntry = this.readJson('json/timeEntry/properTimeEntry.json', TimeEntryDto)
        timeEntry.durationMinutes = durationMinutes
        timeEntry.workDate = workDate
        timeEntry.note = note
        timeEntry.architectId = architectId
        return timeEntry
    }

    private TimeEntryDto createTimeEntry(long projectId, long stageId, long taskId, int durationMinutes,
            LocalDate workDate) {
        def response = this.postTimeEntry(projectId, stageId, taskId, this.timeEntry(durationMinutes, workDate, null))
        assert response.status == HttpStatus.CREATED.value()
        return this.read(response, TimeEntryDto)
    }

    // splits the minutes into entries of at most a day, on consecutive days
    private void logMinutes(long projectId, long stageId, long taskId, int minutes) {
        def day = 0
        for (int left = minutes; left > 0; left -= 1440) {
            this.createTimeEntry(projectId, stageId, taskId, Math.min(left, 1440), LocalDate.now().minusDays(day++))
        }
    }

    private SupervisionDto createSupervision(long projectId) {
        def supervision = new SupervisionDto(projectId: projectId, hasInvoice: true, baseNetRate: 100,
                hourlyNetRate: 50, visitNetRate: 20)
        def response = this.post("/supervisions", supervision)
        assert response.status == HttpStatus.OK.value()
        return this.read(response, SupervisionDto)
    }

    private void createSupervisionVisit(long supervisionId, int hours, boolean payable) {
        def visit = new SupervisionVisitDto(dateOfVisit: LocalDate.now(), hoursCount: hours, payable: payable)
        assert this.post("/supervisions/$supervisionId/visits", visit).status == HttpStatus.OK.value()
    }

    private void acceptContractOffer(long contractId) {
        def contract = new ContractDto(status: ContractStatusDto.ACCEPTED)
        assert this.post("$CONTRACTS_URI/$contractId/status", contract).status == HttpStatus.OK.value()
    }

    private MockHttpServletResponse updateTaskStatus(long projectId, long stageId, long taskId, TaskStatusDto status) {
        return this.post("$PROJECTS_URI/$projectId/stages/$stageId/tasks/$taskId/status", new TaskDto(status: status))
    }

    private TaskDto getTask(long projectId, long stageId, long taskId) {
        return this.read(this.get("$PROJECTS_URI/$projectId/stages/$stageId/tasks/$taskId"), TaskDto)
    }

    private ProjectTimeSummaryDto getTimeSummary(long projectId) {
        return this.read(this.get("$PROJECTS_URI/$projectId/time-summary"), ProjectTimeSummaryDto)
    }

    private List timeEntriesOfTasks(List taskIds) {
        return this.timeEntryRepository.findAll().findAll { it.taskId in taskIds }
    }

    private String timeEntriesUri(long projectId, long stageId, long taskId) {
        return "$PROJECTS_URI/$projectId/stages/$stageId/tasks/$taskId/time-entries"
    }

    private MockHttpServletResponse postTimeEntry(long projectId, long stageId, long taskId, TimeEntryDto timeEntry) {
        return this.post(this.timeEntriesUri(projectId, stageId, taskId), timeEntry)
    }

    private MockHttpServletResponse putTimeEntry(long projectId, long stageId, long taskId, long timeEntryId,
            TimeEntryDto timeEntry) {
        return this.mockMvc.perform(MockMvcRequestBuilders.put(this.timeEntriesUri(projectId, stageId, taskId) + "/$timeEntryId")
                .header("Content-Type", "application/json")
                .content(MAPPER.writeValueAsString(timeEntry))).andReturn().response
    }

    private MockHttpServletResponse deleteTimeEntry(long projectId, long stageId, long taskId, long timeEntryId) {
        return this.delete(this.timeEntriesUri(projectId, stageId, taskId) + "/$timeEntryId")
    }

    private MockHttpServletResponse getTimeEntriesResponse(long projectId, long stageId, long taskId) {
        return this.get(this.timeEntriesUri(projectId, stageId, taskId))
    }

    private List<TimeEntryDto> getTimeEntries(long projectId, long stageId, long taskId) {
        def response = this.getTimeEntriesResponse(projectId, stageId, taskId)
        assert response.status == HttpStatus.OK.value()
        return this.read(response, TimeEntryDto[]).toList()
    }

    private MockHttpServletResponse post(String uri, Object body) {
        return this.mockMvc.perform(MockMvcRequestBuilders.post(uri)
                .header("Content-Type", "application/json")
                .content(MAPPER.writeValueAsString(body))).andReturn().response
    }

    private MockHttpServletResponse get(String uri) {
        return this.mockMvc.perform(MockMvcRequestBuilders.get(uri)).andReturn().response
    }

    private MockHttpServletResponse delete(String uri) {
        return this.mockMvc.perform(MockMvcRequestBuilders.delete(uri)).andReturn().response
    }

    private static <T> T read(MockHttpServletResponse response, Class<T> clazz) {
        return MAPPER.readValue(response.contentAsString, clazz)
    }

    private <T> T readJson(String path, Class<T> clazz) {
        return MAPPER.readValue(new File(getClass().classLoader.getResource(path).file), clazz)
    }
}
