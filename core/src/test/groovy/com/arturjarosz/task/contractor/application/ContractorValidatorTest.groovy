package com.arturjarosz.task.contractor.application

import com.arturjarosz.task.contractor.infrastructure.ContractorCategoryRepository
import com.arturjarosz.task.contractor.infrastructure.ContractorRepository
import com.arturjarosz.task.contractor.model.Contractor
import com.arturjarosz.task.dto.ContractorDto
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class ContractorValidatorTest extends Specification {
    final static Long EXISTING_CONTRACTOR_ID = 1L
    final static Long NOT_EXISTING_CONTRACTOR_ID = 10L
    final static String NAME = "name"
    final static Long CONTRACTOR_CATEGORY = 1L
    final static Long NOT_EXISTING_CONTRACTOR_CATEGORY = 99L
    final static String NOTE = "some note"
    final static String TELEPHONE = "123-456-789"
    final static String EMAIL = "test@email.com"


    def contractorRepository = Mock(ContractorRepository)
    def contractorCategoryRepository = Stub(ContractorCategoryRepository) {
        existsById(CONTRACTOR_CATEGORY) >> true
    }

    @Subject
    def contractorValidator = new ContractorValidator(contractorRepository, contractorCategoryRepository)

    @Unroll
    def "validateCreateContractorDto throws exception with proper error message"() {
        given: "ContractorDto"
            ContractorDto contractorDto = givenContractorDto
            if (contractorDto != null) {
                contractorDto.with { testedContractorDto ->
                    testedContractorDto.name = name
                    testedContractorDto.categoryId = category
                }
            }

        when: "Calling validateCreateContractorDto"
            this.contractorValidator.validateCreateContractorDto(contractorDto)

        then: "Exception with proper message is thrown"
            Exception exception = thrown()
            exception.message == exceptionMessage

        where:
            givenContractorDto  | name | category                         || exceptionMessage
            null                | null | null                             || "isNull.contractor"
            new ContractorDto() | null | CONTRACTOR_CATEGORY              || "isNull.contractor.name"
            new ContractorDto() | ""   | CONTRACTOR_CATEGORY              || "isEmpty.contractor.name"
            new ContractorDto() | NAME | null                             || "isNull.contractor.category"
            new ContractorDto() | NAME | NOT_EXISTING_CONTRACTOR_CATEGORY || "notExist.contractorCategory"

    }

    def "validateCreateContractorDto does not throw any exception when passed proper contractorDto"() {
        given: "Proper ContractorDto"
            ContractorDto contractorDto = new ContractorDto(name: NAME, categoryId: CONTRACTOR_CATEGORY)

        when: "Calling validateCreateContractorDto"
            this.contractorValidator.validateCreateContractorDto(contractorDto)

        then: "No exception is thrown"
            noExceptionThrown()
    }

    @Unroll("Running test for #exceptionMessage")
    def "validateUpdateContractorDto throws exception with proper error message"() {
        given: "ContractorDto"
            ContractorDto contractorDto = givenContractorDto
            if (contractorDto != null) {
                contractorDto.with { testedContractorDto ->
                    testedContractorDto.name = name
                    testedContractorDto.categoryId = category
                }
            }

        when: "Calling validateCreateContractorDto"
            this.contractorValidator.validateUpdateContractorDto(contractorDto)

        then: "Exception with proper message is thrown"
            Exception exception = thrown()
            exception.message == exceptionMessage

        where:
            givenContractorDto  | name | category                         || exceptionMessage
            null                | null | null                             || "isNull.contractor"
            new ContractorDto() | null | CONTRACTOR_CATEGORY              || "isNull.contractor.name"
            new ContractorDto() | ""   | CONTRACTOR_CATEGORY              || "isEmpty.contractor.name"
            new ContractorDto() | NAME | null                             || "isNull.contractor.category"
            new ContractorDto() | NAME | NOT_EXISTING_CONTRACTOR_CATEGORY || "notExist.contractorCategory"

    }

    def "validateUpdateContractorDto does not throw any exception when passed proper contractorDto"() {
        given: "Proper ContractorDto"
            ContractorDto contractorDto = new ContractorDto(name: NAME, categoryId: CONTRACTOR_CATEGORY)

        when: "Calling validateUpdateContractorDto"
            this.contractorValidator.validateUpdateContractorDto(contractorDto)

        then: "No exception is thrown"
            noExceptionThrown()
    }

    def "validateContractorExistence throws an exception when Contractor with given contractorId does not exist"() {
        given: "Existing Contractor"
            this.mockContractorRepositoryLoadOfNotExistingContractor()

        when: "Calling validateContractorExistence with contractorId of existing Contractor"
            this.contractorValidator.validateContractorExistence(NOT_EXISTING_CONTRACTOR_ID)

        then: "Exception with correct"
            Exception exception = thrown()
            exception.message == "notExist.contractor"
    }

    def "validateContractorExistence does not throw any exception on contractorId of existing Contractor"() {
        given: "Existing Contractor"
            this.mockContractorRepositoryLoadOfExistingContractor()

        when: "Calling validateContractorExistence with contractorId of existing Contractor"
            this.contractorValidator.validateContractorExistence(EXISTING_CONTRACTOR_ID)

        then: "Exception with correct"
            noExceptionThrown()
    }

    private void mockContractorRepositoryLoadOfExistingContractor() {
        1 * this.contractorRepository.findById(EXISTING_CONTRACTOR_ID) >>
                Optional.of(new Contractor(NAME, CONTRACTOR_CATEGORY, EMAIL, TELEPHONE, NOTE))
    }

    private void mockContractorRepositoryLoadOfNotExistingContractor() {
        1 * this.contractorRepository.findById(NOT_EXISTING_CONTRACTOR_ID) >> Optional.ofNullable(null)
    }
}
