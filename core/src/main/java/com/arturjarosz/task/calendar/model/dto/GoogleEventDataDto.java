package com.arturjarosz.task.calendar.model.dto;

/**
 * Identifying data of an event stored in the Google Calendar.
 */
public record GoogleEventDataDto(String googleEventId, String googleEtag) {
}
