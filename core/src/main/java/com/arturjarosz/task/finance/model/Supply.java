package com.arturjarosz.task.finance.model;

import com.arturjarosz.task.finance.application.dto.SupplyDto;
import com.arturjarosz.task.sharedkernel.model.Money;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@DiscriminatorValue(value = "CONTRACTOR_JOB")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "COOPERATOR_JOB")
public class Supply extends CooperatorJob implements PartialFinancialData {

    protected Supply() {
    }

    public Supply(String name, Long supplierId, BigDecimal value, boolean hasInvoice, boolean payable) {
        super(name, supplierId, CooperatorJobType.SUPPLY, value, hasInvoice, payable);
    }

    @Transient
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
