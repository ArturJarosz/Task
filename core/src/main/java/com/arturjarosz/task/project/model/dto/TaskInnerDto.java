package com.arturjarosz.task.project.model.dto;

import com.arturjarosz.task.project.model.TaskType;

import java.io.Serializable;
import java.time.LocalDate;

public class TaskInnerDto implements Serializable {
    private static final long serialVersionUID = -8940317214839882986L;

    String name;
    TaskType taskType;
    LocalDate startDate;
    LocalDate endDate;
    String note;

    public TaskInnerDto() {
        // needed by JPA
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TaskType getTaskType() {
        return this.taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
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
