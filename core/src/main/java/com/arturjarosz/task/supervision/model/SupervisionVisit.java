package com.arturjarosz.task.supervision.model;

import com.arturjarosz.task.sharedkernel.model.AbstractEntity;
import com.arturjarosz.task.supervision.application.dto.SupervisionVisitDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.io.Serial;
import java.time.LocalDate;

@SuppressWarnings("java:S2160") // equality is tested on uuid value, no need to override with same code
@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "supervision_visit_sequence", allocationSize = 1)
@Table(name = "SUPERVISION_VISIT")
public class SupervisionVisit extends AbstractEntity {
    @Serial
    private static final long serialVersionUID = 6142572844344116972L;

    @Column(name = "DATE_OF_VISIT", nullable = false)
    private LocalDate dateOfVisit;

    @Column(name = "HOURS_COUNT", nullable = false)
    private int hoursCount;

    @Column(name = "PAYABLE", nullable = false)
    private boolean payable;

    protected SupervisionVisit() {
        // needed by JPA
    }

    public SupervisionVisit(LocalDate dateOfVisit, int hoursCount, boolean payable) {
        this.dateOfVisit = dateOfVisit;
        this.hoursCount = hoursCount;
        this.payable = payable;
    }

    public LocalDate getDateOfVisit() {
        return this.dateOfVisit;
    }

    public int getHoursCount() {
        return this.hoursCount;
    }

    public boolean isPayable() {
        return this.payable;
    }

    public SupervisionVisit update(SupervisionVisitDto supervisionVisitDto) {
        this.payable = supervisionVisitDto.getPayable();
        this.hoursCount = supervisionVisitDto.getHoursCount();
        this.dateOfVisit = supervisionVisitDto.getDateOfVisit();
        return this;
    }
}
