package com.arturjarosz.task.project.application.dto;

import com.arturjarosz.task.project.model.CostCategory;

import java.io.Serializable;
import java.time.LocalDate;

public class CostDto implements Serializable {
    private static final long serialVersionUID = 5692523801946817998L;

    private Long id;
    private String name;
    private CostCategory category;
    private Double value;
    private LocalDate date;
    private String description;

    public CostDto(String name, CostCategory category, Double value, LocalDate date, String description) {
        this.name = name;
        this.category = category;
        this.value = value;
        this.date = date;
        this.description = description;
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

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getValue() {
        return this.value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}
