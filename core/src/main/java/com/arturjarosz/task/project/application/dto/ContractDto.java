package com.arturjarosz.task.project.application.dto;

import com.arturjarosz.task.contract.status.ContractStatus;

import java.io.Serializable;

public class ContractDto implements Serializable {
    private Long id;
    private Double projectValue;
    private ContractStatus contractStatus;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getProjectValue() {
        return this.projectValue;
    }

    public void setProjectValue(Double projectValue) {
        this.projectValue = projectValue;
    }

    public ContractStatus getContractStatus() {
        return this.contractStatus;
    }

    public void setContractStatus(ContractStatus contractStatus) {
        this.contractStatus = contractStatus;
    }
}
