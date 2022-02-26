package com.arturjarosz.task.cooperator.application.impl

import com.arturjarosz.task.cooperator.application.ContractorValidator
import com.arturjarosz.task.cooperator.application.dto.ContractorDto
import com.arturjarosz.task.cooperator.infrastructure.impl.CooperatorRepositoryImpl
import com.arturjarosz.task.cooperator.model.Cooperator
import com.arturjarosz.task.cooperator.model.CooperatorCategory
import spock.lang.Specification
import spock.lang.Subject

class ContractorApplicationServiceImplTest extends Specification {
    final static String NAME = "name"
    final static String UPDATED_NAME = "updated_name"
    final static CooperatorCategory.ContractorCategory CATEGORY = CooperatorCategory.ContractorCategory.ARTIST
    final static CooperatorCategory.ContractorCategory UPDATED_CATEGORY =
            CooperatorCategory.ContractorCategory.CARPENTER
    final static String UPDATED_EMAIL = "email@email.com"
    final static String TELEPHONE = "123456789"
    final static String NOTE = "note"
    final static Long CONTRACTOR_ID = 1L

    def cooperatorRepository = Mock(CooperatorRepositoryImpl)
    def contractorValidator = Mock(ContractorValidator)

    @Subject
    def contractorApplicationService = new ContractorApplicationServiceImpl(cooperatorRepository, contractorValidator)


    def "createContractor should call validateCreateContractorDto from contractorValidator"() {
        given:
            def contractorDto = new ContractorDto(name: NAME, category: CATEGORY)

        when:
            this.contractorApplicationService.createContractor(contractorDto)

        then:
            1 * this.contractorValidator.validateCreateContractorDto(contractorDto)
    }

    def "createContract should save created contractor"() {
        given:
            def contractorDto = new ContractorDto(name: NAME, category: CATEGORY)

        when:
            this.contractorApplicationService.createContractor(contractorDto)

        then:
            1 * this.cooperatorRepository.save(_ as Cooperator)
    }

    def "updateContractor should call validateContractorExistence on contractorValidator"() {
        given:
            def updateContractorDto = new ContractorDto(name: UPDATED_NAME, category: UPDATED_CATEGORY,
                    email: UPDATED_EMAIL,
                    telephone: TELEPHONE, note: NOTE)
            this.mockCooperatorRepositoryLoad(CONTRACTOR_ID)

        when:
            this.contractorApplicationService.updateContractor(CONTRACTOR_ID, updateContractorDto)

        then:
            1 * this.contractorValidator.validateContractorExistence(CONTRACTOR_ID)
    }

    def "updateContractor should call validateUpdateContractorDto on contractorValidator"() {
        given:
            def updateContractorDto = new ContractorDto(name: UPDATED_NAME, category: UPDATED_CATEGORY,
                    email: UPDATED_EMAIL,
                    telephone: TELEPHONE, note: NOTE)
            this.mockCooperatorRepositoryLoad(CONTRACTOR_ID)

        when:
            this.contractorApplicationService.updateContractor(CONTRACTOR_ID, updateContractorDto)

        then:
            1 * this.contractorValidator.validateUpdateContractorDto(updateContractorDto)
    }

    def "updateContractor should update data and save changed Contractor"() {
        given:
            def updateContractorDto = new ContractorDto(name: UPDATED_NAME, category: UPDATED_CATEGORY,
                    email: UPDATED_EMAIL,
                    telephone: TELEPHONE, note: NOTE)
            this.mockCooperatorRepositoryLoad(CONTRACTOR_ID)

        when:
            this.contractorApplicationService.updateContractor(CONTRACTOR_ID, updateContractorDto)

        then:
            1 * this.cooperatorRepository.save({ Cooperator cooperator ->
                cooperator.name == UPDATED_NAME
                cooperator.category == UPDATED_CATEGORY.asCooperatorCategory()
                cooperator.email == UPDATED_EMAIL
                cooperator.telephone == TELEPHONE
                cooperator.note == NOTE
            })
    }

    def "deleteContractor should call validateContractorExistence on contractorValidator"() {
        given:
            this.mockCooperatorRepositoryLoad(CONTRACTOR_ID)

        when:
            this.contractorApplicationService.deleteContractor(CONTRACTOR_ID)

        then:
            1 * this.contractorValidator.validateContractorExistence(CONTRACTOR_ID)
    }

    def "deleteContractor should call validateContractorHasNoJobs on contractorValidator"() {
        given:
            this.mockCooperatorRepositoryLoad(CONTRACTOR_ID)

        when:
            this.contractorApplicationService.deleteContractor(CONTRACTOR_ID)

        then:
            1 * this.contractorValidator.validateContractorHasNoJobs(CONTRACTOR_ID)
    }

    def "deleteContractor should call remove on cooperatorRepository"() {
        given:
            this.mockCooperatorRepositoryLoad(CONTRACTOR_ID)

        when:
            this.contractorApplicationService.deleteContractor(CONTRACTOR_ID)

        then:
            1 * this.cooperatorRepository.remove(CONTRACTOR_ID)
    }

    def "getContactor should call validateContractorExistence on contractorValidator"() {
        given:
            this.mockCooperatorRepositoryLoad(CONTRACTOR_ID)

        when:
            this.contractorApplicationService.getContractor(CONTRACTOR_ID)

        then:
            1 * this.contractorValidator.validateContractorExistence(CONTRACTOR_ID)
    }

    def "getContractor should return contractorDto of given contractor"() {
        given:
            this.mockCooperatorRepositoryLoad(CONTRACTOR_ID)

        when:
            def contractorDto = this.contractorApplicationService.getContractor(CONTRACTOR_ID)

        then:
            contractorDto.name == NAME
            contractorDto.category == CATEGORY
    }

    private void mockCooperatorRepositoryLoad(Long cooperatorId) {
        this.cooperatorRepository.load(cooperatorId) >> Cooperator.createContractor(NAME, CATEGORY)
    }
}
