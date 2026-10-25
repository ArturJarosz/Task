package com.arturjarosz.task.contract.application

import com.arturjarosz.task.contract.model.Contract
import com.arturjarosz.task.utils.ContractBuilder
import com.arturjarosz.task.dto.ContractDto
import spock.lang.Specification

import java.time.LocalDate

class ContractValidatorTest extends Specification {
    static final long CONTRACT_ID = 1L
    static final double OFFER_VALUE = 200.00
    static final LocalDate FUTURE_DATE = LocalDate.of(2100, 01, 01)
    static final LocalDate PAST_DATE = LocalDate.of(1900, 01, 01)
    static final LocalDate PAST_DATE_2 = LocalDate.of(1800, 01, 01)

    def contractValidator = new ContractValidator()

    def "validateOffer should throw an exception with proper exception message when dto is not correct"() {
        given:
            def contractDto = givenContractDto
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
            def contractDto = new ContractDto(offerValue: OFFER_VALUE, deadline: FUTURE_DATE)
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
            def contractDto = givenContractDto
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
            def contractDto = new ContractDto(offerValue: OFFER_VALUE, signingDate: PAST_DATE_2,
                    deadline: FUTURE_DATE, startDate: PAST_DATE)
        when:
            this.contractValidator.validateSignContractDto(contractDto)
        then:
            noExceptionThrown()
    }

    def "validateTerminateContractDto should throw and exception with proper exception message when contractDto is not correct"() {
        given:
            def contract = new ContractBuilder().withStartDate(PAST_DATE).build()
        when:
            this.contractValidator.validateTerminateContractDto(givenContractDto, contract)
        then:
            Exception exception = thrown()
            exception.message == exceptionMessage
        where:
            givenContractDto                     || exceptionMessage
            null                                 || "isNull.project.contract"
            new ContractDto()                    || "isNull.contract.endDate"
            new ContractDto(endDate: FUTURE_DATE) || "notValid.contract.endDate"
            new ContractDto(endDate: PAST_DATE_2) || "notValid.contract.endDate.startDate"
    }

    def "validateTerminateContractDto should not throw any exception on proper contractDto"() {
        given:
            def contract = new ContractBuilder().withStartDate(PAST_DATE).build()
        when:
            this.contractValidator.validateTerminateContractDto(new ContractDto(endDate: givenEndDate), contract)
        then:
            noExceptionThrown()
        where:
            givenEndDate << [PAST_DATE, LocalDate.now()]
    }

    def "validateCompleteContractDto should throw and exception with proper exception message when contractDto is not correct"() {
        given:
            def contract = new ContractBuilder().withStartDate(PAST_DATE).build()
        when:
            this.contractValidator.validateCompleteContractDto(givenContractDto, contract)
        then:
            Exception exception = thrown()
            exception.message == exceptionMessage
        where:
            givenContractDto                     || exceptionMessage
            null                                 || "isNull.project.contract"
            new ContractDto()                    || "isNull.contract.endDate"
            new ContractDto(endDate: FUTURE_DATE) || "notValid.contract.endDate"
            new ContractDto(endDate: PAST_DATE_2) || "notValid.contract.endDate.startDate"
    }

    def "validateCompleteContractDto should not throw any exception on proper contractDto"() {
        given:
            def contract = new ContractBuilder().withStartDate(PAST_DATE).build()
        when:
            this.contractValidator.validateCompleteContractDto(new ContractDto(endDate: givenEndDate), contract)
        then:
            noExceptionThrown()
        where:
            givenEndDate << [PAST_DATE, LocalDate.now()]
    }

    def "validateCompleteContractDto should not compare end date with start date when contract is not signed"() {
        given:
            def contract = new ContractBuilder().build()
        when:
            this.contractValidator.validateCompleteContractDto(new ContractDto(endDate: PAST_DATE), contract)
        then:
            noExceptionThrown()
    }
}
