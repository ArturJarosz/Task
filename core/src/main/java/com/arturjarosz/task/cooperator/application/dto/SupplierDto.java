package com.arturjarosz.task.cooperator.application.dto;

import java.io.Serializable;

public class SupplierDto implements Serializable {
    private static final long serialVersionUID = 1761161251042111551L;

    private String name;
    private String note;
    private String email;
    private String telephone;
    private Double jobsValue;

    public SupplierDto() {
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNote() {
        return this.note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return this.telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public Double getJobsValue() {
        return this.jobsValue;
    }

    public void setJobsValue(Double jobsValue) {
        this.jobsValue = jobsValue;
    }
}
