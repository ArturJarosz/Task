package com.arturjarosz.task.calendar.model;

import com.arturjarosz.task.sharedkernel.model.AbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;

import java.io.Serial;
import java.time.LocalDateTime;

@SuppressWarnings("java:S2160")
@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "calendar_event_sequence", allocationSize = 1)
@Table(name = "CALENDAR_EVENT")
public class CalendarEvent extends AbstractAggregateRoot {

    @Serial
    private static final long serialVersionUID = 5124312348123412345L;

    @Getter
    @Column(name = "NAME", nullable = false)
    private String name;

    @Getter
    @Column(name = "DESCRIPTION")
    private String description;

    @Getter
    @Column(name = "START_DATETIME", nullable = false)
    private LocalDateTime startDateTime;

    @Getter
    @Column(name = "END_DATETIME", nullable = false)
    private LocalDateTime endDateTime;

    @Getter
    @Column(name = "ALL_DAY", nullable = false)
    private boolean allDay;

    @Getter
    @Column(name = "GOOGLE_EVENT_ID", unique = true)
    private String googleEventId;

    @Getter
    @Column(name = "GOOGLE_ETAG")
    private String googleEtag;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "SYNC_STATUS", nullable = false)
    private CalendarEventSyncStatus syncStatus;

    protected CalendarEvent() {
        // needed by JPA
    }

    public CalendarEvent(String name, String description, LocalDateTime startDateTime, LocalDateTime endDateTime,
            Boolean allDay) {
        this.name = name;
        this.description = description;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.allDay = allDay != null && allDay;
        this.syncStatus = CalendarEventSyncStatus.PENDING;
    }

    /**
     * Applies changes made by the user and marks the event as waiting for push to the Google Calendar.
     */
    public void update(String name, String description, LocalDateTime startDateTime, LocalDateTime endDateTime,
            Boolean allDay) {
        this.name = name;
        this.description = description;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.allDay = allDay != null && allDay;
        this.syncStatus = CalendarEventSyncStatus.PENDING;
    }

    /**
     * Applies changes received from the Google Calendar without scheduling another push.
     */
    public void updateFromGoogle(String name, String description, LocalDateTime startDateTime,
            LocalDateTime endDateTime, boolean allDay, String googleEtag) {
        this.name = name;
        this.description = description;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.allDay = allDay;
        this.googleEtag = googleEtag;
        this.syncStatus = CalendarEventSyncStatus.SYNCED;
    }

    public void markSynced(String googleEventId, String googleEtag) {
        this.googleEventId = googleEventId;
        this.googleEtag = googleEtag;
        this.syncStatus = CalendarEventSyncStatus.SYNCED;
    }

    public void markDeletePending() {
        this.syncStatus = CalendarEventSyncStatus.DELETE_PENDING;
    }
}
