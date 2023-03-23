package com.arturjarosz.task.finance.application.dto;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public class InstallmentDto implements Serializable {
    @Serial
    private static final long serialVersionUID = -4338716799879536304L;

    public static final String ID_FIELD = "id";
    public static final String VALUE_FIELD = "value";
    public static final String NOTE_FIELD = "note";
    public static final String IS_PAID_FIELD = "paid";
    public static final String HAS_INVOICE_FIELD = "hasInvoice";
    public static final String PAYMENT_DATE = "paymentDate";

    private Long id;
    private BigDecimal value;
    private Boolean paid;
    private String note;
    private LocalDate paymentDate;
    private Boolean hasInvoice;
    private Long stageId;

    public InstallmentDto() {
        // needed by JPA
    }

    public BigDecimal getValue() {
        return this.value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public Boolean getPaid() {
        return this.paid;
    }

    public void setPaid(Boolean paid) {
        this.paid = paid;
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
        return this.hasInvoice;
    }

    public void setHasInvoice(Boolean hasInvoice) {
        this.hasInvoice = hasInvoice;
    }

    public Long getStageId() {
        return this.stageId;
    }

    public void setStageId(Long stageId) {
        this.stageId = stageId;
    }
}
