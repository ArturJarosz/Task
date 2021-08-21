package com.arturjarosz.task.project.model;

import com.arturjarosz.task.sharedkernel.model.AbstractEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.time.LocalDate;

@Entity
@SequenceGenerator(name = "supervision_visit_sequence_generator", sequenceName = "supervision_visit_sequence", allocationSize = 1)
@Table(name = "SUPERVISION_VISIT")
public class SupervisionVisit extends AbstractEntity {

    private static final long serialVersionUID = 6142572844344116972L;
    @Column(name = "DATE_OF_VISIT", nullable = false)
    private LocalDate dateOfVisit;

    @Column(name = "HOURS_COUNT", nullable = false)
    private int hoursCount;

    @Column(name = "IS_PAYABLE", nullable = false)
    private boolean isPayable;

    protected SupervisionVisit() {
        // Needed by Hibernate
    }

    public SupervisionVisit(LocalDate dateOfVisit, int hoursCount, boolean isPayable) {
        this.dateOfVisit = dateOfVisit;
        this.hoursCount = hoursCount;
        this.isPayable = isPayable;
    }

    public LocalDate getDateOfVisit() {
        return this.dateOfVisit;
    }

    public int getHoursCount() {
        return this.hoursCount;
    }

    public boolean isPayable() {
        return this.isPayable;
    }
}
