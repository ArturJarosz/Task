package com.arturjarosz.task.project.model;

import com.arturjarosz.task.sharedkernel.model.AbstractEntity;
import com.arturjarosz.task.sharedkernel.model.Money;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.time.LocalDate;

@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "installment_sequence", allocationSize = 1)
@Table(name = "INSTALLMENT")
public class Installment extends AbstractEntity {

    private static final long serialVersionUID = -8420590861357070177L;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "AMOUNT"))
    private Money amount;

    @Column(name = "IS_PAID", nullable = false)
    private Boolean isPaid;

    @Column(name = "PAYMENT_DATE")
    private LocalDate paymentDate;

    @Column(name = "NOTE")
    private String note;

    protected Installment() {
        //needed by Hibernate
    }

    public Installment(Double amount) {
        this.amount = new Money(amount);
        this.isPaid = false;
    }

    public void payInstallment(LocalDate date) {
        this.isPaid = true;
        this.paymentDate = date;
    }

    public void update(Double amount, String note, LocalDate date) {
        this.amount = new Money(amount);
        this.note = note;
        this.paymentDate = date;
    }

    public Boolean isPaid() {
        return this.isPaid;
    }

    public Money getAmount() {
        return this.amount;
    }

    public Boolean getPaid() {
        return this.isPaid;
    }

    public LocalDate getPaymentDate() {
        return this.paymentDate;
    }

    public String getNote() {
        return this.note;
    }

}
