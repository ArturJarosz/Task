package com.arturjarosz.task.releasenote.query;

import com.arturjarosz.task.dto.ReleaseNoteDto;

import java.util.List;

public interface ReleaseNoteQueryService {

    /**
     * Loads all release notes, newest first.
     */
    List<ReleaseNoteDto> getReleaseNotes();

    /**
     * Loads release notes with id greater than given lastSeenId, all of them when it is null, newest first, together
     * with when the note with lastSeenId was published.
     */
    UnseenReleaseNotes getUnseenReleaseNotes(Long lastSeenId);

    /**
     * Loads id of the newest release note. Returns null when there are no notes.
     */
    Long getNewestReleaseNoteId();
}
