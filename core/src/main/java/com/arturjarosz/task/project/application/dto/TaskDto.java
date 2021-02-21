package com.arturjarosz.task.project.application.dto;

import com.arturjarosz.task.project.model.TaskType;

import java.io.Serializable;
import java.time.LocalDate;

public class TaskDto implements Serializable {

    private static final long serialVersionUID = 6275436633825075027L;
    private String name;
    private TaskType type;
    private LocalDate startDate;
    private LocalDate endDate;
    private String note;

    public TaskDto() {
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TaskType getType() {
        return this.type;
    }

    public void setType(TaskType type) {
        this.type = type;
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

    public String getNote() {
        return this.note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
