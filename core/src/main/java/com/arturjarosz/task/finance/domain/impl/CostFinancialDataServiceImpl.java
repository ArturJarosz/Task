package com.arturjarosz.task.finance.domain.impl;

import com.arturjarosz.task.configuration.UserProperties;
import com.arturjarosz.task.finance.application.dto.FinancialValueDto;
import com.arturjarosz.task.finance.domain.AbstractPartialFinancialDataService;
import com.arturjarosz.task.finance.domain.PartialFinancialDataService;
import com.arturjarosz.task.finance.domain.SummationStrategy;
import com.arturjarosz.task.finance.domain.dto.FinancialDataDto;
import com.arturjarosz.task.finance.model.Cost;
import com.arturjarosz.task.finance.model.PartialFinancialDataType;
import com.arturjarosz.task.finance.query.FinancialDataQueryService;
import com.arturjarosz.task.sharedkernel.annotations.DomainService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@DomainService
public class CostFinancialDataServiceImpl extends AbstractPartialFinancialDataService implements PartialFinancialDataService<Cost> {

    private final FinancialDataQueryService financialDataQueryService;

    @Autowired
    public CostFinancialDataServiceImpl(FinancialDataQueryService financialDataQueryService,
            UserProperties userProperties) {
        super(userProperties);
        this.financialDataQueryService = financialDataQueryService;
    }

    @Override
    public PartialFinancialDataType getType() {
        return PartialFinancialDataType.COST;
    }

    @Override
    public FinancialValueDto getPartialFinancialData(long projectId) {
        List<FinancialDataDto> costsFinancialData = this.financialDataQueryService.getCostsFinancialData(projectId);
        FinancialValueDto costsFinancialValue = new FinancialValueDto();
        return this.addUpFinancialData(costsFinancialValue, costsFinancialData);
    }

    @Override
    public SummationStrategy getSummationStrategy() {
        return SummationStrategy.SUBTRACT;
    }
}
