package com.arturjarosz.task.systemparameter.domain.validator;

import com.arturjarosz.task.sharedkernel.exceptions.BaseValidator;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException;
import com.arturjarosz.task.systemparameter.domain.SystemParameterExceptionCodes;
import com.arturjarosz.task.systemparameter.domain.SystemParameters;
import com.arturjarosz.task.systemparameter.domain.dto.SystemParameterDto;
import com.arturjarosz.task.systemparameter.query.SystemParameterQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotNull;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

@Component
public class VatTaxValidator extends AbstractSystemParameterValidator implements SystemParameterValidator {
    private final SystemParameterQueryService systemParameterQueryService;

    @Autowired
    public VatTaxValidator(SystemParameterQueryService systemParameterQueryService) {
        this.systemParameterQueryService = systemParameterQueryService;
    }

    @Override
    public void validate(String name) {
        super.validate(name);
        SystemParameterDto systemProperty = this.systemParameterQueryService.getSystemPropertyByName(name);
        assertNotNull(systemProperty,
                createMessageCode(ExceptionCodes.NOT_EXIST, SystemParameterExceptionCodes.SYSTEM_PARAMETER));
        this.validateValue(systemProperty.getValue());

    }

    @Override
    public void validateOnUpdate(SystemParameterDto systemParameterDto) {
        this.validateValue(systemParameterDto.getValue());
    }

    private void validateValue(String textValue) {
        try {
            BigDecimal value = new BigDecimal(textValue);
            assertIsTrue((new BigDecimal("0.0").compareTo(value) <= 0 && new BigDecimal("1.0").compareTo(value) >= 0),
                    createMessageCode(ExceptionCodes.NOT_VALID, SystemParameterExceptionCodes.SYSTEM_PARAMETER,
                            SystemParameterExceptionCodes.VAT_TAX, SystemParameterExceptionCodes.VALUE));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(BaseValidator.createMessageCode(ExceptionCodes.NOT_VALID,
                    SystemParameterExceptionCodes.SYSTEM_PARAMETER, SystemParameterExceptionCodes.DECIMAL_NUMBER,
                    SystemParameterExceptionCodes.VALUE), textValue);
        }
    }

    @Override
    public String getSystemParameterName() {
        return SystemParameters.VAT_TAX;
    }
}
