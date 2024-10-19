package com.arturjarosz.task.contract.application.impl

import com.arturjarosz.task.contract.application.ContractValidator
import com.arturjarosz.task.contract.application.mapper.ContractMapperImpl
import com.arturjarosz.task.contract.domain.ContractDomainService
import com.arturjarosz.task.contract.intrastructure.ContractRepository
import com.arturjarosz.task.contract.model.Contract
import com.arturjarosz.task.contract.status.ContractStatus
import com.arturjarosz.task.contract.status.ContractStatusWorkflow
import com.arturjarosz.task.contract.utils.ContractBuilder
import com.arturjarosz.task.dto.ContractDto
import com.arturjarosz.task.dto.ContractStatusDto
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException
import com.arturjarosz.task.sharedkernel.model.Money
import spock.lang.Specification

import java.time.LocalDate

class ContractServiceImplTest extends Specification {

    static final EXISTING_CONTRACT_ID = 1L
    static final NOT_EXISTING_CONTRACT_ID = 2L
    static final ACCEPTED_CONTRACT_ID = 3L
    static final OFFER_VALUE = 100.00d
    static final NEW_OFFER_VALUE = 200.00d
    static final DEADLINE = LocalDate.of(2100, 1, 1)
    static final NEW_DEADLINE = LocalDate.of(2105, 1, 1)
    static final NEW_START_DATE = LocalDate.of(2000, 1, 1)
    static final NEW_END_DATE = LocalDate.of(2105, 1, 1)
    static final NEW_STATUS = ContractStatusDto.REJECTED

    static final NEW_SIGNING_DATE = LocalDate.of(2000, 1, 1)
    static final CONTRACT_STATUS_WORKFLOW = new ContractStatusWorkflow()

    def contractValidator = Mock(ContractValidator)
    def contractRepository = Mock(ContractRepository)
    def contractMapper = new ContractMapperImpl()
    def contractDomainService = Mock(ContractDomainService)

    def subject = new ContractServiceImpl(contractValidator, contractRepository, contractMapper, contractDomainService)

    def setup() {
        var newContract = new Contract(OFFER_VALUE, DEADLINE, CONTRACT_STATUS_WORKFLOW)
        newContract.changeStatus(ContractStatus.OFFER)
        this.contractDomainService.createContract(_ as ContractDto) >> newContract
        var acceptedOffer = new Contract(OFFER_VALUE, DEADLINE, CONTRACT_STATUS_WORKFLOW)
        acceptedOffer.changeStatus(ContractStatus.ACCEPTED)
        this.contractRepository.findById(ACCEPTED_CONTRACT_ID) >> Optional.of(acceptedOffer)
        var contract = new Contract(OFFER_VALUE, DEADLINE, CONTRACT_STATUS_WORKFLOW)
        contract.changeStatus(ContractStatus.ACCEPTED)
        this.contractRepository.findById(EXISTING_CONTRACT_ID) >> Optional.of(contract)
    }

    def "createContract does not create any contract on missing contractDto"() {
        given:
            def contractDto = null
            this.mockValidatorThrowsAnExceptionOnNullContractDto()
        when:
            def contract = this.subject.createContract(contractDto)
        then:
            thrown(Exception)
            contract == null
    }

    def "createContract does not create any contract on missing data in contractDto"() {
        given:
            def contractDto = new ContractDto()
            this.mockValidatorThrowsAnExceptionOnContractDtoWithMissingData(contractDto)
        when:
            def contract = this.subject.createContract(contractDto)
        then:
            thrown(Exception)
            contract == null
    }

    def "createContract creates and saves new contract with offer status on proper contractDto"() {
        given:
            def contractDto = new ContractDto(deadline: DEADLINE, offerValue: OFFER_VALUE)
        when:
            def contract = this.subject.createContract(contractDto)
        then:
            noExceptionThrown()
            1 * this.contractRepository.save({ def savedContract ->
                savedContract.deadline == DEADLINE
                savedContract.getOfferValue().getValue().doubleValue() == OFFER_VALUE
                savedContract.status == ContractStatus.OFFER
            }) >> new ContractBuilder().withId(EXISTING_CONTRACT_ID).withStatus(ContractStatus.OFFER).withContractValue(new Money(OFFER_VALUE)).build()
            contract.status == ContractStatusDto.OFFER
            contract.id == EXISTING_CONTRACT_ID
    }

    def "changeStatus should not update status if base validation fails"() {
        given:
            def contractDto = new ContractDto(status: ContractStatusDto.OFFER)
            mockBaseValidationThrowsAnException()
        when:
            def contract = subject.changeStatus(EXISTING_CONTRACT_ID, contractDto)
        then:
            thrown(Exception)
            contract == null
            0 * this.contractDomainService.updateContractStatus(_ as Contract, _ as ContractDto)
    }

    def "changeStatus should not update status if contract does not exits"() {
        given:
            def contractDto = new ContractDto(status: ContractStatusDto.ACCEPTED, id: NOT_EXISTING_CONTRACT_ID)
        when:
            def contract = subject.changeStatus(NOT_EXISTING_CONTRACT_ID, contractDto)
        then:
            thrown(Exception)
            contract == null
            0 * this.contractDomainService.updateContractStatus(_ as Contract, _ as ContractDto)
    }

    def "changeStatus should fail if validator for particular status change fails"() {
        given:
            def contractDto = new ContractDto(status: ContractStatusDto.SIGNED, id: ACCEPTED_CONTRACT_ID)
            mockSignStatusValidatorThrowsAnException()
        when:
            def contract = subject.changeStatus(ACCEPTED_CONTRACT_ID, contractDto)
        then:
            thrown(Exception)
            contract == null
            0 * this.contractDomainService.updateContractStatus(_ as Contract, _ as ContractDto)
    }

    def "changeStatus should change status of the contract if data is correct"() {
        given:
            def contractDto = new ContractDto(status: ContractStatusDto.SIGNED, id: ACCEPTED_CONTRACT_ID)
            def updatedContract = new Contract(OFFER_VALUE, DEADLINE, CONTRACT_STATUS_WORKFLOW)
            updatedContract.changeStatus(ContractStatus.SIGNED)
        when:
            def contract = subject.changeStatus(ACCEPTED_CONTRACT_ID, contractDto)
        then:
            noExceptionThrown()
            contract != null
            1 * this.contractDomainService.updateContractStatus(_ as Contract, _ as ContractDto) >> updatedContract
            contract.status == ContractStatusDto.SIGNED
    }

    def "getContractForProject should not return contract if it does not exist"() {
        given:
            mockExistenceThrowsAnException()
        when:
            def contract = subject.getContractForProject(NOT_EXISTING_CONTRACT_ID)
        then:
            thrown(Exception)
            contract == null
    }

    def "getContractForProject should return contract if exists"() {
        given:
        when:
            def contract = subject.getContractForProject(EXISTING_CONTRACT_ID)
        then:
            noExceptionThrown()
            contract != null
    }

    def "updateContract should not update contract if validation fails"() {
        given:
            def updateContract = new ContractDto(NOT_EXISTING_CONTRACT_ID, NEW_OFFER_VALUE, NEW_SIGNING_DATE, NEW_DEADLINE, NEW_START_DATE, NEW_END_DATE, NEW_STATUS, null, null, null)
            mockExistenceThrowsAnException()
        when:
            def result = subject.updateContract(NOT_EXISTING_CONTRACT_ID, updateContract)
        then:
            thrown(Exception)
            result == null
    }

    def "updateContract should update contract data based on the status"() {
        given:
            def contract = new Contract(OFFER_VALUE, DEADLINE, CONTRACT_STATUS_WORKFLOW)
            contract.changeStatus(ContractStatus.OFFER)
            contractRepository.findById(100L) >> Optional.of(contract)
            def updateContractDto = new ContractDto(100L, NEW_OFFER_VALUE, NEW_SIGNING_DATE, NEW_DEADLINE, NEW_START_DATE, NEW_END_DATE, NEW_STATUS, [ContractStatusDto.OFFER], null, null)
            def updatedContract = new Contract(NEW_OFFER_VALUE, NEW_DEADLINE, CONTRACT_STATUS_WORKFLOW)
            updatedContract.changeStatus(ContractStatus.ACCEPTED)
            contractDomainService.updateContract(_ as Contract, _ as ContractDto) >> updatedContract
        when:
            def result = subject.updateContract(100L, updateContractDto)
        then:
            result.offerValue == NEW_OFFER_VALUE

    }

    private void mockValidatorThrowsAnExceptionOnNullContractDto() {
        this.contractValidator.validateOffer(null) >> { throw new IllegalArgumentException() }
    }

    private void mockValidatorThrowsAnExceptionOnContractDtoWithMissingData(ContractDto contractDto) {
        this.contractValidator.validateOffer(contractDto) >> { throw new IllegalArgumentException() }
    }

    private void mockBaseValidationThrowsAnException() {
        this.contractValidator.validateBaseContractDto(_ as ContractDto) >> { throw new IllegalArgumentException() }
    }

    private void mockSignStatusValidatorThrowsAnException() {
        this.contractValidator.validateSignContractDto(_ as ContractDto) >> { throw new IllegalArgumentException() }
    }

    private void mockExistenceThrowsAnException() {
        this.contractValidator.validateContractExistence(_ as Optional<Contract>, _ as Long) >> { throw new IllegalArgumentException() }
    }

}
