package com.arturjarosz.task.finance.application

import com.arturjarosz.task.contractor.query.impl.ContractorQueryServiceImpl
import com.arturjarosz.task.dto.ContractorJobDto
import com.arturjarosz.task.finance.application.validator.ContractorJobValidator
import com.arturjarosz.task.finance.query.FinancialDataQueryService
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException
import com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException
import spock.lang.Specification
import spock.lang.Unroll

class ContractorJobValidatorTest extends Specification {
    static final Long EXISTING_CONTRACTOR_ID = 1L
    static final Long NOT_EXISTING_CONTRACTOR_ID = 9L
    static final Long EXISTING_CONTRACTOR_JOB_ID = 10L
    static final Long NOT_EXISTING_CONTRACTOR_JOB_ID = 11L
    static final Long PROJECT_ID = 20L

    def contractorQueryService = Mock(ContractorQueryServiceImpl)
    def financialDataQueryService = Mock(FinancialDataQueryService)

    def contractorValidator = new ContractorJobValidator(contractorQueryService,
            financialDataQueryService)

    def setup() {
        this.financialDataQueryService.getContractorJobById(NOT_EXISTING_CONTRACTOR_JOB_ID) >> null
        this.financialDataQueryService.getContractorJobById(EXISTING_CONTRACTOR_JOB_ID) >> new ContractorJobDto()
        this.contractorQueryService.contractorWithIdExists(EXISTING_CONTRACTOR_ID) >> true
        this.contractorQueryService.contractorWithIdExists(NOT_EXISTING_CONTRACTOR_ID) >> false
    }

    def 'validateContractorExistence throws an exception if Contractor with given id does not exist'() {
        given:
        when:
            this.contractorValidator.validateContractorExistence(NOT_EXISTING_CONTRACTOR_ID)
        then:
            Exception exception = thrown()
            exception.message == "notExist.contractor"
    }

    def "validateContractorExistence do not throw any exception if Contractor with given id exists"() {
        given:
        when:
            this.contractorValidator.validateContractorExistence(EXISTING_CONTRACTOR_ID)
        then:
            noExceptionThrown()
    }

    @Unroll
    def "validateContractorExistence throws an exception with proper exception message"() {
        given:
            def contractorJobDto = contractorJob
            if (contractorJobDto != null) {
                contractorJobDto.name = contractorJobName
                contractorJobDto.contractorId = contractorJobId
                contractorJobDto.value = contractorJobValue
            }
        when:
            this.contractorValidator.validateCreateContractorJobDto(contractorJobDto)
        then:
            Exception exception = thrown()
            exception.message == exceptionMessage
        where:
            contractorJob          | contractorJobId        | contractorJobName | contractorJobValue || exceptionMessage
            null                   | null                   | null              | null               || "isNull.contractorJob"
            new ContractorJobDto() | null                   | null              | null               || "isNull.contractorJob.contractor"
            new ContractorJobDto() |
                    EXISTING_CONTRACTOR_ID                  | null              | null               || "isNull.contractorJob.name"
            new ContractorJobDto() |
                    EXISTING_CONTRACTOR_ID                  | ""                | null               || "isEmpty.contractorJob.name"
            new ContractorJobDto() |
                    EXISTING_CONTRACTOR_ID                  | "name"            | null               || "isNull.contractorJob.value"
            new ContractorJobDto() | EXISTING_CONTRACTOR_ID | "name"            | new BigDecimal(
                    "-1")                                                                            || "negative.contractorJob.value"
    }

    @Unroll
    def "validateUpdateContractorJobDto throws an exception with proper exception message"() {
        given:
            def contractorJobDto = contractorJob
            if (contractorJobDto != null) {
                contractorJobDto.name = contractorJobName
                contractorJobDto.value = contractorJobValue
            }
        when:
            this.contractorValidator.validateUpdateContractorJobDto(contractorJobDto)
        then:
            Exception exception = thrown()
            exception.message == exceptionMessage
        where:
            contractorJob          | contractorJobId        | contractorJobName | contractorJobValue || exceptionMessage
            null                   | null                   | null              | null               || "isNull.contractorJob"
            new ContractorJobDto() |
                    EXISTING_CONTRACTOR_ID                  | null              | null               || "isNull.contractorJob.name"
            new ContractorJobDto() |
                    EXISTING_CONTRACTOR_ID                  | ""                | null               || "isEmpty.contractorJob.name"
            new ContractorJobDto() |
                    EXISTING_CONTRACTOR_ID                  | "name"            | null               || "isNull.contractorJob.value"
            new ContractorJobDto() | EXISTING_CONTRACTOR_ID | "name"            | new BigDecimal(
                    "-1")                                                                            || "negative.contractorJob.value"
    }

    def "validateContractorJobOnProjectExistence throws an exception with not existing contractorJobId"() {
        given:
        when:
            this.contractorValidator.validateContractorJobOnProjectExistence(PROJECT_ID, NOT_EXISTING_CONTRACTOR_JOB_ID)
        then:
            thrown(IllegalArgumentException)
    }

    def "validateContractorJobOnProjectExistence does not throw any exception with existing contractorJobId"() {
        given:
        when:
            this.contractorValidator.validateContractorJobOnProjectExistence(PROJECT_ID, EXISTING_CONTRACTOR_JOB_ID)
        then:
            noExceptionThrown()
    }

    def "validateContractorJobExistence throws an exception when null passed as contractorJobDto"() {
        given:
            def contractorJobDto = null
        when:
            this.contractorValidator.validateContractorJobExistence(contractorJobDto, PROJECT_ID, NOT_EXISTING_CONTRACTOR_JOB_ID)
        then:
            thrown(ResourceNotFoundException)
    }

    def "validateContractorJobExistence does not throw any exception when null passed as contractorJobDto"() {
        given:
            def contractorJobDto = new ContractorJobDto()
        when:
            this.contractorValidator.validateContractorJobExistence(contractorJobDto, PROJECT_ID, EXISTING_CONTRACTOR_JOB_ID)
        then:
            noExceptionThrown()
    }
}
