package com.arturjarosz.task.cooperator.application

import com.arturjarosz.task.supplier.model.Supplier
import com.arturjarosz.task.supplier.model.SupplierCategory
import com.arturjarosz.task.supplier.application.SupplierValidator
import com.arturjarosz.task.supplier.application.dto.SupplierDto
import spock.lang.Specification
import spock.lang.Subject

class SupplierValidatorTest extends Specification {
    private final static Long EXISTING_SUPPLIER_ID = 1L
    private final static Long EXISTING_CONTRACTOR_ID = 5L
    private final static Long NOT_EXISTING_SUPPLIER_ID = 10L
    private final static String NAME = "name"
    private final static SupplierCategory SUPPLIER_CATEGORY = SupplierCategory.PAINT_SHOP

    def cooperatorRepository = Mock(CooperatorRepositoryImpl)

    @Subject
    def supplierValidator = new SupplierValidator(cooperatorRepository)

    def "validateCreateSupplierDto throws exception with proper error message"() {
        given: "SupplierDto"
            SupplierDto supplierDto = givenSupplierDto
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
            givenSupplierDto  | name | category || exceptionMessage
            null              | null | null     || "isNull.supplier"
            new SupplierDto() | null | SUPPLIER_CATEGORY || "isNull.supplier.name"
            new SupplierDto() | ""   | SUPPLIER_CATEGORY || "isEmpty.supplier.name"
            new SupplierDto() | NAME | null     || "isNull.supplier.category"

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
            SupplierDto supplierDto = givenSupplierDto
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
            givenSupplierDto  | name | category || exceptionMessage
            null              | null | null     || "isNull.supplier"
            new SupplierDto() | null | SUPPLIER_CATEGORY || "isNull.supplier.name"
            new SupplierDto() | ""   | SUPPLIER_CATEGORY || "isEmpty.supplier.name"
            new SupplierDto() | NAME | null     || "isNull.supplier.category"

    }

    def "validateUpdateSupplierDto does not throw any exception when passed proper supplierDto"() {
        given: "Proper SupplierDto"
            SupplierDto supplierDto = new SupplierDto(name: NAME, category: SUPPLIER_CATEGORY)

        when: "Calling validateCreateSupplierDto"
            this.supplierValidator.validateUpdateSupplierDto(supplierDto)

        then: "No exception is thrown"
            noExceptionThrown()
    }

    def "validateSupplierExistence throws an exception when Supplier with given supplierId does not exist"() {
        given: "Existing Supplier"
            this.mockCooperatorRepositoryLoadOfNotExistingSupplier()

        when: "Calling validateSupplierExistence with supplierId of existing Supplier"
            this.supplierValidator.validateSupplierExistence(NOT_EXISTING_SUPPLIER_ID)

        then: "Exception with correct"
            Exception exception = thrown()
            exception.message == "notExist.supplier"
    }

}
