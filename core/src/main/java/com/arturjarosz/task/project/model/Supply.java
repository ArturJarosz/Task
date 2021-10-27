package com.arturjarosz.task.project.model;

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
}
