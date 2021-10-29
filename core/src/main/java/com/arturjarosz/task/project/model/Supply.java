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
    private static final long serialVersionUID = 5502825087405032096L;

    public Supply() {
    }

    public Supply(String name, Long cooperatorId, BigDecimal value, boolean hasInvoice, boolean payable) {
        super(name, cooperatorId, CooperatorJobType.SUPPLY, value, hasInvoice, payable);
    }

    public long getSupplierId(){
        return this.getCooperatorId();
    }

    public void update(SupplyDto supplyDto){
        this.name = supplyDto.getName();
        this.note = supplyDto.getNote();
        this.financialData.setValue(new Money(supplyDto.getValue()));
        this.financialData.setHasInvoice(supplyDto.getHasInvoice());
        this.financialData.setPayable(supplyDto.getPayable());
    }

}
