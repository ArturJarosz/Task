package com.arturjarosz.task.calendar.model.dto;

import java.util.List;

/**
 * Result of listing changes from the Google Calendar together with the token for the next incremental sync.
 */
public record GoogleEventsSyncDto(List<GoogleEventChangeDto> events, String nextSyncToken) {
}
