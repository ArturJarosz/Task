package com.arturjarosz.task.contract.application.dto;

import com.arturjarosz.task.contract.application.status.ContractStatus;

import java.io.Serializable;
import java.time.LocalDate;

public class ContractDto implements Serializable {
    private static final long serialVersionUID = 2871016534140942045L;

    private Double offerValue;
    private LocalDate signingDate;
    private LocalDate deadline;
    private LocalDate startDate;
    private LocalDate endDate;
    private ContractStatus status;

    public ContractDto() {
        //needed by Hibernate
    }

    public Double getOfferValue() {
        return this.offerValue;
    }

    public void setOfferValue(Double offerValue) {
        this.offerValue = offerValue;
    }

    public LocalDate getSigningDate() {
        return this.signingDate;
    }

    public void setSigningDate(LocalDate signingDate) {
        this.signingDate = signingDate;
    }

    public LocalDate getDeadline() {
        return this.deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public ContractStatus getStatus() {
        return this.status;
    }

    public void setStatus(ContractStatus status) {
        this.status = status;
    }

    public LocalDate getStartDate() {
        return this.startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return this.endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
