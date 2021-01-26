package com.arturjarosz.task.project.application.dto;

import java.io.Serializable;
import java.time.LocalDate;

public class InstallmentDto implements Serializable {

    private static final long serialVersionUID = -4338716799879536304L;

    private Double value;
    private Boolean isPaid;
    private String description;
    private LocalDate payDate;

    public InstallmentDto() {
    }

    public Double getValue() {
        return this.value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Boolean getPaid() {
        return this.isPaid;
    }

    public void setPaid(Boolean paid) {
        this.isPaid = paid;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getPayDate() {
        return this.payDate;
    }

    public void setPayDate(LocalDate payDate) {
        this.payDate = payDate;
    }
}
