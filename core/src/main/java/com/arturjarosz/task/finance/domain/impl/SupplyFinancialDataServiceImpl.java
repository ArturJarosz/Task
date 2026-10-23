package com.arturjarosz.task.finance.domain.impl;

import com.arturjarosz.task.finance.application.dto.FinancialValueDto;
import com.arturjarosz.task.finance.domain.AbstractPartialFinancialDataService;
import com.arturjarosz.task.finance.domain.PartialFinancialDataService;
import com.arturjarosz.task.finance.domain.SummationStrategy;
import com.arturjarosz.task.finance.domain.dto.FinancialDataDto;
import com.arturjarosz.task.finance.model.PartialFinancialDataType;
import com.arturjarosz.task.finance.model.Supply;
import com.arturjarosz.task.finance.query.FinancialDataQueryService;
import com.arturjarosz.task.sharedkernel.annotations.DomainService;
import com.arturjarosz.task.systemparameter.application.TaxRatesProvider;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@DomainService
public class SupplyFinancialDataServiceImpl extends AbstractPartialFinancialDataService implements PartialFinancialDataService<Supply> {

    private final FinancialDataQueryService financialDataQueryService;

    @Autowired
    public SupplyFinancialDataServiceImpl(FinancialDataQueryService financialDataQueryService,
            TaxRatesProvider taxRatesProvider) {
        super(taxRatesProvider);
        this.financialDataQueryService = financialDataQueryService;
    }

    @Override
    public PartialFinancialDataType getType() {
        return PartialFinancialDataType.SUPPLY;
    }

    @Override
    public FinancialValueDto getPartialFinancialData(long projectId) {
        List<FinancialDataDto> supplyFinancialData = this.financialDataQueryService.getSuppliesFinancialData(projectId);
        FinancialValueDto suppliesFinancialValue = new FinancialValueDto();
        return this.addUpFinancialData(suppliesFinancialValue, supplyFinancialData);
    }

    @Override
    public SummationStrategy getSummationStrategy() {
        return SummationStrategy.ADD;
    }
}
