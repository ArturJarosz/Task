package com.arturjarosz.task.project.application.dto;

import java.io.Serializable;
import java.time.LocalDate;

public class SupervisionVisitDto implements Serializable {
    private static final long serialVersionUID = 3217650597832433311L;
    private long projectId;
    private long id;
    private LocalDate dateOfVisit;
    private boolean isPayable;
    private int hoursCount;

    public SupervisionVisitDto() {
    }

    public long getProjectId() {
        return this.projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDate getDateOfVisit() {
        return this.dateOfVisit;
    }

    public void setDateOfVisit(LocalDate dateOfVisit) {
        this.dateOfVisit = dateOfVisit;
    }

    public boolean isPayable() {
        return this.isPayable;
    }

    public void setPayable(boolean payable) {
        this.isPayable = payable;
    }

    public int getHoursCount() {
        return this.hoursCount;
    }

    public void setHoursCount(int hoursCount) {
        this.hoursCount = hoursCount;
    }
}
