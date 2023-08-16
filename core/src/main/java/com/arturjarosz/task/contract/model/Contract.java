package com.arturjarosz.task.contract.model;

import com.arturjarosz.task.contract.application.dto.ContractDto;
import com.arturjarosz.task.contract.status.ContractStatus;
import com.arturjarosz.task.contract.status.ContractStatusWorkflow;
import com.arturjarosz.task.sharedkernel.model.AbstractAggregateRoot;
import com.arturjarosz.task.sharedkernel.model.Money;
import com.arturjarosz.task.sharedkernel.status.WorkflowAware;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.io.Serial;
import java.time.LocalDate;

@SuppressWarnings("java:S2160") // equality is tested on uuid value, no need to override with same code
@Entity
@DiscriminatorValue(value = "CONTRACT")
public class Contract extends AbstractAggregateRoot implements WorkflowAware<ContractStatus> {
    @Serial
    private static final long serialVersionUID = -6156547903688654882L;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "VALUE"))
    private Money offerValue;

    @Column(name = "SIGNING_DATE")
    private LocalDate signingDate;

    @Column(name = "DEADLINE")
    private LocalDate deadline;

    @Column(name = "START_DATE")
    private LocalDate startDate;

    @Column(name = "END_DATE")
    private LocalDate endDate;

    @Column(name = "STATUS", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ContractStatus status;

    @Column(name = "WORKFLOW_NAME", nullable = false)
    private String workflowName;

    protected Contract() {
        // needed by JPA
    }

    public Contract(double offerValue, LocalDate deadline, ContractStatusWorkflow contractWorkflow) {
        this.offerValue = new Money(offerValue);
        this.workflowName = contractWorkflow.getName();
        this.deadline = deadline;
    }

    public LocalDate getSigningDate() {
        return this.signingDate;
    }

    public LocalDate getDeadline() {
        return this.deadline;
    }

    public LocalDate getStartDate() {
        return this.startDate;
    }

    public LocalDate getEndDate() {
        return this.endDate;
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

    public void update(Double offerValue, LocalDate deadline) {
        this.offerValue = new Money(offerValue);
        this.deadline = deadline;
    }

    public Money getOfferValue() {
        return this.offerValue;
    }

    public void sign(ContractDto contractDto) {
        this.offerValue = new Money(contractDto.getOfferValue());
        this.deadline = contractDto.getDeadline();
        this.signingDate = contractDto.getSigningDate();
        this.startDate = contractDto.getStartDate();
    }

    public void updateEnd(ContractDto contractDto) {
        this.endDate = contractDto.getEndDate();
    }

    public void resume() {
        this.endDate = null;
    }

    public void terminate(ContractDto contractDto) {
        this.updateEnd(contractDto);
    }

    public void complete(ContractDto contractDto) {
        this.updateEnd(contractDto);
    }
}
