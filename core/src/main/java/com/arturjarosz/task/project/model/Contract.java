package com.arturjarosz.task.project.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.time.LocalDate;

@Entity
@DiscriminatorValue(value = "CONTRACT")
public class Contract extends Arrangement {
    private static final long serialVersionUID = -6156547903688654882L;

    @Column(name = "SIGNING_DATE")
    private LocalDate signingDate;

    @Column(name = "DEADLINE")
    private LocalDate deadline;

    protected Contract() {
        //needed by Hibernate
    }

    public Contract(double offerValue, LocalDate signingDate, LocalDate deadline) {
        super(offerValue);
        this.signingDate = signingDate;
        this.deadline = deadline;
    }

    public LocalDate getSigningDate() {
        return this.signingDate;
    }

    public LocalDate getDeadline() {
        return this.deadline;
    }

    public void setSigningDate(LocalDate signingDate) {
        this.signingDate = signingDate;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }
}
