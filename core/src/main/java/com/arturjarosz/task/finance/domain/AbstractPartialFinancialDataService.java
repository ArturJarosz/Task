package com.arturjarosz.task.finance.domain;

import com.arturjarosz.task.configuration.UserProperties;
import com.arturjarosz.task.finance.application.TaxCalculator;
import com.arturjarosz.task.finance.application.dto.FinancialValueDto;
import com.arturjarosz.task.finance.domain.dto.FinancialDataDto;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public abstract class AbstractPartialFinancialDataService {
    private final UserProperties userProperties;

    @Autowired
    protected AbstractPartialFinancialDataService(UserProperties userProperties) {
        this.userProperties = userProperties;
    }

    protected FinancialValueDto addUpFinancialData(FinancialValueDto financialValueDto,
            List<FinancialDataDto> objectsFinancialDataDtos) {
        FinancialValueDto summedUpFinancialValueDto = new FinancialValueDto();
        summedUpFinancialValueDto.copyValues(financialValueDto);

        for (FinancialDataDto financialDataDto : objectsFinancialDataDtos) {
            var recalculatedObject = TaxCalculator.recalculateObjectTaxes(financialDataDto, this.userProperties);
            summedUpFinancialValueDto.addValues(recalculatedObject);
        }

        return summedUpFinancialValueDto;
    }
}
