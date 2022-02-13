package com.arturjarosz.task.cooperator.application;

import com.arturjarosz.task.cooperator.application.dto.ContractorDto;
import com.arturjarosz.task.cooperator.domain.CooperatorExceptionCodes;
import com.arturjarosz.task.cooperator.infrastructure.CooperatorRepository;
import com.arturjarosz.task.cooperator.model.Cooperator;
import com.arturjarosz.task.cooperator.model.CooperatorType;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import org.springframework.stereotype.Component;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotEmpty;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotNull;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

@Component
public class ContractorValidator {

    private final CooperatorRepository cooperatorRepository;

    public ContractorValidator(CooperatorRepository cooperatorRepository) {
        this.cooperatorRepository = cooperatorRepository;
    }

    public void validateCreateContractorDto(ContractorDto contractorDto) {
        assertNotNull(contractorDto, createMessageCode(ExceptionCodes.NULL, CooperatorExceptionCodes.CONTRACTOR));
        this.validateName(contractorDto.getName());
        assertNotNull(contractorDto.getCategory(),
                createMessageCode(ExceptionCodes.NULL, CooperatorExceptionCodes.CONTRACTOR,
                        CooperatorExceptionCodes.CATEGORY));
    }

    public void validateUpdateContractorDto(ContractorDto contractorDto) {
        assertNotNull(contractorDto, createMessageCode(ExceptionCodes.NULL, CooperatorExceptionCodes.CONTRACTOR));
        this.validateName(contractorDto.getName());
        assertNotNull(contractorDto.getCategory(),
                createMessageCode(ExceptionCodes.NULL, CooperatorExceptionCodes.CONTRACTOR,
                        CooperatorExceptionCodes.CATEGORY));
    }

    private void validateName(String name) {
        assertNotNull(name, createMessageCode(ExceptionCodes.NULL, CooperatorExceptionCodes.CONTRACTOR,
                CooperatorExceptionCodes.NAME));
        assertNotEmpty(name, createMessageCode(ExceptionCodes.EMPTY, CooperatorExceptionCodes.CONTRACTOR,
                CooperatorExceptionCodes.NAME));
    }

    public void validateContractorExistence(Long contractorId) {
        Cooperator cooperator = this.cooperatorRepository.load(contractorId);
        assertNotNull(cooperator, createMessageCode(ExceptionCodes.NOT_EXIST, CooperatorExceptionCodes.CONTRACTOR));
        assertIsTrue(cooperator.getType().equals(CooperatorType.CONTRACTOR),
                createMessageCode(ExceptionCodes.NOT_EXIST, CooperatorExceptionCodes.CONTRACTOR));
    }

    public void validateContractorHasNoJobs(Long contractorId) {
        //TODO: to implemented when CooperatorJob is ready
    }
}
