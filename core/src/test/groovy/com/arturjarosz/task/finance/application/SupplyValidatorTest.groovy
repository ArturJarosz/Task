package com.arturjarosz.task.finance.application

import com.arturjarosz.task.finance.application.dto.SupplyDto
import com.arturjarosz.task.finance.query.FinancialDataQueryService
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException
import com.arturjarosz.task.supplier.query.impl.SupplierQueryServiceImpl
import spock.lang.Specification
import spock.lang.Unroll

class SupplyValidatorTest extends Specification {
    private static final Long EXISTING_SUPPLIER_ID = 1L
    private static final Long NOT_EXISTING_SUPPLIER_ID = 9L
    private static final Long PROJECT_ID = 20L
    private static final Long EXISTING_SUPPLY_ID = 30L
    private static final Long NOT_EXISTING_SUPPLY_ID = 31L

    def supplierQueryService = Mock(SupplierQueryServiceImpl)
    def financialDataQueryService = Mock(FinancialDataQueryService)

    def supplyValidator = new SupplyValidator(supplierQueryService, financialDataQueryService)

    def setup() {
        this.financialDataQueryService.doesSupplyForProjectExists(PROJECT_ID, EXISTING_SUPPLY_ID) >> true
        this.financialDataQueryService.doesSupplyForProjectExists(PROJECT_ID, NOT_EXISTING_SUPPLY_ID) > false
    }

    def "validateSupplierExistence throws an exception if Supplier with given id does not exist"() {
        given:
            this.mockSupplierQueryServiceSupplierWithIdExistenceDoesntExists()
        when:
            this.supplyValidator.validateSupplierExistence(NOT_EXISTING_SUPPLIER_ID)
        then:
            Exception exception = thrown()
            exception.message == "notExist.supplier"
    }

    def "validateSupplierExistence throws an exception if Supplier with given id exists"() {
        given:
            this.mockSupplierQueryServiceSupplierWithIdExistenceExists()
        when:
            this.supplyValidator.validateSupplierExistence(EXISTING_SUPPLIER_ID)
        then:
            noExceptionThrown()
    }

    @Unroll
    def "validateCreateSupplyDto throws an exception with proper exception message"() {
        given:
            SupplyDto supplyDto = supply
            if (supplyDto != null) {
                supplyDto.name = supplyName
                supplyDto.supplierId = supplierId
                supplyDto.value = supplyValue
            }
        when:
            this.supplyValidator.validateCreateSupplyDto(supplyDto)
        then:
            Exception exception = thrown()
            exception.message == exceptionMessage
        where:
            supply          | supplierId           | supplyName | supplyValue          || exceptionMessage
            null            | null                 | null       | null                 || "isNull.supply"
            new SupplyDto() | null                 | null       | null                 || "isNull.supply.supplier"
            new SupplyDto() | EXISTING_SUPPLIER_ID | null       | null                 || "isNull.supply.name"
            new SupplyDto() | EXISTING_SUPPLIER_ID | ""         | null                 || "isEmpty.supply.name"
            new SupplyDto() | EXISTING_SUPPLIER_ID | "name"     | null                 || "isNull.supply.value"
            new SupplyDto() | EXISTING_SUPPLIER_ID | "name"     | new BigDecimal("-1") || "negative.supply.value"
    }

    @Unroll
    def "validateUpdateSupplyDto throws an exception with proper exception message"() {
        given:
            SupplyDto supplyDto = supply
            if (supplyDto != null) {
                supplyDto.name = supplyName
                supplyDto.value = supplyValue
            }
        when:
            this.supplyValidator.validateUpdateSupplyDto(supplyDto)
        then:
            Exception exception = thrown()
            exception.message == exceptionMessage
        where:
            supply          | supplierId           | supplyName | supplyValue          || exceptionMessage
            null            | null                 | null       | null                 || "isNull.supply"
            new SupplyDto() | EXISTING_SUPPLIER_ID | null       | null                 || "isNull.supply.name"
            new SupplyDto() | EXISTING_SUPPLIER_ID | ""         | null                 || "isEmpty.supply.name"
            new SupplyDto() | EXISTING_SUPPLIER_ID | "name"     | null                 || "isNull.supply.value"
            new SupplyDto() | EXISTING_SUPPLIER_ID | "name"     | new BigDecimal("-1") || "negative.supply.value"
    }

    def "validateSupplyOnProjectExistence should throw exception if supply does not exist on project"() {
        given:
        when:
            this.supplyValidator.validateSupplyOnProjectExistence(PROJECT_ID, NOT_EXISTING_SUPPLY_ID)
        then:
            thrown(IllegalArgumentException)
    }

    def "validateSupplyOnProjectExistence should not throw any exception if supply exists on project"() {
        given:
        when:
            this.supplyValidator.validateSupplyOnProjectExistence(PROJECT_ID, EXISTING_SUPPLY_ID)
        then:
            noExceptionThrown()
    }

    private void mockSupplierQueryServiceSupplierWithIdExistenceDoesntExists() {
        1 * this.supplierQueryService.supplierWithIdExists(NOT_EXISTING_SUPPLIER_ID) >> false
    }

    private void mockSupplierQueryServiceSupplierWithIdExistenceExists() {
        1 * this.supplierQueryService.supplierWithIdExists(EXISTING_SUPPLIER_ID) >> true
    }
}
