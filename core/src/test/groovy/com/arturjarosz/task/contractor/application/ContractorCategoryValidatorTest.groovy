package com.arturjarosz.task.contractor.application

import com.arturjarosz.task.dto.ContractorCategoryDto
import com.arturjarosz.task.contractor.infrastructure.ContractorCategoryRepository
import com.arturjarosz.task.contractor.model.ContractorCategory
import com.arturjarosz.task.contractor.query.ContractorQueryService
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException
import com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException
import spock.lang.Specification

class ContractorCategoryValidatorTest extends Specification {
    static final Long CATEGORY_ID = 100L

    def contractorCategoryRepository = Mock(ContractorCategoryRepository)
    def contractorQueryService = Mock(ContractorQueryService)

    def contractorCategoryValidator = new ContractorCategoryValidator(contractorCategoryRepository, contractorQueryService)

    def "validateCreateContractorCategoryDto should throw an exception for missing or blank name"() {
        when:
            this.contractorCategoryValidator.validateCreateContractorCategoryDto(categoryDto)
        then:
            def exception = thrown(IllegalArgumentException)
            exception.message == message
        where:
            categoryDto                           | message
            null                                  | "isNull.contractorCategory"
            new ContractorCategoryDto()           | "isNull.contractorCategory.name"
            new ContractorCategoryDto(name: "  ") | "isEmpty.contractorCategory.name"
    }

    def "validateCreateContractorCategoryDto should throw an exception for too long name"() {
        when:
            this.contractorCategoryValidator.validateCreateContractorCategoryDto(
                    new ContractorCategoryDto(name: "a" * (ContractorCategory.NAME_MAX_LENGTH + 1)))
        then:
            def exception = thrown(IllegalArgumentException)
            exception.message == "notValid.contractorCategory.name.tooLong"
    }

    def "validateCreateContractorCategoryDto should throw an exception when stripped name is already used"() {
        given:
            this.contractorCategoryRepository.existsByNameIgnoreCase("Plumber") >> true
        when:
            this.contractorCategoryValidator.validateCreateContractorCategoryDto(new ContractorCategoryDto(name: " Plumber "))
        then:
            def exception = thrown(IllegalArgumentException)
            exception.message == "notValid.contractorCategory.name.unique"
    }

    def "validateUpdateContractorCategoryDto should check name uniqueness against other categories only"() {
        when:
            this.contractorCategoryValidator.validateUpdateContractorCategoryDto(new ContractorCategoryDto(name: "Plumber"),
                    CATEGORY_ID)
        then:
            1 * this.contractorCategoryRepository.existsByNameIgnoreCaseAndIdNot("Plumber", CATEGORY_ID) >> false
            noExceptionThrown()
    }

    def "validateUpdateContractorCategoryDto should throw an exception when another category has the name"() {
        given:
            this.contractorCategoryRepository.existsByNameIgnoreCaseAndIdNot("Plumber", CATEGORY_ID) >> true
        when:
            this.contractorCategoryValidator.validateUpdateContractorCategoryDto(new ContractorCategoryDto(name: "Plumber"),
                    CATEGORY_ID)
        then:
            def exception = thrown(IllegalArgumentException)
            exception.message == "notValid.contractorCategory.name.unique"
    }

    def "validateContractorCategoryExistence should throw an exception when category does not exist"() {
        given:
            this.contractorCategoryRepository.findById(CATEGORY_ID) >> Optional.empty()
        when:
            this.contractorCategoryValidator.validateContractorCategoryExistence(CATEGORY_ID)
        then:
            def exception = thrown(ResourceNotFoundException)
            exception.message == "notExist.contractorCategory"
    }

    def "validateContractorCategoryHasNoContractors should throw an exception when a contractor has the category"() {
        given:
            this.contractorQueryService.getNumberOfContractorsByCategoryIds([CATEGORY_ID]) >> [(CATEGORY_ID): 2L]
        when:
            this.contractorCategoryValidator.validateContractorCategoryHasNoContractors(CATEGORY_ID)
        then:
            def exception = thrown(IllegalArgumentException)
            exception.message == "notValid.contractorCategory.contractors"
    }

    def "validateContractorCategoryHasNoContractors should not throw an exception when no contractor has the category"() {
        given:
            this.contractorQueryService.getNumberOfContractorsByCategoryIds([CATEGORY_ID]) >> [(CATEGORY_ID): 0L]
        when:
            this.contractorCategoryValidator.validateContractorCategoryHasNoContractors(CATEGORY_ID)
        then:
            noExceptionThrown()
    }
}
