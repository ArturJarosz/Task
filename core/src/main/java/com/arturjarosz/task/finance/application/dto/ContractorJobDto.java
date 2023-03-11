package com.arturjarosz.task.finance.application.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class ContractorJobDto implements Serializable {
    private static final long serialVersionUID = -532775551414801250L;

    public static final String ID_FIELD = "id";
    public static final String NAME_FILED = "name";
    public static final String VALUE_FILED = "value";
    public static final String NOTE_FILED = "note";
    public static final String CONTRACTOR_ID_FILED = "contractorId";
    public static final String HAS_INVOICE_FILED = "hasInvoice";
    public static final String PAYABLE_FILED = "payable";
    public static final String PAID_FIELD = "paid";

    private Long id;
    private String name;
    private BigDecimal value;
    private Long contractorId;
    private String note;
    private Boolean hasInvoice;
    private Boolean payable;
    private Boolean paid;

    public ContractorJobDto() {
        // needed by JPA
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

    public Long getContractorId() {
        return this.contractorId;
    }

    public void setContractorId(Long contractorId) {
        this.contractorId = contractorId;
    }

    public String getNote() {
        return this.note;
    }

    public void setNote(String note) {
        this.note = note;
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
