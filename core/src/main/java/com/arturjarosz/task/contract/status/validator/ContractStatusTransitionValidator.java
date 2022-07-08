package com.arturjarosz.task.contract.status.validator;

import com.arturjarosz.task.contract.model.Contract;
import com.arturjarosz.task.contract.status.ContractStatusTransition;
import com.arturjarosz.task.sharedkernel.status.StatusTransitionValidator;

public interface ContractStatusTransitionValidator extends StatusTransitionValidator<ContractStatusTransition> {

    /**
     * Validate if planed status transition for Contract is possible. If transition criteria are not net, then
     * the exception is being thrown with proper exception message about the reason of fail.
     * In that case, status transition should not take place.
     */
    void validate(Contract contract, ContractStatusTransition statusTransition);
}
