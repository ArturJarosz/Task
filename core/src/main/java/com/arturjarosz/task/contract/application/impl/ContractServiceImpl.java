package com.arturjarosz.task.contract.application.impl;

import com.arturjarosz.task.finance.application.InstallmentApplicationService;
import com.arturjarosz.task.project.query.ProjectQueryService;
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

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

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
    @NonNull
    private final ProjectQueryService projectQueryService;
    @NonNull
    private final InstallmentApplicationService installmentApplicationService;

    private Map<ContractStatus, BiConsumer<Contract, ContractDto>> statusToValidator;

    public ContractServiceImpl(@NonNull ContractValidator contractValidator,
            @NonNull ContractRepository contractRepository,
            @NonNull ContractMapper contractMapper, @NonNull ContractDomainService contractDomainService,
            @NonNull ProjectQueryService projectQueryService,
            @NonNull InstallmentApplicationService installmentApplicationService) {
        this.contractValidator = contractValidator;
        this.contractRepository = contractRepository;
        this.contractMapper = contractMapper;
        this.contractDomainService = contractDomainService;
        this.projectQueryService = projectQueryService;
        this.installmentApplicationService = installmentApplicationService;
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
        var targetStatus = ContractStatus.valueOf(contractDto.getStatus().name());
        Optional.ofNullable(this.statusToValidator.get(targetStatus))
                .ifPresent(statusValidator -> statusValidator.accept(contract, contractDto));
        // Signing needs a deposit; the project may already have one when a terminated contract is reopened
        var signing = targetStatus == ContractStatus.SIGNED;
        var projectId = signing ? this.projectQueryService.getProjectIdForContract(contractId) : null;
        if (signing) {
            this.installmentApplicationService.validateDepositForSigning(projectId, contractDto.getDeposit());
        }
        var updatedContract = this.contractDomainService.updateContractStatus(contract, contractDto);

        this.contractRepository.save(updatedContract);
        // Added after the status change, as a deposit is taken only on a signed contract. In the same transaction, so
        // the contract is not signed when the deposit fails
        if (signing && contractDto.getDeposit() != null) {
            this.installmentApplicationService.createDeposit(projectId, contractDto.getDeposit());
        }
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

    @Override
    public Map<Long, ContractDto> getContractsByIds(Collection<Long> contractIds) {
        if (contractIds.isEmpty()) {
            return Map.of();
        }
        return this.contractRepository.findAllById(contractIds)
                .stream()
                .collect(Collectors.toMap(Contract::getId, this.contractMapper::mapToDto));
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
