package com.arturjarosz.task.finance.application


import com.arturjarosz.task.finance.application.dto.InstallmentDto
import com.arturjarosz.task.finance.query.FinancialDataQueryService
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException
import spock.lang.Specification

import java.time.LocalDate

class InstallmentValidatorTest extends Specification {

    private static final String NOTE = "note"
    private static final BigDecimal VALUE = new BigDecimal("20.0")
    private static final LocalDate PAY_DATE = LocalDate.now()
    private static final LocalDate FUTURE_PAY_DATE = LocalDate.now().plusDays(100)
    private static final LocalDate PAST_PAY_DATE = LocalDate.now().minusDays(100)
    private static final Long INSTALLMENT_ID = 1L
    private static final Long NOT_EXISTING_INSTALLMENT_ID = 2L

    def financialDataQueryService = Mock(FinancialDataQueryService)

    def installmentValidator = new InstallmentValidator(financialDataQueryService)

    def setup() {
        financialDataQueryService.doesInstallmentExistsByInstallmentId(INSTALLMENT_ID) >> true
        financialDataQueryService.doesInstallmentExistsByInstallmentId(NOT_EXISTING_INSTALLMENT_ID) >> false
    }

    def "when dto is null, validateCreateInstallmentDto should throw an exception with specific message"() {
        given:
            InstallmentDto installmentDto = null
        when:
            installmentValidator.validateCreateInstallmentDto(installmentDto)
        then:
            Exception exception = thrown()
            exception.message == "isNull.installment"
    }

    def "when value in installment is null, validateCreateInstallmentDto should throw an exception with specific message"() {
        given:
            InstallmentDto installmentDto = new InstallmentDto(value: null)
        when:
            installmentValidator.validateCreateInstallmentDto(installmentDto)
        then:
            Exception exception = thrown()
            exception.message == "isNull.installment.value"
    }

    def "when dto is correct, validateCreateInstallmentDto should not throw any exception"() {
        given:
            InstallmentDto installmentDto = new InstallmentDto(value: VALUE, note: NOTE, paymentDate: PAY_DATE)
        when:
            installmentValidator.validateCreateInstallmentDto(installmentDto)
        then:
            noExceptionThrown()
    }

    def "when payDate is future, validatePayDateNotFuture should throw an exception with specific message"() {
        given:
            LocalDate futureDate = LocalDate.now().plusDays(2)
        when:
            installmentValidator.validatePayDateNotFuture(futureDate)
        then:
            Exception exception = thrown()
            exception.message == "notValid.installment.payDate"
    }

    def "validateUpdateInstallmentDto should throw exception with correct message on incorrect input"() {
        given:
        when:
            installmentValidator.validateUpdateInstallmentDto(installmentDto, isPaid)
        then:
            def exception = thrown(IllegalArgumentException)
            exception.localizedMessage == exceptionMessage
        where:
            installmentDto                  | isPaid || exceptionMessage
            null                            | true    | "isNull.installment"
            new InstallmentDto(value: null) | true    | "isNull.installment.value"
    }

    def "validateUpdateInstallmentDto should throw exception if paid and paidDate in the future"() {
        given:
            def installentDto = new InstallmentDto(paymentDate: FUTURE_PAY_DATE, value: VALUE)
        when:
            installmentValidator.validateUpdateInstallmentDto(installentDto, true)
        then:
            Exception exception = thrown()
            exception.message == "notValid.installment.payDate"
    }

    def "validateUpdateInstallmentDto should not throw any exception if is paid and paidDate not in the future and correct data"() {
        given:
            def installentDto = new InstallmentDto(paymentDate: PAST_PAY_DATE, value: VALUE)
        when:
            installmentValidator.validateUpdateInstallmentDto(installentDto, true)
        then:
            noExceptionThrown()
    }

    def "validateUpdateInstallmentDto should not throw any exception if not paid and correct data"() {
        given:
            def installentDto = new InstallmentDto(paymentDate: PAST_PAY_DATE, value: VALUE)
        when:
            installmentValidator.validateUpdateInstallmentDto(installentDto, false)
        then:
            noExceptionThrown()
    }

    def "validateInstallmentExistence should throw exception with specific message if installment with given id does not exist"() {
        given:
        when:
            installmentValidator.validateInstallmentExistence(NOT_EXISTING_INSTALLMENT_ID)
        then:
            Exception exception = thrown()
            exception.message == "notExist.installment"
    }

    def "validateInstallmentExistence should not throw any exception if installment with given id does exists"() {
        given:
        when:
            installmentValidator.validateInstallmentExistence(INSTALLMENT_ID)
        then:
            noExceptionThrown()
    }

    def "validatePayInstallmentDto should throw exception with specific message if input not correct"() {
        given:
        when:
            installmentValidator.validatePayInstallmentDto(installmentDto, isPaid)
        then:
            def exception = thrown(IllegalArgumentException)
            exception.localizedMessage == exceptionMessage
        where:
            installmentDto                                   | isPaid || exceptionMessage
            null                                             | false   | "isNull.installment"
            new InstallmentDto()                             | true    | "notValid.installment.paid"
            new InstallmentDto(paymentDate: FUTURE_PAY_DATE) | false   | "notValid.installment.payDate"
    }

    def "validatePayInstallmentDto should not throw any exception on correct data"() {
        given:
            def installmentDto = new InstallmentDto(paymentDate: PAST_PAY_DATE)
        when:
            installmentValidator.validatePayInstallmentDto(installmentDto, false)
        then:
            noExceptionThrown()
    }
}
