package com.arturjarosz.task.project.model;

import com.arturjarosz.task.project.status.Contract.ContractStatus;
import com.arturjarosz.task.sharedkernel.status.WorkflowAware;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.time.LocalDate;

@Entity
@DiscriminatorValue(value = "CONTRACT")
public class Contract extends Arrangement implements WorkflowAware<ContractStatus> {
    private static final long serialVersionUID = -6156547903688654882L;

    @Column(name = "SIGNING_DATE")
    private LocalDate signingDate;

    @Column(name = "DEADLINE")
    private LocalDate deadline;

    @Column(name = "STATUS", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ContractStatus status;

    @Column(name = "WORKFLOW_NAME", nullable = false)
    private String workflowName;

    protected Contract() {
        //needed by Hibernate
    }

    public Contract(double offerValue, LocalDate signingDate, LocalDate deadline) {
        super(offerValue);
        this.signingDate = signingDate;
        this.deadline = deadline;
    }

    public LocalDate getSigningDate() {
        return this.signingDate;
    }

    public void setSigningDate(LocalDate signingDate) {
        this.signingDate = signingDate;
    }

    public LocalDate getDeadline() {
        return this.deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    @Override
    public ContractStatus getStatus() {
        return this.status;
    }

    @Override
    public String getWorkflowName() {
        return this.workflowName;
    }

    @Override
    public void changeStatus(ContractStatus status) {
        this.status = status;
    }
}
