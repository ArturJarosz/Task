package com.arturjarosz.task.project.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Table;
import java.math.BigDecimal;

@Table(name = "CONTRACTOR_JOB")
@DiscriminatorValue(value = "SUPPLY")
public class Supply extends CooperatorJob {
    private static final long serialVersionUID = 5502825087405032096L;

    public Supply(String name, Long cooperatorId, BigDecimal value, boolean hasInvoice, boolean payable) {
        super(name, cooperatorId, CooperatorJobType.SUPPLY, value, hasInvoice, payable);
    }
}
