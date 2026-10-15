package com.arturjarosz.task.calendar.model.dto;

import java.time.LocalDateTime;

/**
 * Single event state received from the Google Calendar during synchronization.
 */
public record GoogleEventChangeDto(String googleEventId, String googleEtag, String name, String description,
        LocalDateTime startDateTime, LocalDateTime endDateTime, boolean allDay, boolean cancelled) {
}
