package com.arturjarosz.task.supervision.query.impl;

import com.arturjarosz.task.dto.SupervisionVisitDto;
import com.arturjarosz.task.sharedkernel.annotations.Finder;
import com.arturjarosz.task.sharedkernel.infrastructure.AbstractQueryService;
import com.arturjarosz.task.supervision.application.mapper.SupervisionVisitFields;
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

    @Override
    public boolean supervisionExists(Long supervisionId) {
        return this.queryFromAggregate().where(SUPERVISION.id.eq(supervisionId)).fetchOne() != null;
    }

    @Override
    public boolean supervisionVisitExistsInSupervision(Long supervisionId, Long supervisionVisitId) {
        return this.query().from(SUPERVISION)
                .join(SUPERVISION.supervisionVisits, SUPERVISION_VISIT)
                .where(SUPERVISION.id.eq(supervisionId).and(SUPERVISION_VISIT.id.eq(supervisionVisitId)))
                .select(SUPERVISION_VISIT)
                .fetchOne() != null;
    }

    @Override
    public SupervisionVisitDto getSupervisionVisit(Long supervisionVisitId) {
        return this.query().from(SUPERVISION_VISIT).where(SUPERVISION_VISIT.id.eq(supervisionVisitId))
                .select(Projections.bean(SupervisionVisitDto.class,
                        SUPERVISION_VISIT.id.as(SupervisionVisitFields.ID),
                        SUPERVISION_VISIT.hoursCount.as(SupervisionVisitFields.HOURS_COUNT),
                        SUPERVISION_VISIT.dateOfVisit.as(SupervisionVisitFields.DATE_OF_VISIT),
                        SUPERVISION_VISIT.payable.as(SupervisionVisitFields.PAYABLE)
                )).fetchOne();
    }

    @Override
    public long getProjectIdForSupervision(Long supervisionId) {
        return this.query()
                .from(SUPERVISION)
                .where(SUPERVISION.id.eq(supervisionId))
                .select(SUPERVISION.projectId)
                .fetchOne();
    }

    @Override
    public boolean supervisionOnProjectExistence(Long projectId) {
        return this.query()
                .from(SUPERVISION)
                .where(SUPERVISION.projectId.eq(projectId))
                .select(SUPERVISION)
                .fetchOne() != null;
    }
}
