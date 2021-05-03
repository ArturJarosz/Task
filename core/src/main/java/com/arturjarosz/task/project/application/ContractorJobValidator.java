package com.arturjarosz.task.project.application;

import com.arturjarosz.task.cooperator.domain.CooperatorExceptionCodes;
import com.arturjarosz.task.cooperator.infrastructure.CooperatorRepository;
import com.arturjarosz.task.cooperator.model.Cooperator;
import com.arturjarosz.task.cooperator.model.CooperatorType;
import com.arturjarosz.task.project.application.dto.ContractorJobDto;
import com.arturjarosz.task.project.model.CooperatorJob;
import com.arturjarosz.task.project.model.CooperatorJobType;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import org.springframework.stereotype.Component;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotEmpty;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotNull;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

@Component
public class ContractorJobValidator {
    private CooperatorRepository cooperatorRepository;

    public ContractorJobValidator(CooperatorRepository cooperatorRepository) {
        this.cooperatorRepository = cooperatorRepository;
    }

    public void validateCreateContractorJobDto(ContractorJobDto contractorJobDto) {
        assertNotNull(contractorJobDto,
                createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.CONTRACTOR_JOB));
        this.validateContractorName(contractorJobDto);
        assertNotNull(contractorJobDto.getContractorId(),
                createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.CONTRACTOR_JOB,
                        ProjectExceptionCodes.CONTRACTOR));
        this.validateContractorJobValue(contractorJobDto);
    }

    public void validateContractorExistence(Long contractorId) {
        Cooperator cooperator = this.cooperatorRepository.load(contractorId);
        assertNotNull(cooperator, createMessageCode(ExceptionCodes.NOT_EXISTS, CooperatorExceptionCodes.CONTRACTOR));
        assertIsTrue(cooperator.getType().equals(CooperatorType.CONTRACTOR),
                createMessageCode(ExceptionCodes.NOT_EXISTS, CooperatorExceptionCodes.CONTRACTOR));
    }

    public void validateContractorJobOnProjectExistence(Project project, Long contractorJobId) {
        CooperatorJob cooperatorJob = project.getCooperatorJobs().stream()
                .filter(cooperatorJobOnProject -> cooperatorJobOnProject.getId().equals(contractorJobId)).findFirst()
                .orElse(null);
        assertNotNull(cooperatorJob,
                createMessageCode(ExceptionCodes.NOT_EXISTS, ProjectExceptionCodes.CONTRACTOR_JOB));
        assertIsTrue(cooperatorJob.getType().equals(CooperatorJobType.CONTRACTOR_JOB),
                createMessageCode(ExceptionCodes.NOT_EXISTS, ProjectExceptionCodes.CONTRACTOR_JOB));
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
        assertIsTrue(contractorJobDto.getValue() >= 0,
                createMessageCode(ExceptionCodes.NOT_VALID, ProjectExceptionCodes.CONTRACTOR_JOB,
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
