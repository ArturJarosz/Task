package com.arturjarosz.task.project.application.dto;

import java.io.Serializable;
import java.time.LocalDate;

public class InstallmentDto implements Serializable {

    private static final long serialVersionUID = -4338716799879536304L;

    private Long id;
    private Double value;
    private Boolean isPaid;
    private String note;
    private LocalDate payDate;

    public InstallmentDto() {
        //needed by Hibernate
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

    public String getNote() {
        return this.note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDate getPayDate() {
        return this.payDate;
    }

    public void setPayDate(LocalDate payDate) {
        this.payDate = payDate;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
