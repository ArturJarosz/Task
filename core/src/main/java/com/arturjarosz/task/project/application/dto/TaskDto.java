package com.arturjarosz.task.project.application.dto;

import com.arturjarosz.task.project.model.TaskType;
import com.arturjarosz.task.project.status.task.TaskStatus;

import java.io.Serializable;
import java.time.LocalDate;

public class TaskDto implements Serializable {

    private static final long serialVersionUID = 6275436633825075027L;

    public static final String END_DATE = "endDate";
    public static final String NAME = "name";
    public static final String NOTE = "note";
    public static final String START_DATE = "startDate";
    public static final String STATUS = "status";
    public static final String TASK_TYPE = "type";

    private String name;
    private TaskType type;
    private LocalDate startDate;
    private LocalDate endDate;
    private String note;
    private TaskStatus status;

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

    public TaskStatus getStatus() {
        return this.status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }
}
