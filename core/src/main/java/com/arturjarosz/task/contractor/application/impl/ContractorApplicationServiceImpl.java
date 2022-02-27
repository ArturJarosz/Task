package com.arturjarosz.task.contractor.application.impl;

import com.arturjarosz.task.contractor.application.ContractorApplicationService;
import com.arturjarosz.task.contractor.application.ContractorValidator;
import com.arturjarosz.task.contractor.application.dto.ContractorDto;
import com.arturjarosz.task.contractor.application.mapper.ContractorDtoMapper;
import com.arturjarosz.task.contractor.infrastructure.ContractorRepository;
import com.arturjarosz.task.contractor.model.Contractor;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.model.CreatedEntityDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationService
public class ContractorApplicationServiceImpl implements ContractorApplicationService {
    private static final Logger LOG = LoggerFactory.getLogger(ContractorApplicationServiceImpl.class);

    private final ContractorRepository contractorRepository;
    private final ContractorValidator contractorValidator;

    @Autowired
    public ContractorApplicationServiceImpl(ContractorRepository contractorRepository,
                                            ContractorValidator contractorValidator) {
        this.contractorRepository = contractorRepository;
        this.contractorValidator = contractorValidator;
    }

    @Transactional
    @Override
    public CreatedEntityDto createContractor(ContractorDto contractorDto) {
        LOG.debug("Creating Contractor.");
        this.contractorValidator.validateCreateContractorDto(contractorDto);
        Contractor cooperator = ContractorDtoMapper.INSTANCE.createContractorDtoToContractor(contractorDto);
        this.contractorRepository.save(cooperator);
        LOG.debug("Contractor created.");
        return new CreatedEntityDto(cooperator.getId());
    }

    @Transactional
    @Override
    public void updateContractor(Long contractorId, ContractorDto contractorDto) {
        LOG.debug("Updating Contractor with id {}", contractorId);
        this.contractorValidator.validateContractorExistence(contractorId);
        this.contractorValidator.validateUpdateContractorDto(contractorDto);
        Contractor cooperator = this.contractorRepository.load(contractorId);
        cooperator.update(contractorDto.getName(), contractorDto.getCategory(),
                contractorDto.getEmail(), contractorDto.getTelephone(), contractorDto.getNote());
        this.contractorRepository.save(cooperator);
        LOG.debug("Contractor with id {} updated", contractorId);
    }

    @Transactional
    @Override
    public void deleteContractor(Long contractorId) {
        LOG.debug("Deleting Contractor with id {}", contractorId);
        this.contractorValidator.validateContractorExistence(contractorId);
        this.contractorValidator.validateContractorHasNoJobs(contractorId);
        this.contractorRepository.remove(contractorId);
        LOG.debug("Contractor with id {} deleted.", contractorId);
    }

    @Override
    public ContractorDto getContractor(Long contractorId) {
        LOG.debug("Loading Contractor with id {}.", contractorId);
        this.contractorValidator.validateContractorExistence(contractorId);
        Contractor cooperator = this.contractorRepository.load(contractorId);
        ContractorDto contractorDto = ContractorDtoMapper.INSTANCE.contractorToContractorDto(cooperator);
        LOG.debug("Contractor with id {} loaded", contractorId);
        return contractorDto;
    }

    @Override
    public List<ContractorDto> getBasicContractors() {
        LOG.debug("Loading Contractors list");
        return this.contractorRepository.loadAll().stream()
                .map(ContractorDtoMapper.INSTANCE::cooperatorToBasicContractor).collect(Collectors.toList());
    }
}
