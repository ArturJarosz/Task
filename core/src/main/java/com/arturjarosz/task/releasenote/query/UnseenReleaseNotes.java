package com.arturjarosz.task.releasenote.query;

import com.arturjarosz.task.dto.ReleaseNoteDto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Release notes newer than the last one a user has seen, newest first, and when that last note was published; the time
 * is null when the user has seen none.
 */
public record UnseenReleaseNotes(LocalDateTime lastSeenPublished, List<ReleaseNoteDto> releaseNotes) {
}
