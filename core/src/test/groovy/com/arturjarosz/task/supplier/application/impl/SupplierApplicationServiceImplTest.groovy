package com.arturjarosz.task.supplier.application.impl

import com.arturjarosz.task.dto.SupplierCategoryDto
import com.arturjarosz.task.dto.SupplierDto
import com.arturjarosz.task.supplier.application.SupplierValidator
import com.arturjarosz.task.supplier.infrastructure.SupplierRepository
import com.arturjarosz.task.supplier.model.Supplier
import com.arturjarosz.task.supplier.model.SupplierCategory
import spock.lang.Specification

class SupplierApplicationServiceImplTest extends Specification {
    final static String NAME = "name"
    final static String UPDATED_NAME = "updated name"
    final static SupplierCategoryDto CATEGORY = SupplierCategoryDto.BATHROOM_CERAMICS_SHOP
    final static SupplierCategoryDto UPDATED_CATEGORY = SupplierCategoryDto.FLOORING_SHOP
    final static String UPDATED_EMAIL = "email@email.com"
    final static String TELEPHONE = "123456789"
    final static String NOTE = "note"
    final static Long SUPPLIER_ID = 1L

    def supplierValidator = Mock(SupplierValidator)
    def supplierRepository = Mock(SupplierRepository)

    def supplierApplicationService = new SupplierApplicationServiceImpl(supplierRepository, supplierValidator)

    def "createSupplier should call validateCreateSupplierDto on supplierValidator"() {
        given:
            def supplierDto = new SupplierDto(name: NAME, category: CATEGORY)

        when:
            this.supplierApplicationService.createSupplier(supplierDto)

        then:
            1 * this.supplierValidator.validateCreateSupplierDto(supplierDto)
    }

    def "createSupplier should return created Supplier"() {
        given:
            def supplierDto = new SupplierDto(name: NAME, category: CATEGORY)

        when:
            def createdSupplier = this.supplierApplicationService.createSupplier(supplierDto)

        then:
            createdSupplier != null
            createdSupplier.name == NAME
            createdSupplier.category == CATEGORY
    }

    def "updateSupplier should save created supplier"() {
        given:
            def supplierDto = new SupplierDto(name: NAME, category: CATEGORY)

        when:
            this.supplierApplicationService.createSupplier(supplierDto)

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
            this.supplierApplicationService.updateSupplier(SUPPLIER_ID, updateSupplierDto)

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
            this.supplierApplicationService.updateSupplier(SUPPLIER_ID, updateSupplierDto)

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
            this.supplierApplicationService.updateSupplier(SUPPLIER_ID, updateSupplierDto)

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

        when:
            def updatedSupplier = this.supplierApplicationService.updateSupplier(SUPPLIER_ID, updateSupplierDto)

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
            this.supplierApplicationService.deleteSupplier(SUPPLIER_ID)

        then:
            1 * this.supplierValidator.validateSupplierExistence(SUPPLIER_ID)
    }

    def "deleteSupplier should call validateSupplierHasNoSupply on supplierValidator"() {
        given:
            this.mockSupplierRepositoryLoad(SUPPLIER_ID)

        when:
            this.supplierApplicationService.deleteSupplier(SUPPLIER_ID)

        then:
            1 * this.supplierValidator.validateSupplierHasNoSupply(SUPPLIER_ID)
    }

    def "deleteSupplier should call remove on supplierRepository"() {
        given:
            this.mockSupplierRepositoryLoad(SUPPLIER_ID)

        when:
            this.supplierApplicationService.deleteSupplier(SUPPLIER_ID)

        then:
            1 * this.supplierRepository.deleteById(SUPPLIER_ID)
    }

    def "getSupplier should call validateSupplierExistence on supplierValidator"() {
        given:
            this.mockSupplierRepositoryLoad(SUPPLIER_ID)

        when:
            this.supplierApplicationService.getSupplier(SUPPLIER_ID)

        then:
            1 * this.supplierValidator.validateSupplierExistence(SUPPLIER_ID)
    }

    def "getSupplier should return supplierDto of given supplier"() {
        given:
            this.mockSupplierRepositoryLoad(SUPPLIER_ID)

        when:
            def supplierDto = this.supplierApplicationService.getSupplier(SUPPLIER_ID)

        then:
            supplierDto.name == NAME
            supplierDto.category == CATEGORY
    }

    private void mockSupplierRepositoryLoad(Long supplierId) {
        this.supplierRepository.findById(supplierId) >> Optional.of(new Supplier(NAME, SupplierCategory.valueOf(CATEGORY.name())))
    }

}
