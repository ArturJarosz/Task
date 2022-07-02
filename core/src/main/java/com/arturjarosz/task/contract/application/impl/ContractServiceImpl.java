package com.arturjarosz.task.contract.application.impl;

import com.arturjarosz.task.contract.application.ContractService;
import com.arturjarosz.task.contract.application.ContractValidator;
import com.arturjarosz.task.contract.application.dto.ContractDto;
import com.arturjarosz.task.contract.application.mapper.ContractDtoMapper;
import com.arturjarosz.task.contract.intrastructure.ContractRepository;
import com.arturjarosz.task.contract.model.Contract;
import com.arturjarosz.task.contract.status.ContractStatusTransitionService;
import com.arturjarosz.task.contract.status.ContractStatusWorkflow;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.transaction.Transactional;

@ApplicationService
public class ContractServiceImpl implements ContractService {
    private static final Logger LOG = LoggerFactory.getLogger(ContractServiceImpl.class);

    private final ContractStatusTransitionService contractStatusTransitionService;
    private final ContractStatusWorkflow contractWorkflow;
    private final ContractValidator contractValidator;
    private final ContractRepository contractRepository;

    @Autowired
    public ContractServiceImpl(ContractStatusTransitionService contractStatusTransitionService,
            ContractStatusWorkflow contractWorkflow, ContractValidator contractValidator,
            ContractRepository contractRepository) {
        this.contractStatusTransitionService = contractStatusTransitionService;
        this.contractWorkflow = contractWorkflow;
        this.contractValidator = contractValidator;
        this.contractRepository = contractRepository;
    }

    @Transactional
    @Override
    public Contract createContract(ContractDto contractDto) {
        LOG.debug("Creating contract.");
        this.contractValidator.validateOffer(contractDto);
        Contract contract = new Contract(contractDto.getOfferValue(), contractDto.getDeadline(), this.contractWorkflow);
        this.contractStatusTransitionService.createOffer(contract);
        contract = this.contractRepository.save(contract);
        LOG.debug("Contract with id {} created", contract.getId());
        return contract;
    }

    @Transactional
    @Override
    public ContractDto reject(Long contractId) {
        LOG.debug("Rejecting contract with id {}", contractId);
        Contract contract = this.contractRepository.getById(contractId);
        this.contractValidator.validateContractExistence(contract, contractId);
        this.contractStatusTransitionService.rejectOffer(contract);
        LOG.debug("Contract with id {} rejected.", contractId);
        return ContractDtoMapper.INSTANCE.contractToContractDto(contract);
    }

    @Transactional
    @Override
    public ContractDto makeNewOffer(Long contractId, ContractDto contractDto) {
        LOG.debug("Making new offer for contract with id {}", contractId);
        Contract contract = this.contractRepository.getById(contractId);
        this.contractValidator.validateContractExistence(contract, contractId);
        this.contractValidator.validateOffer(contractDto);
        contract.update(contractDto.getOfferValue(), contractDto.getDeadline());
        this.contractStatusTransitionService.makeNewOffer(contract);
        LOG.debug("New offer for contract with id {} made.", contractId);
        return ContractDtoMapper.INSTANCE.contractToContractDto(contract);
    }

    @Transactional
    @Override
    public ContractDto acceptOffer(Long contractId) {
        LOG.debug("Accepting offer for contract with id {}.", contractId);
        Contract contract = this.contractRepository.getById(contractId);
        this.contractValidator.validateContractExistence(contract, contractId);
        this.contractStatusTransitionService.acceptOffer(contract);
        LOG.debug("Offer for contract with id {} accepted.", contractId);
        return ContractDtoMapper.INSTANCE.contractToContractDto(contract);
    }

    @Transactional
    @Override
    public ContractDto sign(Long contractId, ContractDto contractDto) {
        LOG.debug("Signing contract with id {}.", contractId);
        Contract contract = this.contractRepository.getById(contractId);
        this.contractValidator.validateContractExistence(contract, contractId);
        this.contractValidator.validateSignContractDto(contractDto);
        contract.sign(contractDto);
        this.contractStatusTransitionService.signContract(contract);
        LOG.debug("Contract with id {} signed.", contractId);
        return ContractDtoMapper.INSTANCE.contractToContractDto(contract);
    }

    @Transactional
    @Override
    public ContractDto terminate(Long contractId, ContractDto contractDto) {
        LOG.debug("Terminating contract with id {}.", contractId);
        Contract contract = this.contractRepository.getById(contractId);
        this.contractValidator.validateContractExistence(contract, contractId);
        this.contractValidator.validateTerminateContractDto(contractDto);
        contract.terminate(contractDto);
        this.contractStatusTransitionService.terminateContract(contract);
        LOG.debug("Contract with id {} terminated.", contractId);
        return ContractDtoMapper.INSTANCE.contractToContractDto(contract);
    }

    @Transactional
    @Override
    public ContractDto resume(Long contractId) {
        LOG.debug("Resuming contract with id {}.", contractId);
        Contract contract = this.contractRepository.getById(contractId);
        this.contractValidator.validateContractExistence(contract, contractId);
        this.contractStatusTransitionService.resumeContract(contract);
        contract.resume();
        LOG.debug("Contract with id {} was resumed.", contractId);
        return ContractDtoMapper.INSTANCE.contractToContractDto(contract);
    }

    @Transactional
    @Override
    public ContractDto complete(Long contractId, ContractDto contractDto) {
        LOG.debug("Completing contract with id {}.", contractId);
        Contract contract = this.contractRepository.getById(contractId);
        this.contractValidator.validateContractExistence(contract, contractId);
        this.contractValidator.validateCompleteContractDto(contractDto);
        contract.complete(contractDto);
        this.contractStatusTransitionService.completeContract(contract);
        LOG.debug("Contract with id {} has been completed.", contractId);
        return ContractDtoMapper.INSTANCE.contractToContractDto(contract);
    }

}
