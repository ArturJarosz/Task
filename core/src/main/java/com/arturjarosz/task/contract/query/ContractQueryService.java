package com.arturjarosz.task.contract.query;

import java.math.BigDecimal;
import java.util.Map;

public interface ContractQueryService {
    Map<Long, BigDecimal> getContractValuesForProjectsByClientId(long clientId);
}
