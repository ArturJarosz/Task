package com.arturjarosz.task.project.application

import com.arturjarosz.task.cooperator.query.impl.CooperatorQueryServiceImpl
import com.arturjarosz.task.project.application.dto.ContractorJobDto
import com.arturjarosz.task.project.query.impl.ProjectQueryServiceImpl
import spock.lang.Specification
import spock.lang.Unroll

class ContractorJobValidatorTest extends Specification {
    private static final Long EXISTING_CONTRACTOR_ID = 1L;
    private static final Long NOT_EXISTING_CONTRACTOR_ID = 9L;

    def cooperatorQueryService = Mock(CooperatorQueryServiceImpl);
    def projectQueryService = Mock(ProjectQueryServiceImpl);

    def cooperatorValidator = new ContractorJobValidator(cooperatorQueryService, projectQueryService);

    def 'validateContractorExistence throws an exception if Contractor with given id does not exist'() {
        given:
            this.mockCooperatorQueryServiceContractorWithIdExistenceDoesntExists();
        when:
            this.cooperatorValidator.validateContractorExistence(NOT_EXISTING_CONTRACTOR_ID);
        then:
            Exception exception = thrown();
            exception.message == "notExist.contractor"
    }

    def "validateContractorExistence do not throw any exception if Contractor with given id exists"() {
        given:
            this.mockCooperatorQueryServiceContractorWithIdExistenceExists();
        when:
            this.cooperatorValidator.validateContractorExistence(EXISTING_CONTRACTOR_ID);
        then:
            noExceptionThrown();
    }

    @Unroll
    def "validateContractorExistence throws an exception with proper exception message"() {
        given:
            ContractorJobDto contractorJobDto = contractorJob;
            if (contractorJobDto != null) {
                contractorJobDto.setName(contractorJobName);
                contractorJobDto.setContractorId(contractorJobId);
                contractorJobDto.setValue(contractorJobValue);
            }
        when:
            this.cooperatorValidator.validateCreateContractorJobDto(contractorJobDto);
        then:
            Exception exception = thrown();
            exception.message == exceptionMessage;
        where:
            contractorJob          | contractorJobId        | contractorJobName | contractorJobValue || exceptionMessage
            null                   | null                   | null              | null               || "isNull.contractorJob"
            new ContractorJobDto() | null                   | null              | null               || "isNull.contractorJob.contractor"
            new ContractorJobDto() | EXISTING_CONTRACTOR_ID | null              | null               || "isNull.contractorJob.name"
            new ContractorJobDto() | EXISTING_CONTRACTOR_ID | ""                | null               || "isEmpty.contractorJob.name"
            new ContractorJobDto() | EXISTING_CONTRACTOR_ID | "name"            | null               || "isNull.contractorJob.value"
            new ContractorJobDto() | EXISTING_CONTRACTOR_ID | "name"            | new BigDecimal(
                    -1)                                                                              || "negative.contractorJob.value"
    }

    @Unroll
    def "validateUpdateContractorJobDto throws an exception with proper exception message"() {
        given:
            ContractorJobDto contractorJobDto = contractorJob;
            if (contractorJobDto != null) {
                contractorJobDto.setName(contractorJobName);
                contractorJobDto.setValue(contractorJobValue);
            }
        when:
            this.cooperatorValidator.validateUpdateContractorJobDto(contractorJobDto);
        then:
            Exception exception = thrown();
            exception.message == exceptionMessage;
        where:
            contractorJob          | contractorJobId        | contractorJobName | contractorJobValue || exceptionMessage
            null                   | null                   | null              | null               || "isNull.contractorJob"
            new ContractorJobDto() | EXISTING_CONTRACTOR_ID | null              | null               || "isNull.contractorJob.name"
            new ContractorJobDto() | EXISTING_CONTRACTOR_ID | ""                | null               || "isEmpty.contractorJob.name"
            new ContractorJobDto() | EXISTING_CONTRACTOR_ID | "name"            | null               || "isNull.contractorJob.value"
            new ContractorJobDto() | EXISTING_CONTRACTOR_ID | "name"            | new BigDecimal(
                    -1)                                                                              || "negative.contractorJob.value"
    }

    private void mockCooperatorQueryServiceContractorWithIdExistenceDoesntExists() {
        1 * this.cooperatorQueryService.contractorWithIdExists(NOT_EXISTING_CONTRACTOR_ID) >> false;
    }

    private void mockCooperatorQueryServiceContractorWithIdExistenceExists() {
        1 * this.cooperatorQueryService.contractorWithIdExists(EXISTING_CONTRACTOR_ID) >> true;
    }
}
