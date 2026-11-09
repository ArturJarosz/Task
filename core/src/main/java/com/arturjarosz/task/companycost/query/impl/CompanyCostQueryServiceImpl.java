package com.arturjarosz.task.companycost.query.impl;

import com.arturjarosz.task.companycost.model.QCompanyCost;
import com.arturjarosz.task.companycost.model.QCompanyCostPayment;
import com.arturjarosz.task.companycost.query.CompanyCostQueryService;
import com.arturjarosz.task.finance.domain.dto.FinancialReportItemDto;
import com.arturjarosz.task.sharedkernel.annotations.Finder;
import com.arturjarosz.task.sharedkernel.infrastructure.AbstractQueryService;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Finder
public class CompanyCostQueryServiceImpl extends AbstractQueryService<QCompanyCost> implements CompanyCostQueryService {

    private static final QCompanyCost COMPANY_COST = QCompanyCost.companyCost;
    private static final QCompanyCostPayment PAYMENT = QCompanyCostPayment.companyCostPayment;

    public CompanyCostQueryServiceImpl() {
        super(COMPANY_COST);
    }

    @Override
    public Map<Long, Long> getNumberOfCompanyCostsByCategoryIds(Collection<Long> categoryIds) {
        Map<Long, Long> countsByCategoryId = new HashMap<>();
        categoryIds.stream().filter(Objects::nonNull).forEach(id -> countsByCategoryId.put(id, 0L));
        if (countsByCategoryId.isEmpty()) {
            return countsByCategoryId;
        }
        List<Tuple> counts = this.query()
                .select(COMPANY_COST.categoryId, COMPANY_COST.id.count())
                .from(COMPANY_COST)
                .where(COMPANY_COST.categoryId.in(countsByCategoryId.keySet()))
                .groupBy(COMPANY_COST.categoryId)
                .fetch();
        counts.forEach(tuple -> countsByCategoryId.put(tuple.get(COMPANY_COST.categoryId),
                tuple.get(COMPANY_COST.id.count())));
        return countsByCategoryId;
    }

    @Override
    public List<FinancialReportItemDto> getPaymentReportItems(LocalDate startDate, LocalDate endDate) {
        return this.query()
                .from(COMPANY_COST)
                .join(COMPANY_COST.payments, PAYMENT)
                .where(PAYMENT.paymentDate.between(startDate, endDate))
                .select(Projections.bean(FinancialReportItemDto.class,
                        PAYMENT.netValue.value.as(FinancialReportItemDto.VALUE_FIELD),
                        COMPANY_COST.hasInvoice.as(FinancialReportItemDto.HAS_INVOICE_FIELD),
                        PAYMENT.paymentDate.as(FinancialReportItemDto.EFFECTIVE_DATE_FIELD)))
                .fetch();
    }
}
