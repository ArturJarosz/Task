package com.arturjarosz.task.systemparameter.domain.validator;

import com.arturjarosz.task.dto.SystemParameterDto;
import com.arturjarosz.task.sharedkernel.exceptions.BaseValidator;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException;
import com.arturjarosz.task.systemparameter.domain.SystemParameterExceptionCodes;
import com.arturjarosz.task.systemparameter.query.SystemParameterQueryService;

import java.math.BigDecimal;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotNull;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

/**
 * Validates system parameters holding tax rate, which has to be decimal number between 0 and 1.
 */
public abstract class AbstractTaxValidator extends AbstractSystemParameterValidator implements SystemParameterValidator {
    private final SystemParameterQueryService systemParameterQueryService;

    protected AbstractTaxValidator(SystemParameterQueryService systemParameterQueryService) {
        this.systemParameterQueryService = systemParameterQueryService;
    }

    /**
     * Returns exception code part identifying validated tax.
     */
    protected abstract String getTaxExceptionCode();

    @Override
    public void validate(String name) {
        super.validate(name);
        var systemProperty = this.systemParameterQueryService.getSystemPropertyByName(name);
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
            var value = new BigDecimal(textValue);
            assertIsTrue((new BigDecimal("0.0").compareTo(value) <= 0 && new BigDecimal("1.0").compareTo(value) >= 0),
                    createMessageCode(ExceptionCodes.NOT_VALID, SystemParameterExceptionCodes.SYSTEM_PARAMETER,
                            this.getTaxExceptionCode(), SystemParameterExceptionCodes.VALUE));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(BaseValidator.createMessageCode(ExceptionCodes.NOT_VALID,
                    SystemParameterExceptionCodes.SYSTEM_PARAMETER, SystemParameterExceptionCodes.DECIMAL_NUMBER,
                    SystemParameterExceptionCodes.VALUE), textValue);
        }
    }
}
