package com.arturjarosz.task.contract.query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ContractQueryService {
    Map<Long, BigDecimal> getContractValuesForProjectsByClientId(long clientId);

    Map<Long, BigDecimal> getContractValuesForProjects(List<Long> projectIds);
}
