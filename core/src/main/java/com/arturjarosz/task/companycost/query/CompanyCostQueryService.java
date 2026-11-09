package com.arturjarosz.task.companycost.query;

import com.arturjarosz.task.finance.domain.dto.FinancialReportItemDto;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface CompanyCostQueryService {

    /**
     * Number of company costs of each of the given category ids, 0 for unused categories.
     */
    Map<Long, Long> getNumberOfCompanyCostsByCategoryIds(Collection<Long> categoryIds);

    /**
     * Payments of company costs with a payment date between startDate and endDate (both included), as financial
     * report items effective on their payment date.
     */
    List<FinancialReportItemDto> getPaymentReportItems(LocalDate startDate, LocalDate endDate);
}
