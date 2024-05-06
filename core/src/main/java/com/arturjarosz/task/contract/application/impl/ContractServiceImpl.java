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
import org.apache.logging.log4j.util.TriConsumer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

    private Map<ContractStatus, TriConsumer<Optional<Contract>, ContractDto, Long>> statusToValidator;

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
        this.statusToValidator = new HashMap<>();
        this.statusToValidator.put(ContractStatus.COMPLETED, (maybeContract, contractDto, contractId) -> {
            this.contractValidator.validateCompleteContractDto(contractDto);
        });
        this.statusToValidator.put(ContractStatus.SIGNED, (maybeContract, contractDto, contractId) -> {
            this.contractValidator.validateSignContractDto(contractDto);
        });
        this.statusToValidator.put(ContractStatus.OFFER, (maybeContract, contractDto, contractId) -> {
            this.contractValidator.validateOffer(contractDto);
        });
        this.statusToValidator.put(ContractStatus.TERMINATED, (maybeContract, contractDto, contractId) -> {
            this.contractValidator.validateTerminateContractDto(contractDto);
        });
    }

    @Transactional
    @Override
    public ContractDto createContract(ContractDto contractDto) {
        LOG.debug("Creating contract.");

        this.contractValidator.validateOffer(contractDto);
        var contract = this.contractDomainService.createContract(contractDto);
        contract = this.contractRepository.save(contract);

        LOG.debug("Contract with id {} created", contract.getId());
        return this.contractMapper.mapToDto(contract);
    }

    @Override
    public ContractDto changeStatus(Long contractId, ContractDto contractDto) {
        LOG.debug("Changing contract status");

        var maybeContract = this.contractRepository.findById(contractId);
        this.contractValidator.validateBaseContractDto(contractDto);
        this.contractValidator.validateContractExistence(maybeContract, contractId);
        Optional.ofNullable(this.statusToValidator.get(ContractStatus.valueOf(contractDto.getStatus().name())))
                .ifPresent(statusValidator -> statusValidator.accept(maybeContract, contractDto, contractId));
        var contract = maybeContract.orElseThrow(ResourceNotFoundException::new);
        contract = this.contractDomainService.updateContractStatus(contract, contractDto);

        this.contractRepository.save(contract);
        LOG.debug("Contract status for contract with id {} was updated.", contractId);

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
