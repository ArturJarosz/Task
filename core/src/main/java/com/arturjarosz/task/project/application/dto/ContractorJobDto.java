package com.arturjarosz.task.project.application.dto;

import java.io.Serializable;

public class ContractorJobDto implements Serializable {
    private static final long serialVersionUID = -532775551414801250L;

    private Long id;
    private String name;
    private Double value;
    private Long contractorId;
    private String note;

    public ContractorJobDto() {
        //needed by Hibernate
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getValue() {
        return this.value;
    }

    public void setValue(Double value) {
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
}
