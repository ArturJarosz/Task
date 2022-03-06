package com.arturjarosz.task.project.application;

import com.arturjarosz.task.contractor.domain.ContractorExceptionCodes;
import com.arturjarosz.task.contractor.query.ContractorQueryService;
import com.arturjarosz.task.project.application.dto.ContractorJobDto;
import com.arturjarosz.task.project.query.ProjectQueryService;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotEmpty;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotNull;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

@Component
public class ContractorJobValidator {
    private final ContractorQueryService contractorQueryService;
    private final ProjectQueryService projectQueryService;

    @Autowired
    public ContractorJobValidator(ContractorQueryService contractorQueryService,
                                  ProjectQueryService projectQueryService) {
        this.contractorQueryService = contractorQueryService;
        this.projectQueryService = projectQueryService;
    }

    public void validateCreateContractorJobDto(ContractorJobDto contractorJobDto) {
        assertNotNull(contractorJobDto,
                createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.CONTRACTOR_JOB));
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
        assertNotNull(this.projectQueryService.getContractorJobForProject(contractorJobId, projectId),
                createMessageCode(ExceptionCodes.NOT_EXIST, ProjectExceptionCodes.PROJECT,
                        ProjectExceptionCodes.CONTRACTOR_JOB), projectId, contractorJobId);
    }

    public void validateUpdateContractorJobDto(ContractorJobDto contractorJobDto) {
        assertNotNull(contractorJobDto,
                createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.CONTRACTOR_JOB));
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

}
