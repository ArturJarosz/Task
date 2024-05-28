package com.arturjarosz.task.finance.model;

import com.arturjarosz.task.dto.InstallmentDto;
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
import java.time.LocalDate;

@SuppressWarnings("java:S2160") // equality is tested on uuid value, no need to override with same code
@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "installment_sequence", allocationSize = 1)
@Table(name = "INSTALLMENT")
public class Installment extends AbstractHistoryAwareEntity implements PartialFinancialData {
    @Serial
    private static final long serialVersionUID = -8420590861357070177L;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "FINANCIAL_DATA_ID", referencedColumnName = "ID", nullable = false)
    private FinancialData financialData;

    @Getter
    @Column(name = "PROJECT_FINANCIAL_DATA_ID", insertable = false, updatable = false)
    private Long projectFinancialDataId;

    @Getter
    @Column(name = "STAGE_ID", nullable = false)
    private Long stageId;

    @Getter
    @Column(name = "NOTE")
    private String note;

    protected Installment() {
        // needed by JPA
    }

    public Installment(InstallmentDto installmentDto, Long stageId) {
        this.financialData = new FinancialData(new Money(installmentDto.getValue()), installmentDto.getHasInvoice(),
                true);
        this.stageId = stageId;
    }

    public void payInstallment(LocalDate date) {
        this.financialData.pay(date);
    }

    public void update(InstallmentDto installmentDto) {
        this.financialData = new FinancialData(new Money(installmentDto.getValue()), installmentDto.getHasInvoice(),
                true, this.financialData.isPaid());
        this.note = installmentDto.getNote();
        if (this.financialData.isPaid()) {
            this.financialData.setPaymentDate(installmentDto.getPaymentDate());
        }
    }

    public boolean isPaid() {
        return this.financialData.isPaid();
    }

    public Money getAmount() {
        return this.financialData.getValue();
    }

    public boolean getPaid() {
        return this.financialData.isPaid();
    }

    public LocalDate getPaymentDate() {
        return this.financialData.getPaymentDate();
    }

    public boolean isHasInvoice() {
        return this.financialData.isHasInvoice();
    }

    public void setStageId(Long stageId) {
        this.stageId = stageId;
    }

}
