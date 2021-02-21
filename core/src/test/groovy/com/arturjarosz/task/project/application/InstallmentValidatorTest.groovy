package com.arturjarosz.task.project.application

import com.arturjarosz.task.project.application.dto.InstallmentDto
import spock.lang.Specification

import java.time.LocalDate

class InstallmentValidatorTest extends Specification {

    private static final String NOTE = "note";
    private static final Double VALUE = 20.0D;
    private static final LocalDate PAY_DATE = LocalDate.now();

    def "when dto is null, validateCreateInstallmentDto should throw an exception with specific message"() {
        given:
            InstallmentDto installmentDto = null;
        when:
            InstallmentValidator.validateCreateInstallmentDto(installmentDto);
        then:
            Exception exception = thrown();
            exception.message == "isNull.installment"
    }

    def "when value in installment is null, validateCreateInstallmentDto should throw an exception with specific message"() {
        given:
            InstallmentDto installmentDto = new InstallmentDto();
            installmentDto.setValue(null);
        when:
            InstallmentValidator.validateCreateInstallmentDto(installmentDto);
        then:
            Exception exception = thrown();
            exception.message == "isNull.installment.value";
    }

    def "when dto is correct, validateCreateInstallmentDto should not throw any exception"() {
        given:
            InstallmentDto installmentDto = new InstallmentDto();
            installmentDto.setValue(VALUE);
            installmentDto.setNote(NOTE);
            installmentDto.setPayDate(PAY_DATE);
        when:
            InstallmentValidator.validateCreateInstallmentDto(installmentDto);
        then:
            noExceptionThrown();
    }

    def "when payDate is future, validatePayDateNotFuture should throw an exception with specific message"() {
        given:
            LocalDate futureDate = LocalDate.now().plusDays(2);
        when:
            InstallmentValidator.validatePayDateNotFuture(futureDate);
        then:
            Exception exception = thrown();
            exception.message == "notValid.installment.payDate"
    }
}
