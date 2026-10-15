package com.arturjarosz.task.calendar.model;

import com.arturjarosz.task.sharedkernel.model.AbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

/**
 * Holds the synchronization state against the shared Google Calendar. Single row is expected.
 */
@SuppressWarnings("java:S2160")
@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "calendar_sync_state_sequence", allocationSize = 1)
@Table(name = "CALENDAR_SYNC_STATE")
public class CalendarSyncState extends AbstractAggregateRoot {

    @Serial
    private static final long serialVersionUID = -8123412341234123412L;

    @Getter
    @Setter
    @Column(name = "GOOGLE_CALENDAR_ID")
    private String googleCalendarId;

    @Getter
    @Setter
    @Column(name = "SYNC_TOKEN")
    private String syncToken;

    public CalendarSyncState() {
        // needed by JPA
    }
}
