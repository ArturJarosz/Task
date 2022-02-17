package com.arturjarosz.task.cooperator.application

import com.arturjarosz.task.cooperator.application.dto.ContractorDto
import com.arturjarosz.task.cooperator.infrastructure.impl.CooperatorRepositoryImpl
import com.arturjarosz.task.cooperator.model.Cooperator
import com.arturjarosz.task.cooperator.model.CooperatorCategory
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class ContractorValidatorTest extends Specification {
    private final static Long EXISTING_CONTRACTOR_ID = 1L
    private final static Long EXISTING_SUPPLIER_ID = 5L
    private final static Long NOT_EXISTING_CONTRACTOR_ID = 10L
    private final static String NAME = "name"
    private final static CooperatorCategory.SupplierCategory SUPPLIER_CATEGORY =
            CooperatorCategory.SupplierCategory.PAINT_SHOP
    private final static CooperatorCategory.ContractorCategory CONTRACTOR_CATEGORY =
            CooperatorCategory.ContractorCategory.ARTIST

    def cooperatorRepository = Mock(CooperatorRepositoryImpl)

    @Subject
    def contractorValidator = new ContractorValidator(cooperatorRepository)

    @Unroll
    def "validateCreateContractorDto throws exception with proper error message"() {
        given: "ContractorDto"
            ContractorDto contractorDto = givenContractorDto
            if (contractorDto != null) {
                contractorDto.with { testedContractorDto ->
                    testedContractorDto.name = name
                    testedContractorDto.category = category
                }
            }

        when: "Calling validateCreateContractorDto"
            this.contractorValidator.validateCreateContractorDto(contractorDto)

        then: "Exception with proper message is thrown"
            Exception exception = thrown()
            exception.message == exceptionMessage

        where:
            givenContractorDto  | name | category            || exceptionMessage
            null                | null | null                || "isNull.contractor"
            new ContractorDto() | null | CONTRACTOR_CATEGORY || "isNull.contractor.name"
            new ContractorDto() | ""   | CONTRACTOR_CATEGORY || "isEmpty.contractor.name"
            new ContractorDto() | NAME | null                || "isNull.contractor.category"

    }

    def "validateCreateContractorDto does not throw any exception when passed proper contractorDto"() {
        given: "Proper ContractorDto"
            ContractorDto contractorDto = new ContractorDto(name: NAME, category: CONTRACTOR_CATEGORY)

        when: "Calling validateCreateContractorDto"
            this.contractorValidator.validateCreateContractorDto(contractorDto)

        then: "No exception is thrown"
            noExceptionThrown()
    }

    @Unroll("Runnning test for #exceptionMessage")
    def "validateUpdateContractorDto throws exception with proper error message"() {
        given: "ContractorDto"
            ContractorDto contractorDto = givenContractorDto
            if (contractorDto != null) {
                contractorDto.with { testedContractorDto ->
                    testedContractorDto.name = name
                    testedContractorDto.category = category
                }
            }

        when: "Calling validateCreateContractorDto"
            this.contractorValidator.validateUpdateContractorDto(contractorDto)

        then: "Exception with proper message is thrown"
            Exception exception = thrown()
            exception.message == exceptionMessage

        where:
            givenContractorDto  | name | category            || exceptionMessage
            null                | null | null                || "isNull.contractor"
            new ContractorDto() | null | CONTRACTOR_CATEGORY || "isNull.contractor.name"
            new ContractorDto() | ""   | CONTRACTOR_CATEGORY || "isEmpty.contractor.name"
            new ContractorDto() | NAME | null                || "isNull.contractor.category"

    }

    def "validateUpdateContractorDto does not throw any exception when passed proper contractorDto"() {
        given: "Proper ContractorDto"
            ContractorDto contractorDto = new ContractorDto(name: NAME, category: CONTRACTOR_CATEGORY)

        when: "Calling validateUpdateContractorDto"
            this.contractorValidator.validateUpdateContractorDto(contractorDto)

        then: "No exception is thrown"
            noExceptionThrown()
    }

    def "validateContractorExistence throws an exception when Contractor with given contractorId does not exist"() {
        given: "Existing Contractor"
            this.mockCooperatorRepositoryLoadOfNotExistingContractor()

        when: "Calling validateContractorExistence with contractorId of existing Contractor"
            this.contractorValidator.validateContractorExistence(NOT_EXISTING_CONTRACTOR_ID)

        then: "Exception with correct"
            Exception exception = thrown()
            exception.message == "notExist.contractor"
    }

    def "validateContractorExistence throws an exception when Cooperator of given contractorId is not Contractor"() {
        given: "Existing Contractor"
            this.mockCooperatorRepositoryLoadOfSupplier()

        when: "Calling validateContractorExistence with contractorId of existing Contractor"
            this.contractorValidator.validateContractorExistence(EXISTING_SUPPLIER_ID)

        then: "Exception with correct"
            Exception exception = thrown()
            exception.message == "notExist.contractor"
    }

    def "validateContractorExistence does not throw any exception on contractorId of existing Contractor"() {
        given: "Existing Contractor"
            this.mockCooperatorRepositoryLoadOfExistingContractor()

        when: "Calling validateContractorExistence with contractorId of existing Contractor"
            this.contractorValidator.validateContractorExistence(EXISTING_CONTRACTOR_ID)

        then: "Exception with correct"
            noExceptionThrown()
    }

    private void mockCooperatorRepositoryLoadOfExistingContractor() {
        1 * this.cooperatorRepository.load(EXISTING_CONTRACTOR_ID) >>
                Cooperator.createContractor(NAME, CONTRACTOR_CATEGORY)
    }

    private void mockCooperatorRepositoryLoadOfNotExistingContractor() {
        1 * this.cooperatorRepository.load(NOT_EXISTING_CONTRACTOR_ID) >> null
    }

    private void mockCooperatorRepositoryLoadOfSupplier() {
        1 * this.cooperatorRepository.load(EXISTING_SUPPLIER_ID) >> Cooperator.createSupplier(NAME, SUPPLIER_CATEGORY)
    }
}
