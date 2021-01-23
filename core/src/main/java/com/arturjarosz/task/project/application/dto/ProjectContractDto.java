package com.arturjarosz.task.project.application.dto;

import java.io.Serializable;
import java.time.LocalDate;

public class ProjectContractDto implements Serializable {
    private static final long serialVersionUID = 7397583738097957909L;

    private Long id;
    private LocalDate signingDate;
    private LocalDate startDate;
    private LocalDate deadline;
    private LocalDate endDate;
    //TODO: TA-34 add project value when signing contract

    public ProjectContractDto() {

    }

    public ProjectContractDto(Long id, LocalDate signingDate, LocalDate startDate, LocalDate deadline,
                              LocalDate endDate) {
        this.id = id;
        this.signingDate = signingDate;
        this.startDate = startDate;
        this.deadline = deadline;
        this.endDate = endDate;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getSigningDate() {
        return this.signingDate;
    }

    public void setSigningDate(LocalDate signingDate) {
        this.signingDate = signingDate;
    }

    public LocalDate getStartDate() {
        return this.startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getDeadline() {
        return this.deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public LocalDate getEndDate() {
        return this.endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
