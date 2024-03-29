package com.arturjarosz.task.contractor.application;

import com.arturjarosz.task.contractor.domain.ContractorExceptionCodes;
import com.arturjarosz.task.contractor.infrastructure.ContractorRepository;
import com.arturjarosz.task.contractor.model.Contractor;
import com.arturjarosz.task.dto.ContractorDto;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertEntityPresent;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotEmpty;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotNull;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

@RequiredArgsConstructor
@Component
public class ContractorValidator {

    @NonNull
    private final ContractorRepository contractorRepository;

    public void validateCreateContractorDto(ContractorDto contractorDto) {
        assertNotNull(contractorDto, createMessageCode(ExceptionCodes.NULL, ContractorExceptionCodes.CONTRACTOR));
        this.validateName(contractorDto.getName());
        assertNotNull(contractorDto.getCategory(),
                createMessageCode(ExceptionCodes.NULL, ContractorExceptionCodes.CONTRACTOR,
                        ContractorExceptionCodes.CATEGORY));
    }

    public void validateUpdateContractorDto(ContractorDto contractorDto) {
        this.validateCreateContractorDto(contractorDto);
    }

    private void validateName(String name) {
        assertNotNull(name, createMessageCode(ExceptionCodes.NULL, ContractorExceptionCodes.CONTRACTOR,
                ContractorExceptionCodes.NAME));
        assertNotEmpty(name, createMessageCode(ExceptionCodes.EMPTY, ContractorExceptionCodes.CONTRACTOR,
                ContractorExceptionCodes.NAME));
    }

    public void validateContractorExistence(Long contractorId) {
        Optional<Contractor> maybeContractor = this.contractorRepository.findById(contractorId);
        this.validateContractorExistence(maybeContractor, contractorId);
    }

    public void validateContractorHasNoJobs(Long contractorId) {
        //TODO: to implemented when CooperatorJob is ready
    }

    public void validateContractorExistence(Optional<Contractor> maybeContractor, Long contractorId) {
        assertEntityPresent(maybeContractor.isPresent(),
                createMessageCode(ExceptionCodes.NOT_EXIST, ContractorExceptionCodes.CONTRACTOR), contractorId);
    }
}
