package com.arturjarosz.task.finance.domain.impl;

import com.arturjarosz.task.configuration.UserProperties;
import com.arturjarosz.task.finance.application.dto.FinancialValueDto;
import com.arturjarosz.task.finance.application.dto.ProjectFinancialSummaryDto;
import com.arturjarosz.task.finance.domain.AbstractPartialFinancialDataService;
import com.arturjarosz.task.finance.domain.PartialFinancialDataService;
import com.arturjarosz.task.finance.domain.dto.FinancialDataDto;
import com.arturjarosz.task.finance.query.FinancialDataQueryService;
import com.arturjarosz.task.sharedkernel.annotations.DomainService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;

@DomainService
public class SupervisionFinancialDataServiceImpl extends AbstractPartialFinancialDataService implements PartialFinancialDataService {

    private final FinancialDataQueryService financialDataQueryService;

    @Autowired
    public SupervisionFinancialDataServiceImpl(FinancialDataQueryService financialDataQueryService,
                                               UserProperties userProperties) {
        super(userProperties);
        this.financialDataQueryService = financialDataQueryService;
    }

    @Override
    public ProjectFinancialSummaryDto providePartialFinancialData(long projectId) {
        FinancialDataDto supervisionFinancialData = this.financialDataQueryService.getSupervisionFinancialData(
                projectId);
        FinancialValueDto supervisionFinancialValue = new FinancialValueDto();
        List<FinancialDataDto> supervisionsFinancialData = supervisionFinancialData != null ? Collections.singletonList(
                supervisionFinancialData) : Collections.emptyList();
        supervisionFinancialValue = this.recalculateFinancialData(supervisionFinancialValue, supervisionsFinancialData);
        ProjectFinancialSummaryDto projectFinancialSummaryDto = new ProjectFinancialSummaryDto();
        projectFinancialSummaryDto.setSupervisionValue(supervisionFinancialValue);
        return projectFinancialSummaryDto;
    }
}
