package com.arturjarosz.task.supplier.application

import com.arturjarosz.task.dto.SupplierCategoryDto
import com.arturjarosz.task.dto.SupplierDto
import com.arturjarosz.task.supplier.infrastructure.SupplierRepository
import com.arturjarosz.task.supplier.model.Supplier
import com.arturjarosz.task.supplier.model.SupplierCategory
import spock.lang.Specification
import spock.lang.Subject

class SupplierValidatorTest extends Specification {
    private final static Long EXISTING_SUPPLIER_ID = 1L
    private final static Long NOT_EXISTING_SUPPLIER_ID = 10L
    private final static String NAME = "name"
    private final static SupplierCategoryDto SUPPLIER_CATEGORY = SupplierCategoryDto.PAINT_SHOP
    final static String NOTE = "some note"
    final static String TELEPHONE = "123-456-789"
    final static String EMAIL = "test@email.com"

    def supplierRepository = Mock(SupplierRepository)

    @Subject
    def supplierValidator = new SupplierValidator(supplierRepository)

    def "validateCreateSupplierDto throws exception with proper error message"() {
        given: "SupplierDto"
            def supplierDto = givenSupplierDto
            if (supplierDto != null) {
                supplierDto.with { testedSupplierDto ->
                    testedSupplierDto.name = name
                    testedSupplierDto.category = category
                }
            }

        when: "Calling validateCreateSupplierDto"
            this.supplierValidator.validateCreateSupplierDto(supplierDto)

        then: "Exception with proper message is thrown"
            Exception exception = thrown()
            exception.message == exceptionMessage

        where:
            givenSupplierDto  | name | category          || exceptionMessage
            null              | null | null              || "isNull.supplier"
            new SupplierDto() | null | SUPPLIER_CATEGORY || "isNull.supplier.name"
            new SupplierDto() | ""   | SUPPLIER_CATEGORY || "isEmpty.supplier.name"
            new SupplierDto() | NAME | null              || "isNull.supplier.category"

    }

    def "validateCreateSupplierDto does not throw any exception when passed proper supplierDto"() {
        given: "Proper SupplierDto"
            SupplierDto supplierDto = new SupplierDto(name: NAME, category: SUPPLIER_CATEGORY)

        when: "Calling validateCreateSupplierDto"
            this.supplierValidator.validateCreateSupplierDto(supplierDto)

        then: "No exception is thrown"
            noExceptionThrown()
    }

    def "validateUpdateSupplierDto throws exception with proper error message"() {
        given: "SupplierDto"
            def supplierDto = givenSupplierDto
            if (supplierDto != null) {
                supplierDto.with { testedSupplierDto ->
                    testedSupplierDto.name = name
                    testedSupplierDto.category = category
                }
            }

        when: "Calling validateCreateSupplierDto"
            this.supplierValidator.validateUpdateSupplierDto(supplierDto)

        then: "Exception with proper message is thrown"
            Exception exception = thrown()
            exception.message == exceptionMessage

        where:
            givenSupplierDto  | name | category          || exceptionMessage
            null              | null | null              || "isNull.supplier"
            new SupplierDto() | null | SUPPLIER_CATEGORY || "isNull.supplier.name"
            new SupplierDto() | ""   | SUPPLIER_CATEGORY || "isEmpty.supplier.name"
            new SupplierDto() | NAME | null              || "isNull.supplier.category"

    }

    def "validateUpdateSupplierDto does not throw any exception when passed proper supplierDto"() {
        given: "Proper SupplierDto"
            def supplierDto = new SupplierDto(name: NAME, category: SUPPLIER_CATEGORY)

        when: "Calling validateCreateSupplierDto"
            this.supplierValidator.validateUpdateSupplierDto(supplierDto)

        then: "No exception is thrown"
            noExceptionThrown()
    }

    def "validateSupplierExistence throws an exception when Supplier with given supplierId does not exist"() {
        given: "Existing Supplier"
            this.mockSupplierRepositoryLoadOfNotExistingSupplier()

        when: "Calling validateSupplierExistence with supplierId of existing Supplier"
            this.supplierValidator.validateSupplierExistence(NOT_EXISTING_SUPPLIER_ID)

        then: "Exception with correct"
            Exception exception = thrown()
            exception.message == "notExist.supplier"
    }

    def "validateSupplierExistence does not throw any exception on supplierId of existing Supplier"() {
        given: "Existing Supplier"
            this.mockSupplierRepositoryLoadOfExistingSupplier()

        when: "Calling validateSupplierExistence with supplierId of existing Supplier"
            this.supplierValidator.validateSupplierExistence(EXISTING_SUPPLIER_ID)

        then: "Exception with correct"
            noExceptionThrown()
    }

    private void mockSupplierRepositoryLoadOfExistingSupplier() {
        1 * this.supplierRepository.findById(EXISTING_SUPPLIER_ID) >> Optional.of(new Supplier(NAME, SupplierCategory.valueOf(SUPPLIER_CATEGORY.name()), EMAIL, TELEPHONE, NOTE))
    }

    private void mockSupplierRepositoryLoadOfNotExistingSupplier() {
        1 * this.supplierRepository.findById(NOT_EXISTING_SUPPLIER_ID) >> Optional.ofNullable(null)
    }

}
