package com.arturjarosz.task.contract.domain.impl

import com.arturjarosz.task.contract.model.Contract
import com.arturjarosz.task.contract.status.ContractStatus
import com.arturjarosz.task.contract.status.ContractStatusTransitionService
import com.arturjarosz.task.contract.status.ContractStatusWorkflow
import com.arturjarosz.task.dto.ContractDto
import com.arturjarosz.task.dto.ContractStatusDto
import com.arturjarosz.task.sharedkernel.model.Money
import spock.lang.Specification

import java.time.LocalDate

class ContractDomainServiceImplTest extends Specification {
    static final OFFER_VALUE = 100.00d
    static final NEW_OFFER_VALUE = 200.00d
    static final CONTRACT_STATUS_WORKFLOW = new ContractStatusWorkflow()
    static final DEADLINE = LocalDate.of(2100, 1, 1)
    static final NEW_DEADLINE = LocalDate.of(2101, 1, 1)
    static final START_DATE = LocalDate.of(2102, 1, 1)
    static final NEW_START_DATE = LocalDate.of(2102, 5, 1)
    static final END_DATE = LocalDate.of(2104, 1, 1)
    static final NEW_END_DATE = LocalDate.of(2104, 5, 1)
    static final SIGNING_DATE = LocalDate.of(2105, 1, 1)
    static final NEW_SIGNING_DATE = LocalDate.of(2105, 5, 1)

    def contractWorkflow = new ContractStatusWorkflow()
    def contractStatusTransitionService = new ContractStatusTransitionServiceTest()

    def subject = new ContractDomainServiceImpl(contractWorkflow, contractStatusTransitionService)

    def "createContract should return new contract in OFFER status"() {
        given:
            def contractDto = new ContractDto().offerValue(OFFER_VALUE).deadline(DEADLINE)
        when:
            def contract = subject.createContract(contractDto)
        then:
            contract != null
            contract.status == ContractStatus.OFFER
    }

    def "updateContractStatus should change status and update appropriate values on the contract"() {
        given:
            def contract = new Contract(OFFER_VALUE, DEADLINE, CONTRACT_STATUS_WORKFLOW)
            def contractDto = new ContractDto()
                    .status(status)
                    .deadline(NEW_DEADLINE)
                    .offerValue(NEW_OFFER_VALUE)
                    .startDate(START_DATE)
                    .endDate(END_DATE)
                    .signingDate(SIGNING_DATE)
        when:
            def updatedContract = subject.updateContractStatus(contract, contractDto)
        then:
            updatedContract.status == newStatus
            updatedContract.offerValue == newValue
            updatedContract.deadline == newDeadline
            updatedContract.startDate == newStartDate
            updatedContract.endDate == newEndDate
            updatedContract.signingDate == newSigningDate
        where:
            status                       || newStatus                 | newValue                   | newDeadline  | newStartDate | newEndDate | newSigningDate
            ContractStatusDto.OFFER      || ContractStatus.OFFER      | new Money(NEW_OFFER_VALUE) | NEW_DEADLINE | null         | null       | null
            ContractStatusDto.SIGNED     || ContractStatus.SIGNED     | new Money(NEW_OFFER_VALUE) | NEW_DEADLINE | START_DATE   | null       | SIGNING_DATE
            ContractStatusDto.ACCEPTED   || ContractStatus.ACCEPTED   | new Money(OFFER_VALUE)     | DEADLINE     | null         | null       | null
            ContractStatusDto.COMPLETED  || ContractStatus.COMPLETED  | new Money(OFFER_VALUE)     | DEADLINE     | null         | END_DATE   | null
            ContractStatusDto.REJECTED   || ContractStatus.REJECTED   | new Money(OFFER_VALUE)     | DEADLINE     | null         | null       | null
            ContractStatusDto.TERMINATED || ContractStatus.TERMINATED | new Money(OFFER_VALUE)     | DEADLINE     | null         | END_DATE   | null

    }

    def "updateContract should update appropriate values one the contract based on the contract status"() {
        given:
            def contract = new Contract(OFFER_VALUE, DEADLINE, CONTRACT_STATUS_WORKFLOW)
            contract.updateEndDate(END_DATE)
            contract.changeStatus(ContractStatus.valueOf(status.name()))
            def contractDto = new ContractDto()
                    .status(status)
                    .deadline(NEW_DEADLINE)
                    .offerValue(NEW_OFFER_VALUE)
                    .startDate(NEW_START_DATE)
                    .endDate(NEW_END_DATE)
                    .signingDate(NEW_SIGNING_DATE)

        when:
            def updatedContract = subject.updateContract(contract, contractDto)

        then:
            updatedContract.offerValue == newValue
            updatedContract.deadline == newDeadline
            updatedContract.startDate == newStartDate
            updatedContract.endDate == newEndDate
            updatedContract.signingDate == newSigningDate

        where:
            status                       || newValue                   | newDeadline  | newStartDate   | newEndDate   | newSigningDate
            ContractStatusDto.OFFER      || new Money(NEW_OFFER_VALUE) | NEW_DEADLINE | null           | END_DATE     | null
            ContractStatusDto.SIGNED     || new Money(NEW_OFFER_VALUE) | NEW_DEADLINE | NEW_START_DATE | null         | NEW_SIGNING_DATE
            ContractStatusDto.ACCEPTED   || new Money(OFFER_VALUE)     | DEADLINE     | null           | END_DATE     | null
            ContractStatusDto.COMPLETED  || new Money(OFFER_VALUE)     | DEADLINE     | null           | NEW_END_DATE | null
            ContractStatusDto.REJECTED   || new Money(OFFER_VALUE)     | DEADLINE     | null           | END_DATE     | null
            ContractStatusDto.TERMINATED || new Money(OFFER_VALUE)     | DEADLINE     | null           | NEW_END_DATE | null
    }
}

class ContractStatusTransitionServiceTest implements ContractStatusTransitionService {

    @Override
    void createOffer(Contract contract) {
        contract.changeStatus(ContractStatus.OFFER)
    }

    @Override
    void rejectOffer(Contract contract) {}

    @Override
    void rejectAcceptedOffer(Contract contract) {}

    @Override
    void reject(Contract contract) {}

    @Override
    void changeStatus(Contract contract, ContractStatus newStatus) {
        contract.changeStatus(newStatus)
    }
}
