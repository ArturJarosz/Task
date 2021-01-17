package com.arturjarosz.task.project.application.dto;

import com.arturjarosz.task.project.model.StageType;

import java.io.Serializable;
import java.time.LocalDate;

public class StageDto implements Serializable {
    private static final long serialVersionUID = -8148528445387228580L;

    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate deadline;
    private StageType stageType;
    private String note;

    public StageDto() {

    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getStartDate() {
        return this.startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return this.endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalDate getDeadline() {
        return this.deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public StageType getStageType() {
        return this.stageType;
    }

    public void setStageType(StageType stageType) {
        this.stageType = stageType;
    }

    public String getNote() {
        return this.note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
