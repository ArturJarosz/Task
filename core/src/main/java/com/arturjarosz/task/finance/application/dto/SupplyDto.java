package com.arturjarosz.task.finance.application.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class SupplyDto implements Serializable {

    public static final String ID_FIELD = "id";
    public static final String NAME_FILED = "name";
    public static final String VALUE_FILED = "value";
    public static final String NOTE_FILED = "note";
    public static final String SUPPLIER_ID_FILED = "supplierId";
    public static final String HAS_INVOICE_FILED = "hasInvoice";
    public static final String PAYABLE_FILED = "payable";
    public static final String PAID_FIELD = "paid";

    private Long id;
    private String name;
    private BigDecimal value;
    private Long supplierId;
    private String note;
    private Boolean hasInvoice;
    private Boolean payable;
    private Long projectId;
    private Boolean paid;

    public SupplyDto() {
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

    public BigDecimal getValue() {
        return this.value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public Long getSupplierId() {
        return this.supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public String getNote() {
        return this.note;
    }

    public void setNote(String note) {
        this.note = note;
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

    public Long getProjectId() {
        return this.projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Boolean getPaid() {
        return this.paid;
    }

    public void setPaid(Boolean paid) {
        this.paid = paid;
    }
}
