package com.arturjarosz.task.project.query;

import org.hibernate.envers.RevisionType;

import java.time.LocalDateTime;

/**
 * State of an audited record right after the revision that changed it, with when, by whom and how it was changed.
 * The author is the Auth0 id of the person, or null when the change was made without one.
 */
public record RecordRevision<T>(T snapshot, int revision, LocalDateTime dateTime, String authorAuthId,
                                RevisionType type) {
}
