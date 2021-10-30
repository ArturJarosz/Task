package com.arturjarosz.task.project.model;

import com.arturjarosz.task.project.application.dto.SupplyDto;
import com.arturjarosz.task.sharedkernel.model.Money;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@DiscriminatorValue(value = "SUPPLY")
@Table(name = "COOPERATOR_JOB")
public class Supply extends CooperatorJob {

    protected Supply() {
    }

    public Supply(String name, Long supplierId, BigDecimal value, boolean hasInvoice, boolean payable) {
        super(name, supplierId, CooperatorJobType.SUPPLY, value, hasInvoice, payable);
    }

    public long getSupplierId() {
        return this.getCooperatorId();
    }

    public void update(SupplyDto supplyDto) {
        this.name = supplyDto.getName();
        this.note = supplyDto.getNote();
        this.financialData.setValue(new Money(supplyDto.getValue()));
        this.financialData.setHasInvoice(supplyDto.getHasInvoice());
        this.financialData.setPayable(supplyDto.getPayable());
    }

}
