package com.arturjarosz.task.project.application.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public class InstallmentDto implements Serializable {

    private static final long serialVersionUID = -4338716799879536304L;

    private Long id;
    private BigDecimal value;
    private Boolean isPaid;
    private String note;
    private LocalDate paymentDate;
    private Boolean hasInvoice;

    public InstallmentDto() {
        //needed by Hibernate
    }

    public BigDecimal getValue() {
        return this.value;
    }

    public void setValue(BigDecimal value) {
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

    public LocalDate getPaymentDate() {
        return this.paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getHasInvoice() {
        return hasInvoice;
    }

    public void setHasInvoice(Boolean hasInvoice) {
        this.hasInvoice = hasInvoice;
    }
}
