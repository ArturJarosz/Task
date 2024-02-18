package com.arturjarosz.task.contractor.application.impl;

import com.arturjarosz.task.contractor.application.ContractorApplicationService;
import com.arturjarosz.task.contractor.application.ContractorValidator;
import com.arturjarosz.task.contractor.application.mapper.ContractorDtoMapper;
import com.arturjarosz.task.contractor.infrastructure.ContractorRepository;
import com.arturjarosz.task.contractor.model.ContractorCategory;
import com.arturjarosz.task.dto.ContractorDto;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        var contractor = ContractorDtoMapper.INSTANCE.createContractorDtoToContractor(contractorDto);
        this.contractorRepository.save(contractor);

        LOG.debug("Contractor created.");
        return ContractorDtoMapper.INSTANCE.contractorToContractorDto(contractor);
    }

    @Transactional
    @Override
    public ContractorDto updateContractor(Long contractorId, ContractorDto contractorDto) {
        LOG.debug("Updating Contractor with id {}", contractorId);

        var maybeContractor = this.contractorRepository.findById(contractorId);
        this.contractorValidator.validateContractorExistence(maybeContractor, contractorId);
        this.contractorValidator.validateUpdateContractorDto(contractorDto);
        var contractor = maybeContractor.orElseThrow(ResourceNotFoundException::new);
        contractor.update(contractorDto.getName(), ContractorCategory.valueOf(contractorDto.getCategory().name()),
                contractorDto.getEmail(),
                contractorDto.getTelephone(), contractorDto.getNote());
        this.contractorRepository.save(contractor);

        LOG.debug("Contractor with id {} updated", contractorId);
        return ContractorDtoMapper.INSTANCE.contractorToContractorDto(contractor);
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

        var maybeContractor = this.contractorRepository.findById(contractorId);
        this.contractorValidator.validateContractorExistence(maybeContractor, contractorId);
        var contractorDto = ContractorDtoMapper.INSTANCE.contractorToContractorDto(
                maybeContractor.orElseThrow(ResourceNotFoundException::new));

        LOG.debug("Contractor with id {} loaded", contractorId);
        return contractorDto;
    }

    @Override
    public List<ContractorDto> getBasicContractors() {
        LOG.debug("Loading Contractors list");
        return this.contractorRepository.findAll().stream()
                .map(ContractorDtoMapper.INSTANCE::contractorToContractorDto).toList();
    }
}
