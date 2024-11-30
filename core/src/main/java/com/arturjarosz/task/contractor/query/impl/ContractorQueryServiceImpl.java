package com.arturjarosz.task.contractor.query.impl;

import com.arturjarosz.task.contractor.model.QContractor;
import com.arturjarosz.task.contractor.query.ContractorQueryService;
import com.arturjarosz.task.finance.model.QContractorJob;
import com.arturjarosz.task.sharedkernel.annotations.Finder;
import com.arturjarosz.task.sharedkernel.infrastructure.AbstractQueryService;

import java.util.Map;
import java.util.stream.Collectors;

@Finder
public class ContractorQueryServiceImpl extends AbstractQueryService<QContractor> implements ContractorQueryService {

    private static final QContractor CONTRACTOR = QContractor.contractor;
    private static final QContractorJob CONTRACTOR_JOB = QContractorJob.contractorJob;

    public ContractorQueryServiceImpl() {
        super(CONTRACTOR);
    }

    @Override
    public boolean contractorWithIdExists(long contractorId) {
        return this.query().from(CONTRACTOR).where(CONTRACTOR.id.eq(contractorId)).fetchOne() != null;
    }

    @Override
    public Map<Long, Long> getNumberOfJobsPerContractor() {
        return this.query()
                .from(CONTRACTOR)
                .leftJoin(CONTRACTOR_JOB)
                .on(CONTRACTOR.id.eq(CONTRACTOR_JOB.contractorId))
                .select(CONTRACTOR.id, CONTRACTOR_JOB.count())
                .groupBy(CONTRACTOR.id)
                .fetch()
                .stream()
                .collect(Collectors.toMap(contractorIdToCount -> contractorIdToCount.get(CONTRACTOR.id),
                        contractorIdToCount -> contractorIdToCount.get(CONTRACTOR_JOB.count())));
    }
}

