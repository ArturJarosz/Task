package com.arturjarosz.task.finance.model;

import com.arturjarosz.task.dto.SupplyDto;
import com.arturjarosz.task.sharedkernel.model.AbstractHistoryAwareEntity;
import com.arturjarosz.task.sharedkernel.model.Money;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;

import java.io.Serial;
import java.math.BigDecimal;

@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "supply_sequence", allocationSize = 1)
@Table(name = "SUPPLY")
public class Supply extends AbstractHistoryAwareEntity implements PartialFinancialData {

    @Serial
    private static final long serialVersionUID = 7163934162317209832L;

    @Getter
    @Column(name = "NAME", nullable = false)
    String name;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "FINANCIAL_DATA_ID", referencedColumnName = "ID", nullable = false)
    FinancialData financialData;

    @Getter
    @Column(name = "NOTE")
    String note;

    @Getter
    @Column(name = "SUPPLIER_ID", nullable = false)
    long supplierId;

    @Getter
    @Column(name = "PROJECT_FINANCIAL_DATA_ID", insertable = false, updatable = false)
    private Long projectFinancialDataId;

    protected Supply() {
    }

    public Supply(String name, Long supplierId, BigDecimal value, boolean hasInvoice, boolean payable) {
        this.name = name;
        this.supplierId = supplierId;
        this.financialData = new FinancialData(new Money(value), hasInvoice, payable);
    }

    public void update(SupplyDto supplyDto) {
        this.name = supplyDto.getName();
        this.note = supplyDto.getNote();
        this.financialData.setValue(new Money(supplyDto.getValue()));
        this.financialData.setHasInvoice(supplyDto.getHasInvoice());
        this.financialData.setPayable(supplyDto.getPayable());
    }

    public BigDecimal getValue() {
        return this.financialData.getValue().getValue();
    }

    public void setValue(BigDecimal value) {
        this.financialData.setValue(new Money(value));
    }

    public boolean isHasInvoice() {
        return this.financialData.isHasInvoice();
    }

    public boolean isPayable() {
        return this.financialData.isPayable();
    }

    public boolean isPaid() {
        return this.financialData.isPaid();
    }

}
