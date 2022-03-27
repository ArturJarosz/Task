package com.arturjarosz.task.contract.status;

import com.arturjarosz.task.sharedkernel.status.StatusTransition;

public enum ContractStatusTransition implements StatusTransition<ContractStatus> {
    // creating Contract
    CREATE_CONTRACT(null, ContractStatus.OFFER),
    // from OFFER
    REJECT_OFFER(ContractStatus.OFFER, ContractStatus.REJECTED),
    ACCEPT_OFFER(ContractStatus.OFFER, ContractStatus.ACCEPTED),
    // from REJECTED
    MAKE_NEW_OFFER(ContractStatus.REJECTED, ContractStatus.OFFER),
    // from ACCEPTED
    SIGN_CONTRACT(ContractStatus.ACCEPTED, ContractStatus.SIGNED),
    REJECT_ACCEPTED(ContractStatus.ACCEPTED, ContractStatus.REJECTED),
    // from SIGNED
    TERMINATE_CONTRACT(ContractStatus.SIGNED, ContractStatus.TERMINATED),
    COMPLETED_CONTRACT(ContractStatus.SIGNED, ContractStatus.COMPLETED),
    // from TERMINATED
    REOPEN_CONTRACT(ContractStatus.TERMINATED, ContractStatus.SIGNED);

    private final ContractStatus currentStatus;
    private final ContractStatus nextStatus;

    ContractStatusTransition(ContractStatus currentStatus, ContractStatus nextStatus) {
        this.currentStatus = currentStatus;
        this.nextStatus = nextStatus;
    }

    @Override
    public ContractStatus getCurrentStatus() {
        return this.currentStatus;
    }

    @Override
    public ContractStatus getNextStatus() {
        return this.nextStatus;
    }

    @Override
    public String getName() {
        return this.name();
    }
}
