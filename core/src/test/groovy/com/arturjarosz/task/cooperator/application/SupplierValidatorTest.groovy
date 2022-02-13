package com.arturjarosz.task.cooperator.application

import com.arturjarosz.task.cooperator.application.dto.SupplierDto
import com.arturjarosz.task.cooperator.infrastructure.impl.CooperatorRepositoryImpl
import com.arturjarosz.task.cooperator.model.CooperatorCategory
import spock.lang.Specification
import spock.lang.Subject

class SupplierValidatorTest extends Specification {
    private final static String NAME = "name"
    private final static CooperatorCategory.SupplierCategory CATEGORY = CooperatorCategory.SupplierCategory.PAINT_SHOP

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
            new SupplierDto() | null | CATEGORY || "isNull.supplier.name"
            new SupplierDto() | ""   | CATEGORY || "isEmpty.supplier.name"
            new SupplierDto() | NAME | null     || "isNull.supplier.category"

    }

    def "validateCreateSupplierDto does not throw any exception when passed proper supplierDto"() {
        given: "Proper SupplierDto"
            SupplierDto supplierDto = new SupplierDto(name: NAME, category: CATEGORY)

        when: "Calling validateCreateSupplierDto"
            this.supplierValidator.validateCreateSupplierDto(supplierDto)

        then: "No exception is thrown"
            noExceptionThrown()
    }

}
