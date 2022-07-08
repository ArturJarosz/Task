package com.arturjarosz.task.contractor.application.impl

import com.arturjarosz.task.contract.intrastructure.ContractRepository
import com.arturjarosz.task.contractor.application.ContractorValidator
import com.arturjarosz.task.contractor.application.dto.ContractorDto
import com.arturjarosz.task.contractor.infrastructure.ContractorRepository
import com.arturjarosz.task.contractor.model.Contractor
import com.arturjarosz.task.contractor.model.ContractorCategory
import spock.lang.Specification
import spock.lang.Subject

class ContractorApplicationServiceImplTest extends Specification {
    final static String NAME = "name"
    final static String UPDATED_NAME = "updated_name"
    final static ContractorCategory CATEGORY = ContractorCategory.ARTIST
    final static ContractorCategory UPDATED_CATEGORY = ContractorCategory.CARPENTER
    final static String UPDATED_EMAIL = "email@email.com"
    final static String TELEPHONE = "123456789"
    final static String NOTE = "note"
    final static Long CONTRACTOR_ID = 1L

    def contractorRepository = Mock(ContractRepository)
    def contractorValidator = Mock(ContractorValidator)

    @Subject
    def contractorApplicationService = new ContractorApplicationServiceImpl(
            contractorRepository as ContractorRepository, contractorValidator)


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
            1 * this.contractorRepository.save(_ as Contractor)
    }

    def "updateContractor should call validateContractorExistence on contractorValidator"() {
        given:
            def updateContractorDto = new ContractorDto(name: UPDATED_NAME, category: UPDATED_CATEGORY,
                    email: UPDATED_EMAIL,
                    telephone: TELEPHONE, note: NOTE)
            this.mockContractorRepositoryLoad(CONTRACTOR_ID)

        when:
            this.contractorApplicationService.updateContractor(CONTRACTOR_ID, updateContractorDto)

        then:
            1 * this.contractorValidator.validateContractorExistence(_ as Optional<Contractor>, CONTRACTOR_ID)
    }

    def "updateContractor should call validateUpdateContractorDto on contractorValidator"() {
        given:
            def updateContractorDto = new ContractorDto(name: UPDATED_NAME, category: UPDATED_CATEGORY,
                    email: UPDATED_EMAIL,
                    telephone: TELEPHONE, note: NOTE)
            this.mockContractorRepositoryLoad(CONTRACTOR_ID)

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
            this.mockContractorRepositoryLoad(CONTRACTOR_ID)

        when:
            this.contractorApplicationService.updateContractor(CONTRACTOR_ID, updateContractorDto)

        then:
            1 * this.contractorRepository.save({ Contractor contractor ->
                contractor.name == UPDATED_NAME
                contractor.category == UPDATED_CATEGORY
                contractor.email == UPDATED_EMAIL
                contractor.telephone == TELEPHONE
                contractor.note == NOTE
            })
    }

    def "deleteContractor should call validateContractorExistence on contractorValidator"() {
        given:
            this.mockContractorRepositoryLoad(CONTRACTOR_ID)

        when:
            this.contractorApplicationService.deleteContractor(CONTRACTOR_ID)

        then:
            1 * this.contractorValidator.validateContractorExistence(CONTRACTOR_ID)
    }

    def "deleteContractor should call validateContractorHasNoJobs on contractorValidator"() {
        given:
            this.mockContractorRepositoryLoad(CONTRACTOR_ID)

        when:
            this.contractorApplicationService.deleteContractor(CONTRACTOR_ID)

        then:
            1 * this.contractorValidator.validateContractorHasNoJobs(CONTRACTOR_ID)
    }

    def "deleteContractor should call remove on contractorRepository"() {
        given:
            this.mockContractorRepositoryLoad(CONTRACTOR_ID)

        when:
            this.contractorApplicationService.deleteContractor(CONTRACTOR_ID)

        then:
            1 * this.contractorRepository.deleteById(CONTRACTOR_ID)
    }

    def "getContactor should call validateContractorExistence on contractorValidator"() {
        given:
            this.mockContractorRepositoryLoad(CONTRACTOR_ID)

        when:
            this.contractorApplicationService.getContractor(CONTRACTOR_ID)

        then:
            1 * this.contractorValidator.validateContractorExistence(_ as Optional<Contractor>, CONTRACTOR_ID)
    }

    def "getContractor should return contractorDto of given contractor"() {
        given:
            this.mockContractorRepositoryLoad(CONTRACTOR_ID)

        when:
            def contractorDto = this.contractorApplicationService.getContractor(CONTRACTOR_ID)

        then:
            contractorDto.name == NAME
            contractorDto.category == CATEGORY
    }

    private void mockContractorRepositoryLoad(Long contractorId) {
        this.contractorRepository.findById(contractorId) >> Optional.of(new Contractor(NAME, CATEGORY))
    }
}
