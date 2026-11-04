package com.arturjarosz.task.releasenote.model;

import com.arturjarosz.task.sharedkernel.model.AbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;

import java.io.Serial;

/**
 * Change of the application described for its users. Notes are only added by Liquibase changesets, one per story, so
 * the application never creates or changes them; the creation time is when the note was published.
 */
@SuppressWarnings("java:S2160") // equality is tested on uuid value, no need to override with same code
@Getter
@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "release_note_sequence", allocationSize = 1)
@Table(name = "RELEASE_NOTE")
public class ReleaseNote extends AbstractAggregateRoot {
    @Serial
    private static final long serialVersionUID = 3518824506713960229L;

    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE", nullable = false)
    private ReleaseNoteType type;

    @Column(name = "TITLE", nullable = false)
    private String title;

    @Column(name = "DESCRIPTION", nullable = false)
    private String description;

    // Release the change came with, such as 0.6.2; "What's new" groups the notes by it
    @Column(name = "APPLICATION_VERSION", nullable = false)
    private String applicationVersion;

    protected ReleaseNote() {
        // needed by JPA
    }
}
