package com.arturjarosz.task.contractor.application.impl

import com.arturjarosz.task.configuration.UserProperties
import com.arturjarosz.task.contractor.application.ContractorValidator
import com.arturjarosz.task.contractor.application.mapper.ContractorContractorJobMapper
import com.arturjarosz.task.contractor.application.mapper.ContractorContractorJobMapperImpl
import com.arturjarosz.task.contractor.application.mapper.ContractorMapperImpl
import com.arturjarosz.task.contractor.infrastructure.ContractorRepository
import com.arturjarosz.task.contractor.model.Contractor
import com.arturjarosz.task.contractor.model.ContractorCategory
import com.arturjarosz.task.contractor.query.ContractorQueryService
import com.arturjarosz.task.dto.ContractorCategoryDto
import com.arturjarosz.task.dto.ContractorDto
import com.arturjarosz.task.dto.ContractorJobDto
import com.arturjarosz.task.finance.domain.dto.FinancialDataDto
import com.arturjarosz.task.finance.model.ContractorContractorJobDto
import com.arturjarosz.task.sharedkernel.testhelpers.TestUtils
import spock.lang.Specification

class ContractorApplicationServiceImplTest extends Specification {
    final static NAME = "name"
    final static UPDATED_NAME = "updated_name"
    final static CATEGORY = ContractorCategoryDto.ARTIST
    final static UPDATED_CATEGORY = ContractorCategoryDto.CARPENTER
    final static UPDATED_EMAIL = "email@email.com"
    final static TELEPHONE = "123456789"
    final static NOTE = "note"
    final static CONTRACTOR_ID = 1L
    final static CONTRACTOR_ID_2 = 2L
    final static EMAIL = "email@test.com"

    def contractorRepository = Mock(ContractorRepository)
    def contractorValidator = Mock(ContractorValidator)
    def contractorMapper = new ContractorMapperImpl()
    def contractorQueryService = Mock(ContractorQueryService)
    def contractorContractorJobMapper = new ContractorContractorJobMapperImpl()

    def subject = new ContractorApplicationServiceImpl(contractorRepository, contractorValidator,
            contractorMapper, contractorQueryService, contractorContractorJobMapper)


    def "createContractor should call validateCreateContractorDto from contractorValidator"() {
        given:
            def contractorDto = new ContractorDto(name: NAME, category: CATEGORY)
        when:
            this.subject.createContractor(contractorDto)
        then:
            1 * this.contractorValidator.validateCreateContractorDto(contractorDto)
    }

    def "createContract should save created contractor"() {
        given:
            def contractorDto = new ContractorDto(name: NAME, category: CATEGORY)
        when:
            this.subject.createContractor(contractorDto)
        then:
            1 * this.contractorRepository.save(_ as Contractor)
    }

    def "createContractor should return created contractor"() {
        given:
            def contractorDto = new ContractorDto(name: NAME, category: CATEGORY, note: NOTE, email: UPDATED_EMAIL, telephone: TELEPHONE)
        when:
            def createdContractor = this.subject.createContractor(contractorDto)
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
            this.subject.updateContractor(CONTRACTOR_ID, updateContractorDto)

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
            this.subject.updateContractor(CONTRACTOR_ID, updateContractorDto)

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
            this.subject.updateContractor(CONTRACTOR_ID, updateContractorDto)

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
            def updatedContractor = this.subject.updateContractor(CONTRACTOR_ID, updateContractorDto)
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
            this.subject.deleteContractor(CONTRACTOR_ID)

        then:
            1 * this.contractorValidator.validateContractorExistence(CONTRACTOR_ID)
    }

    def "deleteContractor should call validateContractorHasNoJobs on contractorValidator"() {
        given:
            this.mockContractorRepositoryLoad(CONTRACTOR_ID)

        when:
            this.subject.deleteContractor(CONTRACTOR_ID)

        then:
            1 * this.contractorValidator.validateContractorHasNoJobs(CONTRACTOR_ID)
    }

    def "deleteContractor should call remove on contractorRepository"() {
        given:
            this.mockContractorRepositoryLoad(CONTRACTOR_ID)

        when:
            this.subject.deleteContractor(CONTRACTOR_ID)

        then:
            1 * this.contractorRepository.deleteById(CONTRACTOR_ID)
    }

    def "getContactor should call validateContractorExistence on contractorValidator"() {
        given:
            this.mockContractorRepositoryLoad(CONTRACTOR_ID)

        when:
            this.subject.getContractor(CONTRACTOR_ID)

        then:
            1 * this.contractorValidator.validateContractorExistence(_ as Optional<Contractor>, CONTRACTOR_ID)
    }

    def "getContractor should return contractorDto of given contractor"() {
        given:
            this.mockContractorRepositoryLoad(CONTRACTOR_ID)

        when:
            def contractorDto = this.subject.getContractor(CONTRACTOR_ID)

        then:
            contractorDto.name == NAME
            contractorDto.category == CATEGORY
    }

    def "getContractors should return list of suppliers"() {
        given:
            def contractor1 = new Contractor(NAME, ContractorCategory.ARTIST, EMAIL, TELEPHONE, NOTE)
            TestUtils.setFieldForObject(contractor1, "id", CONTRACTOR_ID)
            def contractor2 = new Contractor(NAME, ContractorCategory.ARTIST, EMAIL, TELEPHONE, NOTE)
            TestUtils.setFieldForObject(contractor2, "id", CONTRACTOR_ID_2)
            this.contractorQueryService.getNumberOfJobsPerContractor() >> [(CONTRACTOR_ID): 3L, (CONTRACTOR_ID_2): 0L]
            this.contractorRepository.findAll() >> [contractor1, contractor2]

        when:
            def result = this.subject.getContractors()

        then:
            result.size() == 2
            with(result[0]) {
                name == NAME
                note == NOTE
                email == EMAIL
                TELEPHONE == TELEPHONE
                category == ContractorCategoryDto.ARTIST
                numberOfContractorJobs == 3
            }
    }

    def "getContractorJobsData should return contractor validate contractor existence"() {
        given:
            this.contractorQueryService.getContractorJobsData(CONTRACTOR_ID) >> Set.of()

        when:
            def result = subject.getContractorJobsData(CONTRACTOR_ID)

        then:
            1 * this.contractorValidator.validateContractorExistence(CONTRACTOR_ID)
    }

    def "getContractorJobsData should return contractor jobs data for given contractor"() {
        given:
            var contractorJob1 = new ContractorJobDto(id: 1L)
            var financialDataDto1 = new FinancialDataDto()
            financialDataDto1.setHasInvoice(true)
            financialDataDto1.setPaid(true)
            financialDataDto1.setPayable(true)
            financialDataDto1.setValue(BigDecimal.valueOf(100.0D))
            var contractorJobData1 = new ContractorContractorJobDto(contractorJob1, financialDataDto1)
            var contractorJob2 = new ContractorJobDto(id: 2L)
            var financialDataDto2 = new FinancialDataDto()
            financialDataDto2.setHasInvoice(false)
            financialDataDto2.setPaid(true)
            financialDataDto2.setPayable(true)
            financialDataDto2.setValue(BigDecimal.valueOf(200.0D))
            var contractorJobData2 = new ContractorContractorJobDto(contractorJob2, financialDataDto2)
            this.contractorQueryService.getContractorJobsData(CONTRACTOR_ID) >> Set.of(contractorJobData1, contractorJobData2)
            mockUserProperties(this.contractorContractorJobMapper)

        when:
            def result = subject.getContractorJobsData(CONTRACTOR_ID)

        then:
            result.contractorJobs.size() == 2
            result.averageFinancialData != null
            with(result.averageFinancialData) {
                netValue == 150.0D
                grossValue == 161.5D
                vatTax == 11.5D
                incomeTax == 9.0D
            }
            result.financialData != null
            with(result.financialData) {
                count == 2
                netValue == 300.0D
                grossValue == 323.0D
                vatTax == 23.0D
                incomeTax == 18.0D
            }
    }

    private void mockContractorRepositoryLoad(Long contractorId) {
        this.contractorRepository.findById(contractorId) >> Optional.of(new Contractor(NAME, ContractorCategory.valueOf(CATEGORY.name()), UPDATED_EMAIL, TELEPHONE, NOTE))
    }

    private void mockUserProperties(ContractorContractorJobMapper mapper) {
        var userProperties = new UserProperties()
        userProperties.setIncomeTax(0.18D)
        userProperties.setVatTax(0.23D)
        TestUtils.setFieldForObject(mapper, "userProperties", userProperties)
    }
}
