package com.arturjarosz.task.finance.model;

import com.arturjarosz.task.dto.SupplyDto;
import com.arturjarosz.task.sharedkernel.model.Money;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.io.Serial;
import java.math.BigDecimal;

@Entity
@DiscriminatorValue(value = "CONTRACTOR_JOB")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "COOPERATOR_JOB")
public class Supply extends CooperatorJob implements PartialFinancialData {

    @Serial
    private static final long serialVersionUID = 7163934162317209832L;

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
