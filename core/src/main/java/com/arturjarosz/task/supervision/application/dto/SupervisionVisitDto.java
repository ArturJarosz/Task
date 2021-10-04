package com.arturjarosz.task.supervision.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.time.LocalDate;

public class SupervisionVisitDto implements Serializable {
    private static final long serialVersionUID = 3217650597832433311L;
    @JsonIgnore
    public static final String ID = "id";
    @JsonIgnore
    public static final String DATE_OF_VISIT = "dateOfVisit";
    @JsonIgnore
    public static final String PAYABLE = "payable";
    @JsonIgnore
    public static final String HOURS_COUNT = "hoursCount";

    private long supervisionId;
    private long id;
    private LocalDate dateOfVisit;
    private Boolean payable;
    private Integer hoursCount;

    public SupervisionVisitDto() {
    }

    public long getSupervisionId() {
        return this.supervisionId;
    }

    public void setSupervisionId(long supervisionId) {
        this.supervisionId = supervisionId;
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

    public Boolean getPayable() {
        return payable;
    }

    public void setPayable(Boolean payable) {
        this.payable = payable;
    }

    public Integer getHoursCount() {
        return this.hoursCount;
    }

    public void setHoursCount(Integer hoursCount) {
        this.hoursCount = hoursCount;
    }
}
