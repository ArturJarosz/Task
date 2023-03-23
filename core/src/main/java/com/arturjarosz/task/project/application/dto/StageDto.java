package com.arturjarosz.task.project.application.dto;

import com.arturjarosz.task.finance.application.dto.InstallmentDto;
import com.arturjarosz.task.project.model.StageType;
import com.arturjarosz.task.project.status.stage.StageStatus;

import java.io.Serializable;
import java.time.LocalDate;

public class StageDto implements Serializable {
    private static final long serialVersionUID = -8148528445387228580L;

    public static final String DEADLINE = "deadline";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String STAGE_TYPE = "stageType";
    public static final String STATUS = "status";

    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate deadline;
    private StageType stageType;
    private String note;
    private InstallmentDto installmentDto;
    private Integer tasksNumber;
    private StageStatus status;

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

    public InstallmentDto getInstallmentDto() {
        return this.installmentDto;
    }

    public void setInstallmentDto(InstallmentDto installmentDto) {
        this.installmentDto = installmentDto;
    }

    public Integer getTasksNumber() {
        return this.tasksNumber;
    }

    public void setTasksNumber(Integer tasksNumber) {
        this.tasksNumber = tasksNumber;
    }

    public StageStatus getStatus() {
        return this.status;
    }

    public void setStatus(StageStatus status) {
        this.status = status;
    }
}
