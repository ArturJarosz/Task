package com.arturjarosz.task.contract.query.impl;

import com.arturjarosz.task.contract.model.QContract;
import com.arturjarosz.task.contract.query.ContractQueryService;
import com.arturjarosz.task.project.model.QProject;
import com.arturjarosz.task.sharedkernel.annotations.Finder;
import com.arturjarosz.task.sharedkernel.infrastructure.AbstractQueryService;
import com.arturjarosz.task.sharedkernel.model.Money;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Finder
public class ContractQueryServiceImpl extends AbstractQueryService<QContract> implements ContractQueryService {

    private static final QContract CONTRACT = QContract.contract;
    private static final QProject PROJECT = QProject.project;

    public ContractQueryServiceImpl() {
        super(CONTRACT);
    }

    @Override
    public Map<Long, BigDecimal> getContractValuesForProjectsByClientId(long clientId) {
        return this.query()
                .from(CONTRACT)
                .join(PROJECT)
                .on(CONTRACT.id.eq(PROJECT.contractId))
                .where(PROJECT.clientId.eq(clientId))
                .select(PROJECT.id, CONTRACT.offerValue)
                .fetch()
                .stream()
                .collect(Collectors.toMap(projectIdAndValue -> projectIdAndValue.get(0, Long.class),
                        projectIdAndValue -> Objects.requireNonNull(projectIdAndValue.get(1, Money.class)).getValue()));
    }

    @Override
    public Map<Long, BigDecimal> getContractValuesForProjects(List<Long> projectIds) {
        return this.query()
                .from(CONTRACT)
                .join(PROJECT)
                .on(CONTRACT.id.eq(PROJECT.contractId))
                .where(PROJECT.id.in(projectIds))
                .select(PROJECT.id, CONTRACT.offerValue)
                .fetch()
                .stream()
                .collect(Collectors.toMap(projectIdAndValue -> projectIdAndValue.get(0, Long.class),
                        projectIdAndValue -> Objects.requireNonNull(projectIdAndValue.get(1, Money.class)).getValue()));
    }
}
