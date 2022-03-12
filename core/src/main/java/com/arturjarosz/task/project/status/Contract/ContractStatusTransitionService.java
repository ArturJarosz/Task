package com.arturjarosz.task.project.status.Contract;

import com.arturjarosz.task.project.model.Contract;

public interface ContractStatusTransitionService {

    void create(Contract contract);

    void rejectOffer(Contract contract);

    void makeNewOffer(Contract contract);

    void acceptOffer(Contract contract);

    void rejectAcceptedOffer(Contract contract);

    void signContract(Contract contract);

    void terminateContract(Contract contract);

    void resumeContract(Contract contract);

    void completeContract(Contract contract);
}
