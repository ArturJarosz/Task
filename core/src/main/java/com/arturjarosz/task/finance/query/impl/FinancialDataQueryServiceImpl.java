package com.arturjarosz.task.finance.query.impl;

import com.arturjarosz.task.finance.model.QFinancialData;
import com.arturjarosz.task.finance.query.FinancialDataQueryService;
import com.arturjarosz.task.finance.model.dto.SupervisionRatesDto;
import com.arturjarosz.task.finance.model.dto.SupervisionVisitFinancialDto;
import com.arturjarosz.task.sharedkernel.annotations.Finder;
import com.arturjarosz.task.sharedkernel.infrastructure.AbstractQueryService;
import com.arturjarosz.task.supervision.model.QSupervision;
import com.arturjarosz.task.supervision.model.QSupervisionVisit;
import com.querydsl.core.types.Projections;

import java.util.List;

@Finder
public class FinancialDataQueryServiceImpl extends AbstractQueryService<QFinancialData> implements FinancialDataQueryService {
    private static final QFinancialData FINANCIAL_DATA = QFinancialData.financialData;
    private static final QSupervision SUPERVISION = QSupervision.supervision;
    private static final QSupervisionVisit SUPERVISION_VISIT = QSupervisionVisit.supervisionVisit;

    public FinancialDataQueryServiceImpl() {
        super(FINANCIAL_DATA);
    }

    @Override
    public SupervisionRatesDto getSupervisionRatesDto(long supervisionId) {
        return this.query().from(SUPERVISION)
                .where(SUPERVISION.id.eq(supervisionId))
                .select(Projections.bean(SupervisionRatesDto.class,
                        SUPERVISION.baseNetRate.value.as(SupervisionRatesDto.BASE_NET_RATE),
                        SUPERVISION.hourlyNetRate.value.as(SupervisionRatesDto.HOURLY_NET_RATE),
                        SUPERVISION.visitNetRate.value.as(SupervisionRatesDto.VISIT_NET_RATE)
                )).fetchOne();
    }

    @Override
    public List<SupervisionVisitFinancialDto> getVisitsFinancialDto(Long supervisionId) {
        return this.query().from(SUPERVISION)
                .where(SUPERVISION.id.eq(supervisionId))
                .join(SUPERVISION.supervisionVisits, SUPERVISION_VISIT)
                .select(Projections.bean(SupervisionVisitFinancialDto.class,
                        SUPERVISION_VISIT.payable.as(SupervisionVisitFinancialDto.PAYABLE),
                        SUPERVISION_VISIT.hoursCount.as(SupervisionVisitFinancialDto.HOURS_COUNT)))
                .fetch();
    }
}
