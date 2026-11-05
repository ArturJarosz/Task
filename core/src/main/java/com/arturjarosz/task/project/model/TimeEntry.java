package com.arturjarosz.task.project.model;

import com.arturjarosz.task.sharedkernel.model.AbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;

import java.io.Serial;
import java.time.LocalDate;

/**
 * Time an architect spent on a task. It is kept apart from the project aggregate, so entries are not loaded with the
 * project.
 */
@SuppressWarnings("java:S2160") // equality is tested on uuid value, no need to override with same code
@Getter
@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "time_entry_sequence", allocationSize = 1)
@Table(name = "TIME_ENTRY")
public class TimeEntry extends AbstractAggregateRoot {
    @Serial
    private static final long serialVersionUID = 3842395617502941782L;

    @Column(name = "TASK_ID", nullable = false, updatable = false)
    private Long taskId;

    @Column(name = "ARCHITECT_ID", nullable = false)
    private Long architectId;

    @Column(name = "WORK_DATE", nullable = false)
    private LocalDate workDate;

    @Column(name = "DURATION_MINUTES", nullable = false)
    private int durationMinutes;

    @Column(name = "NOTE")
    private String note;

    protected TimeEntry() {
        // needed by JPA
    }

    public TimeEntry(Long taskId, Long architectId, LocalDate workDate, int durationMinutes, String note) {
        this.taskId = taskId;
        this.update(architectId, workDate, durationMinutes, note);
    }

    public void update(Long architectId, LocalDate workDate, int durationMinutes, String note) {
        this.architectId = architectId;
        this.workDate = workDate;
        this.durationMinutes = durationMinutes;
        this.note = note;
    }
}
