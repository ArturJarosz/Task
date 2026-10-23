package com.arturjarosz.task.finance.domain;

import com.arturjarosz.task.finance.application.TaxCalculator;
import com.arturjarosz.task.finance.application.dto.FinancialValueDto;
import com.arturjarosz.task.finance.domain.dto.FinancialDataDto;
import com.arturjarosz.task.systemparameter.application.TaxRatesProvider;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public abstract class AbstractPartialFinancialDataService {
    private final TaxRatesProvider taxRatesProvider;

    @Autowired
    protected AbstractPartialFinancialDataService(TaxRatesProvider taxRatesProvider) {
        this.taxRatesProvider = taxRatesProvider;
    }

    protected FinancialValueDto addUpFinancialData(FinancialValueDto financialValueDto,
            List<FinancialDataDto> objectsFinancialDataDtos) {
        FinancialValueDto summedUpFinancialValueDto = new FinancialValueDto();
        summedUpFinancialValueDto.copyValues(financialValueDto);
        var taxRates = this.taxRatesProvider.getTaxRates();

        for (FinancialDataDto financialDataDto : objectsFinancialDataDtos) {
            var recalculatedObject = TaxCalculator.recalculateObjectTaxes(financialDataDto, taxRates);
            summedUpFinancialValueDto.addValues(recalculatedObject);
        }

        return summedUpFinancialValueDto;
    }
}
