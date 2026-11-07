package com.arturjarosz.task.systemparameter.domain.validator;

import com.arturjarosz.task.dto.SystemParameterDto;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import com.arturjarosz.task.systemparameter.domain.SystemParameterExceptionCodes;
import com.arturjarosz.task.systemparameter.query.SystemParameterQueryService;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

/**
 * Validates how many events the event lists show, which is a whole number between 1 and 100.
 */
public abstract class AbstractEventsShownValidator extends AbstractSystemParameterValidator
        implements SystemParameterValidator {
    private static final int MIN_EVENTS = 1;
    private static final int MAX_EVENTS = 100;
    private static final String WHOLE_NUMBER_PATTERN = "\\d{1,3}";

    private final SystemParameterQueryService systemParameterQueryService;

    protected AbstractEventsShownValidator(SystemParameterQueryService systemParameterQueryService) {
        this.systemParameterQueryService = systemParameterQueryService;
    }

    /**
     * Returns exception code part identifying validated parameter.
     */
    protected abstract String getEventsShownExceptionCode();

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
        var messageCode = createMessageCode(ExceptionCodes.NOT_VALID, SystemParameterExceptionCodes.SYSTEM_PARAMETER,
                this.getEventsShownExceptionCode(), SystemParameterExceptionCodes.VALUE);
        assertIsTrue(value != null && value.matches(WHOLE_NUMBER_PATTERN), messageCode, MIN_EVENTS, MAX_EVENTS);
        var events = Integer.parseInt(value);
        assertIsTrue(events >= MIN_EVENTS && events <= MAX_EVENTS, messageCode, MIN_EVENTS, MAX_EVENTS);
    }
}
