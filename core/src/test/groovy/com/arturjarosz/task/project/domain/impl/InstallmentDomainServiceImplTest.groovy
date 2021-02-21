package com.arturjarosz.task.project.domain.impl

import com.arturjarosz.task.project.model.Installment
import com.arturjarosz.task.project.model.Stage
import com.arturjarosz.task.project.model.StageType
import com.arturjarosz.task.project.utils.InstallmentBuilder
import com.arturjarosz.task.sharedkernel.model.Money
import com.arturjarosz.task.sharedkernel.utils.TestUtils
import spock.lang.Specification

import java.time.LocalDate

class InstallmentDomainServiceImplTest extends Specification {

    private static final String STAGE_NAME = "stage";
    private static final StageType STAGE_TYPE = StageType.AUTHORS_SUPERVISION;
    private static final Double OLD_AMOUNT = 1.00D;
    private static final Double NEW_AMOUNT = 2.00D;
    private static final LocalDate OLD_DATE = LocalDate.now().minusDays(10);
    private static final LocalDate NEW_DATE = LocalDate.now();
    private static final LocalDate FUTURE_DATE = LocalDate.now().plusDays(10);
    private static final String NEW_NOTE = "some note";
    private static final String OLD_NOTE = "old note";

    def installmentDomainService = new InstallmentDomainServiceImpl();

    def "updateInstallment should update value and note when updating not paid installment"() {
        given:
            Stage stage = new Stage(STAGE_NAME, STAGE_TYPE);
            Installment installment = new InstallmentBuilder()
                    .withAmount(new Money(OLD_AMOUNT))
                    .withNote(OLD_NOTE)
                    .withPayDate(null)
                    .withIsPaid(false)
                    .build();
            stage.setInstallment(installment);
        when:
            this.installmentDomainService.updateInstallment(stage, NEW_AMOUNT, OLD_DATE, NEW_NOTE);
        then:
            String note = (String) TestUtils.getFieldValue(installment, "note");
            note == NEW_NOTE;
            Double value = ((Money) TestUtils.getFieldValue(installment, "amount")).value.doubleValue();
            value == NEW_AMOUNT;
            LocalDate date = (LocalDate) TestUtils.getFieldValue(installment, "paymentDate");
            date == null;
    }

    def "updateInstallment should update value, note and date when updating paid installment"() {
        given:
            Stage stage = new Stage(STAGE_NAME, STAGE_TYPE);
            Installment installment = new InstallmentBuilder()
                    .withAmount(new Money(OLD_AMOUNT))
                    .withNote(OLD_NOTE)
                    .withPayDate(OLD_DATE)
                    .withIsPaid(true)
                    .build();
            stage.setInstallment(installment);
        when:
            this.installmentDomainService.updateInstallment(stage, NEW_AMOUNT, NEW_DATE, NEW_NOTE);
        then:
            String note = (String) TestUtils.getFieldValue(installment, "note");
            note == NEW_NOTE;
            Double value = ((Money) TestUtils.getFieldValue(installment, "amount")).value.doubleValue();
            value == NEW_AMOUNT;
            LocalDate date = (LocalDate) TestUtils.getFieldValue(installment, "paymentDate");
            date.isEqual(NEW_DATE);
    }

    def "payForInstallment should not change installment isPaid to true and throw an exception if dto date is in future"() {
        given:
            Stage stage = new Stage(STAGE_NAME, STAGE_TYPE);
            Installment installment = new InstallmentBuilder()
                    .withAmount(new Money(OLD_AMOUNT))
                    .withIsPaid(false)
                    .build();
            stage.setInstallment(installment);
        when:
            this.installmentDomainService.payForInstallment(stage, FUTURE_DATE);
        then:
            Exception exception = thrown();
            exception.message == "notValid.installment.payDate";
            Boolean isPaid = (boolean) TestUtils.getFieldValue(installment, "isPaid");
            isPaid == false;
    }

    def "payForInstallment should throw an exception if installment is already paid"() {
        given:
            Stage stage = new Stage(STAGE_NAME, STAGE_TYPE);
            Installment installment = new InstallmentBuilder()
                    .withAmount(new Money(OLD_AMOUNT))
                    .withIsPaid(true)
                    .build();
            stage.setInstallment(installment);
        when:
            this.installmentDomainService.payForInstallment(stage, NEW_DATE);
        then:
            Exception exception = thrown();
            exception.message == "notValid.installment.isPaid";
    }

    def "payForInstallment should set payment day for today is date is not given and set isPaid to true"() {
        given:
            Stage stage = new Stage(STAGE_NAME, STAGE_TYPE);
            Installment installment = new InstallmentBuilder()
                    .withAmount(new Money(OLD_AMOUNT))
                    .withIsPaid(false)
                    .build();
            stage.setInstallment(installment);
        when:
            this.installmentDomainService.payForInstallment(stage, null);
        then:
            noExceptionThrown();
            LocalDate date = (LocalDate) TestUtils.getFieldValue(installment, "paymentDate");
            date.isEqual(LocalDate.now());
            Boolean isPaid = (boolean) TestUtils.getFieldValue(installment, "isPaid");
            isPaid == true;
    }

    def "payForInstallment should set payment day for given date and set isPaid to true"() {
        given:
            Stage stage = new Stage(STAGE_NAME, STAGE_TYPE);
            Installment installment = new InstallmentBuilder()
                    .withAmount(new Money(OLD_AMOUNT))
                    .withIsPaid(false)
                    .build();
            stage.setInstallment(installment);
        when:
            this.installmentDomainService.payForInstallment(stage, NEW_DATE);
        then:
            noExceptionThrown();
            LocalDate date = (LocalDate) TestUtils.getFieldValue(installment, "paymentDate");
            date.isEqual(NEW_DATE);
            Boolean isPaid = (boolean) TestUtils.getFieldValue(installment, "isPaid");
            isPaid == true;
    }
}
