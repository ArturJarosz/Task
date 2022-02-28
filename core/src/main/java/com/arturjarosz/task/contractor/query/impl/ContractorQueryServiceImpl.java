package com.arturjarosz.task.contractor.query.impl;

import com.arturjarosz.task.contractor.model.QContractor;
import com.arturjarosz.task.contractor.query.ContractorQueryService;
import com.arturjarosz.task.sharedkernel.annotations.Finder;
import com.arturjarosz.task.sharedkernel.infrastructure.AbstractQueryService;

@Finder
public class ContractorQueryServiceImpl extends AbstractQueryService<QContractor> implements ContractorQueryService {

    private static final QContractor CONTRACTOR = QContractor.contractor;

    public ContractorQueryServiceImpl() {
        super(CONTRACTOR);
    }

    @Override
    public boolean contractorWithIdExists(long contractorId) {
        return this.query().from(CONTRACTOR).where(CONTRACTOR.id.eq(contractorId)).fetchOne() != null;
    }
}

