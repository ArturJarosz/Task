package com.arturjarosz.task.contractor.application.impl

import com.arturjarosz.task.contractor.application.ContractorValidator
import com.arturjarosz.task.contractor.application.mapper.ContractorMapperImpl
import com.arturjarosz.task.contractor.infrastructure.ContractorRepository
import com.arturjarosz.task.contractor.model.Contractor
import com.arturjarosz.task.contractor.model.ContractorCategory
import com.arturjarosz.task.dto.ContractorCategoryDto
import com.arturjarosz.task.dto.ContractorDto
import spock.lang.Specification
import spock.lang.Subject

class ContractorApplicationServiceImplTest extends Specification {
    final static String NAME = "name"
    final static String UPDATED_NAME = "updated_name"
    final static ContractorCategoryDto CATEGORY = ContractorCategoryDto.ARTIST
    final static ContractorCategoryDto UPDATED_CATEGORY = ContractorCategoryDto.CARPENTER
    final static String UPDATED_EMAIL = "email@email.com"
    final static String TELEPHONE = "123456789"
    final static String NOTE = "note"
    final static Long CONTRACTOR_ID = 1L

    def contractorRepository = Mock(ContractorRepository)
    def contractorValidator = Mock(ContractorValidator)
    def contractorMapper = new ContractorMapperImpl()

    @Subject
    def contractorApplicationService = new ContractorApplicationServiceImpl(contractorRepository, contractorValidator, contractorMapper)


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

    def "createContractor should return created contractor"() {
        given:
            def contractorDto = new ContractorDto(name: NAME, category: CATEGORY, note: NOTE, email: UPDATED_EMAIL, telephone: TELEPHONE)
        when:
            def createdContractor = this.contractorApplicationService.createContractor(contractorDto)
        then:
            createdContractor != null
            createdContractor.name == NAME
            createdContractor.category == CATEGORY
            createdContractor.email == UPDATED_EMAIL
            createdContractor.telephone == TELEPHONE
            createdContractor.note == NOTE
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
                contractor.category == ContractorCategory.valueOf(UPDATED_CATEGORY.name())
                contractor.email == UPDATED_EMAIL
                contractor.telephone == TELEPHONE
                contractor.note == NOTE
            })
    }

    def "updateContractor should return updated contractorDto"() {
        given:
            def updateContractorDto = new ContractorDto(name: UPDATED_NAME, category: UPDATED_CATEGORY,
                    email: UPDATED_EMAIL,
                    telephone: TELEPHONE, note: NOTE)
            this.mockContractorRepositoryLoad(CONTRACTOR_ID)

        when:
            def updatedContractor = this.contractorApplicationService.updateContractor(CONTRACTOR_ID, updateContractorDto)
        then:
            with(updatedContractor) {
                name == updateContractorDto.name
                category == updateContractorDto.category
                email == updateContractorDto.email
                telephone == updateContractorDto.telephone
                note == updateContractorDto.note
            }
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
        this.contractorRepository.findById(contractorId) >> Optional.of(new Contractor(NAME, ContractorCategory.valueOf(CATEGORY.name()), UPDATED_EMAIL, TELEPHONE, NOTE))
    }
}
