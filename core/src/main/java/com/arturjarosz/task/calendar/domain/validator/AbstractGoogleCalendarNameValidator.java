package com.arturjarosz.task.calendar.domain.validator;

import com.arturjarosz.task.dto.SystemParameterDto;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import com.arturjarosz.task.systemparameter.domain.SystemParameterExceptionCodes;
import com.arturjarosz.task.systemparameter.domain.validator.AbstractSystemParameterValidator;
import com.arturjarosz.task.systemparameter.domain.validator.SystemParameterValidator;
import com.arturjarosz.task.systemparameter.query.SystemParameterQueryService;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

/**
 * Validates names sent to Google Calendar, which cannot be blank or longer than 255 characters.
 */
public abstract class AbstractGoogleCalendarNameValidator extends AbstractSystemParameterValidator implements SystemParameterValidator {
    private static final int MAX_LENGTH = 255;

    private final SystemParameterQueryService systemParameterQueryService;

    protected AbstractGoogleCalendarNameValidator(SystemParameterQueryService systemParameterQueryService) {
        this.systemParameterQueryService = systemParameterQueryService;
    }

    /**
     * Returns exception code part identifying validated name.
     */
    protected abstract String getNameExceptionCode();

    @Override
    public void validate(String name) {
        super.validate(name);
        this.validateValue(this.systemParameterQueryService.getValueByName(name));
    }

    @Override
    public void validateOnUpdate(SystemParameterDto systemParameterDto) {
        this.validateValue(systemParameterDto.getValue());
    }

    private void validateValue(String value) {
        assertIsTrue(value != null && !value.isBlank(),
                createMessageCode(ExceptionCodes.EMPTY, SystemParameterExceptionCodes.SYSTEM_PARAMETER,
                        this.getNameExceptionCode(), SystemParameterExceptionCodes.VALUE));
        assertIsTrue(value.length() <= MAX_LENGTH,
                createMessageCode(ExceptionCodes.NOT_VALID, SystemParameterExceptionCodes.SYSTEM_PARAMETER,
                        this.getNameExceptionCode(), SystemParameterExceptionCodes.VALUE), MAX_LENGTH);
    }
}
