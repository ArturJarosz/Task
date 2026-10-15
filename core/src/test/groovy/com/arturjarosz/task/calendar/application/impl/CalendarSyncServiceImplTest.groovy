package com.arturjarosz.task.calendar.application.impl

import com.arturjarosz.task.calendar.domain.GoogleCalendarException
import com.arturjarosz.task.calendar.domain.GoogleCalendarService
import com.arturjarosz.task.calendar.domain.SyncTokenExpiredException
import com.arturjarosz.task.calendar.infrastructure.repository.CalendarEventRepository
import com.arturjarosz.task.calendar.infrastructure.repository.CalendarSyncStateRepository
import com.arturjarosz.task.calendar.model.CalendarEvent
import com.arturjarosz.task.calendar.model.CalendarEventSyncStatus
import com.arturjarosz.task.calendar.model.CalendarSyncState
import com.arturjarosz.task.calendar.model.dto.GoogleEventChangeDto
import com.arturjarosz.task.calendar.model.dto.GoogleEventDataDto
import com.arturjarosz.task.calendar.model.dto.GoogleEventsSyncDto
import com.arturjarosz.task.properties.GoogleCalendarProperties
import org.springframework.beans.factory.ObjectProvider
import spock.lang.Specification

import java.time.LocalDateTime

class CalendarSyncServiceImplTest extends Specification {
    static final GOOGLE_CALENDAR_ID = "googleCalendar123"
    static final GOOGLE_EVENT_ID = "googleEvent123"
    static final SYNC_TOKEN = "syncToken1"
    static final NEXT_SYNC_TOKEN = "syncToken2"
    static final NAME = "Meeting with client"
    static final START_DATETIME = LocalDateTime.of(2026, 7, 20, 10, 0)
    static final END_DATETIME = LocalDateTime.of(2026, 7, 20, 11, 0)

    def calendarEventRepository = Mock(CalendarEventRepository)
    def calendarSyncStateRepository = Mock(CalendarSyncStateRepository)
    def googleCalendarService = Mock(GoogleCalendarService)
    def googleCalendarServiceProvider = Mock(ObjectProvider)
    def googleCalendarProperties = new GoogleCalendarProperties(true, "credentials.json", "Task", "Task")

    def subject = new CalendarSyncServiceImpl(calendarEventRepository, calendarSyncStateRepository,
            googleCalendarServiceProvider, googleCalendarProperties)

    def "pushEvent should leave event untouched when Google Calendar integration is not available"() {
        given:
            this.googleCalendarServiceProvider.getIfAvailable() >> null
            def calendarEvent = prepareCalendarEvent()

        when:
            def result = this.subject.pushEvent(calendarEvent)

        then:
            0 * this.calendarEventRepository.save(_)
            result.syncStatus == CalendarEventSyncStatus.PENDING
    }

    def "pushEvent should insert new event into Google Calendar and mark it as synced"() {
        given:
            this.googleCalendarServiceProvider.getIfAvailable() >> this.googleCalendarService
            this.calendarSyncStateRepository.findFirstByOrderByIdAsc() >> Optional.of(prepareSyncState())
            def calendarEvent = prepareCalendarEvent()

        when:
            def result = this.subject.pushEvent(calendarEvent)

        then:
            1 * this.googleCalendarService.insertEvent(GOOGLE_CALENDAR_ID, calendarEvent) >>
                    new GoogleEventDataDto(GOOGLE_EVENT_ID, "etag")
            1 * this.calendarEventRepository.save(calendarEvent) >> calendarEvent
        and:
            result.syncStatus == CalendarEventSyncStatus.SYNCED
            result.googleEventId == GOOGLE_EVENT_ID
    }

    def "pushEvent should update event already connected with Google Calendar"() {
        given:
            this.googleCalendarServiceProvider.getIfAvailable() >> this.googleCalendarService
            this.calendarSyncStateRepository.findFirstByOrderByIdAsc() >> Optional.of(prepareSyncState())
            def calendarEvent = prepareCalendarEvent()
            calendarEvent.markSynced(GOOGLE_EVENT_ID, "etag")
            calendarEvent.update(NAME, null, START_DATETIME, END_DATETIME, false)

        when:
            def result = this.subject.pushEvent(calendarEvent)

        then:
            1 * this.googleCalendarService.updateEvent(GOOGLE_CALENDAR_ID, calendarEvent) >>
                    new GoogleEventDataDto(GOOGLE_EVENT_ID, "etag2")
            1 * this.calendarEventRepository.save(calendarEvent) >> calendarEvent
        and:
            result.syncStatus == CalendarEventSyncStatus.SYNCED
            result.googleEtag == "etag2"
    }

    def "pushEvent should leave event in pending status when pushing to Google Calendar fails"() {
        given:
            this.googleCalendarServiceProvider.getIfAvailable() >> this.googleCalendarService
            this.calendarSyncStateRepository.findFirstByOrderByIdAsc() >> Optional.of(prepareSyncState())
            this.googleCalendarService.insertEvent(_, _) >> {
                throw new GoogleCalendarException("failed", new IOException())
            }
            def calendarEvent = prepareCalendarEvent()

        when:
            def result = this.subject.pushEvent(calendarEvent)

        then:
            noExceptionThrown()
            result.syncStatus == CalendarEventSyncStatus.PENDING
    }

    def "pushEvent should create dedicated Google Calendar when it does not exist yet"() {
        given:
            this.googleCalendarServiceProvider.getIfAvailable() >> this.googleCalendarService
            def syncState = new CalendarSyncState()
            this.calendarSyncStateRepository.findFirstByOrderByIdAsc() >> Optional.of(syncState)
            this.calendarSyncStateRepository.save(syncState) >> syncState
            def calendarEvent = prepareCalendarEvent()

        when:
            this.subject.pushEvent(calendarEvent)

        then:
            1 * this.googleCalendarService.createCalendar("Task") >> GOOGLE_CALENDAR_ID
            1 * this.googleCalendarService.insertEvent(GOOGLE_CALENDAR_ID, calendarEvent) >>
                    new GoogleEventDataDto(GOOGLE_EVENT_ID, "etag")
            1 * this.calendarEventRepository.save(calendarEvent) >> calendarEvent
    }

    def "pushDelete should remove event from Google Calendar and locally"() {
        given:
            this.googleCalendarServiceProvider.getIfAvailable() >> this.googleCalendarService
            this.calendarSyncStateRepository.findFirstByOrderByIdAsc() >> Optional.of(prepareSyncState())
            def calendarEvent = prepareCalendarEvent()
            calendarEvent.markSynced(GOOGLE_EVENT_ID, "etag")
            calendarEvent.markDeletePending()

        when:
            this.subject.pushDelete(calendarEvent)

        then:
            1 * this.googleCalendarService.deleteEvent(GOOGLE_CALENDAR_ID, GOOGLE_EVENT_ID)
            1 * this.calendarEventRepository.delete(calendarEvent)
    }

    def "pushDelete should leave event in delete pending status when Google Calendar cannot be reached"() {
        given:
            this.googleCalendarServiceProvider.getIfAvailable() >> this.googleCalendarService
            this.calendarSyncStateRepository.findFirstByOrderByIdAsc() >> Optional.of(prepareSyncState())
            this.googleCalendarService.deleteEvent(_, _) >> {
                throw new GoogleCalendarException("failed", new IOException())
            }
            def calendarEvent = prepareCalendarEvent()
            calendarEvent.markSynced(GOOGLE_EVENT_ID, "etag")
            calendarEvent.markDeletePending()

        when:
            this.subject.pushDelete(calendarEvent)

        then:
            noExceptionThrown()
            0 * this.calendarEventRepository.delete(_)
            calendarEvent.syncStatus == CalendarEventSyncStatus.DELETE_PENDING
    }

    def "runIncrementalSync should create local events for new Google events and store next sync token"() {
        given:
            this.googleCalendarServiceProvider.getIfAvailable() >> this.googleCalendarService
            def syncState = prepareSyncState()
            syncState.syncToken = SYNC_TOKEN
            this.calendarSyncStateRepository.findFirstByOrderByIdAsc() >> Optional.of(syncState)
            def change = new GoogleEventChangeDto(GOOGLE_EVENT_ID, "etag", NAME, null, START_DATETIME, END_DATETIME,
                    false, false)
            this.googleCalendarService.listEvents(GOOGLE_CALENDAR_ID, SYNC_TOKEN) >>
                    new GoogleEventsSyncDto([change], NEXT_SYNC_TOKEN)
            this.calendarEventRepository.findByGoogleEventId(GOOGLE_EVENT_ID) >> Optional.empty()

        when:
            this.subject.runIncrementalSync()

        then:
            1 * this.calendarEventRepository.save({ CalendarEvent event ->
                event.googleEventId == GOOGLE_EVENT_ID && event.syncStatus == CalendarEventSyncStatus.SYNCED
            })
            1 * this.calendarSyncStateRepository.save({ CalendarSyncState state ->
                state.syncToken == NEXT_SYNC_TOKEN
            })
    }

    def "runIncrementalSync should update local event when Google event has changed"() {
        given:
            this.googleCalendarServiceProvider.getIfAvailable() >> this.googleCalendarService
            def syncState = prepareSyncState()
            syncState.syncToken = SYNC_TOKEN
            this.calendarSyncStateRepository.findFirstByOrderByIdAsc() >> Optional.of(syncState)
            def change = new GoogleEventChangeDto(GOOGLE_EVENT_ID, "etag2", "Updated name", null, START_DATETIME,
                    END_DATETIME, false, false)
            this.googleCalendarService.listEvents(GOOGLE_CALENDAR_ID, SYNC_TOKEN) >>
                    new GoogleEventsSyncDto([change], NEXT_SYNC_TOKEN)
            def calendarEvent = prepareCalendarEvent()
            calendarEvent.markSynced(GOOGLE_EVENT_ID, "etag")
            this.calendarEventRepository.findByGoogleEventId(GOOGLE_EVENT_ID) >> Optional.of(calendarEvent)

        when:
            this.subject.runIncrementalSync()

        then:
            1 * this.calendarEventRepository.save({ CalendarEvent event ->
                event.name == "Updated name" && event.googleEtag == "etag2" \
                        && event.syncStatus == CalendarEventSyncStatus.SYNCED
            })
    }

    def "runIncrementalSync should not overwrite local event waiting for push"() {
        given:
            this.googleCalendarServiceProvider.getIfAvailable() >> this.googleCalendarService
            def syncState = prepareSyncState()
            syncState.syncToken = SYNC_TOKEN
            this.calendarSyncStateRepository.findFirstByOrderByIdAsc() >> Optional.of(syncState)
            def change = new GoogleEventChangeDto(GOOGLE_EVENT_ID, "etag2", "Updated name", null, START_DATETIME,
                    END_DATETIME, false, false)
            this.googleCalendarService.listEvents(GOOGLE_CALENDAR_ID, SYNC_TOKEN) >>
                    new GoogleEventsSyncDto([change], NEXT_SYNC_TOKEN)
            def calendarEvent = prepareCalendarEvent()
            calendarEvent.markSynced(GOOGLE_EVENT_ID, "etag")
            calendarEvent.update("Local name", null, START_DATETIME, END_DATETIME, false)
            this.calendarEventRepository.findByGoogleEventId(GOOGLE_EVENT_ID) >> Optional.of(calendarEvent)

        when:
            this.subject.runIncrementalSync()

        then:
            0 * this.calendarEventRepository.save({ CalendarEvent event -> event.name == "Updated name" })
            calendarEvent.name == "Local name"
    }

    def "runIncrementalSync should remove local event when Google event was cancelled"() {
        given:
            this.googleCalendarServiceProvider.getIfAvailable() >> this.googleCalendarService
            def syncState = prepareSyncState()
            syncState.syncToken = SYNC_TOKEN
            this.calendarSyncStateRepository.findFirstByOrderByIdAsc() >> Optional.of(syncState)
            def change = new GoogleEventChangeDto(GOOGLE_EVENT_ID, "etag", null, null, null, null, false, true)
            this.googleCalendarService.listEvents(GOOGLE_CALENDAR_ID, SYNC_TOKEN) >>
                    new GoogleEventsSyncDto([change], NEXT_SYNC_TOKEN)
            def calendarEvent = prepareCalendarEvent()
            calendarEvent.markSynced(GOOGLE_EVENT_ID, "etag")
            this.calendarEventRepository.findByGoogleEventId(GOOGLE_EVENT_ID) >> Optional.of(calendarEvent)

        when:
            this.subject.runIncrementalSync()

        then:
            1 * this.calendarEventRepository.delete(calendarEvent)
    }

    def "runIncrementalSync should run full synchronization when sync token is expired"() {
        given:
            this.googleCalendarServiceProvider.getIfAvailable() >> this.googleCalendarService
            def syncState = prepareSyncState()
            syncState.syncToken = SYNC_TOKEN
            this.calendarSyncStateRepository.findFirstByOrderByIdAsc() >> Optional.of(syncState)
            this.googleCalendarService.listEvents(GOOGLE_CALENDAR_ID, SYNC_TOKEN) >> {
                throw new SyncTokenExpiredException(new IOException())
            }
            this.googleCalendarService.listEvents(GOOGLE_CALENDAR_ID, null) >>
                    new GoogleEventsSyncDto([], NEXT_SYNC_TOKEN)
            def localOnlyGoogleEvent = prepareCalendarEvent()
            localOnlyGoogleEvent.markSynced("removedOnGoogleSide", "etag")
            this.calendarEventRepository.findBySyncStatus(CalendarEventSyncStatus.SYNCED) >> [localOnlyGoogleEvent]

        when:
            this.subject.runIncrementalSync()

        then:
            1 * this.calendarEventRepository.delete(localOnlyGoogleEvent)
            1 * this.calendarSyncStateRepository.save({ CalendarSyncState state ->
                state.syncToken == NEXT_SYNC_TOKEN
            })
    }

    def "runIncrementalSync should do nothing when Google Calendar integration is not available"() {
        given:
            this.googleCalendarServiceProvider.getIfAvailable() >> null

        when:
            this.subject.runIncrementalSync()

        then:
            0 * this.googleCalendarService.listEvents(_, _)
            0 * this.calendarSyncStateRepository.save(_)
    }

    private static CalendarEvent prepareCalendarEvent() {
        return new CalendarEvent(NAME, null, START_DATETIME, END_DATETIME, false)
    }

    private static CalendarSyncState prepareSyncState() {
        def syncState = new CalendarSyncState()
        syncState.googleCalendarId = GOOGLE_CALENDAR_ID
        return syncState
    }
}
