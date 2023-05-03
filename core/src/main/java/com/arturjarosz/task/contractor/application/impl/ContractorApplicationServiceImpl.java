package com.arturjarosz.task.contractor.application.impl;

import com.arturjarosz.task.contractor.application.ContractorApplicationService;
import com.arturjarosz.task.contractor.application.ContractorValidator;
import com.arturjarosz.task.contractor.application.dto.ContractorDto;
import com.arturjarosz.task.contractor.application.mapper.ContractorDtoMapper;
import com.arturjarosz.task.contractor.infrastructure.ContractorRepository;
import com.arturjarosz.task.contractor.model.Contractor;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@ApplicationService
public class ContractorApplicationServiceImpl implements ContractorApplicationService {

    @NonNull
    private final ContractorRepository contractorRepository;
    @NonNull
    private final ContractorValidator contractorValidator;


    @Transactional
    @Override
    public ContractorDto createContractor(ContractorDto contractorDto) {
        LOG.debug("Creating Contractor.");
        this.contractorValidator.validateCreateContractorDto(contractorDto);
        Contractor contractor = ContractorDtoMapper.INSTANCE.createContractorDtoToContractor(contractorDto);
        this.contractorRepository.save(contractor);
        LOG.debug("Contractor created.");
        return ContractorDtoMapper.INSTANCE.contractorToContractorDto(contractor);
    }

    @Transactional
    @Override
    public void updateContractor(Long contractorId, ContractorDto contractorDto) {
        LOG.debug("Updating Contractor with id {}", contractorId);
        Optional<Contractor> maybeContractor = this.contractorRepository.findById(contractorId);
        this.contractorValidator.validateContractorExistence(maybeContractor, contractorId);
        this.contractorValidator.validateUpdateContractorDto(contractorDto);
        Contractor contractor = maybeContractor.orElseThrow(ResourceNotFoundException::new);
        contractor.update(contractorDto.getName(), contractorDto.getCategory(), contractorDto.getEmail(),
                contractorDto.getTelephone(), contractorDto.getNote());
        this.contractorRepository.save(contractor);
        LOG.debug("Contractor with id {} updated", contractorId);
    }

    @Transactional
    @Override
    public void deleteContractor(Long contractorId) {
        LOG.debug("Deleting Contractor with id {}", contractorId);
        this.contractorValidator.validateContractorExistence(contractorId);
        this.contractorValidator.validateContractorHasNoJobs(contractorId);
        this.contractorRepository.deleteById(contractorId);
        LOG.debug("Contractor with id {} deleted.", contractorId);
    }

    @Override
    public ContractorDto getContractor(Long contractorId) {
        LOG.debug("Loading Contractor with id {}.", contractorId);
        Optional<Contractor> maybeContractor = this.contractorRepository.findById(contractorId);
        this.contractorValidator.validateContractorExistence(maybeContractor, contractorId);
        ContractorDto contractorDto = ContractorDtoMapper.INSTANCE.contractorToContractorDto(
                maybeContractor.orElseThrow(ResourceNotFoundException::new));
        LOG.debug("Contractor with id {} loaded", contractorId);
        return contractorDto;
    }

    @Override
    public List<ContractorDto> getBasicContractors() {
        LOG.debug("Loading Contractors list");
        return this.contractorRepository.findAll().stream()
                .map(ContractorDtoMapper.INSTANCE::contractorToBasicContractor).toList();
    }
}
