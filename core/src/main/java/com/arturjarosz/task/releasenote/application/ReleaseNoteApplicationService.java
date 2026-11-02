package com.arturjarosz.task.releasenote.application;

import com.arturjarosz.task.dto.ReleaseNoteDto;
import com.arturjarosz.task.dto.ReleaseNotesSeenDto;
import com.arturjarosz.task.dto.UnseenReleaseNotesDto;

import java.util.List;

public interface ReleaseNoteApplicationService {

    /**
     * Returns all release notes, newest first.
     */
    List<ReleaseNoteDto> getReleaseNotes();

    /**
     * Returns release notes the {@link com.arturjarosz.task.user.model.User} matching the current JWT token has not
     * seen yet: newer than the last note the user marked as seen, newest first. Notes published before the user was
     * created count as seen. They are listed since the newest note the user has seen was published, or since the user
     * was created when that is later or the user has seen none; there is no time when there are no notes. Returns no
     * notes when there is no such user.
     */
    UnseenReleaseNotesDto getUnseenReleaseNotes();

    /**
     * Marks release notes up to the given one as seen by the {@link com.arturjarosz.task.user.model.User} matching the
     * current JWT token, creating the user when it does not exist yet. Marking a note not newer than the last one seen
     * changes nothing. Throws {@link com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException} when the
     * token does not belong to a person, or when the note is newer than the last one seen and does not exist.
     */
    void markReleaseNotesSeen(ReleaseNotesSeenDto releaseNotesSeenDto);
}
