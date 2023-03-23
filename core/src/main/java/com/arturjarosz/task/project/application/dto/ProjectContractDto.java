package com.arturjarosz.task.project.application.dto;

import java.io.Serializable;
import java.time.LocalDate;

public class ProjectContractDto implements Serializable {
    private static final long serialVersionUID = 7397583738097957909L;

    private Long id;
    private LocalDate signingDate;
    private LocalDate deadline;
    //TODO: TA-34 add project value when signing contract

    public ProjectContractDto() {
        // needed by JPA
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


    public LocalDate getDeadline() {
        return this.deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

}
