package com.arturjarosz.task.project.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Table;

@Table(name = "CONTRACTOR_JOB")
@DiscriminatorValue(value = "SUPPLY")
public class Supply extends CooperatorJob {
    private static final long serialVersionUID = 5502825087405032096L;

    public Supply(String name, Long cooperatorId) {
        super(name, cooperatorId, CooperatorJobType.SUPPLY);
    }
}
