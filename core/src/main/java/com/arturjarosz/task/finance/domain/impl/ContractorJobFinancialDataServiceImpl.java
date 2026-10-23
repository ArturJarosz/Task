package com.arturjarosz.task.finance.domain.impl;

import com.arturjarosz.task.finance.application.dto.FinancialValueDto;
import com.arturjarosz.task.finance.domain.AbstractPartialFinancialDataService;
import com.arturjarosz.task.finance.domain.PartialFinancialDataService;
import com.arturjarosz.task.finance.domain.SummationStrategy;
import com.arturjarosz.task.finance.domain.dto.FinancialDataDto;
import com.arturjarosz.task.finance.model.ContractorJob;
import com.arturjarosz.task.finance.model.PartialFinancialDataType;
import com.arturjarosz.task.finance.query.FinancialDataQueryService;
import com.arturjarosz.task.sharedkernel.annotations.DomainService;
import com.arturjarosz.task.systemparameter.application.TaxRatesProvider;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@DomainService
public class ContractorJobFinancialDataServiceImpl extends AbstractPartialFinancialDataService implements PartialFinancialDataService<ContractorJob> {

    private final FinancialDataQueryService financialDataQueryService;

    @Autowired
    public ContractorJobFinancialDataServiceImpl(FinancialDataQueryService financialDataQueryService,
            TaxRatesProvider taxRatesProvider) {
        super(taxRatesProvider);
        this.financialDataQueryService = financialDataQueryService;

    }

    @Override
    public PartialFinancialDataType getType() {
        return PartialFinancialDataType.CONTRACTOR_JOB;
    }

    @Override
    public FinancialValueDto getPartialFinancialData(long projectId) {
        List<FinancialDataDto> contractorsJobsFinancialData = this.financialDataQueryService.getContractorsJobsFinancialData(
                projectId);
        FinancialValueDto contractorsJobsFinancialValue = new FinancialValueDto();
        return this.addUpFinancialData(contractorsJobsFinancialValue,
                contractorsJobsFinancialData);
    }

    @Override
    public SummationStrategy getSummationStrategy() {
        return SummationStrategy.ADD;
    }
}
