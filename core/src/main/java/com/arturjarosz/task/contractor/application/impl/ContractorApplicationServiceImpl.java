package com.arturjarosz.task.contractor.application.impl;

import com.arturjarosz.task.contractor.application.ContractorApplicationService;
import com.arturjarosz.task.contractor.application.ContractorValidator;
import com.arturjarosz.task.contractor.application.mapper.ContractorMapper;
import com.arturjarosz.task.contractor.infrastructure.ContractorRepository;
import com.arturjarosz.task.contractor.model.ContractorCategory;
import com.arturjarosz.task.contractor.query.ContractorQueryService;
import com.arturjarosz.task.dto.ContractorDto;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@ApplicationService
public class ContractorApplicationServiceImpl implements ContractorApplicationService {

    private final ContractorRepository contractorRepository;
    private final ContractorValidator contractorValidator;
    private final ContractorMapper contractorMapper;
    private final ContractorQueryService contractorQueryService;


    @Transactional
    @Override
    public ContractorDto createContractor(ContractorDto contractorDto) {
        LOG.debug("Creating Contractor.");

        this.contractorValidator.validateCreateContractorDto(contractorDto);
        var contractor = this.contractorMapper.mapFromDto(contractorDto);
        this.contractorRepository.save(contractor);

        LOG.debug("Contractor created.");
        return this.contractorMapper.mapToDto(contractor);
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
                contractorDto.getEmail(), contractorDto.getTelephone(), contractorDto.getNote());
        this.contractorRepository.save(contractor);

        LOG.debug("Contractor with id {} updated", contractorId);
        return this.contractorMapper.mapToDto(contractor);
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
        var contractorDto = this.contractorMapper.mapToDto(
                maybeContractor.orElseThrow(ResourceNotFoundException::new));

        LOG.debug("Contractor with id {} loaded", contractorId);
        return contractorDto;
    }

    @Override
    public List<ContractorDto> getContractors() {
        LOG.debug("Loading Contractors list");
        var numberOfJobsByContractorId = this.contractorQueryService.getNumberOfJobsPerContractor();
        return this.contractorRepository.findAll()
                .stream()
                .map(contractor -> this.contractorMapper.mapToDto(contractor,
                        numberOfJobsByContractorId.get(contractor.getId())))
                .toList();
    }
}
