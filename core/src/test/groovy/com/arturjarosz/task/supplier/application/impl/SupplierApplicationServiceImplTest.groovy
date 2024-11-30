package com.arturjarosz.task.supplier.application.impl

import com.arturjarosz.task.configuration.UserProperties
import com.arturjarosz.task.dto.SupplierCategoryDto
import com.arturjarosz.task.dto.SupplierDto
import com.arturjarosz.task.dto.SupplyDto
import com.arturjarosz.task.finance.domain.dto.FinancialDataDto
import com.arturjarosz.task.finance.model.SupplierSupplyDataDto
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException
import com.arturjarosz.task.sharedkernel.testhelpers.TestUtils
import com.arturjarosz.task.supplier.application.SupplierValidator
import com.arturjarosz.task.supplier.application.mapper.SupplierMapperImpl
import com.arturjarosz.task.supplier.application.mapper.SupplierSuppliesMapperImpl
import com.arturjarosz.task.supplier.infrastructure.SupplierRepository
import com.arturjarosz.task.supplier.model.Supplier
import com.arturjarosz.task.supplier.model.SupplierCategory
import com.arturjarosz.task.supplier.query.SupplierQueryService
import spock.lang.Specification

class SupplierApplicationServiceImplTest extends Specification {
    final static NAME = "name"
    final static UPDATED_NAME = "updated name"
    final static CATEGORY = SupplierCategoryDto.BATHROOM_CERAMICS_SHOP
    final static UPDATED_CATEGORY = SupplierCategoryDto.FLOORING_SHOP
    final static UPDATED_EMAIL = "email@email.com"
    final static TELEPHONE = "123456789"
    final static NOTE = "note"
    static final EMAIL = "some@mail.com"
    final static SUPPLIER_ID = 1L
    final static SUPPLIER_ID_2 = 2L
    final static NOT_EXISTING_SUPPLIER_ID = 3L

    def supplierValidator = Mock(SupplierValidator)
    def supplierQueryService = Mock(SupplierQueryService)
    def supplierRepository = Mock(SupplierRepository)
    def supplierMapper = new SupplierMapperImpl()
    def supplierSuppliesMapper = new SupplierSuppliesMapperImpl()

    def subject = new SupplierApplicationServiceImpl(supplierRepository, supplierQueryService, supplierValidator, supplierMapper, supplierSuppliesMapper)

    def "createSupplier should call validateCreateSupplierDto on supplierValidator"() {
        given:
            def supplierDto = new SupplierDto(name: NAME, category: CATEGORY)

        when:
            this.subject.createSupplier(supplierDto)

        then:
            1 * this.supplierValidator.validateCreateSupplierDto(supplierDto)
    }

    def "createSupplier should return created Supplier"() {
        given:
            def supplierDto = new SupplierDto(name: NAME, category: CATEGORY)

        when:
            def createdSupplier = this.subject.createSupplier(supplierDto)

        then:
            createdSupplier != null
            createdSupplier.name == NAME
            createdSupplier.category == CATEGORY
    }

    def "updateSupplier should save created supplier"() {
        given:
            def supplierDto = new SupplierDto(name: NAME, category: CATEGORY)

        when:
            this.subject.createSupplier(supplierDto)

        then:
            1 * this.supplierRepository.save(_ as Supplier)
    }

    def "updateSupplier should call validateSupplierExistence on supplierValidator"() {
        given:
            def updateSupplierDto = new SupplierDto(name: UPDATED_NAME, category: UPDATED_CATEGORY,
                    email: UPDATED_EMAIL,
                    telephone: TELEPHONE, note: NOTE)
            this.mockSupplierRepositoryLoad(SUPPLIER_ID)

        when:
            this.subject.updateSupplier(SUPPLIER_ID, updateSupplierDto)

        then:
            1 * this.supplierValidator.validateSupplierExistence(_ as Optional<Supplier>, SUPPLIER_ID)
    }

    def "updateSupplier should call validateUpdateSupplierDto on supplierValidator"() {
        given:
            def updateSupplierDto = new SupplierDto(name: UPDATED_NAME, category: UPDATED_CATEGORY,
                    email: UPDATED_EMAIL,
                    telephone: TELEPHONE, note: NOTE)
            this.mockSupplierRepositoryLoad(SUPPLIER_ID)

        when:
            this.subject.updateSupplier(SUPPLIER_ID, updateSupplierDto)

        then:
            1 * this.supplierValidator.validateUpdateSupplierDto(updateSupplierDto)
    }

    def "updateSupplier should update data and save changed supplier"() {
        given:
            def updateSupplierDto = new SupplierDto(name: UPDATED_NAME, category: UPDATED_CATEGORY,
                    email: UPDATED_EMAIL,
                    telephone: TELEPHONE, note: NOTE)
            this.mockSupplierRepositoryLoad(SUPPLIER_ID)

        when:
            this.subject.updateSupplier(SUPPLIER_ID, updateSupplierDto)

        then:
            1 * this.supplierRepository.save({ Supplier supplier ->
                supplier.name == UPDATED_NAME
                supplier.category == SupplierCategory.valueOf(UPDATED_CATEGORY.name())
                supplier.email == UPDATED_EMAIL
                supplier.telephone == TELEPHONE
                supplier.note == NOTE
            })
    }

    def "updateSupplier should return updated object"() {
        given:
            def updateSupplierDto = new SupplierDto(name: UPDATED_NAME, category: UPDATED_CATEGORY,
                    email: UPDATED_EMAIL,
                    telephone: TELEPHONE, note: NOTE)
            this.mockSupplierRepositoryLoad(SUPPLIER_ID)
            this.mockSupplierRepositorySave()

        when:
            def updatedSupplier = this.subject.updateSupplier(SUPPLIER_ID, updateSupplierDto)

        then:
            updatedSupplier != null
            updatedSupplier.name == UPDATED_NAME
            updatedSupplier.email == UPDATED_EMAIL
            updatedSupplier.category == UPDATED_CATEGORY
            updatedSupplier.note == NOTE
            updatedSupplier.telephone == TELEPHONE
    }

    def "deleteSupplier should call validateSupplierExistence on supplierValidator"() {
        given:
            this.mockSupplierRepositoryLoad(SUPPLIER_ID)

        when:
            this.subject.deleteSupplier(SUPPLIER_ID)

        then:
            1 * this.supplierValidator.validateSupplierExistence(SUPPLIER_ID)
    }

    def "deleteSupplier should call validateSupplierHasNoSupply on supplierValidator"() {
        given:
            this.mockSupplierRepositoryLoad(SUPPLIER_ID)

        when:
            this.subject.deleteSupplier(SUPPLIER_ID)

        then:
            1 * this.supplierValidator.validateSupplierHasNoSupply(SUPPLIER_ID)
    }

    def "deleteSupplier should call remove on supplierRepository"() {
        given:
            this.mockSupplierRepositoryLoad(SUPPLIER_ID)

        when:
            this.subject.deleteSupplier(SUPPLIER_ID)

        then:
            1 * this.supplierRepository.deleteById(SUPPLIER_ID)
    }

    def "getSupplier should call validateSupplierExistence on supplierValidator"() {
        given:
            this.mockSupplierRepositoryLoad(SUPPLIER_ID)

        when:
            this.subject.getSupplier(SUPPLIER_ID)

        then:
            1 * this.supplierValidator.validateSupplierExistence(SUPPLIER_ID)
    }

    def "getSupplier should return supplierDto of given supplier"() {
        given:
            this.mockSupplierRepositoryLoad(SUPPLIER_ID)

        when:
            def supplierDto = this.subject.getSupplier(SUPPLIER_ID)

        then:
            supplierDto.name == NAME
            supplierDto.category == CATEGORY
    }

    def "getSuppliesData should throw exception if given supplier does not exist"() {
        given:
            this.supplierValidator.validateSupplierExistence(NOT_EXISTING_SUPPLIER_ID) >> { throw new IllegalArgumentException() }

        when:
            def result = this.subject.getSuppliesData(NOT_EXISTING_SUPPLIER_ID)

        then:
            thrown(IllegalArgumentException)
    }

    def "getSupplies should return SupplierSuppliesDataDto for existing supplier"() {
        given:
            def userProperties = new UserProperties(incomeTax: 0.1D, vatTax: 0.23D)
            TestUtils.setFieldForObject(supplierSuppliesMapper, "userProperties", userProperties)
            var supplierSupplyDataDto = new SupplierSupplyDataDto(new SupplyDto(value: 1.0D), new FinancialDataDto(hasInvoice: true, value: 1.0D))
            this.supplierQueryService.getSupplierSuppliesData(SUPPLIER_ID) >> ([supplierSupplyDataDto] as Set)

        when:
            def result = this.subject.getSuppliesData(SUPPLIER_ID)

        then:
            noExceptionThrown()
            result != null
    }

    def "getSuppliers should return list of suppliers"() {
        given:
            def supplier1 = new Supplier(NAME, SupplierCategory.FLOORING_SHOP, EMAIL, TELEPHONE, NOTE)
            TestUtils.setFieldForObject(supplier1, "id", SUPPLIER_ID)
            def supplier2 = new Supplier(NAME, SupplierCategory.FLOORING_SHOP, EMAIL, TELEPHONE, NOTE)
            TestUtils.setFieldForObject(supplier2, "id", SUPPLIER_ID_2)
            this.supplierQueryService.getNumberOfSupplierPerSupplier() >> [(SUPPLIER_ID): 3L, (SUPPLIER_ID_2): 0L]
            this.supplierRepository.findAll() >> [supplier1, supplier2]

        when:
            def result = this.subject.getSuppliers()

        then:
            result.size() == 2
            with(result[0]) {
                name == NAME
                note == NOTE
                email == EMAIL
                TELEPHONE == TELEPHONE
                category == SupplierCategoryDto.FLOORING_SHOP
                numberOfSupplies == 3
            }
    }

    private void mockSupplierRepositoryLoad(Long supplierId) {
        this.supplierRepository.findById(supplierId) >> Optional.of(new Supplier(NAME, SupplierCategory.valueOf(CATEGORY.name()), UPDATED_EMAIL, TELEPHONE, NOTE))
    }

    private void mockSupplierRepositorySave() {
        this.supplierRepository.save(_ as Supplier) >> { arguments ->
            {
                return arguments[0]
            }
        }
    }

}
