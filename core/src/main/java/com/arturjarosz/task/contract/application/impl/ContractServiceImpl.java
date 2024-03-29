package com.arturjarosz.task.contract.application.impl;

import com.arturjarosz.task.contract.application.ContractService;
import com.arturjarosz.task.contract.application.ContractValidator;
import com.arturjarosz.task.contract.application.mapper.ContractMapper;
import com.arturjarosz.task.contract.intrastructure.ContractRepository;
import com.arturjarosz.task.contract.model.Contract;
import com.arturjarosz.task.contract.status.ContractStatusTransitionService;
import com.arturjarosz.task.contract.status.ContractStatusWorkflow;
import com.arturjarosz.task.dto.ContractDto;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@ApplicationService
public class ContractServiceImpl implements ContractService {

    @NonNull
    private final ContractStatusTransitionService contractStatusTransitionService;
    @NonNull
    private final ContractStatusWorkflow contractWorkflow;
    @NonNull
    private final ContractValidator contractValidator;
    @NonNull
    private final ContractRepository contractRepository;
    @NonNull
    private final ContractMapper contractMapper;

    @Transactional
    @Override
    public ContractDto createContract(ContractDto contractDto) {
        LOG.debug("Creating contract.");

        this.contractValidator.validateOffer(contractDto);
        var contract = new Contract(contractDto.getOfferValue(), contractDto.getDeadline(), this.contractWorkflow);
        this.contractStatusTransitionService.createOffer(contract);
        contract = this.contractRepository.save(contract);

        LOG.debug("Contract with id {} created", contract.getId());
        return this.contractMapper.mapToDto(contract);
    }

    @Transactional
    @Override
    public ContractDto reject(Long contractId) {
        LOG.debug("Rejecting contract with id {}", contractId);

        var maybeContract = this.contractRepository.findById(contractId);
        this.contractValidator.validateContractExistence(maybeContract, contractId);
        var contract = maybeContract.orElseThrow(ResourceNotFoundException::new);
        this.contractStatusTransitionService.rejectOffer(contract);

        LOG.debug("Contract with id {} rejected.", contractId);
        return this.contractMapper.mapToDto(contract);
    }

    @Transactional
    @Override
    public ContractDto makeNewOffer(Long contractId, ContractDto contractDto) {
        LOG.debug("Making new offer for contract with id {}", contractId);

        var maybeContract = this.contractRepository.findById(contractId);
        this.contractValidator.validateContractExistence(maybeContract, contractId);
        this.contractValidator.validateOffer(contractDto);
        var contract = maybeContract.orElseThrow(ResourceNotFoundException::new);
        contract.update(contractDto.getOfferValue(), contractDto.getDeadline());
        this.contractStatusTransitionService.makeNewOffer(contract);

        LOG.debug("New offer for contract with id {} made.", contractId);
        return this.contractMapper.mapToDto(contract);
    }

    @Transactional
    @Override
    public ContractDto acceptOffer(Long contractId) {
        LOG.debug("Accepting offer for contract with id {}.", contractId);

        var maybeContract = this.contractRepository.findById(contractId);
        this.contractValidator.validateContractExistence(maybeContract, contractId);
        var contract = maybeContract.orElseThrow(ResourceNotFoundException::new);
        this.contractStatusTransitionService.acceptOffer(contract);

        LOG.debug("Offer for contract with id {} accepted.", contractId);
        return this.contractMapper.mapToDto(contract);
    }

    @Transactional
    @Override
    public ContractDto sign(Long contractId, ContractDto contractDto) {
        LOG.debug("Signing contract with id {}.", contractId);

        var maybeContract = this.contractRepository.findById(contractId);
        this.contractValidator.validateContractExistence(maybeContract, contractId);
        this.contractValidator.validateSignContractDto(contractDto);
        var contract = maybeContract.orElseThrow(ResourceNotFoundException::new);
        contract.sign(contractDto);
        this.contractStatusTransitionService.signContract(contract);

        LOG.debug("Contract with id {} signed.", contractId);
        return this.contractMapper.mapToDto(contract);
    }

    @Transactional
    @Override
    public ContractDto terminate(Long contractId, ContractDto contractDto) {
        LOG.debug("Terminating contract with id {}.", contractId);

        var maybeContract = this.contractRepository.findById(contractId);
        this.contractValidator.validateContractExistence(maybeContract, contractId);
        this.contractValidator.validateTerminateContractDto(contractDto);
        var contract = maybeContract.orElseThrow(ResourceNotFoundException::new);
        contract.terminate(contractDto);
        this.contractStatusTransitionService.terminateContract(contract);

        LOG.debug("Contract with id {} terminated.", contractId);
        return this.contractMapper.mapToDto(contract);
    }

    @Transactional
    @Override
    public ContractDto resume(Long contractId) {
        LOG.debug("Resuming contract with id {}.", contractId);

        var maybeContract = this.contractRepository.findById(contractId);
        this.contractValidator.validateContractExistence(maybeContract, contractId);
        var contract = maybeContract.orElseThrow(ResourceNotFoundException::new);
        this.contractStatusTransitionService.resumeContract(contract);
        contract.resume();

        LOG.debug("Contract with id {} was resumed.", contractId);
        return this.contractMapper.mapToDto(contract);
    }

    @Transactional
    @Override
    public ContractDto complete(Long contractId, ContractDto contractDto) {
        LOG.debug("Completing contract with id {}.", contractId);

        var maybeContract = this.contractRepository.findById(contractId);
        this.contractValidator.validateContractExistence(maybeContract, contractId);
        this.contractValidator.validateCompleteContractDto(contractDto);
        var contract = maybeContract.orElseThrow(ResourceNotFoundException::new);
        contract.complete(contractDto);
        this.contractStatusTransitionService.completeContract(contract);

        LOG.debug("Contract with id {} has been completed.", contractId);
        return this.contractMapper.mapToDto(contract);
    }

    @Override
    public ContractDto getContractForProject(Long contractId) {
        LOG.debug("Getting contract with id {}", contractId);

        var maybeContract = this.contractRepository.findById(contractId);
        this.contractValidator.validateContractExistence(maybeContract, contractId);
        var contract = maybeContract.orElseThrow(ResourceNotFoundException::new);

        return this.contractMapper.mapToDto(contract);
    }

}
