package com.arturjarosz.task.finance.application.impl;

import com.arturjarosz.task.configuration.UserProperties;
import com.arturjarosz.task.dto.FinancialPartialDataDto;
import com.arturjarosz.task.dto.FinancialReportCategoryGroupDto;
import com.arturjarosz.task.dto.FinancialReportDto;
import com.arturjarosz.task.dto.FinancialReportPeriodDto;
import com.arturjarosz.task.dto.PeriodTypeDto;
import com.arturjarosz.task.finance.application.DatePeriod;
import com.arturjarosz.task.finance.application.FinancialReportApplicationService;
import com.arturjarosz.task.finance.application.FinancialReportValidator;
import com.arturjarosz.task.finance.application.PeriodCalculator;
import com.arturjarosz.task.finance.application.TaxCalculator;
import com.arturjarosz.task.finance.application.dto.FinancialValueDto;
import com.arturjarosz.task.finance.domain.dto.FinancialDataDto;
import com.arturjarosz.task.finance.domain.dto.FinancialReportItemDto;
import com.arturjarosz.task.finance.query.FinancialDataQueryService;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@ApplicationService
@Transactional(readOnly = true)
public class FinancialReportApplicationServiceImpl implements FinancialReportApplicationService {

    @NonNull
    private final FinancialDataQueryService financialDataQueryService;
    @NonNull
    private final UserProperties userProperties;
    @NonNull
    private final FinancialReportValidator financialReportValidator;

    @Override
    public FinancialReportDto getFinancialReport(LocalDate startDate, LocalDate endDate, PeriodTypeDto periodType) {
        this.financialReportValidator.validateFinancialReportParameters(startDate, endDate, periodType);

        var costExpenseItems = this.financialDataQueryService.getCostExpenseItems(startDate, endDate);
        var costIncomeItems = this.financialDataQueryService.getCostIncomeItems(startDate, endDate);
        var installmentIncomeItems = this.financialDataQueryService.getInstallmentIncomeItems(startDate, endDate);
        var supervisionIncomeItems = this.financialDataQueryService.getSupervisionIncomeItems(startDate, endDate);
        var supplyIncomeItems = this.financialDataQueryService.getSupplyIncomeItems(startDate, endDate);
        var contractorJobExpenseItems = this.financialDataQueryService.getContractorJobExpenseItems(startDate, endDate);

        var periods = PeriodCalculator.calculatePeriods(startDate, endDate, periodType);

        var totalIncome = new FinancialValueDto();
        var totalExpense = new FinancialValueDto();
        int totalIncomeCount = 0;
        int totalExpenseCount = 0;

        List<FinancialReportPeriodDto> periodDtos = new ArrayList<>();

        for (DatePeriod period : periods) {
            var periodIncome = new CategoryAccumulator();
            var periodExpense = new CategoryAccumulator();

            this.accumulateItems(filterByPeriod(installmentIncomeItems, period), periodIncome.installment);
            this.accumulateItems(filterByPeriod(supervisionIncomeItems, period), periodIncome.supervision);
            this.accumulateItems(filterByPeriod(costIncomeItems, period), periodIncome.cost);

            this.accumulateItems(filterByPeriod(costExpenseItems, period), periodExpense.cost);
            this.accumulateItems(filterByPeriod(supplyIncomeItems, period), periodIncome.supply);
            this.accumulateItems(filterByPeriod(contractorJobExpenseItems, period), periodExpense.contractorJob);

            var incomeTotal = sumAccumulators(periodIncome);
            var expenseTotal = sumAccumulators(periodExpense);

            var balance = new FinancialValueDto();
            balance.addValues(incomeTotal.financialValue);
            balance.subtractValues(expenseTotal.financialValue);

            var incomeGroup = buildCategoryGroup(periodIncome, incomeTotal);
            var expenseGroup = buildCategoryGroup(periodExpense, expenseTotal);

            periodDtos.add(FinancialReportPeriodDto.builder()
                    .startDate(period.startDate())
                    .endDate(period.endDate())
                    .income(incomeGroup)
                    .expense(expenseGroup)
                    .balance(toPartialDataDto(balance, incomeTotal.count + expenseTotal.count))
                    .build());

            totalIncome.addValues(incomeTotal.financialValue);
            totalExpense.addValues(expenseTotal.financialValue);
            totalIncomeCount += incomeTotal.count;
            totalExpenseCount += expenseTotal.count;
        }

        var totalBalance = new FinancialValueDto();
        totalBalance.addValues(totalIncome);
        totalBalance.subtractValues(totalExpense);

        return FinancialReportDto.builder()
                .startDate(startDate)
                .endDate(endDate)
                .periodType(periodType)
                .periods(periodDtos)
                .totalIncome(toPartialDataDto(totalIncome, totalIncomeCount))
                .totalExpense(toPartialDataDto(totalExpense, totalExpenseCount))
                .totalBalance(toPartialDataDto(totalBalance, totalIncomeCount + totalExpenseCount))
                .build();
    }

    private List<FinancialReportItemDto> filterByPeriod(List<FinancialReportItemDto> items, DatePeriod period) {
        return items.stream()
                .filter(item -> !item.getEffectiveDate().isBefore(period.startDate())
                        && !item.getEffectiveDate().isAfter(period.endDate()))
                .toList();
    }

    private void accumulateItems(List<FinancialReportItemDto> items, CategoryValue categoryValue) {
        for (FinancialReportItemDto item : items) {
            var financialDataDto = new FinancialDataDto();
            financialDataDto.setValue(item.getValue());
            financialDataDto.setHasInvoice(item.isHasInvoice());
            financialDataDto.setPayable(item.isPayable());
            financialDataDto.setPaid(item.isPaid());

            var taxed = TaxCalculator.recalculateObjectTaxes(financialDataDto, this.userProperties);
            categoryValue.financialValue.addValues(taxed);
            categoryValue.count++;
        }
    }

    private AccumulatorTotal sumAccumulators(CategoryAccumulator accumulator) {
        var total = new FinancialValueDto();
        int count = 0;
        for (CategoryValue cv : List.of(accumulator.installment, accumulator.supervision,
                accumulator.cost, accumulator.supply, accumulator.contractorJob)) {
            total.addValues(cv.financialValue);
            count += cv.count;
        }
        return new AccumulatorTotal(total, count);
    }

    private FinancialReportCategoryGroupDto buildCategoryGroup(CategoryAccumulator accumulator,
            AccumulatorTotal total) {
        return FinancialReportCategoryGroupDto.builder()
                .installment(toPartialDataDto(accumulator.installment))
                .supervision(toPartialDataDto(accumulator.supervision))
                .cost(toPartialDataDto(accumulator.cost))
                .supply(toPartialDataDto(accumulator.supply))
                .contractorJob(toPartialDataDto(accumulator.contractorJob))
                .total(toPartialDataDto(total.financialValue, total.count))
                .build();
    }

    private FinancialPartialDataDto toPartialDataDto(CategoryValue categoryValue) {
        return toPartialDataDto(categoryValue.financialValue, categoryValue.count);
    }

    private FinancialPartialDataDto toPartialDataDto(FinancialValueDto financialValue, int count) {
        return FinancialPartialDataDto.builder()
                .count(count)
                .netValue(financialValue.getNetValue().doubleValue())
                .grossValue(financialValue.getGrossValue().doubleValue())
                .vatTax(financialValue.getVatTax().doubleValue())
                .incomeTax(financialValue.getIncomeTax().doubleValue())
                .build();
    }

    private static class CategoryValue {
        FinancialValueDto financialValue = new FinancialValueDto();
        int count = 0;
    }

    private static class CategoryAccumulator {
        CategoryValue installment = new CategoryValue();
        CategoryValue supervision = new CategoryValue();
        CategoryValue cost = new CategoryValue();
        CategoryValue supply = new CategoryValue();
        CategoryValue contractorJob = new CategoryValue();
    }

    private record AccumulatorTotal(FinancialValueDto financialValue, int count) {
    }
}
