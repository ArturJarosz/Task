package com.arturjarosz.task.cooperator.application.impl;

import com.arturjarosz.task.cooperator.application.ContractorApplicationService;
import com.arturjarosz.task.cooperator.application.ContractorValidator;
import com.arturjarosz.task.cooperator.application.dto.ContractorDto;
import com.arturjarosz.task.cooperator.application.mapper.ContractorDtoMapper;
import com.arturjarosz.task.cooperator.infrastructure.CooperatorRepository;
import com.arturjarosz.task.cooperator.model.Cooperator;
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

    private final CooperatorRepository cooperatorRepository;
    private final ContractorValidator contractorValidator;

    @Autowired
    public ContractorApplicationServiceImpl(CooperatorRepository cooperatorRepository,
                                            ContractorValidator contractorValidator) {
        this.cooperatorRepository = cooperatorRepository;
        this.contractorValidator = contractorValidator;
    }

    @Transactional
    @Override
    public CreatedEntityDto createContractor(ContractorDto contractorDto) {
        LOG.debug("Creating Contractor.");
        this.contractorValidator.validateCreateContractorDto(contractorDto);
        Cooperator cooperator = ContractorDtoMapper.INSTANCE.createContractorDtoToCooperator(contractorDto);
        this.cooperatorRepository.save(cooperator);
        LOG.debug("Contractor created.");
        return new CreatedEntityDto(cooperator.getId());
    }

    @Transactional
    @Override
    public void updateContractor(Long contractorId, ContractorDto contractorDto) {
        LOG.debug("Updating Contractor with id {}", contractorId);
        this.contractorValidator.validateContractorExistence(contractorId);
        this.contractorValidator.validateUpdateContractorDto(contractorDto);
        Cooperator cooperator = this.cooperatorRepository.load(contractorId);
        cooperator.update(contractorDto.getName(), contractorDto.getCategory().asCooperatorCategory(),
                contractorDto.getEmail(), contractorDto.getTelephone(), contractorDto.getNote());
        this.cooperatorRepository.save(cooperator);
        LOG.debug("Contractor with id {} updated", contractorId);
    }

    @Transactional
    @Override
    public void deleteContractor(Long contractorId) {
        LOG.debug("Deleting Contractor with id {}", contractorId);
        this.contractorValidator.validateContractorExistence(contractorId);
        this.contractorValidator.validateContractorHasNoJobs(contractorId);
        this.cooperatorRepository.remove(contractorId);
        LOG.debug("Contractor with id {} deleted.", contractorId);
    }

    @Override
    public ContractorDto getContractor(Long contractorId) {
        LOG.debug("Loading Contractor with id {}.", contractorId);
        this.contractorValidator.validateContractorExistence(contractorId);
        Cooperator cooperator = this.cooperatorRepository.load(contractorId);
        ContractorDto contractorDto = ContractorDtoMapper.INSTANCE.cooperatorToContractorDto(cooperator);
        LOG.debug("Contractor with id {} loaded", contractorId);
        return contractorDto;
    }

    @Override
    public List<ContractorDto> getBasicContractors() {
        LOG.debug("Loading Contractors list");
        return this.cooperatorRepository.loadAll().stream()
                .map(ContractorDtoMapper.INSTANCE::cooperatorToBasicContractor).collect(Collectors.toList());
    }
}
