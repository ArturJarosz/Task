package com.arturjarosz.task.cooperator.query.impl;

import com.arturjarosz.task.cooperator.model.CooperatorType;
import com.arturjarosz.task.cooperator.model.QCooperator;
import com.arturjarosz.task.cooperator.query.CooperatorQueryService;
import com.arturjarosz.task.sharedkernel.annotations.Finder;
import com.arturjarosz.task.sharedkernel.infrastructure.AbstractQueryService;

@Finder
public class CooperatorQueryServiceImpl extends AbstractQueryService<QCooperator> implements CooperatorQueryService {

    private static final QCooperator COOPERATOR = QCooperator.cooperator;

    public CooperatorQueryServiceImpl() {
        super(COOPERATOR);
    }

    @Override
    public boolean supplierWithIdExists(Long cooperatorId) {
        return this.query()
                .from(COOPERATOR)
                .where(COOPERATOR.id.eq(cooperatorId).and(COOPERATOR.type.eq(CooperatorType.SUPPLIER)))
                .fetchOne() != null;
    }
}
