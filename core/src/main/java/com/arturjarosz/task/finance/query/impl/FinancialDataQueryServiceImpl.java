package com.arturjarosz.task.finance.query.impl;

import com.arturjarosz.task.finance.domain.dto.FinancialDataDto;
import com.arturjarosz.task.finance.model.QFinancialData;
import com.arturjarosz.task.finance.model.dto.SupervisionRatesDto;
import com.arturjarosz.task.finance.model.dto.SupervisionVisitFinancialDto;
import com.arturjarosz.task.finance.query.FinancialDataQueryService;
import com.arturjarosz.task.project.model.QCost;
import com.arturjarosz.task.project.model.QProject;
import com.arturjarosz.task.sharedkernel.annotations.Finder;
import com.arturjarosz.task.sharedkernel.infrastructure.AbstractQueryService;
import com.arturjarosz.task.supervision.model.QSupervision;
import com.arturjarosz.task.supervision.model.QSupervisionVisit;
import com.querydsl.core.types.Projections;

import java.util.List;

@Finder
public class FinancialDataQueryServiceImpl extends AbstractQueryService<QFinancialData> implements FinancialDataQueryService {
    private static final QCost COST = QCost.cost;
    private static final QFinancialData FINANCIAL_DATA = QFinancialData.financialData;
    private static final QProject PROJECT = QProject.project;
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

    @Override
    public List<FinancialDataDto> getCostsFinancialData(long projectId) {
        return this.query().from(PROJECT)
                .join(PROJECT.costs, COST)
                .join(COST.financialData, FINANCIAL_DATA)
                .where(PROJECT.id.eq(projectId))
                .select(Projections.bean(FinancialDataDto.class,
                        FINANCIAL_DATA.payable.as(FinancialDataDto.PAYABLE),
                        FINANCIAL_DATA.hasInvoice.as(FinancialDataDto.HAS_INVOICE),
                        FINANCIAL_DATA.paid.as(FinancialDataDto.PAID),
                        FINANCIAL_DATA.value.value.as(FinancialDataDto.VALUE)))
                .fetch();
    }
}
