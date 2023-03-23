package com.arturjarosz.task.finance.model;

import com.arturjarosz.task.finance.application.dto.InstallmentDto;
import com.arturjarosz.task.sharedkernel.model.AbstractEntity;
import com.arturjarosz.task.sharedkernel.model.Money;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.time.LocalDate;

@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "installment_sequence", allocationSize = 1)
@Table(name = "INSTALLMENT")
public class Installment extends AbstractEntity {

    private static final long serialVersionUID = -8420590861357070177L;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "FINANCIAL_DATA_ID", referencedColumnName = "ID", nullable = false)
    private FinancialData financialData;

    @Column(name = "PROJECT_FINANCIAL_DATA_ID", insertable = false, updatable = false)
    private Long projectFinancialDataId;

    @Column(name = "STAGE_ID", nullable = false)
    private Long stageId;


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

    public String getNote() {
        return this.note;
    }

    public boolean isHasInvoice() {
        return this.financialData.isHasInvoice();
    }

    public Long getStageId() {
        return this.stageId;
    }

    public void setStageId(Long stageId) {
        this.stageId = stageId;
    }

    public Long getProjectFinancialDataId() {
        return this.projectFinancialDataId;
    }
}
