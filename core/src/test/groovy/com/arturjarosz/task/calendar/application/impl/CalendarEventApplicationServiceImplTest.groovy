package com.arturjarosz.task.calendar.application.impl

import com.arturjarosz.task.calendar.application.CalendarSyncService
import com.arturjarosz.task.calendar.application.mapper.CalendarEventMapper
import com.arturjarosz.task.calendar.infrastructure.repository.CalendarEventRepository
import com.arturjarosz.task.calendar.model.CalendarEvent
import com.arturjarosz.task.calendar.model.CalendarEventSyncStatus
import com.arturjarosz.task.dto.CalendarEventDto
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException
import com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException
import spock.lang.Specification

import java.time.LocalDate
import java.time.LocalDateTime

class CalendarEventApplicationServiceImplTest extends Specification {
    static final EVENT_ID = 1L
    static final NAME = "Meeting with client"
    static final DESCRIPTION = "Discussing project"
    static final START_DATETIME = LocalDateTime.of(2026, 7, 20, 10, 0)
    static final END_DATETIME = LocalDateTime.of(2026, 7, 20, 11, 0)
    static final GOOGLE_EVENT_ID = "googleEvent123"

    def calendarEventRepository = Mock(CalendarEventRepository)
    def calendarEventMapper = Mock(CalendarEventMapper)
    def calendarSyncService = Mock(CalendarSyncService)

    def subject = new CalendarEventApplicationServiceImpl(calendarEventRepository, calendarEventMapper,
            calendarSyncService)

    def "createCalendarEvent should save event and push it to Google Calendar"() {
        given:
            def calendarEventDto = prepareCalendarEventDto()

        when:
            def result = this.subject.createCalendarEvent(calendarEventDto)

        then:
            1 * this.calendarEventRepository.save({ CalendarEvent event ->
                event.name == NAME && event.startDateTime == START_DATETIME && event.endDateTime == END_DATETIME \
                        && event.syncStatus == CalendarEventSyncStatus.PENDING
            }) >> { CalendarEvent event -> event }
            1 * this.calendarSyncService.pushEvent(_ as CalendarEvent) >> { CalendarEvent event -> event }
            1 * this.calendarEventMapper.mapToDto(_ as CalendarEvent) >> new CalendarEventDto(id: EVENT_ID)
        and:
            result != null
            result.id == EVENT_ID
    }

    def "createCalendarEvent should not accept dto without name"() {
        given:
            def calendarEventDto = prepareCalendarEventDto()
            calendarEventDto.name = null

        when:
            this.subject.createCalendarEvent(calendarEventDto)

        then:
            def exception = thrown(IllegalArgumentException)
            exception.message == "isNull.calendarEvent.name"
    }

    def "createCalendarEvent should not accept dto with end date before start date"() {
        given:
            def calendarEventDto = prepareCalendarEventDto()
            calendarEventDto.endDateTime = START_DATETIME.minusHours(2).toString()

        when:
            this.subject.createCalendarEvent(calendarEventDto)

        then:
            def exception = thrown(IllegalArgumentException)
            exception.message == "notValid.calendarEvent.dates"
    }

    def "createCalendarEvent should not accept dto with wrong date format"() {
        given:
            def calendarEventDto = prepareCalendarEventDto()
            calendarEventDto.startDateTime = "not a date"

        when:
            this.subject.createCalendarEvent(calendarEventDto)

        then:
            def exception = thrown(IllegalArgumentException)
            exception.message == "notValid.calendarEvent.startDateTime"
    }

    def "updateCalendarEvent should update event data and push it to Google Calendar"() {
        given:
            def calendarEvent = prepareCalendarEvent()
            def calendarEventDto = prepareCalendarEventDto()
            calendarEventDto.name = "Updated name"
            this.calendarEventRepository.findById(EVENT_ID) >> Optional.of(calendarEvent)

        when:
            def result = this.subject.updateCalendarEvent(EVENT_ID, calendarEventDto)

        then:
            1 * this.calendarEventRepository.save({ CalendarEvent event ->
                event.name == "Updated name" && event.syncStatus == CalendarEventSyncStatus.PENDING
            }) >> { CalendarEvent event -> event }
            1 * this.calendarSyncService.pushEvent(_ as CalendarEvent) >> { CalendarEvent event -> event }
            1 * this.calendarEventMapper.mapToDto(_ as CalendarEvent) >> new CalendarEventDto(id: EVENT_ID)
        and:
            result != null
    }

    def "updateCalendarEvent should not accept not existing event"() {
        given:
            def calendarEventDto = prepareCalendarEventDto()
            this.calendarEventRepository.findById(EVENT_ID) >> Optional.empty()

        when:
            this.subject.updateCalendarEvent(EVENT_ID, calendarEventDto)

        then:
            thrown(ResourceNotFoundException)
    }

    def "deleteCalendarEvent should remove local event that was not pushed to Google Calendar yet"() {
        given:
            def calendarEvent = prepareCalendarEvent()
            this.calendarEventRepository.findById(EVENT_ID) >> Optional.of(calendarEvent)

        when:
            this.subject.deleteCalendarEvent(EVENT_ID)

        then:
            1 * this.calendarEventRepository.delete(calendarEvent)
            0 * this.calendarSyncService.pushDelete(_)
    }

    def "deleteCalendarEvent should mark event connected with Google Calendar for removal and push the removal"() {
        given:
            def calendarEvent = prepareCalendarEvent()
            calendarEvent.markSynced(GOOGLE_EVENT_ID, "etag")
            this.calendarEventRepository.findById(EVENT_ID) >> Optional.of(calendarEvent)

        when:
            this.subject.deleteCalendarEvent(EVENT_ID)

        then:
            1 * this.calendarEventRepository.save({ CalendarEvent event ->
                event.syncStatus == CalendarEventSyncStatus.DELETE_PENDING
            }) >> { CalendarEvent event -> event }
            1 * this.calendarSyncService.pushDelete(_ as CalendarEvent)
            0 * this.calendarEventRepository.delete(_)
    }

    def "getCalendarEvents without date range should return all events not waiting for removal"() {
        given:
            def calendarEvent = prepareCalendarEvent()
            this.calendarEventRepository.findBySyncStatusNot(CalendarEventSyncStatus.DELETE_PENDING) >> [calendarEvent]
            this.calendarEventMapper.mapToDto(calendarEvent) >> new CalendarEventDto(id: EVENT_ID)

        when:
            def result = this.subject.getCalendarEvents(null, null)

        then:
            result.size() == 1
            result[0].id == EVENT_ID
    }

    def "getCalendarEvents with date range should return events overlapping the range"() {
        given:
            def calendarEvent = prepareCalendarEvent()
            def startDate = LocalDate.of(2026, 7, 1)
            def endDate = LocalDate.of(2026, 7, 31)
            this.calendarEventRepository.findBySyncStatusNotAndEndDateTimeGreaterThanEqualAndStartDateTimeLessThan(
                    CalendarEventSyncStatus.DELETE_PENDING, startDate.atStartOfDay(),
                    endDate.plusDays(1).atStartOfDay()) >> [calendarEvent]
            this.calendarEventMapper.mapToDto(calendarEvent) >> new CalendarEventDto(id: EVENT_ID)

        when:
            def result = this.subject.getCalendarEvents(startDate, endDate)

        then:
            result.size() == 1
            result[0].id == EVENT_ID
    }

    private static CalendarEventDto prepareCalendarEventDto() {
        return new CalendarEventDto(name: NAME, description: DESCRIPTION, startDateTime: START_DATETIME.toString(),
                endDateTime: END_DATETIME.toString(), allDay: false)
    }

    private static CalendarEvent prepareCalendarEvent() {
        return new CalendarEvent(NAME, DESCRIPTION, START_DATETIME, END_DATETIME, false)
    }
}
