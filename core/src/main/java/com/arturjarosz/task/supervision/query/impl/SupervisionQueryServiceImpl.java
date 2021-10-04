package com.arturjarosz.task.supervision.query.impl;

import com.arturjarosz.task.sharedkernel.annotations.Finder;
import com.arturjarosz.task.sharedkernel.infrastructure.AbstractQueryService;
import com.arturjarosz.task.supervision.application.dto.SupervisionVisitDto;
import com.arturjarosz.task.supervision.model.QSupervision;
import com.arturjarosz.task.supervision.model.QSupervisionVisit;
import com.arturjarosz.task.supervision.query.SupervisionQueryService;
import com.querydsl.core.types.Projections;

@Finder
public class SupervisionQueryServiceImpl extends AbstractQueryService<QSupervision> implements SupervisionQueryService {

    private static final QSupervision SUPERVISION = QSupervision.supervision;
    private static final QSupervisionVisit SUPERVISION_VISIT = QSupervisionVisit.supervisionVisit;

    public SupervisionQueryServiceImpl() {
        super(SUPERVISION);
    }

    public boolean supervisionExists(Long supervisionId) {
        return this.queryFromAggregate().where(SUPERVISION.id.eq(supervisionId)).fetchOne() != null;
    }

    @Override
    public boolean supervisionVisitExistsInSupervision(Long supervisionId, Long supervisionVisitId) {
        return this.query().from(SUPERVISION_VISIT)
                .join(SUPERVISION.supervisionVisits, SUPERVISION_VISIT)
                .where(SUPERVISION.id.eq(supervisionId).and(SUPERVISION_VISIT.id.eq(supervisionVisitId)))
                .fetchOne() != null;
    }

    @Override
    public SupervisionVisitDto getSupervisionVisit(Long supervisionVisitId) {
        return this.query().from(SUPERVISION_VISIT).where(SUPERVISION_VISIT.id.eq(supervisionVisitId))
                .select(Projections.bean(SupervisionVisitDto.class,
                        SUPERVISION_VISIT.id.as(SupervisionVisitDto.ID),
                        SUPERVISION_VISIT.hoursCount.as(SupervisionVisitDto.HOURS_COUNT),
                        SUPERVISION_VISIT.dateOfVisit.as(SupervisionVisitDto.DATE_OF_VISIT),
                        SUPERVISION_VISIT.isPayable.as(SupervisionVisitDto.IS_PAYABLE)
                )).fetchOne();
    }
}
