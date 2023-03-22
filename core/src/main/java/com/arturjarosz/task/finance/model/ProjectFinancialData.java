package com.arturjarosz.task.finance.model;


import com.arturjarosz.task.sharedkernel.model.AbstractAggregateRoot;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;

@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "project_financial_data_sequence", allocationSize = 1)
public class ProjectFinancialData extends AbstractAggregateRoot {

    @Column(name = "PROJECT_ID", nullable = false)
    private Long projectId;

    protected ProjectFinancialData() {
        // needed by JPA
    }

    public Long getProjectId() {
        return this.projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
}
