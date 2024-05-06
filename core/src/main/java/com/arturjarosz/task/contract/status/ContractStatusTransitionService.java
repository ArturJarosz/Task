package com.arturjarosz.task.contract.status;

import com.arturjarosz.task.contract.model.Contract;

public interface ContractStatusTransitionService {

    void createOffer(Contract contract);

    void rejectOffer(Contract contract);

    void rejectAcceptedOffer(Contract contract);

    void reject(Contract contract);

    void changeStatus(Contract contract, ContractStatus newStatus);
}
