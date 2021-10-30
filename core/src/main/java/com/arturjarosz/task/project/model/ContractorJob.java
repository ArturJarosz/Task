package com.arturjarosz.task.project.model;

import com.arturjarosz.task.project.application.dto.ContractorJobDto;
import com.arturjarosz.task.sharedkernel.model.Money;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@DiscriminatorValue(value = "CONTRACTOR_JOB")
@Table(name = "COOPERATOR_JOB")
public class ContractorJob extends CooperatorJob {

    protected ContractorJob() {
    }

    public ContractorJob(String name, Long contractorId, BigDecimal value, boolean hasInvoice, boolean payable) {
        super(name, contractorId, CooperatorJobType.CONTRACTOR_JOB, value, hasInvoice, payable);
    }

    public long getContractorId(){
        return this.getCooperatorId();
    }

    public void update(ContractorJobDto contractorJobDto) {
        this.name = contractorJobDto.getName();
        this.note = contractorJobDto.getNote();
        this.financialData.setValue(new Money(contractorJobDto.getValue()));
        this.financialData.setHasInvoice(contractorJobDto.getHasInvoice());
        this.financialData.setPayable(contractorJobDto.getPayable());
    }
}
