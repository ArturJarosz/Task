package com.arturjarosz.task.companycost.application;

import com.arturjarosz.task.companycost.domain.ChargeSchedule;
import com.arturjarosz.task.companycost.model.CompanyCost;
import com.arturjarosz.task.companycost.model.CompanyCostPayment;
import com.arturjarosz.task.companycost.model.CompanyCostRepeats;
import com.arturjarosz.task.dto.CompanyCostAmountDto;
import com.arturjarosz.task.dto.CompanyCostDto;
import com.arturjarosz.task.dto.CompanyCostKindDto;
import com.arturjarosz.task.dto.CompanyCostPaymentDto;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.arturjarosz.task.companycost.domain.CompanyCostExceptionCodes.AMOUNT;
import static com.arturjarosz.task.companycost.domain.CompanyCostExceptionCodes.CATEGORY;
import static com.arturjarosz.task.companycost.domain.CompanyCostExceptionCodes.CHARGE_DAY;
import static com.arturjarosz.task.companycost.domain.CompanyCostExceptionCodes.CHARGE_MONTH;
import static com.arturjarosz.task.companycost.domain.CompanyCostExceptionCodes.COMPANY_COST;
import static com.arturjarosz.task.companycost.domain.CompanyCostExceptionCodes.DATE;
import static com.arturjarosz.task.companycost.domain.CompanyCostExceptionCodes.END_MONTH;
import static com.arturjarosz.task.companycost.domain.CompanyCostExceptionCodes.FIRST;
import static com.arturjarosz.task.companycost.domain.CompanyCostExceptionCodes.KIND;
import static com.arturjarosz.task.companycost.domain.CompanyCostExceptionCodes.NAME;
import static com.arturjarosz.task.companycost.domain.CompanyCostExceptionCodes.NET_VALUE;
import static com.arturjarosz.task.companycost.domain.CompanyCostExceptionCodes.NOTE;
import static com.arturjarosz.task.companycost.domain.CompanyCostExceptionCodes.ONE_TIME;
import static com.arturjarosz.task.companycost.domain.CompanyCostExceptionCodes.PAID;
import static com.arturjarosz.task.companycost.domain.CompanyCostExceptionCodes.PAID_CHARGES;
import static com.arturjarosz.task.companycost.domain.CompanyCostExceptionCodes.PAYMENT;
import static com.arturjarosz.task.companycost.domain.CompanyCostExceptionCodes.PAYMENT_DATE;
import static com.arturjarosz.task.companycost.domain.CompanyCostExceptionCodes.PERIOD;
import static com.arturjarosz.task.companycost.domain.CompanyCostExceptionCodes.REPEATS;
import static com.arturjarosz.task.companycost.domain.CompanyCostExceptionCodes.START_DATE;
import static com.arturjarosz.task.companycost.domain.CompanyCostExceptionCodes.START_MONTH;
import static com.arturjarosz.task.companycost.domain.CompanyCostExceptionCodes.TOO_LONG;
import static com.arturjarosz.task.companycost.domain.CompanyCostExceptionCodes.UNIQUE;
import static com.arturjarosz.task.companycost.domain.CompanyCostExceptionCodes.VALID_FROM;
import static com.arturjarosz.task.companycost.domain.CompanyCostExceptionCodes.YEAR;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertEntityPresent;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsFalse;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotEmpty;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotNull;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

/**
 * Validates company cost dtos and changes to CompanyCost entities. A change never leaves a paid charge outside the
 * charges of its cost, and a paid charge never changes.
 */
@RequiredArgsConstructor
@Component
public class CompanyCostValidator {

    public static final int MAX_NAME_LENGTH = 255;
    public static final int MAX_NOTE_LENGTH = 255;
    public static final int MAX_CHARGE_DAY = 31;
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("MM.yyyy");

    private final CompanyCostCategoryValidator companyCostCategoryValidator;

    public void validateExistence(Optional<CompanyCost> maybeCompanyCost, Long companyCostId) {
        assertEntityPresent(maybeCompanyCost.isPresent(), createMessageCode(ExceptionCodes.NOT_EXIST, COMPANY_COST),
                companyCostId);
    }

    public void validateCreateDto(CompanyCostDto companyCostDto, LocalDate today) {
        assertNotNull(companyCostDto, createMessageCode(ExceptionCodes.NULL, COMPANY_COST));
        assertNotNull(companyCostDto.getKind(), createMessageCode(ExceptionCodes.NULL, COMPANY_COST, KIND));
        this.validateCommonFields(companyCostDto);
        this.validateNetValue(companyCostDto.getNetValue(), createMessageCode(COMPANY_COST, NET_VALUE));
        if (companyCostDto.getKind() == CompanyCostKindDto.ONE_TIME) {
            this.validateOneTimeDate(companyCostDto.getDate());
            if (Boolean.TRUE.equals(companyCostDto.getPaid())) {
                this.validatePaymentDate(companyCostDto.getPaymentDate(), today,
                        createMessageCode(COMPANY_COST, PAYMENT_DATE));
            }
        } else {
            this.validateRecurringSchedule(companyCostDto);
        }
    }

    public void validateUpdateDto(CompanyCostDto companyCostDto, CompanyCost companyCost) {
        assertNotNull(companyCostDto, createMessageCode(ExceptionCodes.NULL, COMPANY_COST));
        this.validateCommonFields(companyCostDto);
        if (companyCost.isRecurring()) {
            this.validateRecurringSchedule(companyCostDto);
            var schedule = ChargeSchedule.recurring(toRepeats(companyCostDto), companyCostDto.getStartMonth(),
                    companyCostDto.getEndMonth(), companyCostDto.getChargeDay(), companyCost.getAmounts());
            var paidMonthsOutside = companyCost.getPayments().stream()
                    .map(CompanyCostPayment::getChargeMonth)
                    .filter(chargeMonth -> !schedule.hasCharge(chargeMonth))
                    .toList();
            assertIsTrue(paidMonthsOutside.isEmpty(), createMessageCode(ExceptionCodes.NOT_VALID, COMPANY_COST,
                    PAID_CHARGES), formatMonths(paidMonthsOutside));
        } else {
            this.validateNetValue(companyCostDto.getNetValue(), createMessageCode(COMPANY_COST, NET_VALUE));
            this.validateOneTimeDate(companyCostDto.getDate());
            var paid = !companyCost.getPayments().isEmpty();
            var chargeChanged = !CompanyCost.monthOf(companyCostDto.getDate()).equals(
                    CompanyCost.monthOf(companyCost.getDate()))
                    || companyCostDto.getNetValue().compareTo(companyCost.getFirstAmount().getNetValue()) != 0;
            assertIsFalse(paid && chargeChanged, createMessageCode(ExceptionCodes.NOT_VALID, COMPANY_COST, PAID));
        }
    }

    /**
     * A period has both its first and its last day, or neither, and does not end before it starts.
     */
    public void validatePeriod(LocalDate startDate, LocalDate endDate) {
        assertIsTrue((startDate == null) == (endDate == null),
                createMessageCode(ExceptionCodes.NOT_VALID, COMPANY_COST, PERIOD));
        assertIsTrue(startDate == null || !startDate.isAfter(endDate),
                createMessageCode(ExceptionCodes.NOT_VALID, COMPANY_COST, PERIOD, START_DATE));
    }

    public void validateChargesYear(Integer year) {
        assertNotNull(year, createMessageCode(ExceptionCodes.NULL, COMPANY_COST, YEAR));
    }

    public void validateNewAmountDto(CompanyCostAmountDto amountDto, CompanyCost companyCost) {
        this.validateAmountsCanChange(companyCost);
        this.validateAmountDto(amountDto, companyCost);
    }

    public void validateUpdatedAmountDto(CompanyCostAmountDto amountDto, CompanyCost companyCost, Long amountId) {
        this.validateAmountsCanChange(companyCost);
        this.validateAmountExistence(companyCost, amountId);
        this.validateAmountDto(amountDto, companyCost);
        var month = CompanyCost.monthOf(amountDto.getValidFrom());
        var isFirst = companyCost.getFirstAmount().getId().equals(amountId);
        assertIsFalse(isFirst && !month.equals(companyCost.getStartMonth()),
                createMessageCode(ExceptionCodes.NOT_VALID, COMPANY_COST, AMOUNT, FIRST));
        var monthTaken = companyCost.getAmounts().stream()
                .anyMatch(amount -> !amount.getId().equals(amountId) && amount.getValidFrom().equals(month));
        assertIsFalse(monthTaken, createMessageCode(ExceptionCodes.NOT_VALID, COMPANY_COST, AMOUNT, VALID_FROM,
                UNIQUE), month.format(MONTH_FORMAT));
    }

    public void validateAmountRemoval(CompanyCost companyCost, Long amountId) {
        this.validateAmountsCanChange(companyCost);
        this.validateAmountExistence(companyCost, amountId);
        assertIsFalse(companyCost.getFirstAmount().getId().equals(amountId),
                createMessageCode(ExceptionCodes.NOT_VALID, COMPANY_COST, AMOUNT, FIRST));
    }

    public void validateNewPaymentDto(CompanyCostPaymentDto paymentDto, CompanyCost companyCost, LocalDate today) {
        assertNotNull(paymentDto, createMessageCode(ExceptionCodes.NULL, COMPANY_COST, PAYMENT));
        assertNotNull(paymentDto.getChargeMonth(), createMessageCode(ExceptionCodes.NULL, COMPANY_COST, PAYMENT,
                CHARGE_MONTH));
        var month = CompanyCost.monthOf(paymentDto.getChargeMonth());
        assertIsTrue(ChargeSchedule.of(companyCost).hasCharge(month),
                createMessageCode(ExceptionCodes.NOT_VALID, COMPANY_COST, PAYMENT, CHARGE_MONTH),
                month.format(MONTH_FORMAT));
        assertIsFalse(companyCost.findPaymentOfCharge(month).isPresent(),
                createMessageCode(ExceptionCodes.NOT_VALID, COMPANY_COST, PAYMENT, CHARGE_MONTH, PAID),
                month.format(MONTH_FORMAT));
        this.validatePaymentDate(paymentDto.getPaymentDate(), today,
                createMessageCode(COMPANY_COST, PAYMENT, PAYMENT_DATE));
    }

    public void validateUpdatedPaymentDto(CompanyCostPaymentDto paymentDto, CompanyCost companyCost, Long paymentId,
            LocalDate today) {
        this.validatePaymentExistence(companyCost, paymentId);
        assertNotNull(paymentDto, createMessageCode(ExceptionCodes.NULL, COMPANY_COST, PAYMENT));
        this.validatePaymentDate(paymentDto.getPaymentDate(), today,
                createMessageCode(COMPANY_COST, PAYMENT, PAYMENT_DATE));
    }

    public void validatePaymentExistence(CompanyCost companyCost, Long paymentId) {
        assertEntityPresent(companyCost.findPayment(paymentId).isPresent(),
                createMessageCode(ExceptionCodes.NOT_EXIST, COMPANY_COST, PAYMENT), companyCost.getId(), paymentId);
    }

    private void validateCommonFields(CompanyCostDto companyCostDto) {
        assertNotNull(companyCostDto.getName(), createMessageCode(ExceptionCodes.NULL, COMPANY_COST, NAME));
        var name = companyCostDto.getName().strip();
        assertNotEmpty(name, createMessageCode(ExceptionCodes.EMPTY, COMPANY_COST, NAME));
        assertIsTrue(name.length() <= MAX_NAME_LENGTH,
                createMessageCode(ExceptionCodes.NOT_VALID, COMPANY_COST, NAME, TOO_LONG), MAX_NAME_LENGTH);
        var note = companyCostDto.getNote();
        assertIsTrue(note == null || note.strip().length() <= MAX_NOTE_LENGTH,
                createMessageCode(ExceptionCodes.NOT_VALID, COMPANY_COST, NOTE, TOO_LONG), MAX_NOTE_LENGTH);
        assertNotNull(companyCostDto.getCategoryId(), createMessageCode(ExceptionCodes.NULL, COMPANY_COST, CATEGORY));
        this.companyCostCategoryValidator.validateExistence(companyCostDto.getCategoryId());
    }

    private void validateOneTimeDate(LocalDate date) {
        assertNotNull(date, createMessageCode(ExceptionCodes.NULL, COMPANY_COST, DATE));
    }

    private void validateRecurringSchedule(CompanyCostDto companyCostDto) {
        assertNotNull(companyCostDto.getRepeats(), createMessageCode(ExceptionCodes.NULL, COMPANY_COST, REPEATS));
        var startMonth = CompanyCost.monthOf(companyCostDto.getStartMonth());
        assertNotNull(startMonth, createMessageCode(ExceptionCodes.NULL, COMPANY_COST, START_MONTH));
        var endMonth = CompanyCost.monthOf(companyCostDto.getEndMonth());
        assertIsTrue(endMonth == null || !endMonth.isBefore(startMonth),
                createMessageCode(ExceptionCodes.NOT_VALID, COMPANY_COST, END_MONTH));
        var chargeDay = companyCostDto.getChargeDay();
        assertNotNull(chargeDay, createMessageCode(ExceptionCodes.NULL, COMPANY_COST, CHARGE_DAY));
        assertIsTrue(chargeDay >= 1 && chargeDay <= MAX_CHARGE_DAY,
                createMessageCode(ExceptionCodes.NOT_VALID, COMPANY_COST, CHARGE_DAY));
    }

    /**
     * fieldCode is the message code of the field without its prefix, e.g. "companyCost.netValue".
     */
    private void validateNetValue(BigDecimal netValue, String fieldCode) {
        assertNotNull(netValue, createMessageCode(ExceptionCodes.NULL, fieldCode));
        assertIsTrue(netValue.signum() > 0, createMessageCode(ExceptionCodes.NOT_VALID, fieldCode));
    }

    /**
     * fieldCode is the message code of the field without its prefix, e.g. "companyCost.paymentDate".
     */
    private void validatePaymentDate(LocalDate paymentDate, LocalDate today, String fieldCode) {
        assertNotNull(paymentDate, createMessageCode(ExceptionCodes.NULL, fieldCode));
        assertIsFalse(paymentDate.isAfter(today), createMessageCode(ExceptionCodes.NOT_VALID, fieldCode));
    }

    private void validateAmountsCanChange(CompanyCost companyCost) {
        assertIsTrue(companyCost.isRecurring(), createMessageCode(ExceptionCodes.NOT_VALID, COMPANY_COST, AMOUNT,
                ONE_TIME));
    }

    private void validateAmountExistence(CompanyCost companyCost, Long amountId) {
        assertEntityPresent(companyCost.findAmount(amountId).isPresent(),
                createMessageCode(ExceptionCodes.NOT_EXIST, COMPANY_COST, AMOUNT), companyCost.getId(), amountId);
    }

    private void validateAmountDto(CompanyCostAmountDto amountDto, CompanyCost companyCost) {
        assertNotNull(amountDto, createMessageCode(ExceptionCodes.NULL, COMPANY_COST, AMOUNT));
        var month = CompanyCost.monthOf(amountDto.getValidFrom());
        assertNotNull(month, createMessageCode(ExceptionCodes.NULL, COMPANY_COST, AMOUNT, VALID_FROM));
        assertIsTrue(!month.isBefore(companyCost.getStartMonth())
                        && (companyCost.getEndMonth() == null || !month.isAfter(companyCost.getEndMonth())),
                createMessageCode(ExceptionCodes.NOT_VALID, COMPANY_COST, AMOUNT, VALID_FROM));
        this.validateNetValue(amountDto.getNetValue(), createMessageCode(COMPANY_COST, AMOUNT, NET_VALUE));
    }

    public static CompanyCostRepeats toRepeats(CompanyCostDto companyCostDto) {
        return CompanyCostRepeats.valueOf(companyCostDto.getRepeats().name());
    }

    private static String formatMonths(Collection<LocalDate> months) {
        return months.stream().map(month -> month.format(MONTH_FORMAT)).collect(Collectors.joining(", "));
    }
}
