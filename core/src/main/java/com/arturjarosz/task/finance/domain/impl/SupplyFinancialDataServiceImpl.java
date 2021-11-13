package com.arturjarosz.task.finance.domain.impl;

import com.arturjarosz.task.configuration.UserProperties;
import com.arturjarosz.task.finance.application.dto.FinancialValueDto;
import com.arturjarosz.task.finance.application.dto.ProjectFinancialDataDto;
import com.arturjarosz.task.finance.domain.AbstractPartialFinancialDataService;
import com.arturjarosz.task.finance.domain.PartialFinancialDataService;
import com.arturjarosz.task.finance.domain.dto.FinancialDataDto;
import com.arturjarosz.task.finance.query.FinancialDataQueryService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class SupplyFinancialDataServiceImpl extends AbstractPartialFinancialDataService implements PartialFinancialDataService {

    private final FinancialDataQueryService financialDataQueryService;

    @Autowired
    public SupplyFinancialDataServiceImpl(FinancialDataQueryService financialDataQueryService,
                                          UserProperties userProperties) {
        super(userProperties);
        this.financialDataQueryService = financialDataQueryService;
    }

    @Override
    public ProjectFinancialDataDto providePartialFinancialData(long projectId) {
        List<FinancialDataDto> supplyFinancialData = this.financialDataQueryService.getSuppliesFinancialData(projectId);
        FinancialValueDto suppliesFinancialValue = new FinancialValueDto();
        suppliesFinancialValue = this.recalculateFinancialData(suppliesFinancialValue, supplyFinancialData);
        ProjectFinancialDataDto projectFinancialDataDto = new ProjectFinancialDataDto();
        projectFinancialDataDto.setSuppliesValue(suppliesFinancialValue);
        return projectFinancialDataDto;
    }
}
