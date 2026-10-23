package com.arturjarosz.task.systemparameter.domain.validator;

import com.arturjarosz.task.systemparameter.domain.SystemParameterExceptionCodes;
import com.arturjarosz.task.systemparameter.domain.SystemParameters;
import com.arturjarosz.task.systemparameter.query.SystemParameterQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VatTaxValidator extends AbstractTaxValidator {

    @Autowired
    public VatTaxValidator(SystemParameterQueryService systemParameterQueryService) {
        super(systemParameterQueryService);
    }

    @Override
    protected String getTaxExceptionCode() {
        return SystemParameterExceptionCodes.VAT_TAX;
    }

    @Override
    public String getSystemParameterName() {
        return SystemParameters.VAT_TAX;
    }
}
