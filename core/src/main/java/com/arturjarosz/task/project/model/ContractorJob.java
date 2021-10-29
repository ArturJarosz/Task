package com.arturjarosz.task.project.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@DiscriminatorValue(value = "CONTRACTOR_JOB")
@Table(name = "COOPERATOR_JOB")
public class ContractorJob extends CooperatorJob {
    public ContractorJob() {
    }
}
