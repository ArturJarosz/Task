package com.arturjarosz.task.cooperator.application

import com.arturjarosz.task.cooperator.application.dto.SupplierDto
import com.arturjarosz.task.cooperator.infrastructure.impl.CooperatorRepositoryImpl
import com.arturjarosz.task.cooperator.model.Cooperator
import com.arturjarosz.task.cooperator.model.CooperatorCategory
import spock.lang.Specification
import spock.lang.Subject

class SupplierValidatorTest extends Specification {
    private final static Long EXISTING_SUPPLIER_ID = 1L
    private final static Long EXISTING_CONTRACTOR_ID = 5L
    private final static Long NOT_EXISTING_SUPPLIER_ID = 10L
    private final static String NAME = "name"
    private final static CooperatorCategory.SupplierCategory SUPPLIER_CATEGORY = CooperatorCategory.SupplierCategory.PAINT_SHOP
    private final static CooperatorCategory.ContractorCategory CONTRACTOR_CATEGORY = CooperatorCategory.ContractorCategory.ARTIST

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

    def "validateSupplierExistence throws an exception when Cooperator of given supplierId is not Supplier"() {
        given: "Existing Contractor"
            this.mockCooperatorRepositoryLoadOfContractor()

        when: "Calling validateSupplierExistence with supplierId of existing Contractor"
            this.supplierValidator.validateSupplierExistence(EXISTING_CONTRACTOR_ID)

        then: "Exception with correct"
            Exception exception = thrown()
            exception.message == "notExist.supplier"
    }

    def "validateSupplierExistence does not throw any exception on supplierId of existing Supplier"() {
        given: "Existing Supplier"
            this.mockCooperatorRepositoryLoadOfExistingSupplier()

        when: "Calling validateSupplierExistence with supplierId of existing Supplier"
            this.supplierValidator.validateSupplierExistence(EXISTING_SUPPLIER_ID)

        then: "Exception with correct"
            noExceptionThrown()
    }

    private void mockCooperatorRepositoryLoadOfExistingSupplier() {
        1 * this.cooperatorRepository.load(EXISTING_SUPPLIER_ID) >> Cooperator.createSupplier(NAME, SUPPLIER_CATEGORY)
    }

    private void mockCooperatorRepositoryLoadOfNotExistingSupplier() {
        1 * this.cooperatorRepository.load(NOT_EXISTING_SUPPLIER_ID) >> null
    }

    private void mockCooperatorRepositoryLoadOfContractor() {
        1 * this.cooperatorRepository.load(EXISTING_CONTRACTOR_ID) >> Cooperator.createContractor(NAME, CONTRACTOR_CATEGORY)
    }

}
