package com.arturjarosz.task.contract.application.impl;

import com.arturjarosz.task.contract.application.ContractService;
import com.arturjarosz.task.contract.application.ContractValidator;
import com.arturjarosz.task.contract.application.mapper.ContractMapper;
import com.arturjarosz.task.contract.domain.ContractDomainService;
import com.arturjarosz.task.contract.intrastructure.ContractRepository;
import com.arturjarosz.task.contract.model.Contract;
import com.arturjarosz.task.contract.status.ContractStatus;
import com.arturjarosz.task.dto.ContractDto;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

@Slf4j
@ApplicationService
public class ContractServiceImpl implements ContractService {

    @NonNull
    private final ContractValidator contractValidator;
    @NonNull
    private final ContractRepository contractRepository;
    @NonNull
    private final ContractMapper contractMapper;
    @NonNull
    private final ContractDomainService contractDomainService;

    private Map<ContractStatus, BiConsumer<Contract, ContractDto>> statusToValidator;

    public ContractServiceImpl(@NonNull ContractValidator contractValidator,
            @NonNull ContractRepository contractRepository,
            @NonNull ContractMapper contractMapper, @NonNull ContractDomainService contractDomainService) {
        this.contractValidator = contractValidator;
        this.contractRepository = contractRepository;
        this.contractMapper = contractMapper;
        this.contractDomainService = contractDomainService;
        this.prepareContractValidators();
    }

    private void prepareContractValidators() {
        this.statusToValidator = new EnumMap<>(ContractStatus.class);
        this.statusToValidator.put(ContractStatus.COMPLETED,
                (contract, contractDto) -> this.contractValidator.validateCompleteContractDto(contractDto, contract));
        this.statusToValidator.put(ContractStatus.SIGNED,
                (contract, contractDto) -> this.contractValidator.validateSignContractDto(contractDto));
        this.statusToValidator.put(ContractStatus.OFFER,
                (contract, contractDto) -> this.contractValidator.validateOffer(contractDto));
        this.statusToValidator.put(ContractStatus.TERMINATED,
                (contract, contractDto) -> this.contractValidator.validateTerminateContractDto(contractDto, contract));
    }

    @Transactional
    @Override
    public ContractDto createContract(ContractDto contractDto) {
        LOG.info("Creating contract.");

        this.contractValidator.validateOffer(contractDto);
        var contract = this.contractDomainService.createContract(contractDto);
        contract = this.contractRepository.save(contract);

        LOG.info("Contract with id {} created", contract.getId());
        return this.contractMapper.mapToDto(contract);
    }

    @Transactional
    @Override
    public ContractDto changeStatus(Long contractId, ContractDto contractDto) {
        LOG.info("Changing contract status");

        var maybeContract = this.contractRepository.findById(contractId);
        this.contractValidator.validateBaseContractDto(contractDto);
        this.contractValidator.validateContractExistence(maybeContract, contractId);
        var contract = maybeContract.orElseThrow(ResourceNotFoundException::new);
        Optional.ofNullable(this.statusToValidator.get(ContractStatus.valueOf(contractDto.getStatus().name())))
                .ifPresent(statusValidator -> statusValidator.accept(contract, contractDto));
        var updatedContract = this.contractDomainService.updateContractStatus(contract, contractDto);

        this.contractRepository.save(updatedContract);
        LOG.info("Contract status for contract with id {} was updated.", contractId);

        return this.contractMapper.mapToDto(updatedContract);
    }

    @Override
    public ContractDto getContractForProject(Long contractId) {
        LOG.info("Getting contract with id {}", contractId);

        var maybeContract = this.contractRepository.findById(contractId);
        this.contractValidator.validateContractExistence(maybeContract, contractId);
        var contract = maybeContract.orElseThrow(ResourceNotFoundException::new);

        return this.contractMapper.mapToDto(contract);
    }

    @Transactional
    @Override
    public ContractDto updateContract(Long contractId, ContractDto contractDto) {
        LOG.info("Updating contract with id {} with data: {}", contractId, contractDto);

        var maybeContract = this.contractRepository.findById(contractId);
        this.contractValidator.validateContractExistence(maybeContract, contractId);
        var contract = maybeContract.orElseThrow(ResourceNotFoundException::new);
        Optional.ofNullable(this.statusToValidator.get(contract.getStatus()))
                .ifPresent(statusValidator -> statusValidator.accept(contract, contractDto));

        var updatedContract = this.contractDomainService.updateContract(contract, contractDto);
        this.contractRepository.save(updatedContract);

        LOG.info("Contract data for contract with id {} was updated.", contractId);

        return this.contractMapper.mapToDto(updatedContract);
    }
}
