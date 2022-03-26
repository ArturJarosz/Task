package com.arturjarosz.task.contract.application.status;

import com.arturjarosz.task.contract.model.Contract;

public interface ContractStatusTransitionService {

    void createOffer(Contract contract);

    void rejectOffer(Contract contract);

    void makeNewOffer(Contract contract);

    void acceptOffer(Contract contract);

    void rejectAcceptedOffer(Contract contract);

    void signContract(Contract contract);

    void terminateContract(Contract contract);

    void resumeContract(Contract contract);

    void completeContract(Contract contract);

    void reject(Contract contract);
}
