package com.arturjarosz.task.companycost.model;

import com.arturjarosz.task.sharedkernel.model.AbstractHistoryAwareEntity;
import com.arturjarosz.task.sharedkernel.model.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A paid charge of a company cost. It is keyed by the month of the charge (its first day), so changing the charge day
 * keeps it matched, and it keeps the amount the charge was paid at, so later amount changes don't change it.
 */
@SuppressWarnings("java:S2160") // equality is tested on uuid value, no need to override with same code
@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "company_cost_payment_sequence", allocationSize = 1)
@Table(name = "COMPANY_COST_PAYMENT")
public class CompanyCostPayment extends AbstractHistoryAwareEntity {
    @Serial
    private static final long serialVersionUID = 2054875161359807784L;

    @Getter
    @Column(name = "CHARGE_MONTH", nullable = false, updatable = false)
    private LocalDate chargeMonth;

    @Getter
    @Column(name = "PAYMENT_DATE", nullable = false)
    private LocalDate paymentDate;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "NET_VALUE", nullable = false, updatable = false))
    private Money netValue;

    protected CompanyCostPayment() {
        // needed by JPA
    }

    CompanyCostPayment(LocalDate chargeMonth, LocalDate paymentDate, BigDecimal netValue) {
        this.chargeMonth = chargeMonth;
        this.paymentDate = paymentDate;
        this.netValue = new Money(netValue);
    }

    void changePaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public BigDecimal getNetValue() {
        return this.netValue.getValue();
    }
}
