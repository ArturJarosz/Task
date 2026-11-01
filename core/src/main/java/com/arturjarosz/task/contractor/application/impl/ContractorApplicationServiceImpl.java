package com.arturjarosz.task.contractor.application.impl;

import com.arturjarosz.task.contractor.application.ContractorApplicationService;
import com.arturjarosz.task.contractor.application.ContractorValidator;
import com.arturjarosz.task.contractor.application.mapper.ContractorContractorJobMapper;
import com.arturjarosz.task.contractor.application.mapper.ContractorMapper;
import com.arturjarosz.task.contractor.infrastructure.ContractorRepository;
import com.arturjarosz.task.contractor.query.ContractorQueryService;
import com.arturjarosz.task.dto.ContractorContractorJobsDataDto;
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
    private final ContractorContractorJobMapper contractorContractorJobMapper;


    @Transactional
    @Override
    public ContractorDto createContractor(ContractorDto contractorDto) {
        LOG.info("Creating Contractor.");

        this.contractorValidator.validateCreateContractorDto(contractorDto);
        var contractor = this.contractorMapper.mapFromDto(contractorDto);
        this.contractorRepository.save(contractor);

        LOG.info("Contractor created.");
        return this.contractorMapper.mapToDto(contractor);
    }

    @Transactional
    @Override
    public ContractorDto updateContractor(Long contractorId, ContractorDto contractorDto) {
        LOG.info("Updating Contractor with id {}", contractorId);

        var maybeContractor = this.contractorRepository.findById(contractorId);
        this.contractorValidator.validateContractorExistence(maybeContractor, contractorId);
        this.contractorValidator.validateUpdateContractorDto(contractorDto);
        var contractor = maybeContractor.orElseThrow(ResourceNotFoundException::new);
        contractor.update(contractorDto.getName(), contractorDto.getCategoryId(),
                contractorDto.getEmail(), contractorDto.getTelephone(), contractorDto.getNote());
        this.contractorRepository.save(contractor);

        LOG.info("Contractor with id {} updated", contractorId);
        return this.contractorMapper.mapToDto(contractor);
    }

    @Transactional
    @Override
    public void deleteContractor(Long contractorId) {
        LOG.info("Deleting Contractor with id {}", contractorId);

        this.contractorValidator.validateContractorExistence(contractorId);
        this.contractorValidator.validateContractorHasNoJobs(contractorId);
        this.contractorRepository.deleteById(contractorId);

        LOG.info("Contractor with id {} deleted.", contractorId);
    }

    @Override
    public ContractorDto getContractor(Long contractorId) {
        LOG.info("Loading Contractor with id {}.", contractorId);

        var maybeContractor = this.contractorRepository.findById(contractorId);
        this.contractorValidator.validateContractorExistence(maybeContractor, contractorId);
        var contractorDto = this.contractorMapper.mapToDto(
                maybeContractor.orElseThrow(ResourceNotFoundException::new));

        LOG.info("Contractor with id {} loaded", contractorId);
        return contractorDto;
    }

    @Override
    public List<ContractorDto> getContractors() {
        LOG.info("Loading Contractors list");
        var numberOfJobsByContractorId = this.contractorQueryService.getNumberOfJobsPerContractor();
        return this.contractorRepository.findAll()
                .stream()
                .map(contractor -> this.contractorMapper.mapToDto(contractor,
                        numberOfJobsByContractorId.get(contractor.getId())))
                .toList();
    }

    @Override
    public ContractorContractorJobsDataDto getContractorJobsData(Long contractorId) {
        LOG.info("Loading Contractor Jobs data for Contractor with id {}", contractorId);

        this.contractorValidator.validateContractorExistence(contractorId);
        var data = this.contractorQueryService.getContractorJobsData(contractorId);

        return this.contractorContractorJobMapper.mapToContractorContractorJobsDataDto(data);
    }
}
