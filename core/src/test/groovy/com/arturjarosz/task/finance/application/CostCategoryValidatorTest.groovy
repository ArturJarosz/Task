package com.arturjarosz.task.finance.application

import com.arturjarosz.task.dto.CostCategoryDto
import com.arturjarosz.task.finance.application.validator.CostCategoryValidator
import com.arturjarosz.task.finance.infrastructure.CostCategoryRepository
import com.arturjarosz.task.finance.model.CostCategory
import com.arturjarosz.task.finance.query.FinancialDataQueryService
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException
import com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException
import spock.lang.Specification

class CostCategoryValidatorTest extends Specification {
    static final Long CATEGORY_ID = 100L

    def costCategoryRepository = Mock(CostCategoryRepository)
    def financialDataQueryService = Mock(FinancialDataQueryService)

    def costCategoryValidator = new CostCategoryValidator(costCategoryRepository, financialDataQueryService)

    def "validateCreateCostCategoryDto should throw an exception for missing or blank name"() {
        when:
            this.costCategoryValidator.validateCreateCostCategoryDto(categoryDto)
        then:
            def exception = thrown(IllegalArgumentException)
            exception.message == message
        where:
            categoryDto                           | message
            null                                  | "isNull.costCategory"
            new CostCategoryDto()           | "isNull.costCategory.name"
            new CostCategoryDto(name: "  ") | "isEmpty.costCategory.name"
    }

    def "validateCreateCostCategoryDto should throw an exception for too long name"() {
        when:
            this.costCategoryValidator.validateCreateCostCategoryDto(
                    new CostCategoryDto(name: "a" * (CostCategory.NAME_MAX_LENGTH + 1)))
        then:
            def exception = thrown(IllegalArgumentException)
            exception.message == "notValid.costCategory.name.tooLong"
    }

    def "validateCreateCostCategoryDto should throw an exception when stripped name is already used"() {
        given:
            this.costCategoryRepository.existsByNameIgnoreCase("Fuel") >> true
        when:
            this.costCategoryValidator.validateCreateCostCategoryDto(new CostCategoryDto(name: " Fuel "))
        then:
            def exception = thrown(IllegalArgumentException)
            exception.message == "notValid.costCategory.name.unique"
    }

    def "validateUpdateCostCategoryDto should check name uniqueness against other categories only"() {
        when:
            this.costCategoryValidator.validateUpdateCostCategoryDto(new CostCategoryDto(name: "Fuel"),
                    CATEGORY_ID)
        then:
            1 * this.costCategoryRepository.existsByNameIgnoreCaseAndIdNot("Fuel", CATEGORY_ID) >> false
            noExceptionThrown()
    }

    def "validateUpdateCostCategoryDto should throw an exception when another category has the name"() {
        given:
            this.costCategoryRepository.existsByNameIgnoreCaseAndIdNot("Fuel", CATEGORY_ID) >> true
        when:
            this.costCategoryValidator.validateUpdateCostCategoryDto(new CostCategoryDto(name: "Fuel"),
                    CATEGORY_ID)
        then:
            def exception = thrown(IllegalArgumentException)
            exception.message == "notValid.costCategory.name.unique"
    }

    def "validateCostCategoryExistence should throw an exception when category does not exist"() {
        given:
            this.costCategoryRepository.findById(CATEGORY_ID) >> Optional.empty()
        when:
            this.costCategoryValidator.validateCostCategoryExistence(CATEGORY_ID)
        then:
            def exception = thrown(ResourceNotFoundException)
            exception.message == "notExist.costCategory"
    }

    def "validateCostCategoryHasNoCosts should throw an exception when a cost has the category"() {
        given:
            this.financialDataQueryService.getNumberOfCostsByCategoryIds([CATEGORY_ID]) >> [(CATEGORY_ID): 2L]
        when:
            this.costCategoryValidator.validateCostCategoryHasNoCosts(CATEGORY_ID)
        then:
            def exception = thrown(IllegalArgumentException)
            exception.message == "notValid.costCategory.costs"
    }

    def "validateCostCategoryHasNoCosts should not throw an exception when no cost has the category"() {
        given:
            this.financialDataQueryService.getNumberOfCostsByCategoryIds([CATEGORY_ID]) >> [(CATEGORY_ID): 0L]
        when:
            this.costCategoryValidator.validateCostCategoryHasNoCosts(CATEGORY_ID)
        then:
            noExceptionThrown()
    }
}
