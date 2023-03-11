package com.arturjarosz.task.project.application.dto;

import com.arturjarosz.task.finance.model.CostCategory;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public class CostDto implements Serializable {

    public static final String ID_FIELD = "id";
    public static final String VALUE_FIELD = "value";
    public static final String NAME_FIELD = "name";
    public static final String CATEGORY_FIELD = "category";
    public static final String DATE_FIELD = "date";
    public static final String NOTE_FIELD = "note";
    public static final String HAS_INVOICE_FIELD = "hasInvoice";
    public static final String IS_PAID_FIELD = "paid";

    private static final long serialVersionUID = 5692523801946817998L;

    private Long id;
    private String name;
    private CostCategory category;
    private BigDecimal value;
    private LocalDate date;
    private String note;
    private Boolean hasInvoice;
    private Boolean payable;
    private Boolean paid;

    public CostDto() {
        // needed by JPA
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

    public Boolean getHasInvoice() {
        return this.hasInvoice;
    }

    public void setHasInvoice(Boolean hasInvoice) {
        this.hasInvoice = hasInvoice;
    }

    public Boolean getPayable() {
        return this.payable;
    }

    public void setPayable(Boolean payable) {
        this.payable = payable;
    }

    public Boolean getPaid() {
        return this.paid;
    }

    public void setPaid(Boolean paid) {
        this.paid = paid;
    }
}
