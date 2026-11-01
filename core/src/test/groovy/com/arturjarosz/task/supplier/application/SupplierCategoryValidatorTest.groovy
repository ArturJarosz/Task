package com.arturjarosz.task.supplier.application

import com.arturjarosz.task.dto.SupplierCategoryDto
import com.arturjarosz.task.supplier.infrastructure.SupplierCategoryRepository
import com.arturjarosz.task.supplier.model.SupplierCategory
import com.arturjarosz.task.supplier.query.SupplierQueryService
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException
import com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException
import spock.lang.Specification

class SupplierCategoryValidatorTest extends Specification {
    static final Long CATEGORY_ID = 100L

    def supplierCategoryRepository = Mock(SupplierCategoryRepository)
    def supplierQueryService = Mock(SupplierQueryService)

    def supplierCategoryValidator = new SupplierCategoryValidator(supplierCategoryRepository, supplierQueryService)

    def "validateCreateSupplierCategoryDto should throw an exception for missing or blank name"() {
        when:
            this.supplierCategoryValidator.validateCreateSupplierCategoryDto(categoryDto)
        then:
            def exception = thrown(IllegalArgumentException)
            exception.message == message
        where:
            categoryDto                         | message
            null                                | "isNull.supplierCategory"
            new SupplierCategoryDto()           | "isNull.supplierCategory.name"
            new SupplierCategoryDto(name: "  ") | "isEmpty.supplierCategory.name"
    }

    def "validateCreateSupplierCategoryDto should throw an exception for too long name"() {
        when:
            this.supplierCategoryValidator.validateCreateSupplierCategoryDto(
                    new SupplierCategoryDto(name: "a" * (SupplierCategory.NAME_MAX_LENGTH + 1)))
        then:
            def exception = thrown(IllegalArgumentException)
            exception.message == "notValid.supplierCategory.name.tooLong"
    }

    def "validateCreateSupplierCategoryDto should throw an exception when stripped name is already used"() {
        given:
            this.supplierCategoryRepository.existsByNameIgnoreCase("Paint shop") >> true
        when:
            this.supplierCategoryValidator.validateCreateSupplierCategoryDto(new SupplierCategoryDto(name: " Paint shop "))
        then:
            def exception = thrown(IllegalArgumentException)
            exception.message == "notValid.supplierCategory.name.unique"
    }

    def "validateUpdateSupplierCategoryDto should check name uniqueness against other categories only"() {
        when:
            this.supplierCategoryValidator.validateUpdateSupplierCategoryDto(new SupplierCategoryDto(name: "Paint shop"),
                    CATEGORY_ID)
        then:
            1 * this.supplierCategoryRepository.existsByNameIgnoreCaseAndIdNot("Paint shop", CATEGORY_ID) >> false
            noExceptionThrown()
    }

    def "validateUpdateSupplierCategoryDto should throw an exception when another category has the name"() {
        given:
            this.supplierCategoryRepository.existsByNameIgnoreCaseAndIdNot("Paint shop", CATEGORY_ID) >> true
        when:
            this.supplierCategoryValidator.validateUpdateSupplierCategoryDto(new SupplierCategoryDto(name: "Paint shop"),
                    CATEGORY_ID)
        then:
            def exception = thrown(IllegalArgumentException)
            exception.message == "notValid.supplierCategory.name.unique"
    }

    def "validateSupplierCategoryExistence should throw an exception when category does not exist"() {
        given:
            this.supplierCategoryRepository.findById(CATEGORY_ID) >> Optional.empty()
        when:
            this.supplierCategoryValidator.validateSupplierCategoryExistence(CATEGORY_ID)
        then:
            def exception = thrown(ResourceNotFoundException)
            exception.message == "notExist.supplierCategory"
    }

    def "validateSupplierCategoryHasNoSuppliers should throw an exception when a supplier has the category"() {
        given:
            this.supplierQueryService.getNumberOfSuppliersByCategoryIds([CATEGORY_ID]) >> [(CATEGORY_ID): 2L]
        when:
            this.supplierCategoryValidator.validateSupplierCategoryHasNoSuppliers(CATEGORY_ID)
        then:
            def exception = thrown(IllegalArgumentException)
            exception.message == "notValid.supplierCategory.suppliers"
    }

    def "validateSupplierCategoryHasNoSuppliers should not throw an exception when no supplier has the category"() {
        given:
            this.supplierQueryService.getNumberOfSuppliersByCategoryIds([CATEGORY_ID]) >> [(CATEGORY_ID): 0L]
        when:
            this.supplierCategoryValidator.validateSupplierCategoryHasNoSuppliers(CATEGORY_ID)
        then:
            noExceptionThrown()
    }
}
