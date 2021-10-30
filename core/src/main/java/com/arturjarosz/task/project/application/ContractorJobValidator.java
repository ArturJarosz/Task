package com.arturjarosz.task.project.application;

import com.arturjarosz.task.cooperator.domain.CooperatorExceptionCodes;
import com.arturjarosz.task.cooperator.query.CooperatorQueryService;
import com.arturjarosz.task.project.application.dto.ContractorJobDto;
import com.arturjarosz.task.project.query.ProjectQueryService;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.*;

@Component
public class ContractorJobValidator {
    private final CooperatorQueryService cooperatorQueryService;
    private final ProjectQueryService projectQueryService;

    @Autowired
    public ContractorJobValidator(CooperatorQueryService cooperatorQueryService,
                                  ProjectQueryService projectQueryService) {
        this.cooperatorQueryService = cooperatorQueryService;
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
        assertIsTrue(this.cooperatorQueryService.contractorWithIdExists(contractorId),
                createMessageCode(ExceptionCodes.NOT_EXIST, CooperatorExceptionCodes.CONTRACTOR), contractorId);
    }

    public void validateContractorJobOnProjectExistence(Long projectId, Long contractorJobId) {
        assertNotNull(this.projectQueryService.getContractorJobForProject(projectId, contractorJobId),
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
