package com.arturjarosz.task.finance.application;

import com.arturjarosz.task.configuration.UserProperties;
import com.arturjarosz.task.finance.application.dto.FinancialValueDto;
import com.arturjarosz.task.finance.domain.dto.FinancialDataDto;
import com.arturjarosz.task.sharedkernel.exceptions.IllegalStateException;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes.NOT_FOR_INSTANTIATING;

public final class TaxCalculator {

    private TaxCalculator() {
        throw new IllegalStateException(NOT_FOR_INSTANTIATING);
    }

    public static FinancialValueDto recalculateObjectTaxes(
            FinancialDataDto financialDataDto, UserProperties userProperties) {
        var financialValueDto = new FinancialValueDto();
        financialValueDto.setNetValue(financialDataDto.getValue());
        BigDecimal netValue = financialDataDto.getValue();
        if (financialDataDto.isHasInvoice()) {
            BigDecimal vatTaxValue = netValue.multiply(BigDecimal.valueOf(userProperties.getVatTax()))
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal grossValue = financialDataDto.getValue().add(vatTaxValue);
            BigDecimal incomeTaxValue = netValue.multiply(BigDecimal.valueOf(userProperties.getIncomeTax()))
                    .setScale(2, RoundingMode.HALF_UP);
            financialValueDto.addGross(grossValue);
            financialValueDto.addIncomeTax(incomeTaxValue);
            financialValueDto.addVatTax(vatTaxValue);
        } else {
            financialValueDto.addGross(financialDataDto.getValue());
        }

        return financialValueDto;
    }

}
