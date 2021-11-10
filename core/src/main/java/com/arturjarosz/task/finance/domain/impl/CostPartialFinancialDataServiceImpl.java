package com.arturjarosz.task.finance.domain.impl;

import com.arturjarosz.task.configuration.UserProperties;
import com.arturjarosz.task.finance.application.dto.FinancialValueDto;
import com.arturjarosz.task.finance.application.dto.ProjectFinancialDataDto;
import com.arturjarosz.task.finance.domain.AbstractPartialFinancialDataService;
import com.arturjarosz.task.finance.domain.PartialFinancialDataService;
import com.arturjarosz.task.finance.domain.dto.FinancialDataDto;
import com.arturjarosz.task.finance.query.FinancialDataQueryService;
import com.arturjarosz.task.sharedkernel.annotations.DomainService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@DomainService
public class CostPartialFinancialDataServiceImpl extends AbstractPartialFinancialDataService implements PartialFinancialDataService {

    private final FinancialDataQueryService financialDataQueryService;
    private final UserProperties userProperties;

    @Autowired
    public CostPartialFinancialDataServiceImpl(FinancialDataQueryService financialDataQueryService,
                                               UserProperties userProperties) {
        super(userProperties);
        this.financialDataQueryService = financialDataQueryService;
        this.userProperties = userProperties;
    }

    @Override
    public ProjectFinancialDataDto providePartialFinancialData(long projectId) {
        List<FinancialDataDto> costsFinancialData = this.financialDataQueryService.getCostsFinancialData(projectId);
        FinancialValueDto costsFinancialValue = new FinancialValueDto();
        costsFinancialValue = this.recalculateFinancialData(costsFinancialValue, costsFinancialData);
        ProjectFinancialDataDto projectFinancialDataDto = new ProjectFinancialDataDto();
        projectFinancialDataDto.setCostsValue(costsFinancialValue);

        return projectFinancialDataDto;
    }

}
