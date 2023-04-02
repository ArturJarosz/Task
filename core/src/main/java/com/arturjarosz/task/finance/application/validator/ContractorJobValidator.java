package com.arturjarosz.task.finance.application.validator;

import com.arturjarosz.task.contractor.domain.ContractorExceptionCodes;
import com.arturjarosz.task.contractor.query.ContractorQueryService;
import com.arturjarosz.task.finance.application.dto.ContractorJobDto;
import com.arturjarosz.task.finance.query.FinancialDataQueryService;
import com.arturjarosz.task.project.application.ProjectExceptionCodes;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotEmpty;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotNull;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

@RequiredArgsConstructor
@Component
public class ContractorJobValidator {
    @NonNull
    private final ContractorQueryService contractorQueryService;
    @NonNull
    private final FinancialDataQueryService financialDataQueryService;

    public void validateCreateContractorJobDto(ContractorJobDto contractorJobDto) {
        assertNotNull(contractorJobDto, createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.CONTRACTOR_JOB));
        assertNotNull(contractorJobDto.getContractorId(),
                createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.CONTRACTOR_JOB,
                        ProjectExceptionCodes.CONTRACTOR));
        this.validateContractorName(contractorJobDto);
        this.validateContractorJobValue(contractorJobDto);
    }

    public void validateContractorExistence(Long contractorId) {
        assertIsTrue(this.contractorQueryService.contractorWithIdExists(contractorId),
                createMessageCode(ExceptionCodes.NOT_EXIST, ContractorExceptionCodes.CONTRACTOR), contractorId);
    }

    public void validateContractorJobOnProjectExistence(Long projectId, Long contractorJobId) {
        assertNotNull(this.financialDataQueryService.getContractorJobById(contractorJobId),
                createMessageCode(ExceptionCodes.NOT_EXIST, ProjectExceptionCodes.PROJECT,
                        ProjectExceptionCodes.CONTRACTOR_JOB), projectId, contractorJobId);
    }

    public void validateUpdateContractorJobDto(ContractorJobDto contractorJobDto) {
        assertNotNull(contractorJobDto, createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.CONTRACTOR_JOB));
        this.validateContractorName(contractorJobDto);
        this.validateContractorJobValue(contractorJobDto);
    }

    private void validateContractorJobValue(ContractorJobDto contractorJobDto) {
        assertNotNull(contractorJobDto.getValue(),
                createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.CONTRACTOR_JOB,
                        ProjectExceptionCodes.VALUE));
        assertIsTrue(contractorJobDto.getValue().doubleValue() >= 0,
                createMessageCode(ExceptionCodes.NEGATIVE, ProjectExceptionCodes.CONTRACTOR_JOB,
                        ProjectExceptionCodes.VALUE));
    }

    private void validateContractorName(ContractorJobDto contractorJobDto) {
        assertNotNull(contractorJobDto.getName(),
                createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.CONTRACTOR_JOB,
                        ProjectExceptionCodes.NAME));
        assertNotEmpty(contractorJobDto.getName(),
                createMessageCode(ExceptionCodes.EMPTY, ProjectExceptionCodes.CONTRACTOR_JOB,
                        ProjectExceptionCodes.NAME));
    }

    public void validateContractorJobExistence(ContractorJobDto contractorJobDto, Long projectId,
            Long contractorJobId) {
        assertNotNull(contractorJobDto, createMessageCode(ExceptionCodes.NOT_EXIST, ProjectExceptionCodes.PROJECT,
                ProjectExceptionCodes.CONTRACTOR_JOB), projectId, contractorJobId);
    }
}
