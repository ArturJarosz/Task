package com.arturjarosz.task.contractor.query.impl;

import com.arturjarosz.task.contractor.model.QContractor;
import com.arturjarosz.task.contractor.query.ContractorQueryService;
import com.arturjarosz.task.finance.application.mapper.ContractorJobMapper;
import com.arturjarosz.task.finance.application.mapper.FinancialDataMapper;
import com.arturjarosz.task.finance.model.ContractorContractorJobDto;
import com.arturjarosz.task.finance.model.QContractorJob;
import com.arturjarosz.task.finance.model.QProjectFinancialData;
import com.arturjarosz.task.sharedkernel.annotations.Finder;
import com.arturjarosz.task.sharedkernel.infrastructure.AbstractQueryService;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Finder
public class ContractorQueryServiceImpl extends AbstractQueryService<QContractor> implements ContractorQueryService {

    private static final QContractor CONTRACTOR = QContractor.contractor;
    private static final QContractorJob CONTRACTOR_JOB = QContractorJob.contractorJob;
    private static final QProjectFinancialData PROJECT_FINANCIAL_DATA = QProjectFinancialData.projectFinancialData;
    private final ContractorJobMapper contractorJobMapper;
    private final FinancialDataMapper financialDataMapper;

    public ContractorQueryServiceImpl(ContractorJobMapper contractorJobMapper,
            FinancialDataMapper financialDataMapper) {
        super(CONTRACTOR);
        this.contractorJobMapper = contractorJobMapper;
        this.financialDataMapper = financialDataMapper;
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

    @Override
    public Set<ContractorContractorJobDto> getContractorJobsData(long contractorId) {
        return this.query()
                .from(CONTRACTOR_JOB)
                .where(CONTRACTOR_JOB.contractorId.eq(contractorId))
                .leftJoin(PROJECT_FINANCIAL_DATA)
                .on(CONTRACTOR_JOB.projectFinancialDataId.eq(PROJECT_FINANCIAL_DATA.id))
                .select(CONTRACTOR_JOB, PROJECT_FINANCIAL_DATA.projectId)
                .fetch()
                .stream()
                .map(contractorJobAndProjectId -> new ContractorContractorJobDto(
                        this.contractorJobMapper.mapToDto(contractorJobAndProjectId.get(CONTRACTOR_JOB),
                                contractorJobAndProjectId.get(PROJECT_FINANCIAL_DATA.projectId)),
                        this.financialDataMapper.map(
                                Objects.requireNonNull(contractorJobAndProjectId.get(CONTRACTOR_JOB))
                                        .getFinancialData())))
                .collect(Collectors.toSet());
    }
}

