package com.arturjarosz.task.cooperator.application.impl

import com.arturjarosz.task.cooperator.application.SupplierValidator
import com.arturjarosz.task.cooperator.application.dto.SupplierDto
import com.arturjarosz.task.cooperator.infrastructure.impl.CooperatorRepositoryImpl
import com.arturjarosz.task.cooperator.model.Cooperator
import com.arturjarosz.task.cooperator.model.CooperatorCategory
import spock.lang.Specification

class SupplierApplicationServiceImplTest extends Specification {
    final static String NAME = "name"
    final static String UPDATED_NAME = "updated name"
    final static CooperatorCategory.SupplierCategory CATEGORY =
            CooperatorCategory.SupplierCategory.BATHROOM_CERAMICS_SHOP
    final static CooperatorCategory.SupplierCategory UPDATED_CATEGORY =
            CooperatorCategory.SupplierCategory.FLOORING_SHOP
    final static String UPDATED_EMAIL = "email@email.com"
    final static String TELEPHONE = "123456789"
    final static String NOTE = "note"
    final static Long SUPPLIER_ID = 1L

    def supplierValidator = Mock(SupplierValidator)
    def cooperatorRepository = Mock(CooperatorRepositoryImpl)

    def supplierApplicationService = new SupplierApplicationServiceImpl(cooperatorRepository, supplierValidator)

    def "createSupplier should call validateCreateSupplierDto on supplierValidator"() {
        given:
            def supplierDto = new SupplierDto(name: NAME, category: CATEGORY)

        when:
            this.supplierApplicationService.createSupplier(supplierDto)

        then:
            1 * this.supplierValidator.validateCreateSupplierDto(supplierDto)
    }

    def "updateSupplier should save created supplier"() {
        given:
            def supplierDto = new SupplierDto(name: NAME, category: CATEGORY)

        when:
            this.supplierApplicationService.createSupplier(supplierDto)

        then:
            1 * this.cooperatorRepository.save(_ as Cooperator)
    }

    def "updateSupplier should call validateSupplierExistence on supplierValidator"() {
        given:
            def updateSupplierDto = new SupplierDto(name: UPDATED_NAME, category: UPDATED_CATEGORY,
                    email: UPDATED_EMAIL,
                    telephone: TELEPHONE, note: NOTE)
            this.mockCooperatorRepositoryLoad(SUPPLIER_ID)

        when:
            this.supplierApplicationService.updateSupplier(SUPPLIER_ID, updateSupplierDto)

        then:
            1 * this.supplierValidator.validateSupplierExistence(SUPPLIER_ID)
    }

    def "updateSupplier should call validateUpdateSupplierDto on supplierValidator"() {
        given:
            def updateSupplierDto = new SupplierDto(name: UPDATED_NAME, category: UPDATED_CATEGORY,
                    email: UPDATED_EMAIL,
                    telephone: TELEPHONE, note: NOTE)
            this.mockCooperatorRepositoryLoad(SUPPLIER_ID)

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
            this.mockCooperatorRepositoryLoad(SUPPLIER_ID)

        when:
            this.supplierApplicationService.updateSupplier(SUPPLIER_ID, updateSupplierDto)

        then:
            1 * this.cooperatorRepository.save({ Cooperator cooperator ->
                cooperator.name == UPDATED_NAME
                cooperator.category == UPDATED_CATEGORY.asCooperatorCategory()
                cooperator.email == UPDATED_EMAIL
                cooperator.telephone == TELEPHONE
                cooperator.note == NOTE
            })
    }

    def "deleteSupplier should call validateSupplierExistence on supplierValidator"() {
        given:
            this.mockCooperatorRepositoryLoad(SUPPLIER_ID)

        when:
            this.supplierApplicationService.deleteSupplier(SUPPLIER_ID)

        then:
            1 * this.supplierValidator.validateSupplierExistence(SUPPLIER_ID)
    }

    def "deleteSupplier should call validateSupplierHasNoSupply on supplierValidator"() {
        given:
            this.mockCooperatorRepositoryLoad(SUPPLIER_ID)

        when:
            this.supplierApplicationService.deleteSupplier(SUPPLIER_ID)

        then:
            1 * this.supplierValidator.validateSupplierHasNoSupply(SUPPLIER_ID)
    }

    def "deleteSupplier should call remove on cooperatorRepository"() {
        given:
            this.mockCooperatorRepositoryLoad(SUPPLIER_ID)

        when:
            this.supplierApplicationService.deleteSupplier(SUPPLIER_ID)

        then:
            1 * this.cooperatorRepository.remove(SUPPLIER_ID)
    }

    def "getSupplier should call validateSupplierExistence on supplierValidator"() {
        given:
            this.mockCooperatorRepositoryLoad(SUPPLIER_ID)

        when:
            this.supplierApplicationService.getSupplier(SUPPLIER_ID)

        then:
            1 * this.supplierValidator.validateSupplierExistence(SUPPLIER_ID)
    }

    def "getSupplier should return supplierDto of given supplier"() {
        given:
            this.mockCooperatorRepositoryLoad(SUPPLIER_ID)

        when:
            def supplierDto = this.supplierApplicationService.getSupplier(SUPPLIER_ID)

        then:
            supplierDto.name == NAME
            supplierDto.category == CATEGORY
    }

    private void mockCooperatorRepositoryLoad(Long cooperatorId) {
        this.cooperatorRepository.load(cooperatorId) >> Cooperator.createSupplier(NAME, CATEGORY)
    }

}
