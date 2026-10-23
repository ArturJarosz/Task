package com.arturjarosz.task.systemparameter.application.impl;

import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import com.arturjarosz.task.systemparameter.application.TaxRatesProvider;
import com.arturjarosz.task.systemparameter.application.dto.TaxRates;
import com.arturjarosz.task.systemparameter.domain.SystemParameterExceptionCodes;
import com.arturjarosz.task.systemparameter.domain.SystemParameters;
import com.arturjarosz.task.systemparameter.query.SystemParameterQueryService;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotNull;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

@ApplicationService
public class TaxRatesProviderImpl implements TaxRatesProvider {
    private final SystemParameterQueryService systemParameterQueryService;

    @Autowired
    public TaxRatesProviderImpl(SystemParameterQueryService systemParameterQueryService) {
        this.systemParameterQueryService = systemParameterQueryService;
    }

    @Override
    public TaxRates getTaxRates() {
        return new TaxRates(this.getDecimalParameter(SystemParameters.VAT_TAX),
                this.getDecimalParameter(SystemParameters.INCOME_TAX));
    }

    private BigDecimal getDecimalParameter(String name) {
        var systemParameter = this.systemParameterQueryService.getSystemPropertyByName(name);
        assertNotNull(systemParameter,
                createMessageCode(ExceptionCodes.NOT_EXIST, SystemParameterExceptionCodes.SYSTEM_PARAMETER), name);
        return new BigDecimal(systemParameter.getValue());
    }
}
