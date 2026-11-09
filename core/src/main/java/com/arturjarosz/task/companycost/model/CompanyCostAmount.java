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
 * Net value of each charge of a company cost from the month validFrom (its first day) until the next amount starts.
 */
@SuppressWarnings("java:S2160") // equality is tested on uuid value, no need to override with same code
@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "company_cost_amount_sequence", allocationSize = 1)
@Table(name = "COMPANY_COST_AMOUNT")
public class CompanyCostAmount extends AbstractHistoryAwareEntity {
    @Serial
    private static final long serialVersionUID = -6370021944530873520L;

    @Getter
    @Column(name = "VALID_FROM", nullable = false)
    private LocalDate validFrom;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "NET_VALUE", nullable = false))
    private Money netValue;

    protected CompanyCostAmount() {
        // needed by JPA
    }

    CompanyCostAmount(LocalDate validFrom, BigDecimal netValue) {
        this.update(validFrom, netValue);
    }

    void update(LocalDate validFrom, BigDecimal netValue) {
        this.validFrom = validFrom;
        this.netValue = new Money(netValue);
    }

    public BigDecimal getNetValue() {
        return this.netValue.getValue();
    }
}
