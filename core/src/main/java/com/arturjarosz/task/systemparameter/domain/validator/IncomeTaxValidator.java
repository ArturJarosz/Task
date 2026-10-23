package com.arturjarosz.task.systemparameter.domain.validator;

import com.arturjarosz.task.systemparameter.domain.SystemParameterExceptionCodes;
import com.arturjarosz.task.systemparameter.domain.SystemParameters;
import com.arturjarosz.task.systemparameter.query.SystemParameterQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IncomeTaxValidator extends AbstractTaxValidator {

    @Autowired
    public IncomeTaxValidator(SystemParameterQueryService systemParameterQueryService) {
        super(systemParameterQueryService);
    }

    @Override
    protected String getTaxExceptionCode() {
        return SystemParameterExceptionCodes.INCOME_TAX;
    }

    @Override
    public String getSystemParameterName() {
        return SystemParameters.INCOME_TAX;
    }
}
