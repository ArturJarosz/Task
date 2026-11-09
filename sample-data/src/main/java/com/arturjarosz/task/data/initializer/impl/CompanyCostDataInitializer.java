package com.arturjarosz.task.data.initializer.impl;

import com.arturjarosz.task.companycost.application.CompanyCostApplicationService;
import com.arturjarosz.task.companycost.domain.ChargeSchedule;
import com.arturjarosz.task.companycost.infrastructure.CompanyCostCategoryRepository;
import com.arturjarosz.task.companycost.model.CompanyCostCategory;
import com.arturjarosz.task.data.initializer.DataInitializer;
import com.arturjarosz.task.data.initializer.SampleDataFileReader;
import com.arturjarosz.task.data.model.CompanyCostSample;
import com.arturjarosz.task.dto.CompanyCostAmountDto;
import com.arturjarosz.task.dto.CompanyCostChargeDto;
import com.arturjarosz.task.dto.CompanyCostDto;
import com.arturjarosz.task.dto.CompanyCostKindDto;
import com.arturjarosz.task.dto.CompanyCostPaymentDto;
import com.arturjarosz.task.exception.SampleDataInitializingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Loads company costs with their amounts and payments. Sample costs reference their category by name, so categories
 * have to be loaded first. Payments are added after the amounts, so each charge is paid with the amount valid in its
 * month.
 */
@Slf4j
@Component
@Order(13)
@RequiredArgsConstructor
public class CompanyCostDataInitializer implements DataInitializer {
    private static final String FILE_NAME = "sampleData/companyCostsSample.json";
    private final CompanyCostApplicationService companyCostApplicationService;
    private final CompanyCostCategoryRepository companyCostCategoryRepository;

    @Override
    public void initializeData() {
        LOG.info("Starting importing company costs.");
        // When a name is not unique (e.g. data loaded more than once), the category with the lowest id is used
        Map<String, Long> categoryIdsByName = this.companyCostCategoryRepository.findAll()
                .stream()
                .collect(Collectors.toMap(CompanyCostCategory::getName, CompanyCostCategory::getId, Math::min));
        var today = ChargeSchedule.today();
        this.prepareCompanyCosts().forEach(sample -> this.createCompanyCost(sample,
                getCategoryId(categoryIdsByName, sample.getCategory()), today));
        LOG.info("Company costs added to the database.");
    }

    private void createCompanyCost(CompanyCostSample sample, Long categoryId, LocalDate today) {
        var thisMonth = today.withDayOfMonth(1);
        var companyCostDto = new CompanyCostDto()
                .name(sample.getName())
                .categoryId(categoryId)
                .kind(sample.getKind())
                .netValue(sample.getNetValue())
                .hasInvoice(sample.isHasInvoice())
                .note(sample.getNote());
        if (sample.getKind() == CompanyCostKindDto.ONE_TIME) {
            companyCostDto.date(today.minusDays(sample.getDateDaysAgo()))
                    .paid(sample.getPaymentDaysAgo() != null)
                    .paymentDate(sample.getPaymentDaysAgo() == null ? null
                            : today.minusDays(sample.getPaymentDaysAgo()));
            this.companyCostApplicationService.createCompanyCost(companyCostDto);
            return;
        }
        companyCostDto.repeats(sample.getRepeats())
                .startMonth(thisMonth.minusMonths(sample.getStartMonthsAgo()))
                .endMonth(sample.getEndMonthsAgo() == null ? null : thisMonth.minusMonths(sample.getEndMonthsAgo()))
                .chargeDay(sample.getChargeDay());
        var companyCostId = this.companyCostApplicationService.createCompanyCost(companyCostDto).getId();
        sample.getAmounts().forEach(amount -> this.companyCostApplicationService.createAmount(companyCostId,
                new CompanyCostAmountDto()
                        .validFrom(thisMonth.minusMonths(amount.getFromMonthsAgo()))
                        .netValue(amount.getNetValue())));
        this.payPastCharges(companyCostId, sample, companyCostDto.getStartMonth().getYear(), today);
    }

    private void payPastCharges(Long companyCostId, CompanyCostSample sample, int startYear, LocalDate today) {
        // overdue: due before today and not paid
        var overdueCharges = IntStream.rangeClosed(startYear, today.getYear())
                .boxed()
                .flatMap(year -> this.companyCostApplicationService.getCharges(companyCostId, year).stream())
                .filter(CompanyCostChargeDto::getOverdue)
                .sorted(Comparator.comparing(CompanyCostChargeDto::getChargeMonth))
                .toList();
        overdueCharges.stream()
                .limit(Math.max(overdueCharges.size() - sample.getUnpaidCharges(), 0))
                .forEach(charge -> this.companyCostApplicationService.createPayment(companyCostId,
                        new CompanyCostPaymentDto()
                                .chargeMonth(charge.getChargeMonth())
                                .paymentDate(min(charge.getChargeDate().plusDays(sample.getPaymentDelayDays()),
                                        today))));
    }

    private static LocalDate min(LocalDate date, LocalDate other) {
        return date.isBefore(other) ? date : other;
    }

    private static Long getCategoryId(Map<String, Long> categoryIdsByName, String categoryName) {
        var categoryId = categoryIdsByName.get(categoryName);
        if (categoryId == null) {
            throw new SampleDataInitializingException(
                    String.format("Company cost category %1$s does not exist", categoryName), null);
        }
        return categoryId;
    }

    private List<CompanyCostSample> prepareCompanyCosts() {
        return SampleDataFileReader.readList(FILE_NAME, CompanyCostSample.class);
    }
}
