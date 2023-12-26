package com.arturjarosz.task.finance.domain.impl;

import com.arturjarosz.task.configuration.UserProperties;
import com.arturjarosz.task.finance.application.dto.FinancialValueDto;
import com.arturjarosz.task.finance.domain.AbstractPartialFinancialDataService;
import com.arturjarosz.task.finance.domain.PartialFinancialDataService;
import com.arturjarosz.task.finance.domain.SummationStrategy;
import com.arturjarosz.task.finance.domain.dto.FinancialDataDto;
import com.arturjarosz.task.finance.model.Installment;
import com.arturjarosz.task.finance.model.PartialFinancialDataType;
import com.arturjarosz.task.finance.query.FinancialDataQueryService;
import com.arturjarosz.task.sharedkernel.annotations.DomainService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@DomainService
public class InstallmentFinancialDataServiceImpl extends AbstractPartialFinancialDataService implements PartialFinancialDataService<Installment> {

    private final FinancialDataQueryService financialDataQueryService;

    @Autowired
    public InstallmentFinancialDataServiceImpl(FinancialDataQueryService financialDataQueryService,
            UserProperties userProperties) {
        super(userProperties);
        this.financialDataQueryService = financialDataQueryService;
    }

    @Override
    public PartialFinancialDataType getType() {
        return PartialFinancialDataType.INSTALLMENT;
    }

    @Override
    public FinancialValueDto getPartialFinancialData(long projectId) {
        List<FinancialDataDto> installmentsFinancialData = this.financialDataQueryService.getInstallmentsFinancialData(
                projectId);
        FinancialValueDto installmentsFinancialValue = new FinancialValueDto();
        return this.addUpFinancialData(installmentsFinancialValue, installmentsFinancialData);
    }

    @Override
    public SummationStrategy getSummationStrategy() {
        return SummationStrategy.ADD;
    }
}
