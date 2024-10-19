package com.arturjarosz.task.contract.domain.impl;

import com.arturjarosz.task.contract.domain.ContractDomainService;
import com.arturjarosz.task.contract.model.Contract;
import com.arturjarosz.task.contract.status.ContractStatus;
import com.arturjarosz.task.contract.status.ContractStatusTransitionService;
import com.arturjarosz.task.contract.status.ContractStatusWorkflow;
import com.arturjarosz.task.dto.ContractDto;
import com.arturjarosz.task.sharedkernel.annotations.DomainService;
import lombok.NonNull;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

@DomainService
public class ContractDomainServiceImpl implements ContractDomainService {

    @NonNull
    private final ContractStatusWorkflow contractWorkflow;
    @NonNull
    private final ContractStatusTransitionService contractStatusTransitionService;

    private Map<ContractStatus, BiConsumer<Contract, ContractDto>> statusToUpdater;

    public ContractDomainServiceImpl(@NonNull ContractStatusWorkflow contractWorkflow,
            @NonNull ContractStatusTransitionService contractStatusTransitionService) {
        this.contractWorkflow = contractWorkflow;
        this.contractStatusTransitionService = contractStatusTransitionService;
        this.prepareContractUpdater();
    }

    private void prepareContractUpdater() {
        this.statusToUpdater = new EnumMap<>(ContractStatus.class);
        this.statusToUpdater.put(ContractStatus.OFFER,
                (contract, contractDto) -> contract.update(contractDto.getOfferValue(), contractDto.getDeadline()));
        this.statusToUpdater.put(ContractStatus.SIGNED, (contract, contractDto) -> {
            contract.updateStartDate(contractDto.getStartDate());
            contract.updateDeadline(contractDto.getDeadline());
            contract.updateSigningDate(contractDto.getSigningDate());
            contract.updateValue(contractDto.getOfferValue());
            if (contract.getEndDate() != null) {
                contract.updateEndDate(null);
            }
        });
        this.statusToUpdater.put(ContractStatus.TERMINATED,
                (contract, contractDto) -> contract.updateEndDate(contractDto.getEndDate()));
        this.statusToUpdater.put(ContractStatus.COMPLETED,
                (contract, contractDto) -> contract.updateEndDate(contractDto.getEndDate()));
    }

    @Override
    public Contract createContract(ContractDto contractDto) {
        var contract = new Contract(contractDto.getOfferValue(), contractDto.getDeadline(), this.contractWorkflow);
        this.contractStatusTransitionService.createOffer(contract);
        return contract;
    }

    @Override
    public Contract updateContractStatus(Contract contract, ContractDto contractDto) {
        var newStatus = ContractStatus.valueOf(contractDto.getStatus().getValue());
        this.contractStatusTransitionService.changeStatus(contract, newStatus);
        Optional.ofNullable(this.statusToUpdater.get(newStatus))
                .ifPresent(updater -> updater.accept(contract, contractDto));
        return contract;
    }

    @Override
    public Contract updateContract(Contract contract, ContractDto contractDto) {
        Optional.ofNullable(this.statusToUpdater.get(contract.getStatus()))
                .ifPresent(updater -> updater.accept(contract, contractDto));
        return contract;
    }

}
