package com.arturjarosz.task.contract.application.impl

import com.arturjarosz.task.contract.application.ContractValidator
import com.arturjarosz.task.contract.application.dto.ContractDto
import com.arturjarosz.task.contract.intrastructure.ContractRepository
import com.arturjarosz.task.contract.model.Contract
import com.arturjarosz.task.contract.status.ContractStatus
import com.arturjarosz.task.contract.status.ContractStatusWorkflow
import com.arturjarosz.task.contract.status.impl.ContractStatusTransitionServiceImpl
import com.arturjarosz.task.contract.utils.ContractBuilder
import com.arturjarosz.task.sharedkernel.model.Money
import spock.lang.Specification
import spock.lang.Subject

import java.time.LocalDate

class ContractServiceImplTest extends Specification {

    static final long EXISTING_CONTRACT_ID = 1L
    static final long NOT_EXISTING_CONTRACT_ID = 2L
    static final double OFFER_VALUE = 100.00d
    static final double NEW_OFFER_VALUE = 200.00D
    static final LocalDate DEADLINE = LocalDate.of(2100, 1, 1)
    static final LocalDate NEW_DEADLINE = LocalDate.of(2101, 1, 1)
    static final LocalDate SIGNING_DATE = LocalDate.of(2050, 1, 1)
    static final LocalDate START_DATE = LocalDate.of(2051, 1, 1)
    static final LocalDate END_DATE = LocalDate.of(2052, 1, 1)

    def contractStatusTransitionService = Mock(ContractStatusTransitionServiceImpl)
    def contractStatusWorkflow = Mock(ContractStatusWorkflow)
    def contractValidator = Mock(ContractValidator)
    def contractRepository = Mock(ContractRepository)

    @Subject
    def contractService = new ContractServiceImpl(contractStatusTransitionService, contractStatusWorkflow,
            contractValidator, contractRepository)

    def "createContract does not create any contract on missing contractDto"() {
        given:
            ContractDto contractDto = null
            this.mockValidatorThrowsAnExceptionOnNullContractDto()
        when:
            Contract contract = this.contractService.createContract(contractDto)
        then:
            thrown(Exception)
            contract == null
    }

    def "createContract does not create any contract on missing data in contractDto"() {
        given:
            ContractDto contractDto = new ContractDto()
            this.mockValidatorThrowsAnExceptionOnContractDtoWithMissingData(contractDto)
        when:
            Contract contract = this.contractService.createContract(contractDto)
        then:
            thrown(Exception)
            contract == null
    }

    def "createContract creates and saves new contract with offer status on proper contractDto"() {
        given:
            ContractDto contractDto = new ContractDto(deadline: DEADLINE, offerValue: OFFER_VALUE)
            this.mockContractStatusTransitionServiceCreateOfferReturnContractWithOfferStatus()
        when:
            Contract contract = this.contractService.createContract(contractDto)
        then:
            noExceptionThrown()
            1 * this.contractRepository.save({
                Contract savedContract ->
                    savedContract.deadline == DEADLINE
                    savedContract.getOfferValue().getValue().doubleValue() == OFFER_VALUE
                    savedContract.status == ContractStatus.OFFER
            }) >> new ContractBuilder().withId(EXISTING_CONTRACT_ID).withStatus(ContractStatus.OFFER).build()
            contract.status == ContractStatus.OFFER
            contract.id == EXISTING_CONTRACT_ID
    }

    def "contractReject throws an exception and do not reject offer on id of not existing contract"() {
        given:
            this.mockContractValidatorThrowsAnExceptionOnNotExistingContract()
        when:
            ContractDto contractDto = this.contractService.reject(NOT_EXISTING_CONTRACT_ID)
        then:
            thrown(Exception)
            0 * this.contractStatusTransitionService.rejectOffer(_ as Contract)
            contractDto == null
    }

    def "contractReject rejects contract on id of existing contract"() {
        given:
            this.mockLoadContractFromContractRepository()
            this.mockContractStatusTransitionServiceRejectOffer()
        when:
            ContractDto contractDto = this.contractService.reject(EXISTING_CONTRACT_ID)
        then:
            noExceptionThrown()
            contractDto.status == ContractStatus.REJECTED
    }

    def "makeNewOffer does not update offer on not existing contract throws an exception"() {
        given:
            ContractDto contractOfferDto = new ContractDto(offerValue: NEW_OFFER_VALUE)
            this.mockContractValidatorThrowsAnExceptionOnNotExistingContract()
        when:
            ContractDto contractDto = this.contractService.makeNewOffer(NOT_EXISTING_CONTRACT_ID, contractOfferDto)
        then:
            thrown(Exception)
            0 * this.contractStatusTransitionService.makeNewOffer(_ as Contract)
            contractDto == null
    }

    def "makeNewOffer does not update offer on not proper contractDto of offer throws an exception"() {
        given:
            ContractDto contractOfferDto = new ContractDto(offerValue: NEW_OFFER_VALUE)
            this.mockContractValidatorThrowsAnExceptionOnWrongSignData()
        when:
            ContractDto contractDto = this.contractService.makeNewOffer(NOT_EXISTING_CONTRACT_ID, contractOfferDto)
        then:
            thrown(Exception)
            0 * this.contractStatusTransitionService.makeNewOffer(_ as Contract)
            contractDto == null
    }

    def "makeNewOffer updates offer status to OFFER and data for existing contract"() {
        given:
            ContractDto contractOfferDto = new ContractDto(offerValue: NEW_OFFER_VALUE, deadline: NEW_DEADLINE)
            this.mockLoadContractFromContractRepository()
            this.mockContractStatusTransitionMakeNewOffer()
        when:
            ContractDto contractDto = this.contractService.makeNewOffer(EXISTING_CONTRACT_ID, contractOfferDto)
        then:
            noExceptionThrown()
            contractDto.status == ContractStatus.OFFER
            contractDto.offerValue == NEW_OFFER_VALUE
    }

    def "acceptOffer does not accept not existing offer and throws an exception"() {
        given:
            this.mockContractValidatorThrowsAnExceptionOnNotExistingContract()
        when:
            ContractDto contractDto = this.contractService.acceptOffer(NOT_EXISTING_CONTRACT_ID)
        then:
            thrown(Exception)
            0 * this.contractStatusTransitionService.acceptOffer(_ as Contract)
            contractDto == null
    }

    def "acceptOffer updates data on offer and changes status to ACCEPTED"() {
        given:
            this.mockLoadContractFromContractRepository()
            this.mockContractStatusTransitionAcceptOffer()
        when:
            ContractDto contractDto = this.contractService.acceptOffer(EXISTING_CONTRACT_ID)
        then:
            noExceptionThrown()
            contractDto.status == ContractStatus.ACCEPTED
    }

    def "sign throws an exception and do not change contract status on not existing contract"() {
        given:
            ContractDto signContractDto = new ContractDto()
            this.mockContractValidatorThrowsAnExceptionOnNotExistingContract()
        when:
            ContractDto contractDto = this.contractService.sign(NOT_EXISTING_CONTRACT_ID, signContractDto)
        then:
            thrown(Exception)
            0 * this.contractStatusTransitionService.signContract(_ as Contract)
            contractDto == null
    }

    def "sing throws an exception and do not change contract status on not proper contractDto"() {
        given:
            ContractDto signContractDto = new ContractDto()
            this.mockContractValidatorThrowsAnExceptionOnWrongSignData()
        when:
            ContractDto contractDto = this.contractService.sign(EXISTING_CONTRACT_ID, signContractDto)
        then:
            thrown(Exception)
            0 * this.contractStatusTransitionService.signContract(_ as Contract)
            contractDto == null
    }

    def "sing updated contract data and changes contract status to SIGNED"() {
        given:
            ContractDto signContractDto = new ContractDto(offerValue: OFFER_VALUE, signingDate: SIGNING_DATE,
                    startDate: START_DATE, deadline: DEADLINE)
            this.mockLoadContractFromContractRepository()
            this.mockContractStatusTransitionSign()
        when:
            ContractDto contractDto = this.contractService.sign(EXISTING_CONTRACT_ID, signContractDto)
        then:
            noExceptionThrown()
            contractDto.status == ContractStatus.SIGNED
            contractDto.startDate == START_DATE
            contractDto.signingDate == SIGNING_DATE
            contractDto.deadline == DEADLINE
    }

    def "terminate throws an exception and do not update status on not existing contract"() {
        given:
            ContractDto terminateContractDto = new ContractDto()
            this.mockContractValidatorThrowsAnExceptionOnNotExistingContract()
        when:
            ContractDto contractDto = this.contractService.terminate(NOT_EXISTING_CONTRACT_ID, terminateContractDto)
        then:
            thrown(Exception)
            0 * this.contractStatusTransitionService.terminateContract(_ as Contract)
            contractDto == null
    }

    def "terminate throws an exception and do not update status on not proper terminate contractDto"() {
        given:
            ContractDto terminateContractDto = new ContractDto()
            this.mockContractValidatorThrowsAnExceptionOnWrongTerminateData()
        when:
            ContractDto contractDto = this.contractService.terminate(EXISTING_CONTRACT_ID, terminateContractDto)
        then:
            thrown(Exception)
            0 * this.contractStatusTransitionService.terminateContract(_ as Contract)
            contractDto == null
    }

    def "terminate changes contract status to TERMINATED and updates contract data"() {
        given:
            ContractDto terminateContractDto = new ContractDto(endDate: END_DATE)
            this.mockLoadContractFromContractRepository()
            this.mockContractStatusTransitionTerminate()
        when:
            ContractDto contractDto = this.contractService.terminate(EXISTING_CONTRACT_ID, terminateContractDto)
        then:
            noExceptionThrown()
            contractDto.status == ContractStatus.TERMINATED
            contractDto.endDate == END_DATE
    }

    def "resume throws an exception and does not update contract status"() {
        given:
            this.mockContractValidatorThrowsAnExceptionOnNotExistingContract()
        when:
            ContractDto contractDto = this.contractService.resume(NOT_EXISTING_CONTRACT_ID)
        then:
            thrown(Exception)
            0 * this.contractStatusTransitionService.resumeContract(NOT_EXISTING_CONTRACT_ID)
            contractDto == null
    }

    def "resume changes contract status to SIGNED and updates contract data of existing contract"() {
        given:
            this.mockLoadContractFromContractRepository()
            this.mockContractStatusTransitionResume()
        when:
            ContractDto contractDto = this.contractService.resume(EXISTING_CONTRACT_ID)
        then:
            noExceptionThrown()
            contractDto.status == ContractStatus.SIGNED
            contractDto.endDate == null
    }

    def "complete throws an exception and do not update status on not existing contract"() {
        given:
            ContractDto completeContractDto = new ContractDto()
            this.mockContractValidatorThrowsAnExceptionOnNotExistingContract()
        when:
            ContractDto contractDto = this.contractService.complete(NOT_EXISTING_CONTRACT_ID, completeContractDto)
        then:
            thrown(Exception)
            0 * this.contractStatusTransitionService.terminateContract(_ as Contract)
            contractDto == null
    }

    def "complete throws an exception and do not update status on not proper complete contractDto"() {
        given:
            ContractDto completeContractDto = new ContractDto()
            this.mockContractValidatorThrowsAnExceptionOnWrongTerminateData()
        when:
            ContractDto contractDto = this.contractService.complete(EXISTING_CONTRACT_ID, completeContractDto)
        then:
            thrown(Exception)
            0 * this.contractStatusTransitionService.terminateContract(_ as Contract)
            contractDto == null
    }

    def "complete changes contract status to COMPLETED and updates contract data"() {
        given:
            ContractDto completeContractDto = new ContractDto(endDate: END_DATE)
            this.mockLoadContractFromContractRepository()
            this.mockContractStatusTransitionComplete()
        when:
            ContractDto contractDto = this.contractService.complete(EXISTING_CONTRACT_ID, completeContractDto)
        then:
            noExceptionThrown()
            contractDto.status == ContractStatus.COMPLETED
            contractDto.endDate == END_DATE
    }


    private void mockValidatorThrowsAnExceptionOnNullContractDto() {
        this.contractValidator.validateOffer(null) >> { throw new IllegalArgumentException() }
    }

    private void mockValidatorThrowsAnExceptionOnContractDtoWithMissingData(ContractDto contractDto) {
        this.contractValidator.validateOffer(contractDto) >> { throw new IllegalArgumentException() }
    }

    private void mockContractStatusTransitionServiceCreateOfferReturnContractWithOfferStatus() {
        1 * this.contractStatusTransitionService.createOffer({
            Contract contract ->
                contract.changeStatus(ContractStatus.OFFER)
        })
    }

    private mockContractValidatorThrowsAnExceptionOnNotExistingContract() {
        this.contractValidator.validateContractExistence(null,
                NOT_EXISTING_CONTRACT_ID) >> { throw new IllegalArgumentException() }
    }

    private mockLoadContractFromContractRepository() {
        this.contractRepository.findById(EXISTING_CONTRACT_ID) >> Optional.of(new ContractBuilder()
                .withId(EXISTING_CONTRACT_ID).withContractValue(new Money(OFFER_VALUE)).build())
    }

    private mockContractStatusTransitionServiceRejectOffer() {
        1 * this.contractStatusTransitionService.rejectOffer({
            Contract contract ->
                contract.changeStatus(ContractStatus.REJECTED)
        })
    }

    private mockContractStatusTransitionMakeNewOffer() {
        1 * this.contractStatusTransitionService.makeNewOffer({
            Contract contract ->
                contract.changeStatus(ContractStatus.OFFER)
        })
    }

    private mockContractStatusTransitionAcceptOffer() {
        1 * this.contractStatusTransitionService.acceptOffer({
            Contract contract ->
                contract.changeStatus(ContractStatus.ACCEPTED)
        })
    }

    private mockContractValidatorThrowsAnExceptionOnWrongSignData() {
        this.contractValidator.validateSignContractDto(_ as ContractDto) >> { throw new IllegalArgumentException() }
    }

    private mockContractStatusTransitionSign() {
        1 * this.contractStatusTransitionService.signContract({
            Contract contract ->
                contract.changeStatus(ContractStatus.SIGNED)
        })
    }

    private mockContractValidatorThrowsAnExceptionOnWrongTerminateData() {
        this.contractValidator.validateTerminateContractDto(
                _ as ContractDto) >> { throw new IllegalArgumentException() }
    }

    private mockContractStatusTransitionTerminate() {
        1 * this.contractStatusTransitionService.terminateContract({
            Contract contract ->
                contract.changeStatus(ContractStatus.TERMINATED)
        })
    }

    private mockContractStatusTransitionResume() {
        1 * this.contractStatusTransitionService.resumeContract({
            Contract contract ->
                contract.changeStatus(ContractStatus.SIGNED)
        })
    }

    private mockContractStatusTransitionComplete() {
        1 * this.contractStatusTransitionService.completeContract({
            Contract contract ->
                contract.changeStatus(ContractStatus.COMPLETED)
        })
    }
}
