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

import java.util.List;

@DomainService
public class ContractorJobFinancialDataServiceImpl extends AbstractPartialFinancialDataService implements PartialFinancialDataService {

    private final FinancialDataQueryService financialDataQueryService;

    @Autowired
    public ContractorJobFinancialDataServiceImpl(FinancialDataQueryService financialDataQueryService,
                                                 UserProperties userProperties) {
        super(userProperties);
        this.financialDataQueryService = financialDataQueryService;

    }

    @Override
    public ProjectFinancialSummaryDto providePartialFinancialData(long projectId) {
        List<FinancialDataDto> contractorsJobsFinancialData = this.financialDataQueryService.getContractorsJobsFinancialData(projectId);
        FinancialValueDto contractorsJobsFinancialValue = new FinancialValueDto();
        contractorsJobsFinancialValue = this.recalculateFinancialData(contractorsJobsFinancialValue, contractorsJobsFinancialData);
        ProjectFinancialSummaryDto projectFinancialSummaryDto = new ProjectFinancialSummaryDto();
        projectFinancialSummaryDto.setContractorJobsValue(contractorsJobsFinancialValue);

        return projectFinancialSummaryDto;
    }
}
