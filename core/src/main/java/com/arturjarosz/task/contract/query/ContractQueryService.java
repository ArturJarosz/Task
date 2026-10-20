package com.arturjarosz.task.contract.query;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ContractQueryService {
    Map<Long, BigDecimal> getContractValuesForProjectsByClientId(long clientId);

    Map<Long, BigDecimal> getContractValuesForProjects(List<Long> projectIds);

    /**
     * Sum of contract values of all projects of each Architect from architectIds. Architects without projects get 0.
     */
    Map<Long, BigDecimal> getProjectsValueByArchitectIds(Collection<Long> architectIds);

    /**
     * Sum of contract values of all projects of each Client from clientIds. Clients without projects get 0.
     */
    Map<Long, BigDecimal> getProjectsValueByClientIds(Collection<Long> clientIds);
}
