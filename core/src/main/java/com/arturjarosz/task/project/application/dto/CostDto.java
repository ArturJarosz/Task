package com.arturjarosz.task.project.application.dto;

import com.arturjarosz.task.project.model.CostCategory;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public class CostDto implements Serializable {
    private static final long serialVersionUID = 5692523801946817998L;

    private Long id;
    private String name;
    private CostCategory category;
    private BigDecimal value;
    private LocalDate date;
    private String note;

    public CostDto() {
        //needed by Hibernate
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

    public CostCategory getCategory() {
        return this.category;
    }

    public void setCategory(CostCategory category) {
        this.category = category;
    }

    public LocalDate getDate() {
        return this.date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getNote() {
        return this.note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public BigDecimal getValue() {
        return this.value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }
}
