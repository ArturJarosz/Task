package com.arturjarosz.task.cooperator.application

import com.arturjarosz.task.cooperator.application.dto.SupplierDto
import com.arturjarosz.task.cooperator.infrastructure.impl.CooperatorRepositoryImpl
import spock.lang.Specification
import spock.lang.Subject

class SupplierValidatorTest extends Specification {
    private final static String NAME = "name"

    def cooperatorRepository = Mock(CooperatorRepositoryImpl)

    @Subject
    def supplierValidator = new SupplierValidator(cooperatorRepository)

    def "validateCreateSupplierDto throws an exception when passed dto is null"() {
        given: "Null as a supplierDto"
            SupplierDto supplierDto = null

        when: "Calling validateCreateSupplierDto"
            this.supplierValidator.validateCreateSupplierDto(supplierDto)

        then: "Exception with proper message is thrown"
            Exception exception = thrown()
            with(exception){
                message == "isNull.supplier"
            }
    }

    def "validateCreateSupplierDto throws an exception when passed dto does not have name"() {
        given: "Supplier with no name"
            SupplierDto supplierDto = new SupplierDto()

        when: "Calling validateCreateSupplierDto"
            this.supplierValidator.validateCreateSupplierDto(supplierDto)

        then: "Exception with proper message is thrown"
            Exception exception = thrown()
            exception.message == "isNull.supplier.name"
    }


}
