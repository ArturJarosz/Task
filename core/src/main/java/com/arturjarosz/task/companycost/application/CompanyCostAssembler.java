package com.arturjarosz.task.companycost.application;

import com.arturjarosz.task.companycost.domain.ChargePeriod;
import com.arturjarosz.task.companycost.domain.ChargeSchedule;
import com.arturjarosz.task.companycost.model.CompanyCost;
import com.arturjarosz.task.companycost.model.CompanyCostAmount;
import com.arturjarosz.task.companycost.model.CompanyCostPayment;
import com.arturjarosz.task.dto.CompanyCostAmountDto;
import com.arturjarosz.task.dto.CompanyCostChargeDto;
import com.arturjarosz.task.dto.CompanyCostDto;
import com.arturjarosz.task.dto.CompanyCostKindDto;
import com.arturjarosz.task.dto.CompanyCostPaymentDto;
import com.arturjarosz.task.dto.CompanyCostRepeatsDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Builds company cost dtos with the figures worked out from the charges: the next charge (the oldest one not paid),
 * the overdue charges (not paid and due before today) and the paid totals.
 */
@Component
public class CompanyCostAssembler {

    public CompanyCostDto toDto(CompanyCost companyCost, LocalDate today) {
        return this.toDto(companyCost, today, null);
    }

    /**
     * With a period, the dto also has the figures of the charges due in it and the payments made in it.
     */
    public CompanyCostDto toDto(CompanyCost companyCost, LocalDate today, ChargePeriod period) {
        var schedule = ChargeSchedule.of(companyCost);
        var paymentsByMonth = paymentsByMonth(companyCost);
        var nextChargeMonth = schedule.chargeMonths()
                .filter(month -> !paymentsByMonth.containsKey(month))
                .findFirst();
        var overdueMonths = schedule.chargeMonthsBefore(today)
                .filter(month -> !paymentsByMonth.containsKey(month))
                .toList();
        var amounts = companyCost.getAmounts();

        var dto = CompanyCostDto.builder()
                .id(companyCost.getId())
                .name(companyCost.getName())
                .categoryId(companyCost.getCategoryId())
                .kind(CompanyCostKindDto.valueOf(companyCost.getKind().name()))
                .repeats(companyCost.getRepeats() == null ? null
                        : CompanyCostRepeatsDto.valueOf(companyCost.getRepeats().name()))
                .startMonth(companyCost.getStartMonth())
                .endMonth(companyCost.getEndMonth())
                .chargeDay(companyCost.getChargeDay())
                .date(companyCost.getDate())
                .hasInvoice(companyCost.isHasInvoice())
                .note(companyCost.getNote())
                .netValue(nextChargeMonth.map(schedule::netValue).orElseGet(() -> amounts.getLast().getNetValue()))
                .amounts(companyCost.isRecurring() ? amounts.reversed().stream().map(this::toAmountDto).toList()
                        : new ArrayList<>())
                .nextCharge(nextChargeMonth.map(month -> this.toChargeDto(schedule, month, null, today)).orElse(null))
                .overdueCharges(overdueMonths.size())
                .overdueNetValue(overdueMonths.stream().map(schedule::netValue).reduce(BigDecimal.ZERO,
                        BigDecimal::add))
                .paidThisYear(sum(companyCost, payment -> payment.getPaymentDate().getYear() == today.getYear()))
                .paidToDate(sum(companyCost, payment -> true))
                .createdDateTime(Objects.toString(companyCost.getCreatedDateTime(), null))
                .lastModifiedDateTime(Objects.toString(companyCost.getLastModifiedDateTime(), null))
                .build();
        if (period != null) {
            var periodCharges = schedule.chargeMonthsDueIn(period).stream()
                    .map(month -> this.toChargeDto(schedule, month, paymentsByMonth.get(month), today))
                    .toList();
            var periodOverdue = periodCharges.stream().filter(CompanyCostChargeDto::getOverdue).toList();
            dto.setPeriodCharges(periodCharges.size());
            dto.setPeriodChargesNetValue(sumCharges(periodCharges));
            dto.setPeriodPaidNetValue(sum(companyCost, payment -> period.contains(payment.getPaymentDate())));
            dto.setPeriodOverdueCharges(periodOverdue.size());
            dto.setPeriodOverdueNetValue(sumCharges(periodOverdue));
        }
        return dto;
    }

    private static BigDecimal sumCharges(List<CompanyCostChargeDto> charges) {
        return charges.stream().map(CompanyCostChargeDto::getNetValue).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Charges due in the given year, newest first.
     */
    public List<CompanyCostChargeDto> toChargeDtos(CompanyCost companyCost, int year, LocalDate today) {
        var schedule = ChargeSchedule.of(companyCost);
        var paymentsByMonth = paymentsByMonth(companyCost);
        return schedule.chargeMonthsIn(year).reversed().stream()
                .map(month -> this.toChargeDto(schedule, month, paymentsByMonth.get(month), today))
                .toList();
    }

    public CompanyCostPaymentDto toPaymentDto(CompanyCost companyCost, CompanyCostPayment payment) {
        return CompanyCostPaymentDto.builder()
                .id(payment.getId())
                .chargeMonth(payment.getChargeMonth())
                .chargeDate(ChargeSchedule.of(companyCost).chargeDate(payment.getChargeMonth()))
                .paymentDate(payment.getPaymentDate())
                .netValue(payment.getNetValue())
                .build();
    }

    private CompanyCostChargeDto toChargeDto(ChargeSchedule schedule, LocalDate month, CompanyCostPayment payment,
            LocalDate today) {
        var chargeDate = schedule.chargeDate(month);
        return CompanyCostChargeDto.builder()
                .chargeMonth(month)
                .chargeDate(chargeDate)
                .netValue(payment != null ? payment.getNetValue() : schedule.netValue(month))
                .paid(payment != null)
                .overdue(payment == null && chargeDate.isBefore(today))
                .paymentId(payment != null ? payment.getId() : null)
                .paymentDate(payment != null ? payment.getPaymentDate() : null)
                .build();
    }

    private CompanyCostAmountDto toAmountDto(CompanyCostAmount amount) {
        return CompanyCostAmountDto.builder()
                .id(amount.getId())
                .validFrom(amount.getValidFrom())
                .netValue(amount.getNetValue())
                .build();
    }

    private static Map<LocalDate, CompanyCostPayment> paymentsByMonth(CompanyCost companyCost) {
        return companyCost.getPayments().stream()
                .collect(Collectors.toMap(CompanyCostPayment::getChargeMonth, Function.identity()));
    }

    private static BigDecimal sum(CompanyCost companyCost, Predicate<CompanyCostPayment> filter) {
        return companyCost.getPayments().stream()
                .filter(filter)
                .map(CompanyCostPayment::getNetValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
