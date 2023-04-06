package com.arturjarosz.task.contract.application

import com.arturjarosz.task.contract.application.dto.ContractDto
import com.arturjarosz.task.contract.model.Contract
import spock.lang.Specification

import java.time.LocalDate

class ContractValidatorTest extends Specification {
    private static final long CONTRACT_ID = 1L
    private static final double OFFER_VALUE = 200.00
    private static final LocalDate FUTURE_DATE = LocalDate.of(2100, 01, 01)
    private static final LocalDate PAST_DATE = LocalDate.of(1900, 01, 01)
    private static final LocalDate PAST_DATE_2 = LocalDate.of(1800, 01, 01)

    def contractValidator = new ContractValidator()

    def "validateOffer should throw an exception with proper exception message when dto is not correct"() {
        given:
            ContractDto contractDto = givenContractDto
            if (contractDto != null) {
                contractDto.offerValue = givenOfferValue as Double
                contractDto.deadline = givenDeadline
            }
        when:
            this.contractValidator.validateOffer(contractDto)
        then:
            Exception exception = thrown()
            exception.message == exceptionMessage
        where:
            givenContractDto  | givenOfferValue | givenDeadline | exceptionMessage
            null              | null            | null          | "isNull.project.contract"
            new ContractDto() | null            | null          | "isNull.contract.offer.value"
            new ContractDto() | -1.0            | null          | "negative.contract.offer.value"
            new ContractDto() | OFFER_VALUE     | PAST_DATE     | "notValid.contract.deadline"
    }

    def "validateOffer show not throw any exception when contractDto has proper data"() {
        given:
            ContractDto contractDto = new ContractDto(offerValue: OFFER_VALUE, deadline: FUTURE_DATE)
        when:
            this.contractValidator.validateOffer(contractDto)
        then:
            noExceptionThrown()
    }

    def "validateContractExistence should throw an exception when passed contract is null"() {
        given:
            Optional<Contract> maybeContract = Optional.ofNullable(null)
        when:
            this.contractValidator.validateContractExistence(maybeContract, CONTRACT_ID)
        then:
            Exception exception = thrown()
            exception.message == "notExist.contract"
    }

    def "validateSignContractDto should throw an exception with proper exception message when dto is not correct"() {
        given:
            ContractDto contractDto = givenContractDto
            if (contractDto != null) {
                contractDto.offerValue = offerValue
                contractDto.signingDate = signingDate
                contractDto.deadline = deadline
                contractDto.startDate = startDate
            }
        when:
            this.contractValidator.validateSignContractDto(contractDto)
        then:
            Exception exception = thrown()
            exception.message == exceptionMessage
        where:
            givenContractDto  | offerValue  | signingDate | deadline    | startDate   | exceptionMessage
            null              | null        | null        | null        | null        | "isNull.project.contract"
            new ContractDto() | null        | null        | null        | null        | "isNull.contract.offer.value"
            new ContractDto() | -1.0        | null        | null        | null        | "negative.contract.offer.value"
            new ContractDto() | OFFER_VALUE | null        | null        | null        | "isNull.contract.signingDate"
            new ContractDto() | OFFER_VALUE | FUTURE_DATE | null        | null        | "notValid.contract.signingDate"
            new ContractDto() | OFFER_VALUE | PAST_DATE   | null        | null        | "isNull.contract.deadline"
            new ContractDto() | OFFER_VALUE | PAST_DATE   | PAST_DATE   | null        | "notValid.contract.deadline"
            new ContractDto() | OFFER_VALUE | PAST_DATE   | FUTURE_DATE | null        | "isNull.contract.startDate"
            new ContractDto() | OFFER_VALUE | PAST_DATE   | FUTURE_DATE | PAST_DATE_2 | "notValid.contract.startDate"
    }

    def "validateSignContractDto should not throw any exception on proper contractDto"() {
        given:
            ContractDto contractDto = new ContractDto(offerValue: OFFER_VALUE, signingDate: PAST_DATE_2,
                    deadline: FUTURE_DATE, startDate: PAST_DATE)
        when:
            this.contractValidator.validateSignContractDto(contractDto)
        then:
            noExceptionThrown()
    }

    def "validateTerminateContractDto should throw and exception with proper exception message when contractDto is not correct"() {
        given:
            ContractDto contractDto = givenContractDto
        when:
            this.contractValidator.validateTerminateContractDto(contractDto)
        then:
            Exception exception = thrown()
            exception.message == exceptionMessage
        where:
            givenContractDto  | endDate | exceptionMessage
            null              | null    | "isNull.project.contract"
            new ContractDto() | null    | "isNull.contract.endDate"
    }

    def "validateTerminateContractDto should not throw any exception on proper contractDto"() {
        given:
            ContractDto contractDto = new ContractDto(endDate: FUTURE_DATE)
        when:
            this.contractValidator.validateTerminateContractDto(contractDto)
        then:
            noExceptionThrown()
    }

    def "validateCompleteContractDto should throw and exception with proper exception message when contractDto is not correct"() {
        given:
            ContractDto contractDto = givenContractDto
        when:
            this.contractValidator.validateCompleteContractDto(contractDto)
        then:
            Exception exception = thrown()
            exception.message == exceptionMessage
        where:
            givenContractDto  | endDate | exceptionMessage
            null              | null    | "isNull.project.contract"
            new ContractDto() | null    | "isNull.contract.endDate"
    }

    def "validateCompleteContractDto should not throw any exception on proper contractDto"() {
        given:
            ContractDto contractDto = new ContractDto(endDate: FUTURE_DATE)
        when:
            this.contractValidator.validateCompleteContractDto(contractDto)
        then:
            noExceptionThrown()
    }


}
