package com.arturjarosz.task.finance.domain;

import com.arturjarosz.task.configuration.UserProperties;
import com.arturjarosz.task.finance.application.dto.FinancialValueDto;
import com.arturjarosz.task.finance.domain.dto.FinancialDataDto;
import org.apache.logging.log4j.core.util.Integers;
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

        int withVatValue = 1 + Integers.parseInt(this.userProperties.getVatTax());

        for (FinancialDataDto financialDataDto : objectsFinancialDataDtos) {
            summedUpFinancialValueDto.addGross(financialDataDto.getValue());
            if (financialDataDto.isHasInvoice()) {
                BigDecimal netValue = financialDataDto.getValue().divide(BigDecimal.valueOf(withVatValue), 2, RoundingMode.HALF_UP);
                BigDecimal vatTaxValue = financialDataDto.getValue().subtract(netValue)
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal incomeTaxValue = netValue.multiply(new BigDecimal(this.userProperties.getIncomeTax()))
                        .setScale(2, RoundingMode.HALF_UP);
                summedUpFinancialValueDto.addNet(netValue);
                summedUpFinancialValueDto.addIncomeTax(incomeTaxValue);
                summedUpFinancialValueDto.addVatTax(vatTaxValue);
            } else {
                summedUpFinancialValueDto.addNet(financialDataDto.getValue());
            }
        }

        return summedUpFinancialValueDto;
    }
}
