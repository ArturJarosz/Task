package com.arturjarosz.task.finance.domain;

import com.arturjarosz.task.configuration.UserProperties;
import com.arturjarosz.task.finance.application.dto.FinancialValueDto;
import com.arturjarosz.task.finance.domain.dto.FinancialDataDto;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public abstract class AbstractPartialFinancialDataService {
    private final UserProperties userProperties;

    @Autowired
    protected AbstractPartialFinancialDataService(UserProperties userProperties) {
        this.userProperties = userProperties;
    }

    protected FinancialValueDto recalculateFinancialData(FinancialValueDto financialValueDto,
            List<FinancialDataDto> objectsFinancialDataDtos) {
        FinancialValueDto summedUpFinancialValueDto = new FinancialValueDto();
        summedUpFinancialValueDto.copyValues(financialValueDto);

        for (FinancialDataDto financialDataDto : objectsFinancialDataDtos) {
            summedUpFinancialValueDto.addNet(financialDataDto.getValue());
            BigDecimal netValue = financialDataDto.getValue();
            if (financialDataDto.isHasInvoice()) {
                BigDecimal vatTaxValue = netValue.multiply(BigDecimal.valueOf(this.userProperties.getVatTax()))
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal grossValue = financialDataDto.getValue().add(vatTaxValue);
                BigDecimal incomeTaxValue = netValue.multiply(BigDecimal.valueOf(this.userProperties.getIncomeTax()))
                        .setScale(2, RoundingMode.HALF_UP);
                summedUpFinancialValueDto.addGross(grossValue);
                summedUpFinancialValueDto.addIncomeTax(incomeTaxValue);
                summedUpFinancialValueDto.addVatTax(vatTaxValue);
            } else {
                summedUpFinancialValueDto.addGross(financialDataDto.getValue());
            }
        }

        return summedUpFinancialValueDto;
    }
}
